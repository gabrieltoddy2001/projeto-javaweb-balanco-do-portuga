package com.balancodoportuga.service;

import com.balancodoportuga.dao.ClienteDAO;
import com.balancodoportuga.dao.PagamentoDAO;
import com.balancodoportuga.dao.ReservaDAO;
import com.balancodoportuga.dao.VeiculoDAO;
import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.model.Pagamento;
import com.balancodoportuga.model.Reserva;

import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private ClienteDAO clienteDAO;
    private ReservaDAO reservaDAO;
    private PagamentoDAO pagamentoDAO;
    private VeiculoDAO veiculoDAO;

    public ClienteService() {
        this.clienteDAO = new ClienteDAO();
        this.reservaDAO = new ReservaDAO();
        this.pagamentoDAO = new PagamentoDAO();
        this.veiculoDAO = new VeiculoDAO();
    }

    public List<Cliente> listarTodos() throws SQLException {
        return clienteDAO.getAll();
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return clienteDAO.findById(id);
    }

    public Cliente buscarPorEmail(String email) throws SQLException {
        return clienteDAO.buscarPorEmail(email);
    }

    public void salvar(Cliente c) throws SQLException {
        if (c.getId() == 0) {
            clienteDAO.insert(c);
        } else {
            clienteDAO.update(c);
        }
    }

    public void excluir(int id) throws SQLException {
        clienteDAO.delete(id);
    }

    public Reserva buscarReservaAtiva(int clienteId) throws SQLException {
    List<Reserva> reservas = reservaDAO.getByClientId(clienteId, clienteDAO.getAll(), veiculoDAO.getAll());

    for (Reserva r : reservas) {
        if ("EM_ANDAMENTO".equalsIgnoreCase(r.getStatus()) || "SOLICITADA".equalsIgnoreCase(r.getStatus())) {
            return r;
        }
    }

    return null;
}

    public List<Reserva> listarReservasCliente(int clienteId) throws SQLException {
        return reservaDAO.getByClientId(clienteId, clienteDAO.getAll(), veiculoDAO.getAll());
    }

    public List<Pagamento> listarPagamentosCliente(int clienteId) throws SQLException {
        return pagamentoDAO.getByCliente(clienteId);
    }

    public List<Cliente> filtrar(List<Cliente> lista, String termo) {
        if (lista == null || termo == null || termo.isBlank()) return lista;
        String filtro = termo.trim().toLowerCase();
        return lista.stream()
            .filter(c -> c.getNome().toLowerCase().contains(filtro)
                || c.getCpf().contains(filtro)
                || c.getEmail().toLowerCase().contains(filtro)
                || c.getTelefone().contains(filtro))
            .toList();
    }
}
