package org.example.domain.solicitacao.service;

import org.example.domain.abstraction.DefaultService;
import org.example.domain.enums.Prioridade;
import org.example.domain.enums.StatusSolicitacao;
import org.example.domain.log.entity.LogAcao;
import org.example.domain.log.repository.LogRepository;
import org.example.domain.sla.entity.SlaConfig;
import org.example.domain.sla.repository.SlaRepository;
import org.example.domain.solicitacao.entity.Movimentacao;
import org.example.domain.solicitacao.entity.Solicitacao;
import org.example.domain.solicitacao.repository.MovimentacaoRepository;
import org.example.domain.solicitacao.repository.SolicitacaoRepository;
import org.example.domain.usuario.entity.Usuario;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

public class SolicitacaoService extends DefaultService<Solicitacao, SolicitacaoRepository> {

    private final SolicitacaoRepository solicitacaoRepository   = new SolicitacaoRepository();
    private final MovimentacaoRepository movimentacaoRepository = new MovimentacaoRepository();
    private final SlaRepository slaRepository                   = new SlaRepository();
    private final LogRepository logRepository                   = new LogRepository();

    @Override
    protected SolicitacaoRepository getRepository() {
        return solicitacaoRepository;
    }

    @Override
    public Solicitacao salvar(Solicitacao entity) {
        validarCamposObrigatorios(entity);
        validarLimiteAbertasPorUsuario(entity);

        entity.setProtocolo(gerarProtocolo());
        entity.setStatus(StatusSolicitacao.ABERTO);
        entity.setDataAbertura(LocalDateTime.now());

        Solicitacao salva = solicitacaoRepository.save(entity);

        registrarMovimentacao(salva, null, StatusSolicitacao.ABERTO, "Solicitação aberta.", entity.getUsuario());
        logRepository.save(new LogAcao(entity.getUsuario(), "ABRIR_SOLICITACAO", "solicitacao", salva.getId(), salva.getProtocolo()));

        return salva;
    }

    public Solicitacao buscarPorProtocolo(String protocolo) {
        Solicitacao s = solicitacaoRepository.findByProtocolo(protocolo);
        if (s == null) throw new IllegalArgumentException("Protocolo não encontrado: " + protocolo);
        return s;
    }

    public List<Solicitacao> listarPorStatus(StatusSolicitacao status) {
        return solicitacaoRepository.findByStatus(status);
    }

    public List<Solicitacao> listarTodas() {
        return solicitacaoRepository.findAll();
    }

    public List<Movimentacao> buscarHistorico(Long solicitacaoId) {
        return movimentacaoRepository.findBySolicitacao(solicitacaoId);
    }

    public void moverStatus(Solicitacao solicitacao, StatusSolicitacao novoStatus,
                            Prioridade prioridade, String comentario, Usuario responsavel) {
        if (!solicitacao.getStatus().podeMoverPara(novoStatus)) {
            throw new IllegalStateException(
                "Transição inválida: " + solicitacao.getStatus() + " → " + novoStatus);
        }
        if (comentario == null || comentario.isBlank()) {
            throw new IllegalArgumentException("Comentário é obrigatório ao mover status.");
        }

        StatusSolicitacao statusAnterior = solicitacao.getStatus();

        if (novoStatus == StatusSolicitacao.TRIAGEM && prioridade != null) {
            solicitacao.setPrioridade(prioridade);
            solicitacao.setPrazoAlvo(calcularPrazo(prioridade));
            solicitacao.setAtendente(responsavel);
        }

        if (novoStatus == StatusSolicitacao.ENCERRADO || novoStatus == StatusSolicitacao.RESOLVIDO) {
            solicitacao.setDataEncerramento(LocalDateTime.now());
        }

        solicitacao.setStatus(novoStatus);
        atualizar(solicitacao);

        String justificativaAtraso = resolverJustificativaAtraso(solicitacao, comentario);
        registrarMovimentacao(solicitacao, statusAnterior, novoStatus, comentario, responsavel, justificativaAtraso);
        logRepository.save(new LogAcao(responsavel, "MOVER_STATUS", "solicitacao", solicitacao.getId(),
                statusAnterior + " -> " + novoStatus));
    }

    public void moverStatus(Solicitacao solicitacao, StatusSolicitacao novoStatus,
                            String comentario, Usuario responsavel) {
        moverStatus(solicitacao, novoStatus, null, comentario, responsavel);
    }

    private LocalDateTime calcularPrazo(Prioridade prioridade) {
        SlaConfig sla = slaRepository.findByPrioridade(prioridade);
        if (sla == null) throw new IllegalStateException("SLA não configurado para prioridade: " + prioridade);
        return LocalDateTime.now().plusHours(sla.getPrazoHoras());
    }

    private String resolverJustificativaAtraso(Solicitacao solicitacao, String comentario) {
        if (solicitacao.getPrazoAlvo() != null && LocalDateTime.now().isAfter(solicitacao.getPrazoAlvo())) {
            return comentario;
        }
        return null;
    }

    private void registrarMovimentacao(Solicitacao solicitacao, StatusSolicitacao anterior,
                                       StatusSolicitacao novo, String comentario, Usuario responsavel) {
        registrarMovimentacao(solicitacao, anterior, novo, comentario, responsavel, null);
    }

    private void registrarMovimentacao(Solicitacao solicitacao, StatusSolicitacao anterior,
                                       StatusSolicitacao novo, String comentario,
                                       Usuario responsavel, String justificativaAtraso) {
        Movimentacao m = new Movimentacao();
        m.setSolicitacao(solicitacao);
        m.setStatusAnterior(anterior);
        m.setStatusNovo(novo);
        m.setComentario(comentario);
        m.setResponsavel(responsavel);
        m.setDataMovimentacao(LocalDateTime.now());
        m.setJustificativaAtraso(justificativaAtraso);
        movimentacaoRepository.save(m);
    }

    private String gerarProtocolo() {
        int ano = Year.now().getValue();
        List<Solicitacao> todas = solicitacaoRepository.findAll();
        long sequencial = todas.stream()
                .filter(s -> s.getProtocolo().startsWith("DEN-" + ano))
                .count() + 1;
        return String.format("DEN-%d-%05d", ano, sequencial);
    }

    private void validarCamposObrigatorios(Solicitacao entity) {
        if (entity.getCategoria() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (entity.getDescricao() == null || entity.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (entity.getBairro() == null || entity.getBairro().isBlank()) {
            throw new IllegalArgumentException("Bairro é obrigatório.");
        }
        if (entity.isAnonimo() && entity.getDescricao().length() < 50) {
            throw new IllegalArgumentException("Denúncias anônimas exigem descrição com pelo menos 50 caracteres.");
        }
    }

    private void validarLimiteAbertasPorUsuario(Solicitacao entity) {
        if (!entity.isAnonimo() && entity.getUsuario() != null) {
            int abertas = solicitacaoRepository.countAbertasByUsuario(entity.getUsuario().getId());
            if (abertas >= 5) {
                throw new IllegalStateException("Limite de 5 solicitações abertas atingido.");
            }
        }
    }
}
