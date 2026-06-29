package com.balancodoportuga.service;

import com.balancodoportuga.dao.VeiculoDAO;
import com.balancodoportuga.model.Veiculo;

import java.sql.SQLException;
import java.util.List;

public class VeiculoService {

    private VeiculoDAO veiculoDAO;

    public VeiculoService() {
        this.veiculoDAO = new VeiculoDAO();
    }

    public List<Veiculo> listarTodos() throws SQLException {
        return veiculoDAO.getAll();
    }

    public Veiculo buscarPorId(int id) throws SQLException {
        return veiculoDAO.getById(id);
    }

    public void salvar(Veiculo v) throws SQLException {
        validarVeiculo(v);

        if (v.getId() == 0) {
            veiculoDAO.insert(v);
        } else {
            veiculoDAO.update(v);
        }
    }

    public void excluir(int id) throws SQLException {
        if (veiculoDAO.veiculoTemReservaAtiva(id)) {
            throw new SQLException("Este veículo possui reserva ativa e não pode ser excluído.");
        }

        veiculoDAO.delete(id);
    }

    public List<Veiculo> filtrar(List<Veiculo> lista, String termo) {
        if (lista == null || termo == null || termo.isBlank()) {
            return lista;
        }

        String filtro = termo.trim().toLowerCase();

        return lista.stream()
                .filter(v -> contem(v.getModelo(), filtro)
                        || contem(v.getMarca(), filtro)
                        || contem(v.getPlaca(), filtro)
                        || contem(v.getTipo(), filtro))
                .toList();
    }

    private void validarVeiculo(Veiculo v) throws SQLException {
        if (v == null) {
            throw new SQLException("Veículo inválido.");
        }

        if (v.getModelo() == null || v.getModelo().isBlank()) {
            throw new SQLException("Informe o modelo do veículo.");
        }

        if (v.getMarca() == null || v.getMarca().isBlank()) {
            throw new SQLException("Informe a marca do veículo.");
        }

        if (v.getPlaca() == null || v.getPlaca().isBlank()) {
            throw new SQLException("Informe a placa do veículo.");
        }

        if (v.getAno() < 1950) {
            throw new SQLException("Informe um ano válido para o veículo.");
        }

        if (v.getDiaria() <= 0) {
            throw new SQLException("Informe uma diária maior que zero.");
        }

        if (v.getTipo() == null || v.getTipo().isBlank()) {
            v.setTipo("CARRO");
        }

        if (!"CARRO".equalsIgnoreCase(v.getTipo()) && !"MOTO".equalsIgnoreCase(v.getTipo())) {
            v.setTipo("CARRO");
        }
    }

    private boolean contem(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }
}