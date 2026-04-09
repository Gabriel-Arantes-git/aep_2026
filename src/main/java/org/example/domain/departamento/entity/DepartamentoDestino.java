package org.example.domain.departamento.entity;

import org.example.domain.abstraction.DefaultEntity;

public class DepartamentoDestino extends DefaultEntity {

    private String nome;
    private String descricao;
    private boolean ativo;

    public DepartamentoDestino() {}

    public DepartamentoDestino(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}

