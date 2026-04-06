package org.example.domain.sla.entity;

import org.example.domain.abstraction.DefaultEntity;
import org.example.domain.enums.Prioridade;

public class SlaConfig extends DefaultEntity {

    private Prioridade prioridade;
    private int prazoHoras;
    private String descricao;

    public SlaConfig() {}

    public Prioridade getPrioridade(){
        return prioridade;
    }

    public void setPrioridade(Prioridade p) {
        this.prioridade = p;
    }

    public int  getPrazoHoras(){
        return prazoHoras;
    }
    public void setPrazoHoras(int prazoHoras){
        this.prazoHoras = prazoHoras;
    }

    public String getDescricao(){
        return descricao;
    }
    public void   setDescricao(String descricao){
        this.descricao = descricao;
    }
}
