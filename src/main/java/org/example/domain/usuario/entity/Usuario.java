package org.example.domain.usuario.entity;

import org.example.domain.abstraction.DefaultEntity;
import org.example.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;

public class Usuario extends DefaultEntity {

    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String senhaHash;
    private PerfilUsuario perfil;
    private boolean ativo;
    private LocalDateTime dataCadastro;

    public Usuario() {}

    public Usuario(String nome, String email, String cpf, String telefone, String senhaHash, PerfilUsuario perfil) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
