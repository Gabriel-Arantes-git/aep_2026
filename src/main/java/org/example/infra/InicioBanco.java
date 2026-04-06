package org.example.infra;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicioBanco {

    public static void inicializarBanco() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            criarTabelaUsuario(statement);
            criarTabelaCategoria(statement);
            criarTabelaSlaConfig(statement);
            criarTabelaSolicitacao(statement);
            criarTabelaMovimentacao(statement);
            criarTabelaLogAcao(statement);
            inserirDadosIniciais(statement);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar o banco de dados.", e);
        }
    }

    private static void criarTabelaUsuario(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS usuario (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(255),
                email VARCHAR(255) UNIQUE,
                cpf VARCHAR(14),
                telefone VARCHAR(20),
                senha_hash VARCHAR(255),
                perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('CIDADAO', 'ATENDENTE', 'GESTOR')),
                ativo BOOLEAN DEFAULT TRUE,
                data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """);
    }

    private static void criarTabelaCategoria(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS categoria (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(100) NOT NULL UNIQUE,
                descricao VARCHAR(255),
                ativo BOOLEAN DEFAULT TRUE
            )
            """);
    }

    private static void criarTabelaSlaConfig(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS sla_config (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                prioridade VARCHAR(20) NOT NULL UNIQUE
                    CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
                prazo_horas INT NOT NULL,
                descricao VARCHAR(255)
            )
            """);
    }

    private static void criarTabelaSolicitacao(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS solicitacao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                protocolo VARCHAR(20)   NOT NULL UNIQUE,
                categoria_id BIGINT        NOT NULL,
                descricao VARCHAR(2000) NOT NULL,
                bairro VARCHAR(255)  NOT NULL,
                logradouro VARCHAR(255),
                referencia VARCHAR(255),
                anonimo BOOLEAN       DEFAULT FALSE,
                usuario_id BIGINT,
                nome_contato VARCHAR(255),
                email_contato VARCHAR(255),
                prioridade VARCHAR(20)   CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
                status VARCHAR(20)   NOT NULL DEFAULT 'ABERTO'
                                      CHECK (status IN ('ABERTO', 'TRIAGEM', 'EM_EXECUCAO', 'RESOLVIDO', 'ENCERRADO')),
                data_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                prazo_alvo TIMESTAMP,
                data_encerramento TIMESTAMP,
                atendente_id BIGINT,
                CONSTRAINT fk_solicitacao_categoria FOREIGN KEY (categoria_id)  REFERENCES categoria(id),
                CONSTRAINT fk_solicitacao_usuario   FOREIGN KEY (usuario_id)    REFERENCES usuario(id),
                CONSTRAINT fk_solicitacao_atendente FOREIGN KEY (atendente_id)  REFERENCES usuario(id)
            )
            """);
    }

    private static void criarTabelaMovimentacao(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS movimentacao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                solicitacao_id BIGINT        NOT NULL,
                status_anterior VARCHAR(20),
                status_novo VARCHAR(20)   NOT NULL,
                comentario VARCHAR(2000) NOT NULL,
                responsavel_id BIGINT,
                data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                justificativa_atraso VARCHAR(2000),
                CONSTRAINT fk_movimentacao_solicitacao  FOREIGN KEY (solicitacao_id)  REFERENCES solicitacao(id),
                CONSTRAINT fk_movimentacao_responsavel  FOREIGN KEY (responsavel_id)  REFERENCES usuario(id)
            )
            """);
    }

    private static void criarTabelaLogAcao(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS log_acao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                usuario_id  BIGINT,
                acao VARCHAR(100) NOT NULL,
                entidade VARCHAR(100),
                entidade_id BIGINT,
                detalhes VARCHAR(1000),
                data_acao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_log_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
            )
            """);
    }

    private static void inserirDadosIniciais(Statement statement) throws SQLException {
        inserirCategorias(statement);
        inserirSlaConfig(statement);
        inserirGestorPadrao(statement);
    }

    private static void inserirCategorias(Statement statement) throws SQLException {
        statement.execute("""
            MERGE INTO categoria (nome, descricao, ativo) KEY(nome) VALUES
                ('Lixo Irregular','Descarte irregular de lixo em locais não autorizados', TRUE)
            """);
        statement.execute("""
            MERGE INTO categoria (nome, descricao, ativo) KEY(nome) VALUES
                ('Queimada','Queimada ilegal ou fogo descontrolado', TRUE)
            """);
        statement.execute("""
            MERGE INTO categoria (nome, descricao, ativo) KEY(nome) VALUES
                ('Manutenção Urbana','Problemas de infraestrutura: buracos, iluminação, etc.', TRUE)
            """);
        statement.execute("""
            MERGE INTO categoria (nome, descricao, ativo) KEY(nome) VALUES
                ('Poluição','Poluição de rios, ar ou solo', TRUE)
            """);
    }

    private static void inserirSlaConfig(Statement statement) throws SQLException {
        statement.execute("""
            MERGE INTO sla_config (prioridade, prazo_horas, descricao) KEY(prioridade) VALUES
                ('BAIXA', 168, '7 dias — impacto local e baixo risco')
            """);
        statement.execute("""
            MERGE INTO sla_config (prioridade, prazo_horas, descricao) KEY(prioridade) VALUES
                ('MEDIA', 72, '3 dias — impacto moderado')
            """);
        statement.execute("""
            MERGE INTO sla_config (prioridade, prazo_horas, descricao) KEY(prioridade) VALUES
                ('ALTA', 24, '24 horas — risco à saúde ou segurança')
            """);
        statement.execute("""
            MERGE INTO sla_config (prioridade, prazo_horas, descricao) KEY(prioridade) VALUES
                ('CRITICA', 4, '4 horas — risco imediato, emergência')
            """);
    }

    private static void inserirGestorPadrao(Statement statement) throws SQLException {
        statement.execute("""
            MERGE INTO usuario (id, nome, email, cpf, senha_hash, perfil, ativo) KEY(id) VALUES
                (1, 'Administrador', 'admin@aep.gov', '000.000.000-00', 'admin123', 'GESTOR', TRUE),
                (2, 'Atendente', 'atendente@aep.gov', '111.111.111-11', 'atendente123', 'ATENDENTE', TRUE)
            """);
    }
}
