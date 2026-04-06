package org.example.domain.usuario.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.enums.PerfilUsuario;
import org.example.domain.usuario.entity.Usuario;
import org.example.infra.DatabaseConnection;

import java.sql.*;

public class UsuarioRepository implements DefaultRepository<Usuario> {

    @Override
    public Usuario save(Usuario entity) {
        String sql = "INSERT INTO usuario (nome, email, cpf, telefone, senha_hash, perfil, ativo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getNome());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getCpf());
            ps.setString(4, entity.getTelefone());
            ps.setString(5, entity.getSenhaHash());
            ps.setString(6, entity.getPerfil().name());
            ps.setBoolean(7, entity.isAtivo());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) entity.setId(keys.getLong(1));
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar usuário.", e);
        }
    }

    @Override
    public Usuario findById(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id.", e);
        }
    }

    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ? AND ativo = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por email.", e);
        }
    }

    @Override
    public Usuario update(Usuario entity) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, cpf = ?, telefone = ?, senha_hash = ?, perfil = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getNome());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getCpf());
            ps.setString(4, entity.getTelefone());
            ps.setString(5, entity.getSenhaHash());
            ps.setString(6, entity.getPerfil().name());
            ps.setBoolean(7, entity.isAtivo());
            ps.setLong(8, entity.getId());
            ps.executeUpdate();
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário.", e);
        }
    }

    @Override
    public void delete(Usuario entity) {
        String sql = "UPDATE usuario SET ativo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, entity.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar usuário.", e);
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setCpf(rs.getString("cpf"));
        u.setTelefone(rs.getString("telefone"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setPerfil(PerfilUsuario.valueOf(rs.getString("perfil")));
        u.setAtivo(rs.getBoolean("ativo"));
        Timestamp dt = rs.getTimestamp("data_cadastro");
        if (dt != null) u.setDataCadastro(dt.toLocalDateTime());
        return u;
    }
}
