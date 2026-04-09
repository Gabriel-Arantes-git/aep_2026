package org.example.domain.departamento.service;

import org.example.domain.abstraction.DefaultService;
import org.example.domain.departamento.entity.DepartamentoDestino;
import org.example.domain.departamento.repository.DepartamentoRepository;

import java.util.List;

public class DepartamentoService extends DefaultService<DepartamentoDestino, DepartamentoRepository> {

    private final DepartamentoRepository departamentoRepository = new DepartamentoRepository();

    @Override
    protected DepartamentoRepository getRepository() {
        return departamentoRepository;
    }

    public List<DepartamentoDestino> listarAtivos() {
        return getRepository().findAllAtivos();
    }
}
