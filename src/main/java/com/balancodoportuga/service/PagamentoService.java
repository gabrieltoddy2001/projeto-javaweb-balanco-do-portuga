package com.balancodoportuga.service;

import com.balancodoportuga.dao.PagamentoDAO;
import com.balancodoportuga.dao.ReservaDAO;
import com.balancodoportuga.model.Pagamento;
import com.balancodoportuga.model.Reserva;

import java.sql.SQLException;
import java.util.List;

public class PagamentoService {

    private PagamentoDAO pagamentoDAO;
    private ReservaDAO reservaDAO;

    public PagamentoService() {
        this.pagamentoDAO = new PagamentoDAO();
        this.reservaDAO = new ReservaDAO();
    }

    public List<Pagamento> listarPorCliente(int clienteId) throws SQLException {
        return pagamentoDAO.getByCliente(clienteId);
    }

    public List<Pagamento> listarPorReserva(int reservaId) throws SQLException {
        return pagamentoDAO.getByReservaId(reservaId);
    }

    public void registrarPagamento(Reserva reserva, double valor, String metodo) throws SQLException {
        pagamentoDAO.registrarPagamento(reserva, valor, metodo);
        double pago = pagamentoDAO.getTotalPago(reserva.getId());
        double total = reserva.getTotal();
        if (pago >= total) {
            reservaDAO.atualizarStatusPagamento(reserva.getId(), "Pago");
        } else {
            reservaDAO.atualizarStatusPagamento(reserva.getId(), "Parcial");
        }
    }

    public void aplicarMulta(int reservaId, double valor, String descricao) throws SQLException {
    pagamentoDAO.inserirPagamentoMulta(reservaId, valor, descricao);

    Reserva reservaAtualizada = reservaDAO.getById(reservaId);
    double totalPago = pagamentoDAO.getTotalPago(reservaId);

    if (reservaAtualizada != null) {
        if (totalPago >= reservaAtualizada.getTotal()) {
            reservaDAO.atualizarStatusPagamento(reservaId, "Pago");
        } else if (totalPago > 0) {
            reservaDAO.atualizarStatusPagamento(reservaId, "Parcial");
        } else {
            reservaDAO.atualizarStatusPagamento(reservaId, "Pendente");
        }
    }
}

    public double getTotalPago(int reservaId) throws SQLException {
        return pagamentoDAO.getTotalPago(reservaId);
    }

    public int calcularProgresso(Reserva reserva) throws SQLException {
        double total = reserva.getTotal();
        if (total <= 0) return 0;
        double pago = pagamentoDAO.getTotalPago(reserva.getId());
        return (int) ((pago / total) * 100);
    }

    public double calcularRestante(Reserva reserva) throws SQLException {
        return Math.max(reserva.getTotal() - pagamentoDAO.getTotalPago(reserva.getId()), 0);
    }

    public String getCorProgresso(int progresso) {
        if (progresso >= 100) return "#2ecc71";
        if (progresso >= 50) return "#f1c40f";
        return "#e74c3c";
    }
}
