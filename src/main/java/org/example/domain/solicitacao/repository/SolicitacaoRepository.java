package org.example.domain.solicitacao.repository;

import org.example.domain.abstraction.DefaultRepository;
import org.example.domain.categoria.entity.Categoria;
import org.example.domain.enums.Prioridade;
import org.example.domain.enums.StatusSolicitacao;
import org.example.domain.solicitacao.entity.Solicitacao;
import org.example.domain.usuario.entity.Usuario;
import org.example.domain.usuario.repository.UsuarioRepository;
import org.example.domain.categoria.repository.CategoriaRepository;
import org.example.infra.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoRepository implements DefaultRepository<Solicitacao> {

    private final UsuarioRepository usuarioRepository   = new UsuarioRepository();
    private final CategoriaRepository categoriaRepository = new CategoriaRepository();

    @Override
    public Solicitacao save(Solicitacao entity) {
        String sql = """
            INSERT INTO solicitacao
                (protocolo, categoria_id, descricao, bairro, logradouro, referencia,
                 anonimo, usuario_id, nome_contato, email_contato, prioridade, status,
                 data_abertura, prazo_alvo, atendente_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getProtocolo());
            ps.setLong(2, entity.getCategoria().getId());
            ps.setString(3, entity.getDescricao());
            ps.setString(4, entity.getBairro());
            ps.setString(5, entity.getLogradouro());
            ps.setString(6, entity.getReferencia());
            ps.setBoolean(7, entity.isAnonimo());
            ps.setObject(8, entity.getUsuario() != null ? entity.getUsuario().getId() : null);
            ps.setString(9, entity.getNomeContato());
            ps.setString(10, entity.getEmailContato());
            ps.setString(11, entity.getPrioridade() != null ? entity.getPrioridade().name() : null);
            ps.setString(12, entity.getStatus().name());
            ps.setTimestamp(13, Timestamp.valueOf(entity.getDataAbertura()));
            ps.setObject(14, entity.getPrazoAlvo() != null ? Timestamp.valueOf(entity.getPrazoAlvo()) : null);
            ps.setObject(15, entity.getAtendente() != null ? entity.getAtendente().getId() : null);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) entity.setId(keys.getLong(1));
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar solicitação.", e);
        }
    }

    @Override
    public Solicitacao update(Solicitacao entity) {
        String sql = """
            UPDATE solicitacao SET
                status = ?, prioridade = ?, prazo_alvo = ?,
                data_encerramento = ?, atendente_id = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getStatus().name());
            ps.setString(2, entity.getPrioridade() != null ? entity.getPrioridade().name() : null);
            ps.setObject(3, entity.getPrazoAlvo() != null ? Timestamp.valueOf(entity.getPrazoAlvo()) : null);
            ps.setObject(4, entity.getDataEncerramento() != null ? Timestamp.valueOf(entity.getDataEncerramento()) : null);
            ps.setObject(5, entity.getAtendente() != null ? entity.getAtendente().getId() : null);
            ps.setLong(6, entity.getId());
            ps.executeUpdate();
            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar solicitação.", e);
        }
    }

    @Override
    public Solicitacao findById(Long id) {
        String sql = "SELECT * FROM solicitacao WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar solicitação.", e);
        }
    }

    public Solicitacao findByProtocolo(String protocolo) {
        String sql = "SELECT * FROM solicitacao WHERE protocolo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, protocolo);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar solicitação por protocolo.", e);
        }
    }

    public List<Solicitacao> findByStatus(StatusSolicitacao status) {
        String sql = "SELECT * FROM solicitacao WHERE status = ? ORDER BY data_abertura";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            return listar(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitações por status.", e);
        }
    }

    public List<Solicitacao> findAll() {
        String sql = "SELECT * FROM solicitacao ORDER BY data_abertura DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return listar(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitações.", e);
        }
    }

    public int countAbertasByUsuario(Long usuarioId) {
        String sql = "SELECT COUNT(*) FROM solicitacao WHERE usuario_id = ? AND status NOT IN ('RESOLVIDO', 'ENCERRADO')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar solicitações abertas.", e);
        }
    }

    @Override
    public void delete(Solicitacao entity) {
        throw new UnsupportedOperationException("Solicitações não são removidas, apenas encerradas.");
    }

    private List<Solicitacao> listar(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        List<Solicitacao> lista = new ArrayList<>();
        while (rs.next()) lista.add(mapRow(rs));
        return lista;
    }

    private Solicitacao mapRow(ResultSet rs) throws SQLException {
        Solicitacao s = new Solicitacao();
        s.setId(rs.getLong("id"));
        s.setProtocolo(rs.getString("protocolo"));
        s.setDescricao(rs.getString("descricao"));
        s.setBairro(rs.getString("bairro"));
        s.setLogradouro(rs.getString("logradouro"));
        s.setReferencia(rs.getString("referencia"));
        s.setAnonimo(rs.getBoolean("anonimo"));
        s.setNomeContato(rs.getString("nome_contato"));
        s.setEmailContato(rs.getString("email_contato"));

        String prioridade = rs.getString("prioridade");
        if (prioridade != null) s.setPrioridade(Prioridade.valueOf(prioridade));
        s.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));

        Timestamp dataAbertura = rs.getTimestamp("data_abertura");
        if (dataAbertura != null) s.setDataAbertura(dataAbertura.toLocalDateTime());
        Timestamp prazoAlvo = rs.getTimestamp("prazo_alvo");
        if (prazoAlvo != null) s.setPrazoAlvo(prazoAlvo.toLocalDateTime());
        Timestamp dataEncerramento = rs.getTimestamp("data_encerramento");
        if (dataEncerramento != null) s.setDataEncerramento(dataEncerramento.toLocalDateTime());

        long categoriaId = rs.getLong("categoria_id");
        s.setCategoria(categoriaRepository.findById(categoriaId));

        long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) s.setUsuario(usuarioRepository.findById(usuarioId));

        long atendenteId = rs.getLong("atendente_id");
        if (!rs.wasNull()) s.setAtendente(usuarioRepository.findById(atendenteId));

        return s;
    }
}
