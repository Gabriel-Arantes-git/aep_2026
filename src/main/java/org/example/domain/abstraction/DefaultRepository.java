package org.example.domain.abstraction;

public interface DefaultRepository<T extends DefaultEntity> {
    T save(T entity);
    T findById(Long id);
    T update(T entity);
    void delete(T entity);
}
