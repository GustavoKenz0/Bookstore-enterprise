package br.com.bookstore.dao;

import br.com.bookstore.infra.ConexaoFactory;
import br.com.bookstore.model.Livro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LivroDAOSqlite implements LivroDAO {
    private final String url;

    public LivroDAOSqlite() {
        this(ConexaoFactory.urlPadrao());
    }

    /** Permite usar outro arquivo SQLite, inclusive nos testes. */
    public LivroDAOSqlite(String url) {
        this.url = Objects.requireNonNull(url, "A URL do banco é obrigatória.");
        String sql = """
                CREATE TABLE IF NOT EXISTS livros (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL,
                    autor TEXT NOT NULL,
                    preco REAL NOT NULL
                );
                """;
        try (Connection conn = ConexaoFactory.obterConexao(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar a tabela de livros.", e);
        }
    }

    @Override
    public void salvar(Livro livro) {
        Objects.requireNonNull(livro, "O livro é obrigatório.");
        String sql = "INSERT INTO livros (titulo, autor, preco) VALUES (?, ?, ?);";
        try (Connection conn = ConexaoFactory.obterConexao(url)) {
            // A inserção e a recuperação do ID compõem uma única operação.
            conn.setAutoCommit(false);
            try {
                long idGerado;
                try (PreparedStatement stmt = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, livro.getTitulo());
                    stmt.setString(2, livro.getAutor());
                    stmt.setDouble(3, livro.getPreco());
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("O livro não foi inserido.");
                    }
                    try (ResultSet chaves = stmt.getGeneratedKeys()) {
                        if (!chaves.next()) {
                            throw new SQLException("O banco não retornou o ID gerado.");
                        }
                        idGerado = chaves.getLong(1);
                    }
                }
                conn.commit();
                livro.setId(idGerado);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar livro.", e);
        }
    }

    @Override
    public Optional<Livro> buscarPorId(Long id) {
        Objects.requireNonNull(id, "O ID é obrigatório.");
        String sql = "SELECT id, titulo, autor, preco FROM livros WHERE id = ?;";
        try (Connection conn = ConexaoFactory.obterConexao(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearLinha(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar livro por ID.", e);
        }
    }

    @Override
    public List<Livro> listarTodos() {
        String sql = "SELECT id, titulo, autor, preco FROM livros ORDER BY id;";
        List<Livro> livros = new ArrayList<>();
        try (Connection conn = ConexaoFactory.obterConexao(url);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                livros.add(mapearLinha(rs));
            }
            return livros;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros.", e);
        }
    }

    private Livro mapearLinha(ResultSet rs) throws SQLException {
        return new Livro(rs.getLong("id"), rs.getString("titulo"),
                rs.getString("autor"), rs.getDouble("preco"));
    }
}
