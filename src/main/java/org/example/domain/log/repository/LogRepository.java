package org.example.domain.log.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.log.entity.LogAcao;
import org.example.infra.DatabaseConnection;

import java.sql.*;

public class LogRepository implements DefaultRepository<LogAcao> {

    @Override
    public LogAcao save(LogAcao entity) {
        String sql = "INSERT INTO log_acao (usuario_id, acao, entidade, entidade_id, detalhes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, entity.getUsuario() != null ? entity.getUsuario().getId() : null);
            ps.setString(2, entity.getAcao());
            ps.setString(3, entity.getEntidade());
            ps.setObject(4, entity.getEntidadeId());
            ps.setString(5, entity.getDetalhes());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) entity.setId(keys.getLong(1));
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar log.", e);
        }
    }

    @Override
    public LogAcao update(LogAcao entity) {
        throw new UnsupportedOperationException("Logs não podem ser alterados.");
    }

    @Override
    public LogAcao findById(Long id) {
        throw new UnsupportedOperationException("Log não é consultado por id.");
    }

    @Override
    public void delete(LogAcao entity) {
        throw new UnsupportedOperationException("Logs não podem ser removidos.");
    }
}
