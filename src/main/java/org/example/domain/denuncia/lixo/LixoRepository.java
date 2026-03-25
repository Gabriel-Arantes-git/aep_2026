package org.example.domain.denuncia.lixo;

import org.example.domain.abstraction.DefaultRepository;

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
