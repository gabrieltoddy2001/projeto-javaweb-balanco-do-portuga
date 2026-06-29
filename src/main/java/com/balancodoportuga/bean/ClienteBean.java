package com.balancodoportuga.bean;

import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.service.ClienteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@jakarta.faces.view.ViewScoped
@jakarta.inject.Named("clienteBean")
public class ClienteBean implements Serializable {

    private ClienteService clienteService;
    private List<Cliente> clientes;
    private List<Cliente> clientesFiltrados;
    private Cliente clienteSelecionado;
    private Cliente novoCliente;
    private String filtro;

    public ClienteBean() {
        this.clienteService = new ClienteService();
    }

    @PostConstruct
    public void init() {
        novoCliente = new Cliente();
        carregarClientes();
    }

    public void carregarClientes() {
        try {
            clientes = clienteService.listarTodos();
            clientesFiltrados = clientes;
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar clientes: " + e.getMessage());
        }
    }

    public void filtrar() {
        clientesFiltrados = clienteService.filtrar(clientes, filtro);
    }

    public void prepararNovo() {
        novoCliente = new Cliente();
    }

    public void prepararEdicao() {
        if (clienteSelecionado == null) {
            addMsg(FacesMessage.SEVERITY_WARN, "Selecione um cliente!");
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void limparFiltro() {
        filtro = null;
        clientesFiltrados = clientes;
    }

    public void salvarNovo() {
        try {
            clienteService.salvar(novoCliente);
            carregarClientes();
            novoCliente = new Cliente();
            addMsg(FacesMessage.SEVERITY_INFO, "Cliente cadastrado com sucesso!");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar: " + e.getMessage());
        }
    }

    public void salvarEdicao() {
        try {
            clienteService.salvar(clienteSelecionado);
            carregarClientes();
            addMsg(FacesMessage.SEVERITY_INFO, "Cliente atualizado com sucesso!");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao atualizar: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            if (clienteSelecionado == null) {
                addMsg(FacesMessage.SEVERITY_WARN, "Selecione um cliente!");
                return;
            }
            clienteService.excluir(clienteSelecionado.getId());
            carregarClientes();
            clienteSelecionado = null;
            addMsg(FacesMessage.SEVERITY_INFO, "Cliente excluído com sucesso!");
        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

    private void addMsg(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
    }

    // Getters / Setters
    public List<Cliente> getClientes() { return clientesFiltrados; }
    public List<Cliente> getClientesFiltrados() { return clientesFiltrados; }
    public Cliente getClienteSelecionado() { return clienteSelecionado; }
    public void setClienteSelecionado(Cliente c) { this.clienteSelecionado = c; }
    public Cliente getNovoCliente() { return novoCliente; }
    public void setNovoCliente(Cliente c) { this.novoCliente = c; }
    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
}
