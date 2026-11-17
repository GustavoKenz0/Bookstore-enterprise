package br.com.bookstore;

import br.com.bookstore.dao.LivroDAO;
import br.com.bookstore.dao.LivroDAOSqlite;
import br.com.bookstore.model.Livro;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class BookstoreTest {
    @TempDir Path pasta;

    private String url() {
        return "jdbc:sqlite:" + pasta.resolve("teste.db");
    }

    @Test
    void validaTituloNosDoisConstrutores() {
        for (String titulo : new String[] {null, "", "   ", " a "}) {
            assertThrows(IllegalArgumentException.class, () -> new Livro(titulo, "Autor", 10));
            assertThrows(IllegalArgumentException.class, () -> new Livro(1L, titulo, "Autor", 10));
        }
    }

    @Test
    void validaAutorEPreco() {
        for (String autor : new String[] {null, "", "   "}) {
            assertThrows(IllegalArgumentException.class, () -> new Livro("Livro", autor, 10));
        }
        for (double preco : new double[] {0, -10, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Livro("Livro", "Autor", preco));
        }
    }

    @Test
    void construtoresPreservamDadosValidos() {
        Livro novo = new Livro(" Livro ", " Autor ", 10.5);
        assertNull(novo.getId());
        assertEquals("Livro", novo.getTitulo());
        assertEquals("Autor", novo.getAutor());
        Livro hidratado = new Livro(7L, "Livro", "Autor", 10.5);
        assertEquals(7L, hidratado.getId());
        assertEquals(10.5, hidratado.getPreco());
        assertTrue(hidratado.toString().contains("10,50"));
    }

    @Test
    void bancoNovoTemListaVaziaEBuscaAusente() {
        LivroDAO dao = new LivroDAOSqlite(url());
        assertTrue(dao.listarTodos().isEmpty());
        assertTrue(dao.buscarPorId(999L).isEmpty());
        assertDoesNotThrow(() -> dao.buscarPorId(999L).ifPresentOrElse(
                livro -> fail("Não deveria existir um livro."), () -> { }));
    }

    @Test
    void geraIdsConsultaListaEPersisteEntreInstancias() {
        LivroDAO dao = new LivroDAOSqlite(url());
        Livro primeiro = new Livro("Clean Code", "Robert C. Martin", 149.90);
        Livro segundo = new Livro("Design Patterns", "Erich Gamma", 199.90);
        Livro terceiro = new Livro("Refactoring", "Martin Fowler", 179.90);
        List.of(primeiro, segundo, terceiro).forEach(dao::salvar);
        assertEquals(1L, primeiro.getId());
        assertEquals(2L, segundo.getId());
        assertEquals(3L, terceiro.getId());
        Livro lido = dao.buscarPorId(2L).orElseThrow();
        assertEquals(segundo.getTitulo(), lido.getTitulo());
        assertEquals(segundo.getAutor(), lido.getAutor());
        assertEquals(segundo.getPreco(), lido.getPreco());
        LivroDAO reaberto = new LivroDAOSqlite(url());
        assertEquals(List.of(1L, 2L, 3L), reaberto.listarTodos().stream().map(Livro::getId).toList());
        assertTrue(reaberto.buscarPorId(999L).isEmpty());
    }

    @Test
    void parametrosPreservamAspasEAcentosSemExecutarSqlInserido() {
        LivroDAO dao = new LivroDAOSqlite(url());
        String titulo = "Ação d'água'); DROP TABLE livros; --";
        Livro livro = new Livro(titulo, "Autor d'Ávila", 29.90);
        dao.salvar(livro);
        assertEquals(titulo, dao.buscarPorId(livro.getId()).orElseThrow().getTitulo());
        dao.salvar(new Livro("Outro livro", "Outra pessoa", 30));
        assertEquals(2, dao.listarTodos().size());
    }
}
