package org.example.domain.categoria.service;

import org.example.domain.abstraction.DefaultService;
import org.example.domain.categoria.entity.Categoria;
import org.example.domain.categoria.repository.CategoriaRepository;

import java.util.List;

public class CategoriaService extends DefaultService<Categoria, CategoriaRepository> {

    private final CategoriaRepository categoriaRepository = new CategoriaRepository();

    @Override
    protected CategoriaRepository getRepository() {
        return categoriaRepository;
    }

    public List<Categoria> listarAtivas() {
        return getRepository().findAllAtivas();
    }
}
