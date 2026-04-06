package org.example.domain.categoria.entity;

import org.example.domain.abstraction.DefaultEntity;

public class Categoria extends DefaultEntity {

    private String nome;
    private String descricao;
    private boolean ativo;

    public Categoria() {}

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
    }

    public String getNome()                    { return nome; }
    public void   setNome(String nome)         { this.nome = nome; }

    public String getDescricao()               { return descricao; }
    public void   setDescricao(String d)       { this.descricao = d; }

    public boolean isAtivo()                   { return ativo; }
    public void    setAtivo(boolean ativo)     { this.ativo = ativo; }
}
