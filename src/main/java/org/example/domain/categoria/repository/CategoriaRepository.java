package org.example.domain.categoria.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.categoria.entity.Categoria;
import org.example.infra.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository implements DefaultRepository<Categoria> {

    @Override
    public Categoria save(Categoria entity) {
        String sql = "INSERT INTO categoria (nome, descricao, ativo) VALUES (?, ?, ?)";
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
            throw new RuntimeException("Erro ao salvar categoria.", e);
        }
    }

    @Override
    public Categoria findById(Long id) {
        String sql = "SELECT * FROM categoria WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categoria.", e);
        }
    }

    public List<Categoria> findAllAtivas() {
        String sql = "SELECT * FROM categoria WHERE ativo = TRUE ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<Categoria> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapRow(rs));
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar categorias.", e);
        }
    }

    @Override
    public Categoria update(Categoria entity) {
        String sql = "UPDATE categoria SET nome = ?, descricao = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getNome());
            ps.setString(2, entity.getDescricao());
            ps.setBoolean(3, entity.isAtivo());
            ps.setLong(4, entity.getId());
            ps.executeUpdate();
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar categoria.", e);
        }
    }

    @Override
    public void delete(Categoria entity) {
        String sql = "UPDATE categoria SET ativo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, entity.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar categoria.", e);
        }
    }

    private Categoria mapRow(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getLong("id"));
        c.setNome(rs.getString("nome"));
        c.setDescricao(rs.getString("descricao"));
        c.setAtivo(rs.getBoolean("ativo"));
        return c;
    }
}
