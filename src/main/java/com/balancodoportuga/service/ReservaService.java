package com.balancodoportuga.service;

import com.balancodoportuga.dao.ClienteDAO;
import com.balancodoportuga.dao.ReservaDAO;
import com.balancodoportuga.dao.VeiculoDAO;
import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.model.Veiculo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ReservaService {

    private ReservaDAO reservaDAO;
    private ClienteDAO clienteDAO;
    private VeiculoDAO veiculoDAO;

    public ReservaService() {
        this.reservaDAO = new ReservaDAO();
        this.clienteDAO = new ClienteDAO();
        this.veiculoDAO = new VeiculoDAO();
    }

    public List<Reserva> listarTodas() throws SQLException {
        reservaDAO.atualizarStatusAutomaticamente();
        return reservaDAO.getAll(clienteDAO.getAll(), veiculoDAO.getAll());
    }

    public List<Reserva> listarPorCliente(int clienteId) throws SQLException {
        reservaDAO.atualizarStatusAutomaticamente();
        return reservaDAO.getByClientId(clienteId, clienteDAO.getAll(), veiculoDAO.getAll());
    }

    public Reserva buscarAtiva(int clienteId) throws SQLException {
        List<Reserva> reservas = listarPorCliente(clienteId);

        return reservas.stream()
                .filter(r -> "EM_ANDAMENTO".equalsIgnoreCase(r.getStatus())
                        || "SOLICITADA".equalsIgnoreCase(r.getStatus()))
                .findFirst()
                .orElse(null);
    }

    public void criarReserva(int clienteId, int veiculoId, LocalDate dataInicio, LocalDate dataFim) throws Exception {
        if (dataInicio == null || dataFim == null) {
            throw new Exception("Datas não podem ser vazias.");
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new Exception("A data de fim não pode ser antes da data de início.");
        }

        if (dataInicio.isBefore(LocalDate.now())) {
            throw new Exception("A data inicial não pode ser anterior ao dia atual.");
        }

        long dias = ChronoUnit.DAYS.between(dataInicio, dataFim);

        if (dias <= 0) {
            throw new Exception("A reserva deve ter pelo menos 1 dia.");
        }

        Veiculo veiculo = veiculoDAO.getById(veiculoId);

        if (veiculo == null) {
            throw new Exception("Veículo não encontrado.");
        }

        boolean conflito = reservaDAO.verificarConflitoReserva(veiculoId, dataInicio, dataFim);

        if (conflito) {
            throw new Exception("O veículo selecionado está indisponível neste período.");
        }

        double total = dias * veiculo.getDiaria();

        Reserva reserva = new Reserva(
                0,
                new Cliente(clienteId),
                veiculo,
                dataInicio,
                dataFim,
                total,
                "SOLICITADA",
                "Pendente"
        );

        reservaDAO.insert(reserva);
    }

    public boolean verificarDisponibilidade(int veiculoId, LocalDate inicio, LocalDate fim) throws SQLException {
        return reservaDAO.verificarConflitoReserva(veiculoId, inicio, fim);
    }

    public void cancelarReserva(int reservaId) throws SQLException {
        reservaDAO.cancelarReserva(reservaId);
    }

    public void cancelarReservaComValor(int reservaId, double total) throws SQLException {
        reservaDAO.cancelarReservaComValor(reservaId, total);
    }

    public void concluirReserva(int reservaId, double novoTotal) throws SQLException {
        reservaDAO.concluirReserva(reservaId, novoTotal);
    }

    public double calcularMulta(Reserva r) {
        long diasAtraso = ChronoUnit.DAYS.between(r.getDataFim(), LocalDate.now());
        return diasAtraso > 0 ? r.getVehicle().getDiaria() * 0.3 * diasAtraso : 0;
    }

    public double calcularTotalComMulta(Reserva r) {
        return r.getTotal() + calcularMulta(r);
    }

    public List<Reserva> filtrarReservas(List<Reserva> listaOriginal, String termo, String status, LocalDate inicio, LocalDate fim) {
        return listaOriginal.stream()
                .filter(r -> termo == null || termo.isBlank()
                        || (r.getClient() != null && r.getClient().getNome().toLowerCase().contains(termo.toLowerCase()))
                        || (r.getVehicle() != null && r.getVehicle().getModelo().toLowerCase().contains(termo.toLowerCase()))
                        || (r.getVehicle() != null && r.getVehicle().getPlaca().toLowerCase().contains(termo.toLowerCase())))
                .filter(r -> status == null
                        || status.isBlank()
                        || "Todos".equalsIgnoreCase(status)
                        || r.getStatus().equalsIgnoreCase(status))
                .filter(r -> (inicio == null || !r.getDataInicio().isBefore(inicio))
                        && (fim == null || !r.getDataFim().isAfter(fim)))
                .collect(Collectors.toList());
    }

    public double calcularTotalReservas(List<Reserva> reservas) {
        return reservas.stream()
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getStatus()))
                .mapToDouble(Reserva::getTotal)
                .sum();
    }

    public Reserva buscarPorId(int id) throws SQLException {
        return reservaDAO.getById(id);
    }
}