package com.balancodoportuga.dao;

import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.model.Veiculo;
import com.balancodoportuga.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    private ClienteDAO clienteDAO;
    private VeiculoDAO veiculoDAO;

    public ReservaDAO() {
        this.clienteDAO = new ClienteDAO();
        this.veiculoDAO = new VeiculoDAO();
    }

    public void atualizarStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE reservas SET status = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizarStatus(novoStatus));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void insert(Reserva r) throws SQLException {
        String sqlVerificar = "SELECT COUNT(*) FROM reservas " +
                "WHERE veiculo_id = ? " +
                "AND status IN ('SOLICITADA', 'EM_ANDAMENTO') " +
                "AND ( " +
                "    (date(data_inicio) <= date(?) AND date(data_fim) >= date(?)) OR " +
                "    (date(data_inicio) <= date(?) AND date(data_fim) >= date(?)) OR " +
                "    (date(data_inicio) >= date(?) AND date(data_fim) <= date(?)) " +
                ")";

        String sqlInsert = "INSERT INTO reservas (cliente_id, veiculo_id, data_inicio, data_fim, total, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection()) {
            try (PreparedStatement psVerificar = conn.prepareStatement(sqlVerificar)) {
                psVerificar.setInt(1, r.getVehicle().getId());
                psVerificar.setString(2, r.getDataInicio().toString());
                psVerificar.setString(3, r.getDataInicio().toString());
                psVerificar.setString(4, r.getDataFim().toString());
                psVerificar.setString(5, r.getDataFim().toString());
                psVerificar.setString(6, r.getDataInicio().toString());
                psVerificar.setString(7, r.getDataFim().toString());

                try (ResultSet rs = psVerificar.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("O veículo já está reservado neste período para outra locação ativa.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, r.getClient().getId());
                ps.setInt(2, r.getVehicle().getId());
                ps.setString(3, r.getDataInicio().toString());
                ps.setString(4, r.getDataFim().toString());
                ps.setDouble(5, r.getTotal());
                ps.setString(6, normalizarStatus(r.getStatus()));
                ps.executeUpdate();
            }
        }
    }

    public void atualizarStatusAutomaticamente() throws SQLException {
        String sqlEmAndamento = "UPDATE reservas SET status = 'EM_ANDAMENTO' " +
                "WHERE status = 'SOLICITADA' " +
                "AND date('now') BETWEEN date(data_inicio) AND date(data_fim)";

        String sqlConcluida = "UPDATE reservas SET status = 'CONCLUIDA' " +
                "WHERE status = 'EM_ANDAMENTO' " +
                "AND date('now') > date(data_fim)";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sqlEmAndamento);
            stmt.executeUpdate(sqlConcluida);
        }
    }

    public List<Reserva> getAll(List<Cliente> clients, List<Veiculo> vehicles) throws SQLException {
        atualizarStatusAutomaticamente();

        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas ORDER BY id DESC";

        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapReserva(rs, clients));
            }
        }

        return lista;
    }

    public List<Reserva> getByClientId(int clientId, List<Cliente> clients, List<Veiculo> vehicles) throws SQLException {
        atualizarStatusAutomaticamente();

        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE cliente_id = ? ORDER BY id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapReserva(rs, clients));
                }
            }
        }

        return lista;
    }

    public boolean verificarConflitoReserva(int veiculoId, LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE veiculo_id = ? " +
                "AND (data_inicio <= ? AND data_fim >= ?) " +
                "AND status NOT IN ('CANCELADA', 'CONCLUIDA')";

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, veiculoId);
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public void cancelarReserva(int id) throws SQLException {
        String sql = "UPDATE reservas SET status = 'CANCELADA' WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void cancelarReservaComValor(int id, double total) throws SQLException {
        String sql = "UPDATE reservas SET status = 'CANCELADA', total = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void concluirReserva(int id, double novoTotal) throws SQLException {
        String sql = "UPDATE reservas SET total = ?, status = 'CONCLUIDA' WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, novoTotal);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void atualizarValorTotal(int reservaId, double novoTotal) throws SQLException {
        String sql = "UPDATE reservas SET total = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, novoTotal);
            ps.setInt(2, reservaId);
            ps.executeUpdate();
        }
    }

    public void atualizarStatusPagamento(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE reservas SET status_pagamento = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, novoStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Reserva getById(int id) throws SQLException {
        String sql = "SELECT * FROM reservas WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReserva(rs, null);
                }
            }
        }

        return null;
    }

    private Reserva mapReserva(ResultSet rs, List<Cliente> clients) throws SQLException {
        int clienteId = rs.getInt("cliente_id");
        int veiculoId = rs.getInt("veiculo_id");

        Cliente cliente = null;

        if (clients != null) {
            cliente = clients.stream()
                    .filter(cl -> cl.getId() == clienteId)
                    .findFirst()
                    .orElse(null);
        }

        if (cliente == null) {
            cliente = clienteDAO.findById(clienteId);
        }

        Veiculo veiculo = veiculoDAO.getById(veiculoId);

        return new Reserva(
                rs.getInt("id"),
                cliente,
                veiculo,
                LocalDate.parse(rs.getString("data_inicio")),
                LocalDate.parse(rs.getString("data_fim")),
                rs.getDouble("total"),
                normalizarStatus(rs.getString("status")),
                rs.getString("status_pagamento")
        );
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "SOLICITADA";
        }

        String s = status.trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("À", "A")
                .replace("Ã", "A")
                .replace("Â", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Õ", "O")
                .replace("Ô", "O")
                .replace("Ú", "U")
                .replace("Ç", "C");

        switch (s) {
            case "SOLICITADA":
            case "ATIVA":
                return "SOLICITADA";

            case "EM ANDAMENTO":
            case "EM_ANDAMENTO":
                return "EM_ANDAMENTO";

            case "CONCLUIDA":
                return "CONCLUIDA";

            case "CANCELADA":
                return "CANCELADA";

            default:
                return "SOLICITADA";
        }
    }
}