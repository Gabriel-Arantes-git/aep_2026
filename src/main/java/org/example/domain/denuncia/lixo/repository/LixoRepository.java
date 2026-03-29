package org.example.domain.denuncia.lixo.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.denuncia.lixo.entity.Lixo;

public class LixoRepository implements DefaultRepository<Lixo> {
    @Override
    public Lixo save(Lixo entity) {
        //TO-DO implementar usando o databaseconnection
        return null;
    }

    @Override
    public Lixo findById(Long id) {
        //TO-DO implementar usando o databaseconnection
        return null;
    }

    @Override
    public void delete(Lixo entity) {
        //TO-DO implementar usando o databaseconnection
    }
}
