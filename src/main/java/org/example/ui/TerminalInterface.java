package org.example.ui;

import org.example.auth.AutenticacaoService;
import org.example.domain.categoria.entity.Categoria;
import org.example.domain.categoria.service.CategoriaService;
import org.example.domain.departamento.entity.DepartamentoDestino;
import org.example.domain.departamento.service.DepartamentoService;
import org.example.domain.enums.PerfilUsuario;
import org.example.domain.enums.Prioridade;
import org.example.domain.enums.StatusSolicitacao;
import org.example.domain.log.entity.LogAcao;
import org.example.domain.solicitacao.entity.Movimentacao;
import org.example.domain.solicitacao.entity.Solicitacao;
import org.example.domain.solicitacao.service.SolicitacaoService;
import org.example.domain.usuario.entity.Usuario;
import org.example.domain.usuario.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class TerminalInterface {

    private static final Scanner scanner                         = new Scanner(System.in);
    private static final AutenticacaoService auth                = new AutenticacaoService();
    private static final UsuarioService usuarioService           = new UsuarioService();
    private static final SolicitacaoService solicitacaoService   = new SolicitacaoService();
    private static final CategoriaService categoriaService       = new CategoriaService();
    private static final DepartamentoService departamentoService = new DepartamentoService();

    public static void telaInicial() {
        while (true) {
            System.out.println("\n=== Sistema de Denúncias Ambientais ===");
            System.out.println("[1] Cidadão");
            System.out.println("[2] Atendente / Gestor");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> menuCidadao();
                case "2" -> menuLogin();
                case "0" -> { System.out.println("Encerrando."); return; }
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void menuCidadao() {
        while (true) {
            System.out.println("\n--- Cidadão ---");
            System.out.println("[1] Abrir solicitação (identificado)");
            System.out.println("[2] Abrir solicitação (anônimo)");
            System.out.println("[3] Consultar protocolo");
            System.out.println("[4] Cadastrar-se");
            System.out.println("[0] Voltar");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> abrirSolicitacaoIdentificado();
                case "2" -> abrirSolicitacaoAnonima();
                case "3" -> consultarProtocolo();
                case "4" -> cadastrarCidadao();
                case "0" -> { return; }
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void abrirSolicitacaoIdentificado() {
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        Usuario usuario;
        try {
            usuario = auth.login(email, senha);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
            return;
        }

        Solicitacao s = coletarDadosSolicitacao(false);
        if (s == null) return;
        s.setUsuario(usuario);

        try {
            Solicitacao salva = solicitacaoService.salvar(s);
            System.out.println("Solicitação registrada. Protocolo: " + salva.getProtocolo());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void abrirSolicitacaoAnonima() {
        Solicitacao s = coletarDadosSolicitacao(true);
        if (s == null) return;
        s.setAnonimo(true);

        System.out.print("Nome para contato (opcional, Enter para pular): ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail para contato (opcional, Enter para pular): ");
        String emailContato = scanner.nextLine().trim();

        if (!nome.isBlank()) s.setNomeContato(nome);
        if (!emailContato.isBlank()) s.setEmailContato(emailContato);

        try {
            Solicitacao salva = solicitacaoService.salvar(s);
            System.out.println("Solicitação registrada. Protocolo: " + salva.getProtocolo());
            System.out.println("Guarde o protocolo — é o único meio de acompanhamento.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static Solicitacao coletarDadosSolicitacao(boolean anonimo) {
        List<Categoria> categorias = categoriaService.listarAtivas();
        System.out.println("\nCategorias disponíveis:");
        for (int i = 0; i < categorias.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, categorias.get(i).getNome());
        }
        System.out.print("Categoria: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= categorias.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Categoria inválida.");
            return null;
        }

        System.out.print("Bairro: ");
        String bairro = scanner.nextLine().trim();
        System.out.print("Logradouro (opcional): ");
        String logradouro = scanner.nextLine().trim();
        System.out.print("Referência (opcional): ");
        String referencia = scanner.nextLine().trim();

        int minDesc = anonimo ? 50 : 20;

        String descricao = "";

        while(true) {
            System.out.printf("Descrição (mínimo %d caracteres): ", minDesc);
            descricao = scanner.nextLine().trim();

            if(descricao.length() > minDesc){
                break;
            }
            System.out.printf("A descrição deve ter no mínimo %d caracteres! \n", minDesc);
        }

        Solicitacao s = new Solicitacao();
        s.setCategoria(categorias.get(idx));
        s.setBairro(bairro);
        if (!logradouro.isBlank()) s.setLogradouro(logradouro);
        if (!referencia.isBlank()) s.setReferencia(referencia);
        s.setDescricao(descricao);
        return s;
    }

    private static void consultarProtocolo() {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            imprimirSolicitacao(s);

            List<Movimentacao> historico = solicitacaoService.buscarHistorico(s.getId());
            if (!historico.isEmpty()) {
                System.out.println("\n--- Histórico ---");
                for (Movimentacao m : historico) {
                    System.out.printf("[%s] %s → %s | %s%n",
                            m.getDataMovimentacao().toLocalDate(),
                            m.getStatusAnterior() != null ? m.getStatusAnterior() : "-",
                            m.getStatusNovo(),
                            m.getComentario());
                    if (m.getJustificativaAtraso() != null) {
                        System.out.println("  Justificativa de atraso: " + m.getJustificativaAtraso());
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void cadastrarCidadao() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            usuarioService.cadastrarCidadao(nome, email, cpf, telefone, senha);
            System.out.println("Cadastro realizado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void menuLogin() {
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            Usuario usuario = auth.login(email, senha);
            if (usuario.getPerfil() == PerfilUsuario.GESTOR) {
                menuGestor(usuario);
            } else if (usuario.getPerfil() == PerfilUsuario.ATENDENTE) {
                menuAtendente(usuario);
            } else {
                System.out.println("Acesso negado. Use o menu Cidadão.");
                auth.logout();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void menuAtendente(Usuario atendente) {
        while (true) {
            System.out.println("\n--- Atendente: " + atendente.getNome() + " ---");
            System.out.println("[1] Ver fila (ABERTO)");
            System.out.println("[2] Ver fila (TRIAGEM)");
            System.out.println("[3] Ver fila (EM_EXECUCAO)");
            System.out.println("[4] Assumir solicitação (triagem)");
            System.out.println("[5] Atualizar status");
            System.out.println("[6] Ver detalhes de solicitação");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> listarPorStatus(StatusSolicitacao.ABERTO);
                case "2" -> listarPorStatus(StatusSolicitacao.TRIAGEM);
                case "3" -> listarPorStatus(StatusSolicitacao.EM_EXECUCAO);
                case "4" -> assumirSolicitacao(atendente);
                case "5" -> atualizarStatus(atendente);
                case "6" -> verDetalhesSolicitacao();
                case "0" -> { auth.logout(); return; }
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void assumirSolicitacao(Usuario atendente) {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            imprimirSolicitacao(s);

            System.out.println("\nPrioridade:");
            System.out.println("[1] BAIXA  [2] MEDIA  [3] ALTA  [4] CRITICA");
            Prioridade prioridade = switch (scanner.nextLine().trim()) {
                case "1" -> Prioridade.BAIXA;
                case "2" -> Prioridade.MEDIA;
                case "3" -> Prioridade.ALTA;
                case "4" -> Prioridade.CRITICA;
                default  -> throw new IllegalArgumentException("Prioridade inválida.");
            };

            List<DepartamentoDestino> departamentos = departamentoService.listarAtivos();
            System.out.println("\nDepartamento destino:");
            for (int i = 0; i < departamentos.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, departamentos.get(i).getNome());
            }
            System.out.print("Departamento: ");
            int idx;
            try {
                idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (idx < 0 || idx >= departamentos.size()) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Departamento inválido.");
                return;
            }
            DepartamentoDestino departamento = departamentos.get(idx);

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine().trim();

            solicitacaoService.moverStatus(s, StatusSolicitacao.TRIAGEM, prioridade, departamento, comentario, atendente);
            System.out.println("Solicitação encaminhada para " + departamento.getNome() + ". Prazo alvo definido.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void atualizarStatus(Usuario atendente) {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            imprimirSolicitacao(s);

            System.out.println("Novo status:");
            System.out.println("[1] EM_EXECUCAO  [2] RESOLVIDO  [3] ENCERRADO");
            StatusSolicitacao novoStatus = switch (scanner.nextLine().trim()) {
                case "1" -> StatusSolicitacao.EM_EXECUCAO;
                case "2" -> StatusSolicitacao.RESOLVIDO;
                case "3" -> StatusSolicitacao.ENCERRADO;
                default  -> throw new IllegalArgumentException("Status inválido.");
            };

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine().trim();

            solicitacaoService.moverStatus(s, novoStatus, comentario, atendente);
            System.out.println("Status atualizado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void menuGestor(Usuario gestor) {
        while (true) {
            System.out.println("\n--- Gestor: " + gestor.getNome() + " ---");
            System.out.println("[1] Painel geral");
            System.out.println("[2] Ver fila por status");
            System.out.println("[3] Atualizar status");
            System.out.println("[4] Ver detalhes de solicitação");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> painelGeral();
                case "2" -> selecionarStatusEListar();
                case "3" -> atualizarStatus(gestor);
                case "4" -> verDetalhesSolicitacao();
                case "0" -> { auth.logout(); return; }
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void painelGeral() {
        List<Solicitacao> todas = solicitacaoService.listarTodas();
        System.out.printf("%n%-20s %-12s %-10s %-15s %-20s%n",
                "Protocolo", "Status", "Prioridade", "Bairro", "Categoria");
        System.out.println("-".repeat(80));
        for (Solicitacao s : todas) {
            System.out.printf("%-20s %-12s %-10s %-15s %-20s%n",
                    s.getProtocolo(),
                    s.getStatus(),
                    s.getPrioridade() != null ? s.getPrioridade() : "-",
                    s.getBairro(),
                    s.getCategoria().getNome());
        }
    }

    private static void selecionarStatusEListar() {
        System.out.println("[1] ABERTO  [2] TRIAGEM  [3] EM_EXECUCAO  [4] RESOLVIDO  [5] ENCERRADO");
        StatusSolicitacao status = switch (scanner.nextLine().trim()) {
            case "1" -> StatusSolicitacao.ABERTO;
            case "2" -> StatusSolicitacao.TRIAGEM;
            case "3" -> StatusSolicitacao.EM_EXECUCAO;
            case "4" -> StatusSolicitacao.RESOLVIDO;
            case "5" -> StatusSolicitacao.ENCERRADO;
            default  -> null;
        };
        if (status == null) { System.out.println("Status inválido."); return; }
        listarPorStatus(status);
    }

    private static void listarPorStatus(StatusSolicitacao status) {
        List<Solicitacao> lista = solicitacaoService.listarPorStatus(status);
        if (lista.isEmpty()) {
            System.out.println("Nenhuma solicitação com status " + status + ".");
            return;
        }
        System.out.printf("%n%-20s %-15s %-10s %-20s%n", "Protocolo", "Bairro", "Prioridade", "Categoria");
        System.out.println("-".repeat(70));
        for (Solicitacao s : lista) {
            System.out.printf("%-20s %-15s %-10s %-20s%n",
                    s.getProtocolo(),
                    s.getBairro(),
                    s.getPrioridade() != null ? s.getPrioridade() : "-",
                    s.getCategoria().getNome());
        }
    }

    private static void verDetalhesSolicitacao() {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            imprimirSolicitacao(s);

            List<Movimentacao> historico = solicitacaoService.buscarHistorico(s.getId());
            if (!historico.isEmpty()) {
                System.out.println("\n--- Histórico de movimentações ---");
                for (Movimentacao m : historico) {
                    System.out.printf("[%s] %s → %s | %s%n",
                            m.getDataMovimentacao().toLocalDate(),
                            m.getStatusAnterior() != null ? m.getStatusAnterior() : "-",
                            m.getStatusNovo(),
                            m.getComentario());
                    if (m.getJustificativaAtraso() != null) {
                        System.out.println("  Justificativa de atraso: " + m.getJustificativaAtraso());
                    }
                }
            }

            System.out.print("\nVer logs de auditoria? [s/n]: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                List<LogAcao> logs = solicitacaoService.buscarLogs(s.getId());
                if (logs.isEmpty()) {
                    System.out.println("Nenhum log registrado.");
                } else {
                    System.out.println("\n--- Logs de auditoria ---");
                    for (LogAcao log : logs) {
                        String autor = log.getUsuario() != null ? log.getUsuario().getEmail() : "sistema";
                        System.out.printf("[%s] %s — %s | por: %s%n",
                                log.getDataAcao() != null ? log.getDataAcao().toLocalDate() : "-",
                                log.getAcao(),
                                log.getDetalhes() != null ? log.getDetalhes() : "-",
                                autor);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void imprimirSolicitacao(Solicitacao s) {
        System.out.println("\nProtocolo : " + s.getProtocolo());
        System.out.println("Categoria : " + s.getCategoria().getNome());
        System.out.println("Status    : " + s.getStatus());
        System.out.println("Prioridade: " + (s.getPrioridade() != null ? s.getPrioridade() : "-"));
        System.out.println("Bairro    : " + s.getBairro());
        if (s.getLogradouro() != null) System.out.println("Logradouro: " + s.getLogradouro());
        System.out.println("Descrição : " + s.getDescricao());
        System.out.println("Aberta em : " + s.getDataAbertura().toLocalDate());
        String abertoBy = s.isAnonimo() ? "Anônimo" : (s.getUsuario() != null ? s.getUsuario().getNome() + " (" + s.getUsuario().getEmail() + ")" : "Anônimo");
        System.out.println("Aberto por: " + abertoBy);
        if (s.getPrazoAlvo() != null) System.out.println("Prazo alvo: " + s.getPrazoAlvo().toLocalDate());
        if (s.getAtendente() != null)    System.out.println("Atendente  : " + s.getAtendente().getNome());
        if (s.getDepartamento() != null) System.out.println("Departamento alvo: " + s.getDepartamento().getNome());
        else                             System.out.println("Departamento alvo: Não definido");
    }
}
