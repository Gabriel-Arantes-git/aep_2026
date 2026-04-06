package org.example.domain.sla.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.enums.Prioridade;
import org.example.domain.sla.entity.SlaConfig;
import org.example.infra.DatabaseConnection;

import java.sql.*;

public class SlaRepository implements DefaultRepository<SlaConfig> {

    public SlaConfig findByPrioridade(Prioridade prioridade) {
        String sql = "SELECT * FROM sla_config WHERE prioridade = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prioridade.name());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar SLA.", e);
        }
    }

    @Override
    public SlaConfig update(SlaConfig entity) {
        throw new UnsupportedOperationException("SLA é configurado via InicioBanco.");
    }

    @Override
    public SlaConfig save(SlaConfig entity) {
        throw new UnsupportedOperationException("SLA é configurado via InicioBanco.");
    }

    @Override
    public SlaConfig findById(Long id) {
        String sql = "SELECT * FROM sla_config WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar SLA.", e);
        }
    }

    @Override
    public void delete(SlaConfig entity) {
        throw new UnsupportedOperationException("SLA não pode ser removido.");
    }

    private SlaConfig mapRow(ResultSet rs) throws SQLException {
        SlaConfig s = new SlaConfig();
        s.setId(rs.getLong("id"));
        s.setPrioridade(Prioridade.valueOf(rs.getString("prioridade")));
        s.setPrazoHoras(rs.getInt("prazo_horas"));
        s.setDescricao(rs.getString("descricao"));
        return s;
    }
}
