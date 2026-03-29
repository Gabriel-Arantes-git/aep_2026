package org.example.domain.denuncia.lixo.entity;

import org.example.domain.denuncia.abstraction.Denuncia;

public class Lixo extends Denuncia {

    @Override
    public String getTipoDenuncia() {
        return "Lixo";
    }
}
