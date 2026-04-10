package org.example.auth;

import org.example.domain.usuario.entity.Usuario;
import org.example.domain.usuario.repository.UsuarioRepository;

public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    private static Usuario usuarioAutenticado = null;

    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !usuario.getSenhaHash().equals(senha)) {
            throw new IllegalArgumentException("E-mail ou senha inválidos. \nLembre-se de se cadastrar primeiro (opção 4)");
        }

        usuarioAutenticado = usuario;
        return usuario;
    }

    public void logout() {
        usuarioAutenticado = null;
    }

    public static Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public static boolean estaAutenticado() {
        return usuarioAutenticado != null;
    }
}
