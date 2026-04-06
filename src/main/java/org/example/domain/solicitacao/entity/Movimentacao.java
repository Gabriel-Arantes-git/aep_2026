package org.example.domain.solicitacao.entity;

import org.example.domain.abstraction.DefaultEntity;
import org.example.domain.enums.StatusSolicitacao;
import org.example.domain.usuario.entity.Usuario;

import java.time.LocalDateTime;

public class Movimentacao extends DefaultEntity {

    private Solicitacao solicitacao;
    private StatusSolicitacao statusAnterior;
    private StatusSolicitacao statusNovo;
    private String comentario;
    private Usuario responsavel;
    private LocalDateTime dataMovimentacao;
    private String justificativaAtraso;

    public Movimentacao() {}

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusSolicitacao statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusSolicitacao getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusSolicitacao statusNovo) {
        this.statusNovo = statusNovo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public String getJustificativaAtraso() {
        return justificativaAtraso;
    }

    public void setJustificativaAtraso(String justificativaAtraso) {
        this.justificativaAtraso = justificativaAtraso;
    }
}
