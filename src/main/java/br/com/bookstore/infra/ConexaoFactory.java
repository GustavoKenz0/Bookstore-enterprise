package br.com.bookstore.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexaoFactory {
    private ConexaoFactory() { }

    public static String urlPadrao() {
        return System.getProperty("bookstore.jdbc.url", "jdbc:sqlite:bookstore.db");
    }

    public static Connection obterConexao(String url) throws SQLException {
        return DriverManager.getConnection(url);
    }
}
