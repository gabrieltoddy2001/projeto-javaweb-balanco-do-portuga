package com.balancodoportuga.bean;

import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.model.Veiculo;
import com.balancodoportuga.service.ReservaService;
import com.balancodoportuga.service.VeiculoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@jakarta.faces.view.ViewScoped
@jakarta.inject.Named("reservaBean")
public class ReservaBean implements Serializable {

    private ReservaService reservaService;
    private VeiculoService veiculoService;

    public ReservaBean() {
        this.reservaService = new ReservaService();
        this.veiculoService = new VeiculoService();
    }

    private List<Reserva> reservas;
    private List<Reserva> reservasFiltradas;
    private List<Veiculo> veiculos;
    private Reserva reservaSelecionada;

    private String filtroStatus = "";
    private String filtroCliente;
    private LocalDate filtroDataInicio;
    private LocalDate filtroDataFim;

    private int veiculoSelecionadoId;
    private Veiculo veiculoPreview;
    private LocalDate novaDataInicio;
    private LocalDate novaDataFim;
    private String estimativa;

    private double totalFaturado;
    private String resumoStatus;

    private List<Reserva> reservasCliente;

    @PostConstruct
    public void init() {
        try {
            veiculos = veiculoService.listarTodos();
        } catch (Exception e) {
            veiculos = new ArrayList<>();
        }

        carregarReservas();
    }

