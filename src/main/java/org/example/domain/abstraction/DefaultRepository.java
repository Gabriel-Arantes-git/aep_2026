package org.example.domain.abstraction;

public interface DefaultRepository<T extends DefaultEntity> {
    T save(T entity);
    T findById(Long id);
    void delete(T entity);
}
