package org.example.domain.solicitacao.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.enums.StatusSolicitacao;
import org.example.domain.solicitacao.entity.Movimentacao;
import org.example.domain.solicitacao.entity.Solicitacao;
import org.example.domain.usuario.repository.UsuarioRepository;
import org.example.infra.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoRepository implements DefaultRepository<Movimentacao> {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final SolicitacaoRepository solicitacaoRepository = new SolicitacaoRepository();

    @Override
    public Movimentacao save(Movimentacao entity) {
        String sql = """
            INSERT INTO movimentacao
                (solicitacao_id, status_anterior, status_novo, comentario,
                 responsavel_id, data_movimentacao, justificativa_atraso)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getSolicitacao().getId());
            ps.setString(2, entity.getStatusAnterior() != null ? entity.getStatusAnterior().name() : null);
            ps.setString(3, entity.getStatusNovo().name());
            ps.setString(4, entity.getComentario());
            ps.setObject(5, entity.getResponsavel() != null ? entity.getResponsavel().getId() : null);
            ps.setTimestamp(6, Timestamp.valueOf(entity.getDataMovimentacao()));
            ps.setString(7, entity.getJustificativaAtraso());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) entity.setId(keys.getLong(1));
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar movimentação.", e);
        }
    }

    public List<Movimentacao> findBySolicitacao(Long solicitacaoId) {
        String sql = "SELECT * FROM movimentacao WHERE solicitacao_id = ? ORDER BY data_movimentacao";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, solicitacaoId);
            ResultSet rs = ps.executeQuery();
            List<Movimentacao> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapRow(rs));
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar movimentações.", e);
        }
    }

    @Override
    public Movimentacao update(Movimentacao entity) {
        throw new UnsupportedOperationException("Movimentações não podem ser alteradas.");
    }

    @Override
    public Movimentacao findById(Long id) {
        String sql = "SELECT * FROM movimentacao WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar movimentação.", e);
        }
    }

    @Override
    public void delete(Movimentacao entity) {
        throw new UnsupportedOperationException("Movimentações não podem ser removidas.");
    }

    private Movimentacao mapRow(ResultSet rs) throws SQLException {
        Movimentacao m = new Movimentacao();
        m.setId(rs.getLong("id"));

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(rs.getLong("solicitacao_id"));
        m.setSolicitacao(solicitacao);

        String statusAnterior = rs.getString("status_anterior");
        if (statusAnterior != null) m.setStatusAnterior(StatusSolicitacao.valueOf(statusAnterior));
        m.setStatusNovo(StatusSolicitacao.valueOf(rs.getString("status_novo")));
        m.setComentario(rs.getString("comentario"));
        m.setJustificativaAtraso(rs.getString("justificativa_atraso"));

        Timestamp dt = rs.getTimestamp("data_movimentacao");
        if (dt != null) m.setDataMovimentacao(dt.toLocalDateTime());

        long responsavelId = rs.getLong("responsavel_id");
        if (!rs.wasNull()) m.setResponsavel(usuarioRepository.findById(responsavelId));

        return m;
    }
}
