package org.example.domain.denuncia.abstraction;

import org.example.domain.abstraction.DefaultEntity;
import org.example.domain.usuario.entity.Usuario;

import java.time.LocalDateTime;

public abstract class Denuncia extends DefaultEntity {
    String motivo;
    String endereco;
    String observacao;
    LocalDateTime dataCadastro;
    Usuario usuario;
    public abstract String getTipoDenuncia();

    public Denuncia(String motivo, String endereco, String observacao, LocalDateTime dataCadastro, Usuario usuario) {
        this.motivo = motivo;
        this.endereco = endereco;
        this.observacao = observacao;
        this.dataCadastro = dataCadastro;
        this.usuario = usuario;
    }

    public Denuncia() {
    }
}
