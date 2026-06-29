package com.balancodoportuga.dao;

import com.balancodoportuga.model.Pagamento;
import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    private ReservaDAO reservaDAO;

    public PagamentoDAO() {
        this.reservaDAO = new ReservaDAO();
    }

    public void registrarPagamento(Reserva reserva, double valorPago, String metodo) throws SQLException {
        String sqlInsert = "INSERT INTO pagamentos (reserva_id, valor, data_pagamento, metodo_pagamento, tipo) " +
                "VALUES (?, ?, date('now'), ?, 'Pagamento')";

        String sqlSoma = "SELECT COALESCE(SUM(valor), 0) FROM pagamentos " +
                "WHERE reserva_id = ? " +
                "AND (tipo = 'Pagamento' OR tipo IS NULL OR tipo = '') " +
                "AND metodo_pagamento NOT LIKE 'Multa:%'";

        String sqlPago = "UPDATE reservas SET status_pagamento = 'Pago' WHERE id = ?";
        String sqlParcial = "UPDATE reservas SET status_pagamento = 'Parcial' WHERE id = ?";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setInt(1, reserva.getId());
                    ps.setDouble(2, valorPago);
                    ps.setString(3, metodo);
                    ps.executeUpdate();
                }

                double totalPago = 0;

                try (PreparedStatement ps = conn.prepareStatement(sqlSoma)) {
                    ps.setInt(1, reserva.getId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            totalPago = rs.getDouble(1);
                        }
                    }
                }

                if (totalPago >= reserva.getTotal()) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlPago)) {
                        ps.setInt(1, reserva.getId());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(sqlParcial)) {
                        ps.setInt(1, reserva.getId());
                        ps.executeUpdate();
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Pagamento> getByCliente(int clienteId) throws SQLException {
        List<Pagamento> lista = new ArrayList<>();

        String sql = "SELECT p.* FROM pagamentos p " +
                "INNER JOIN reservas r ON r.id = p.reserva_id " +
                "WHERE r.cliente_id = ? " +
                "ORDER BY date(p.data_pagamento) DESC, p.id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = reservaDAO.getById(rs.getInt("reserva_id"));

                    if (reserva == null) {
                        continue;
                    }

                    lista.add(mapPagamento(rs, reserva));
                }
            }
        }

        return lista;
    }

    public List<Pagamento> getAll() throws SQLException {
        List<Pagamento> lista = new ArrayList<>();

        String sql = "SELECT * FROM pagamentos ORDER BY date(data_pagamento) DESC, id DESC";

        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reserva reserva = reservaDAO.getById(rs.getInt("reserva_id"));

                if (reserva == null) {
                    continue;
                }

                lista.add(mapPagamento(rs, reserva));
            }
        }

        return lista;
    }

    public double getTotalPago(int reservaId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor), 0) FROM pagamentos " +
                "WHERE reserva_id = ? " +
                "AND (tipo = 'Pagamento' OR tipo IS NULL OR tipo = '') " +
                "AND metodo_pagamento NOT LIKE 'Multa:%'";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }

        return 0.0;
    }

    public void inserirPagamentoMulta(int reservaId, double valor, String descricao) throws SQLException {
        String sqlInsert = "INSERT INTO pagamentos (reserva_id, valor, data_pagamento, metodo_pagamento, tipo) " +
                "VALUES (?, ?, date('now'), ?, 'Multa')";

        String sqlAtualizaTotal = "UPDATE reservas SET total = total + ? WHERE id = ?";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setInt(1, reservaId);
                    ps.setDouble(2, valor);
                    ps.setString(3, descricao);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlAtualizaTotal)) {
                    ps.setDouble(1, valor);
                    ps.setInt(2, reservaId);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Pagamento> getByReservaId(int reservaId) throws SQLException {
        List<Pagamento> pagamentos = new ArrayList<>();

        String sql = "SELECT * FROM pagamentos " +
                "WHERE reserva_id = ? " +
                "ORDER BY date(data_pagamento) DESC, id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reserva r = reservaDAO.getById(reservaId);

                    if (r == null) {
                        continue;
                    }

                    pagamentos.add(mapPagamento(rs, r));
                }
            }
        }

        return pagamentos;
    }

    private Pagamento mapPagamento(ResultSet rs, Reserva reserva) throws SQLException {
        String dataStr = rs.getString("data_pagamento");

        LocalDate dataPagamento;

        if (dataStr != null && dataStr.length() >= 10) {
            dataPagamento = LocalDate.parse(dataStr.substring(0, 10));
        } else {
            dataPagamento = LocalDate.now();
        }

        String metodo = rs.getString("metodo_pagamento");
        String tipo = getStringSeguro(rs, "tipo", null);

        if (tipo == null || tipo.isBlank()) {
            if (metodo != null && metodo.toLowerCase().startsWith("multa:")) {
                tipo = "Multa";
                metodo = metodo.substring(6).trim();
            } else {
                tipo = "Pagamento";
            }
        }

        if ("Multa".equalsIgnoreCase(tipo) && metodo != null && metodo.toLowerCase().startsWith("multa:")) {
            metodo = metodo.substring(6).trim();
        }

        return new Pagamento(
                rs.getInt("id"),
                reserva,
                rs.getDouble("valor"),
                dataPagamento,
                metodo,
                tipo
        );
    }

    private String getStringSeguro(ResultSet rs, String coluna, String valorPadrao) {
        try {
            String valor = rs.getString(coluna);

            if (valor == null || valor.trim().isEmpty()) {
                return valorPadrao;
            }

            return valor;

        } catch (SQLException e) {
            return valorPadrao;
        }
    }
}