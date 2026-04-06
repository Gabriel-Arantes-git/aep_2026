package org.example.domain.usuario.service;

import org.example.domain.abstraction.DefaultService;
import org.example.domain.enums.PerfilUsuario;
import org.example.domain.usuario.entity.Usuario;
import org.example.domain.usuario.repository.UsuarioRepository;

public class UsuarioService extends DefaultService<Usuario, UsuarioRepository> {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    @Override
    protected UsuarioRepository getRepository() {
        return usuarioRepository;
    }

    public Usuario buscarPorEmail(String email) {
        return getRepository().findByEmail(email);
    }

    public Usuario cadastrarCidadao(String nome, String email, String cpf, String telefone, String senhaHash) {
        if (getRepository().findByEmail(email) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        return salvar(new Usuario(nome, email, cpf, telefone, senhaHash, PerfilUsuario.CIDADAO));
    }

    @Override
    public Usuario salvar(Usuario entity) {
        if (entity.getPerfil() == null) {
            throw new IllegalArgumentException("Perfil do usuário é obrigatório.");
        }
        if (entity.getPerfil() != PerfilUsuario.CIDADAO && (entity.getEmail() == null || entity.getEmail().isBlank())) {
            throw new IllegalArgumentException("E-mail é obrigatório para atendentes e gestores.");
        }
        return super.salvar(entity);
    }
}
