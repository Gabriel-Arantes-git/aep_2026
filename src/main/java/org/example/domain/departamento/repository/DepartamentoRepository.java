package org.example.domain.departamento.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.departamento.entity.DepartamentoDestino;
import org.example.infra.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoRepository implements DefaultRepository<DepartamentoDestino> {

    public DepartamentoDestino save(DepartamentoDestino entity) {
        String sql = "INSERT INTO departamento_destino (nome, descricao, ativo) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getNome());
            ps.setString(2, entity.getDescricao());
            ps.setBoolean(3, entity.isAtivo());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) entity.setId(keys.getLong(1));
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar departamento.", e);
        }
    }

    public DepartamentoDestino findById(Long id) {
        String sql = "SELECT * FROM departamento_destino WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar departamento.", e);
        }
    }
    
    public DepartamentoDestino update(DepartamentoDestino entity) {
        String sql = "UPDATE departamento_destino SET nome = ?, descricao = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getNome());
            ps.setString(2, entity.getDescricao());
            ps.setBoolean(3, entity.isAtivo());
            ps.setLong(4, entity.getId());
            ps.executeUpdate();
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar departamento.", e);
        }
    }

    @Override
    public void delete(DepartamentoDestino entity) {
        String sql = "UPDATE departamento_destino SET ativo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, entity.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar departamento.", e);
        }
    }

    public List<DepartamentoDestino> findAllAtivos() {
        String sql = "SELECT * FROM departamento_destino WHERE ativo = TRUE ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<DepartamentoDestino> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapRow(rs));
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar departamentos.", e);
        }
    }

    private DepartamentoDestino mapRow(ResultSet rs) throws SQLException {
        DepartamentoDestino d = new DepartamentoDestino();
        d.setId(rs.getLong("id"));
        d.setNome(rs.getString("nome"));
        d.setDescricao(rs.getString("descricao"));
        d.setAtivo(rs.getBoolean("ativo"));
        return d;
    }
}
