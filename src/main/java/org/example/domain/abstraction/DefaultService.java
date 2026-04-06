package org.example.domain.abstraction;

public abstract class DefaultService<T extends DefaultEntity, R extends DefaultRepository<T>> {

    protected abstract R getRepository();

    public T salvar(T entity) {
        return getRepository().save(entity);
    }

    public T buscarPorId(Long id) {
        return getRepository().findById(id);
    }

    public T atualizar(T entity) {
        return getRepository().update(entity);
    }

    public void deletar(T entity) {
        getRepository().delete(entity);
    }
}