    private LoginBean getLoginBean() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        return (LoginBean) ctx.getApplication()
                .evaluateExpressionGet(ctx, "#{loginBean}", LoginBean.class);
    }

    public void carregarReservas() {
        try {
            reservas = reservaService.listarTodas();
            reservasFiltradas = new ArrayList<>(reservas);

            LoginBean lb = getLoginBean();

            if (lb != null && lb.getClienteLogado() != null) {
                reservasCliente = reservaService.listarPorCliente(lb.getClienteLogado().getId());
            } else {
                reservasCliente = new ArrayList<>();
            }

            atualizarResumo();

        } catch (Exception e) {
            reservas = new ArrayList<>();
            reservasFiltradas = new ArrayList<>();
            reservasCliente = new ArrayList<>();
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar reservas: " + e.getMessage());
        }
    }

    public void filtrar() {
        if (reservas == null) {
            return;
        }

        reservasFiltradas = reservaService.filtrarReservas(
                reservas,
                filtroCliente,
                (filtroStatus == null || filtroStatus.isEmpty()) ? "Todos" : filtroStatus,
                filtroDataInicio,
                filtroDataFim
        );

        atualizarResumo();
    }

    public void limparFiltro() {
        filtroStatus = "";
        filtroCliente = null;
        filtroDataInicio = null;
        filtroDataFim = null;
        reservasFiltradas = reservas != null ? new ArrayList<>(reservas) : new ArrayList<>();
        atualizarResumo();
    }

    private void atualizarResumo() {
        if (reservasFiltradas == null) {
            return;
        }

        totalFaturado = reservaService.calcularTotalReservas(reservasFiltradas);

        long sol = reservasFiltradas.stream()
                .filter(r -> "SOLICITADA".equalsIgnoreCase(r.getStatus()))
                .count();

        long and = reservasFiltradas.stream()
                .filter(r -> "EM_ANDAMENTO".equalsIgnoreCase(r.getStatus()))
                .count();

        long con = reservasFiltradas.stream()
                .filter(r -> "CONCLUIDA".equalsIgnoreCase(r.getStatus()))
                .count();

        long can = reservasFiltradas.stream()
                .filter(r -> "CANCELADA".equalsIgnoreCase(r.getStatus()))
                .count();

        resumoStatus = String.format(
                "Solicitadas: %d | Em andamento: %d | Concluídas: %d | Canceladas: %d",
                sol,
                and,
                con,
                can
        );
    }

    public void concluirReserva() {
        try {
            if (reservaSelecionada == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
                return;
            }

            if (!"EM_ANDAMENTO".equalsIgnoreCase(reservaSelecionada.getStatus())) {
                addMsg(FacesMessage.SEVERITY_WARN, "Somente reservas em andamento podem ser concluídas.");
                return;
            }

            double novoTotal = reservaService.calcularTotalComMulta(reservaSelecionada);

            reservaService.concluirReserva(reservaSelecionada.getId(), novoTotal);
            carregarReservas();

            addMsg(FacesMessage.SEVERITY_INFO, String.format("Reserva concluída! Total final: R$ %.2f", novoTotal));

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    public void cancelarReserva() {
        try {
            if (reservaSelecionada == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
                return;
            }

            String status = reservaSelecionada.getStatus();

            if (!"EM_ANDAMENTO".equalsIgnoreCase(status) && !"SOLICITADA".equalsIgnoreCase(status)) {
                addMsg(FacesMessage.SEVERITY_WARN, "Somente reservas 'Solicitadas' ou 'Em andamento' podem ser canceladas!");
                return;
            }

            LocalDate hoje = LocalDate.now();
            long diasUsados = ChronoUnit.DAYS.between(reservaSelecionada.getDataInicio(), hoje);

            if (diasUsados < 1) {
                diasUsados = 1;
            }

            double totalProporcional = diasUsados * reservaSelecionada.getVehicle().getDiaria();

            reservaService.cancelarReservaComValor(reservaSelecionada.getId(), totalProporcional);
            carregarReservas();

            addMsg(
                    FacesMessage.SEVERITY_INFO,
                    String.format("Reserva cancelada! Dias utilizados: %d | Total proporcional: R$ %.2f", diasUsados, totalProporcional)
            );

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao cancelar: " + e.getMessage());
        }
    }

    public void atualizarVeiculoPreview() {
        try {
            if (veiculoSelecionadoId == 0) {
                veiculoPreview = null;
                estimativa = null;
                return;
            }

            veiculoPreview = veiculoService.buscarPorId(veiculoSelecionadoId);
            atualizarEstimativa();

        } catch (Exception e) {
            veiculoPreview = null;
            estimativa = null;
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar veículo selecionado: " + e.getMessage());
        }
    }

    public void verificarDisponibilidade() {
        try {
            LocalDate di = novaDataInicio;
            LocalDate df = novaDataFim;

            if (veiculoSelecionadoId == 0 || di == null || df == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Preencha todos os campos.");
                return;
            }

            boolean conflito = reservaService.verificarDisponibilidade(veiculoSelecionadoId, di, df);

            addMsg(
                    conflito ? FacesMessage.SEVERITY_WARN : FacesMessage.SEVERITY_INFO,
                    conflito ? "Veículo indisponível no período." : "Veículo disponível!"
            );

            atualizarEstimativa();

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    public void criarReservaCliente() {
        try {
            LoginBean lb = getLoginBean();

            if (lb == null || lb.getClienteLogado() == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Sessão expirada. Faça login novamente.");
                return;
            }

            LocalDate di = novaDataInicio;
            LocalDate df = novaDataFim;

            if (veiculoSelecionadoId == 0 || di == null || df == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Preencha todos os campos antes de confirmar.");
                return;
            }

            int clienteId = lb.getClienteLogado().getId();

            Veiculo v = veiculoService.buscarPorId(veiculoSelecionadoId);

            reservaService.criarReserva(clienteId, veiculoSelecionadoId, di, df);

            DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM/yyyy (EEE)", new Locale("pt", "BR"));

            String msg = String.format(
                    "Reserva Realizada! Veículo: %s - %s (%s). Período: %s à %s",
                    v != null ? v.getModelo() : "",
                    v != null ? v.getMarca() : "",
                    v != null ? v.getPlaca() : "",
                    di.format(fmtDia),
                    df.format(fmtDia)
            );

            veiculoSelecionadoId = 0;
            veiculoPreview = null;
            novaDataInicio = null;
            novaDataFim = null;
            estimativa = null;

            addMsg(FacesMessage.SEVERITY_INFO, msg);

            try {
                reservasCliente = reservaService.listarPorCliente(clienteId);
            } catch (Exception ex) {
            }

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao criar reserva: " + e.getMessage());
        }
    }

    public void atualizarEstimativa() {
        LocalDate di = novaDataInicio;
        LocalDate df = novaDataFim;

        if (veiculoSelecionadoId == 0 || di == null || df == null || df.isBefore(di)) {
            estimativa = null;
            return;
        }

        try {
            Veiculo v = veiculoService.buscarPorId(veiculoSelecionadoId);

            if (v == null) {
                estimativa = null;
                return;
            }

            long dias = ChronoUnit.DAYS.between(di, df);

            if (dias <= 0) {
                estimativa = null;
                return;
            }

            double total = dias * v.getDiaria();

            estimativa = String.format("%d dia(s) - Total: R$ %.2f", dias, total);

        } catch (Exception e) {
            estimativa = null;
        }
    }

    private void addMsg(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
    }

    public List<Reserva> getReservas() {
        return reservasFiltradas;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public List<Veiculo> getVeiculosDisponiveis() {
        return veiculos;
    }

    public List<Reserva> getReservasCliente() {
        return reservasCliente;
    }

    public Reserva getReservaSelecionada() {
        return reservaSelecionada;
    }

    public void setReservaSelecionada(Reserva r) {
        this.reservaSelecionada = r;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String s) {
        this.filtroStatus = s;
    }

    public String getFiltroCliente() {
        return filtroCliente;
    }

    public void setFiltroCliente(String s) {
        this.filtroCliente = s;
    }

    public LocalDate getFiltroDataInicio() {
        return filtroDataInicio;
    }

    public void setFiltroDataInicio(LocalDate filtroDataInicio) {
        this.filtroDataInicio = filtroDataInicio;
    }

    public LocalDate getFiltroDataFim() {
        return filtroDataFim;
    }

    public void setFiltroDataFim(LocalDate filtroDataFim) {
        this.filtroDataFim = filtroDataFim;
    }

    public int getVeiculoSelecionadoId() {
        return veiculoSelecionadoId;
    }

    public void setVeiculoSelecionadoId(int id) {
        this.veiculoSelecionadoId = id;
    }

    public Veiculo getVeiculoPreview() {
        return veiculoPreview;
    }

    public void setVeiculoPreview(Veiculo veiculoPreview) {
        this.veiculoPreview = veiculoPreview;
    }

    public LocalDate getNovaDataInicio() {
        return novaDataInicio;
    }

    public void setNovaDataInicio(LocalDate novaDataInicio) {
        this.novaDataInicio = novaDataInicio;
    }

    public LocalDate getNovaDataFim() {
        return novaDataFim;
    }

    public void setNovaDataFim(LocalDate novaDataFim) {
        this.novaDataFim = novaDataFim;
    }

    public String getEstimativa() {
        return estimativa;
    }

    public double getTotalFaturado() {
        return totalFaturado;
    }

    public String getResumoStatus() {
        return resumoStatus;
    }
}