package org.example.domain.denuncia.lixo;

import org.example.domain.abstraction.Denuncia;

public class Lixo extends Denuncia {

    @Override
    public String getTipoDenuncia() {
        return "Lixo";
    }
}
