package com.balancodoportuga.dao;

import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean existeCPF(String cpf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clients WHERE cpf = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void insert(Cliente c) throws SQLException {
        if (existeCPF(c.getCpf())) {
            throw new SQLException("Já existe um cliente cadastrado com este CPF.");
        }
        String sql = "INSERT INTO clients(nome, cpf, email, telefone, endereco, numero, bairro, cidade, estado, cnh) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelefone());
            ps.setString(5, c.getEndereco());
            ps.setString(6, c.getNumero());
            ps.setString(7, c.getBairro());
            ps.setString(8, c.getCidade());
            ps.setString(9, c.getEstado());
            ps.setString(10, c.getCnh());
            ps.executeUpdate();
        }
    }

    public void update(Cliente c) throws SQLException {
        String sql = "UPDATE clients SET nome=?, cpf=?, email=?, telefone=?, endereco=?, numero=?, bairro=?, cidade=?, estado=?, cnh=? WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelefone());
            ps.setString(5, c.getEndereco());
            ps.setString(6, c.getNumero());
            ps.setString(7, c.getBairro());
            ps.setString(8, c.getCidade());
            ps.setString(9, c.getEstado());
            ps.setString(10, c.getCnh());
            ps.setInt(11, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        if (clienteTemReserva(id)) {
            throw new SQLException("Este cliente possui uma ou mais reservas e não pode ser excluído.");
        }
        String sql = "DELETE FROM clients WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private boolean clienteTemReserva(int clienteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE cliente_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Cliente> getAll() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                clientes.add(mapCliente(rs));
            }
        }
        return clientes;
    }

    public Cliente buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM clients WHERE email = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCliente(rs);
            }
        }
        return null;
    }

    public Cliente findById(int id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCliente(rs);
            }
        }
        return null;
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id"), rs.getString("nome"), rs.getString("cpf"),
            rs.getString("email"), rs.getString("telefone"), rs.getString("endereco"),
            rs.getString("numero"), rs.getString("bairro"), rs.getString("cidade"),
            rs.getString("estado"), rs.getString("cnh")
        );
    }
}
