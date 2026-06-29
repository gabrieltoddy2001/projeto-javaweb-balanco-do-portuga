package com.balancodoportuga.dao;

import com.balancodoportuga.model.Veiculo;
import com.balancodoportuga.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    public void insert(Veiculo v) throws SQLException {
        String sql = "INSERT INTO veiculos(modelo, marca, placa, ano, diaria, tipo, foto) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getModelo());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getPlaca());
            ps.setInt(4, v.getAno());
            ps.setDouble(5, v.getDiaria());
            ps.setString(6, normalizarTipo(v.getTipo()));
            ps.setString(7, v.getFoto());

            ps.executeUpdate();
        }
    }

    public void update(Veiculo v) throws SQLException {
        String sql = "UPDATE veiculos SET modelo=?, marca=?, placa=?, ano=?, diaria=?, tipo=?, foto=? WHERE id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getModelo());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getPlaca());
            ps.setInt(4, v.getAno());
            ps.setDouble(5, v.getDiaria());
            ps.setString(6, normalizarTipo(v.getTipo()));
            ps.setString(7, v.getFoto());
            ps.setInt(8, v.getId());

            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM veiculos WHERE id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Veiculo getById(int id) throws SQLException {
        String sql = "SELECT * FROM veiculos WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVeiculo(rs);
                }
            }
        }

        return null;
    }

    public Veiculo buscarPorPlaca(String placa) throws SQLException {
        String sql = "SELECT * FROM veiculos WHERE placa = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placa);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapVeiculo(rs);
                }
            }
        }

        return null;
    }

    public List<Veiculo> getAll() throws SQLException {
        List<Veiculo> veiculos = new ArrayList<>();
        String sql = "SELECT * FROM veiculos ORDER BY id DESC";

        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                veiculos.add(mapVeiculo(rs));
            }
        }

        return veiculos;
    }

    public boolean veiculoTemReservaAtiva(int veiculoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE veiculo_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, veiculoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private Veiculo mapVeiculo(ResultSet rs) throws SQLException {
        Veiculo v = new Veiculo();

        v.setId(rs.getInt("id"));
        v.setModelo(rs.getString("modelo"));
        v.setMarca(rs.getString("marca"));
        v.setPlaca(rs.getString("placa"));
        v.setAno(rs.getInt("ano"));
        v.setDiaria(rs.getDouble("diaria"));
        v.setTipo(getStringSeguro(rs, "tipo", "CARRO"));
        v.setFoto(getStringSeguro(rs, "foto", null));

        return v;
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

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return "CARRO";
        }

        if ("MOTO".equalsIgnoreCase(tipo)) {
            return "MOTO";
        }

        return "CARRO";
    }
}