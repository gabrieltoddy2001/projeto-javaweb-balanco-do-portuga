package com.balancodoportuga.bean;

import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.service.PagamentoService;
import com.balancodoportuga.service.ReservaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named("dashboardFuncionarioBean")
@ViewScoped
public class DashboardFuncionarioBean implements Serializable {

    private static final double EPS = 0.0001;

    private ReservaService reservaService;
    private PagamentoService pagamentoService;

    private List<Reserva> reservas;
    private List<Reserva> ultimasReservas;

    private int totalReservas;
    private int reservasAtivas;
    private int reservasConcluidas;
    private int reservasCanceladas;

    private int clientesAtendidos;
    private int veiculosMovimentados;

    private int pagamentosPendentes;
    private int pagamentosParciais;
    private int pagamentosPagos;

    private double receitaTotal;
    private double receitaRecebida;
    private double receitaPendente;
    private double ticketMedio;

    public DashboardFuncionarioBean() {
        this.reservaService = new ReservaService();
        this.pagamentoService = new PagamentoService();
    }

    @PostConstruct
    public void init() {
        carregarDashboard();
    }

    public void carregarDashboard() {
        try {
            reservas = reservaService.listarTodas();

            if (reservas == null) {
                reservas = new ArrayList<>();
            }

            totalReservas = reservas.size();

            reservasAtivas = 0;
            reservasConcluidas = 0;
            reservasCanceladas = 0;

            pagamentosPendentes = 0;
            pagamentosParciais = 0;
            pagamentosPagos = 0;

            receitaTotal = 0;
            receitaRecebida = 0;
            receitaPendente = 0;

            for (Reserva r : reservas) {
                String status = normalizarStatus(r.getStatus());

                if (isAtiva(status)) {
                    reservasAtivas++;
                } else if (isConcluida(status)) {
                    reservasConcluidas++;
                } else if (isCancelada(status)) {
                    reservasCanceladas++;
                }

                double totalReserva = r.getTotal();
                double totalPago = pagamentoService.getTotalPago(r.getId());
                double restante = Math.max(0, totalReserva - totalPago);

                receitaTotal += totalReserva;
                receitaRecebida += totalPago;
                receitaPendente += restante;

                if (totalPago >= totalReserva - EPS) {
                    pagamentosPagos++;
                } else if (totalPago > EPS) {
                    pagamentosParciais++;
                } else {
                    pagamentosPendentes++;
                }
            }

            clientesAtendidos = (int) reservas.stream()
                    .filter(r -> r.getClient() != null)
                    .map(r -> r.getClient().getId())
                    .distinct()
                    .count();

            veiculosMovimentados = (int) reservas.stream()
                    .filter(r -> r.getVehicle() != null)
                    .map(r -> r.getVehicle().getId())
                    .distinct()
                    .count();

            ticketMedio = totalReservas > 0 ? receitaTotal / totalReservas : 0;

            ultimasReservas = reservas.stream()
                    .sorted(Comparator.comparing(Reserva::getId).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            reservas = new ArrayList<>();
            ultimasReservas = new ArrayList<>();
        }
    }

    private String normalizarStatus(String status) {
        if (status == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(status, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return semAcento.trim().toUpperCase();
    }

    private boolean isAtiva(String status) {
        return status.equals("ATIVA")
                || status.equals("SOLICITADA")
                || status.equals("EM ANDAMENTO")
                || status.equals("EM_ANDAMENTO");
    }

    private boolean isConcluida(String status) {
        return status.equals("CONCLUIDA");
    }

    private boolean isCancelada(String status) {
        return status.equals("CANCELADA");
    }

    public int getPercentualAtivas() {
        return totalReservas > 0 ? (reservasAtivas * 100) / totalReservas : 0;
    }

    public int getPercentualConcluidas() {
        return totalReservas > 0 ? (reservasConcluidas * 100) / totalReservas : 0;
    }

    public int getPercentualCanceladas() {
        return totalReservas > 0 ? (reservasCanceladas * 100) / totalReservas : 0;
    }

    public int getPercentualRecebido() {
        return receitaTotal > 0 ? (int) ((receitaRecebida * 100) / receitaTotal) : 0;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public List<Reserva> getUltimasReservas() {
        return ultimasReservas;
    }

    public int getTotalReservas() {
        return totalReservas;
    }

    public int getReservasAtivas() {
        return reservasAtivas;
    }

    public int getReservasConcluidas() {
        return reservasConcluidas;
    }

    public int getReservasCanceladas() {
        return reservasCanceladas;
    }

    public int getClientesAtendidos() {
        return clientesAtendidos;
    }

    public int getVeiculosMovimentados() {
        return veiculosMovimentados;
    }

    public int getPagamentosPendentes() {
        return pagamentosPendentes;
    }

    public int getPagamentosParciais() {
        return pagamentosParciais;
    }

    public int getPagamentosPagos() {
        return pagamentosPagos;
    }

    public double getReceitaTotal() {
        return receitaTotal;
    }

    public double getReceitaRecebida() {
        return receitaRecebida;
    }

    public double getReceitaPendente() {
        return receitaPendente;
    }

    public double getTicketMedio() {
        return ticketMedio;
    }
}