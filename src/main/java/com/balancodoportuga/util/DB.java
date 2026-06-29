package com.balancodoportuga.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

    private static final String URL = "jdbc:sqlite:balancodoportuga.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite não encontrado", e);
        }

        try (Connection conn = getConnection()) {
            criarTabelas(conn);
            atualizarEstrutura(conn);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }

        return conn;
    }

    private static void criarTabelas(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS clients (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    nome TEXT NOT NULL,\n" +
                    "    cpf TEXT NOT NULL UNIQUE,\n" +
                    "    cnh TEXT NOT NULL,\n" +
                    "    email TEXT NOT NULL,\n" +
                    "    telefone TEXT,\n" +
                    "    endereco TEXT,\n" +
                    "    numero TEXT,\n" +
                    "    bairro TEXT,\n" +
                    "    cidade TEXT,\n" +
                    "    estado TEXT\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS veiculos (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    modelo TEXT NOT NULL,\n" +
                    "    marca TEXT NOT NULL,\n" +
                    "    placa TEXT NOT NULL UNIQUE,\n" +
                    "    ano INTEGER NOT NULL CHECK(ano >= 1950),\n" +
                    "    diaria REAL NOT NULL CHECK(diaria > 0),\n" +
                    "    tipo TEXT DEFAULT 'CARRO',\n" +
                    "    foto TEXT\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS reservas (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    cliente_id INTEGER NOT NULL,\n" +
                    "    veiculo_id INTEGER NOT NULL,\n" +
                    "    data_inicio TEXT NOT NULL,\n" +
                    "    data_fim TEXT NOT NULL,\n" +
                    "    total REAL NOT NULL,\n" +
                    "    status TEXT NOT NULL,\n" +
                    "    status_pagamento TEXT DEFAULT 'Não Pago',\n" +
                    "    FOREIGN KEY (cliente_id) REFERENCES clients(id),\n" +
                    "    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)\n" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS pagamentos (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    reserva_id INTEGER NOT NULL,\n" +
                    "    valor REAL NOT NULL,\n" +
                    "    data_pagamento TEXT NOT NULL,\n" +
                    "    metodo_pagamento TEXT NOT NULL,\n" +
                    "    tipo TEXT DEFAULT 'Pagamento',\n" +
                    "    FOREIGN KEY (reserva_id) REFERENCES reservas(id)\n" +
                    ")");
        }
    }

    private static void atualizarEstrutura(Connection conn) throws SQLException {
        adicionarColunaSeNaoExistir(conn, "veiculos", "tipo", "TEXT DEFAULT 'CARRO'");
        adicionarColunaSeNaoExistir(conn, "veiculos", "foto", "TEXT");

        adicionarColunaSeNaoExistir(conn, "pagamentos", "tipo", "TEXT DEFAULT 'Pagamento'");

        padronizarStatusReservas(conn);
    }

    private static void padronizarStatusReservas(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("UPDATE reservas SET status = 'SOLICITADA' " +
                    "WHERE status IN ('Solicitada', 'solicitada', 'ATIVA', 'Ativa', 'ativa')");

            st.executeUpdate("UPDATE reservas SET status = 'EM_ANDAMENTO' " +
                    "WHERE status IN ('Em andamento', 'em andamento', 'EM ANDAMENTO', 'EM_ANDAMENTO')");

            st.executeUpdate("UPDATE reservas SET status = 'CONCLUIDA' " +
                    "WHERE status IN ('Concluída', 'Concluida', 'concluída', 'concluida', 'CONCLUÍDA', 'CONCLUIDA')");

            st.executeUpdate("UPDATE reservas SET status = 'CANCELADA' " +
                    "WHERE status IN ('Cancelada', 'cancelada', 'CANCELADA')");
        }
    }

    private static void adicionarColunaSeNaoExistir(Connection conn, String tabela, String coluna, String definicao)
            throws SQLException {

        if (!colunaExiste(conn, tabela, coluna)) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + definicao);
            }
        }
    }

    private static boolean colunaExiste(Connection conn, String tabela, String coluna) throws SQLException {
        String sql = "PRAGMA table_info(" + tabela + ")";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String nomeColuna = rs.getString("name");

                if (coluna.equalsIgnoreCase(nomeColuna)) {
                    return true;
                }
            }
        }

        return false;
    }
}