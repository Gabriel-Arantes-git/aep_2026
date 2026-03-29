package org.example.domain.usuario.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.usuario.entity.Usuario;

public class UsuarioRepository implements DefaultRepository<Usuario> {
    @Override
    public Usuario save(Usuario entity) {
        return null;
    }

    @Override
    public Usuario findById(Long id) {
        return null;
    }

    @Override
    public void delete(Usuario entity) {

    }
}
