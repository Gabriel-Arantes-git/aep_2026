package org.example;

import org.example.infra.InicioBanco;
import org.example.ui.TerminalInterface;

public class Main {
    public static void main(String[] args) {
        InicioBanco.inicializarBanco();
        TerminalInterface.telaInicial();
    }
}
