# Sistema de Denúncias Ambientais — AEP 2026

Sistema CLI para registro e acompanhamento de denúncias de problemas ambientais urbanos, como descarte irregular de lixo, queimadas e falhas de manutenção.

---

## Funcionalidades

**Cidadão**
- Abertura de solicitação identificada ou anônima
- Consulta por protocolo com histórico completo de movimentações
- Cadastro de conta

**Atendente**
- Visualização da fila por status
- Triagem com definição de prioridade e encaminhamento para departamento destino (calcula prazo automaticamente via SLA)
- Atualização de status com comentário obrigatório

**Gestor**
- Painel geral com todas as solicitações
- Filtro por status
- Atualização de status e encerramento de solicitações

---

## Fluxo de Status

```
ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO
                ↘                            ↗
                         ENCERRADO
                   (gestor pode encerrar direto da triagem)
```

Toda transição exige comentário. Se o prazo SLA estiver vencido, exige também justificativa de atraso.

---

## SLA (Prazo por Prioridade)

| Prioridade | Prazo   | Uso                              |
|------------|---------|----------------------------------|
| BAIXA      | 7 dias  | Impacto local e baixo risco      |
| MEDIA      | 3 dias  | Impacto moderado                 |
| ALTA       | 24h     | Risco à saúde ou segurança       |
| CRITICA    | 4h      | Risco imediato — emergência      |

O prazo é calculado automaticamente no momento da triagem e armazenado em `prazo_alvo` na solicitação.

---

## Protocolo

Formato: `DEN-YYYY-NNNNN` (ex.: `DEN-2026-00001`)

Gerado automaticamente na abertura. Para denúncias anônimas, é o único meio de acompanhamento.

---

## Arquitetura

```
src/main/java/org/example/
│
├── Main.java                          Ponto de entrada
│
├── infra/
│   ├── DatabaseConnection.java        Conexão H2
│   └── InicioBanco.java               Criação das tabelas e dados iniciais
│
├── auth/
│   └── AutenticacaoService.java       Login, logout, sessão atual
│
├── ui/
│   └── TerminalInterface.java         CLI — todos os menus e fluxos
│
└── domain/
    ├── abstraction/
    │   ├── DefaultEntity.java         Base de todas as entidades (id)
    │   ├── DefaultRepository<T>       Interface CRUD + update
    │   └── DefaultService<T, R>       Classe abstrata com getRepository()
    │
    ├── enums/
    │   ├── PerfilUsuario              CIDADAO · ATENDENTE · GESTOR
    │   ├── Prioridade                 BAIXA · MEDIA · ALTA · CRITICA
    │   └── StatusSolicitacao          ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO
    │                                  (com validação de transição em podeMoverPara())
    ├── usuario/
    │   ├── entity/Usuario
    │   ├── repository/UsuarioRepository
    │   └── service/UsuarioService
    │
    ├── categoria/
    │   ├── entity/Categoria
    │   ├── repository/CategoriaRepository
    │   └── service/CategoriaService
    │
    ├── departamento/
    │   ├── entity/DepartamentoDestino
    │   ├── repository/DepartamentoRepository
    │   └── service/DepartamentoService
    │
    ├── sla/
    │   ├── entity/SlaConfig
    │   └── repository/SlaRepository
    │
    ├── solicitacao/
    │   ├── entity/Solicitacao          Entidade central
    │   ├── entity/Movimentacao         Histórico de transições
    │   ├── repository/SolicitacaoRepository
    │   ├── repository/MovimentacaoRepository
    │   └── service/SolicitacaoService
    │
    └── log/
        ├── entity/LogAcao             Auditoria de ações críticas
        └── repository/LogRepository
```

### Padrão de serviços

Todo service estende `DefaultService<T, R>` e implementa `getRepository()` retornando o repositório concreto. Os métodos `salvar`, `buscarPorId`, `atualizar` e `deletar` são herdados e delegados ao repositório. Regras de negócio são adicionadas via `@Override`.

```
DefaultService<T, R>
  getRepository()  ← abstract, retorna R concreto
  salvar()         → getRepository().save()
  buscarPorId()    → getRepository().findById()
  atualizar()      → getRepository().update()
  deletar()        → getRepository().delete()
```

---

## Banco de Dados

- **H2** embarcado, arquivo local em `./data/observacao_db`
- Schema criado automaticamente na primeira execução via `InicioBanco`
- `MERGE INTO` nos dados iniciais garante idempotência (seguro rodar múltiplas vezes)

**Tabelas:**

| Tabela        | Responsabilidade                              |
|---------------|-----------------------------------------------|
| `usuario`     | Cidadãos, atendentes e gestores               |
| `categoria`   | Tipos de problema ambiental                   |
| `sla_config`  | Prazo-alvo por prioridade                     |
| `solicitacao` | Denúncias — entidade central                  |
| `movimentacao`        | Histórico de mudanças de status               |
| `departamento_destino`| Órgãos/departamentos para encaminhamento      |
| `log_acao`            | Auditoria de operações críticas               |

---

## Departamentos Destino (padrão)

| Departamento         | Responsabilidade                        |
|----------------------|-----------------------------------------|
| Prefeitura Municipal | Infraestrutura urbana geral             |
| COPEL                | Energia elétrica                        |
| SANEPAR              | Saneamento básico                       |
| COMPAGAS             | Distribuição de gás                     |
| SESP                 | Segurança pública                       |
| SEMA                 | Meio ambiente                           |

---

## Regras de Negócio Relevantes

- Denúncias anônimas exigem descrição com no mínimo 50 caracteres
- Transições de status seguem fluxo fixo — voltar status não é permitido
- Comentário obrigatório em toda movimentação
- Justificativa de atraso obrigatória quando `prazo_alvo` está vencido
- Solicitações nunca são deletadas — apenas encerradas

---

## Tecnologias

| Item         | Detalhe              |
|--------------|----------------------|
| Linguagem    | Java 21              |
| Build        | Maven                |
| Banco        | H2 2.4.240 (embedded)|
| Persistência | JDBC puro            |
| Interface    | CLI (terminal)       |

---

## Execução

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

O banco é criado automaticamente em `./data/observacao_db` na primeira execução.

**Usuários padrão criados na inicialização:**

| E-mail                  | Senha          | Perfil    |
|-------------------------|----------------|-----------|
| admin@aep.gov           | admin123       | GESTOR    |
| atendente@aep.gov       | atendente123   | ATENDENTE |
