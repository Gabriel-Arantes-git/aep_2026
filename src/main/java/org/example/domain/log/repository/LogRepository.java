package org.example.domain.log.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.log.entity.LogAcao;
import org.example.domain.usuario.repository.UsuarioRepository;
import org.example.infra.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogRepository implements DefaultRepository<LogAcao> {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

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

    public List<LogAcao> findBySolicitacao(Long solicitacaoId) {
        String sql = """
            SELECT * FROM log_acao
            WHERE entidade = 'solicitacao' AND entidade_id = ?
            ORDER BY data_acao
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, solicitacaoId);
            ResultSet rs = ps.executeQuery();
            List<LogAcao> lista = new ArrayList<>();
            while (rs.next()) {
                LogAcao log = new LogAcao();
                log.setId(rs.getLong("id"));
                log.setAcao(rs.getString("acao"));
                log.setEntidade(rs.getString("entidade"));
                log.setEntidadeId(rs.getLong("entidade_id"));
                log.setDetalhes(rs.getString("detalhes"));
                Timestamp dt = rs.getTimestamp("data_acao");
                if (dt != null) log.setDataAcao(dt.toLocalDateTime());
                long usuarioId = rs.getLong("usuario_id");
                if (!rs.wasNull()) log.setUsuario(usuarioRepository.findById(usuarioId));
                lista.add(log);
            }
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar logs da solicitação.", e);
        }
    }
}
