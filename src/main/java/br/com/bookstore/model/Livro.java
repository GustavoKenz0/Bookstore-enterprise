package br.com.bookstore.model;

import java.util.Locale;

/** Entidade de domínio: contém as validações, sem depender de JDBC. */
public class Livro {
    private Long id;
    private final String titulo;
    private final String autor;
    private final double preco;

    public Livro(Long id, String titulo, String autor, double preco) {
        if (titulo == null || titulo.trim().length() < 2) {
            throw new IllegalArgumentException("O título deve ter pelo menos 2 caracteres.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("O autor não pode ser vazio.");
        }
        if (!Double.isFinite(preco) || preco <= 0.0) {
            throw new IllegalArgumentException("O preço de venda deve ser estritamente positivo");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.autor = autor.trim();
        this.preco = preco;
    }

    public Livro(String titulo, String autor, double preco) {
        this(null, titulo, autor, preco);
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public double getPreco() { return preco; }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"),
                "%4s | %-25s | %-55s | R$ %8.2f",
                id == null ? "-" : id, titulo, autor, preco);
    }
}
