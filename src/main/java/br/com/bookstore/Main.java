package br.com.bookstore;

import br.com.bookstore.dao.LivroDAO;
import br.com.bookstore.dao.LivroDAOSqlite;
import br.com.bookstore.model.Livro;
import java.util.List;

/** Aplicação de console que executa o roteiro do exercício. */
public class Main {
    public static void main(String[] args) {
        LivroDAO dao = new LivroDAOSqlite();
        List<Livro> obras = List.of(
                new Livro("Clean Code", "Robert C. Martin", 149.90),
                new Livro("Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson e John Vlissides", 199.90),
                new Livro("Refactoring", "Martin Fowler", 179.90));

        System.out.println("=== CADASTRO DE LIVROS ===");
        for (Livro livro : obras) {
            dao.salvar(livro);
            System.out.printf("Salvo: %s | ID gerado: %d%n", livro.getTitulo(), livro.getId());
        }
        System.out.println("Em um banco novo, os IDs gerados são 1, 2 e 3.");

        System.out.println("\n=== CONSULTA: ID 2 ===");
        dao.buscarPorId(2L).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Livro de ID 2 não encontrado."));

        System.out.println("\n=== CONSULTA: ID 999 ===");
        dao.buscarPorId(999L).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Livro de ID 999 não encontrado. Ausência tratada com segurança."));

        System.out.println("\n=== CATÁLOGO COMPLETO ===");
        System.out.printf("%4s | %-25s | %-55s | %11s%n", "ID", "TÍTULO", "AUTOR", "PREÇO");
        System.out.println("-".repeat(104));
        dao.listarTodos().forEach(System.out::println);
    }
}
