package com.balancodoportuga.bean;

import com.balancodoportuga.model.Cliente;
import com.balancodoportuga.service.ClienteService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient ClienteService clienteService;

    private String login;
    private String senha;
    private String acesso = "geral";
    private String tipoUsuario;
    private Cliente clienteLogado;
    private boolean logado;

    public LoginBean() {
        this.clienteService = new ClienteService();
    }

    @PostConstruct
    public void init() {
        if (this.clienteService == null) {
            this.clienteService = new ClienteService();
        }
        this.logado = false;
    }

    public String entrar() {
        try {
            if ("funcionario".equals(acesso)) {
                return entrarComoFuncionario();
            }

            if ("cliente".equals(acesso)) {
                return entrarComoCliente();
            }

            return entrarGeral();

        } catch (Exception ex) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao tentar logar: " + ex.getMessage());
            return null;
        }
    }

  private String entrarComoFuncionario() {
    if ("admin".equalsIgnoreCase(login) && "362651".equals(senha)) {
        tipoUsuario = "admin";
        clienteLogado = null;
        logado = true;

        registrarSessao("admin");
        limparCamposLogin();

        return "/funcionario-menu.xhtml?faces-redirect=true";
    }

    addMsg(FacesMessage.SEVERITY_WARN, "Usuário ou senha de funcionário incorretos.");
    return null;
}

private String entrarComoCliente() throws Exception {
    Cliente cliente = clienteService.buscarPorEmail(login);

    if (cliente != null && cpfConfere(cliente.getCpf(), senha)) {
        tipoUsuario = "cliente";
        clienteLogado = cliente;
        logado = true;

        registrarSessao("cliente");
        limparCamposLogin();

        return "/cliente-menu.xhtml?faces-redirect=true";
    }

    addMsg(FacesMessage.SEVERITY_WARN, "E-mail ou CPF incorretos.");
    return null;
}

private String entrarGeral() throws Exception {
    if ("admin".equalsIgnoreCase(login) && "362651".equals(senha)) {
        tipoUsuario = "admin";
        clienteLogado = null;
        logado = true;

        registrarSessao("admin");
        limparCamposLogin();

        return "/funcionario-menu.xhtml?faces-redirect=true";
    }

    Cliente cliente = clienteService.buscarPorEmail(login);

    if (cliente != null && cpfConfere(cliente.getCpf(), senha)) {
        tipoUsuario = "cliente";
        clienteLogado = cliente;
        logado = true;

        registrarSessao("cliente");
        limparCamposLogin();

        return "/cliente-menu.xhtml?faces-redirect=true";
    }

    addMsg(FacesMessage.SEVERITY_WARN, "Usuário ou senha incorretos.");
    return null;
}

public String sair() {
    FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

    login = null;
    senha = null;
    acesso = "geral";
    tipoUsuario = null;
    clienteLogado = null;
    logado = false;

    return "/index.xhtml?faces-redirect=true";
}

    public boolean isAdmin() {
        return "admin".equals(tipoUsuario);
    }

    public boolean isCliente() {
        return "cliente".equals(tipoUsuario);
    }

    public String getTituloAcesso() {
        if ("cliente".equals(acesso)) {
            return "Acesso do Cliente";
        }

        if ("funcionario".equals(acesso)) {
            return "Acesso do Funcionário";
        }

        return "Acesso ao Sistema";
    }

    public String getDescricaoAcesso() {
        if ("cliente".equals(acesso)) {
            return "Entre com seu e-mail e CPF para consultar reservas e pagamentos.";
        }

        if ("funcionario".equals(acesso)) {
            return "Entre com suas credenciais para acessar o painel administrativo.";
        }

        return "Entre com suas credenciais para acessar o sistema.";
    }

    public String getDicaAcesso() {
        if ("cliente".equals(acesso)) {
            
        }

        if ("funcionario".equals(acesso)) {
            
        }
        return null;

        
    }

    private boolean cpfConfere(String cpfCadastrado, String cpfDigitado) {
        if (cpfCadastrado == null || cpfDigitado == null) {
            return false;
        }

        String cpfBanco = limparCpf(cpfCadastrado);
        String cpfSenha = limparCpf(cpfDigitado);

        return cpfCadastrado.equals(cpfDigitado) || cpfBanco.equals(cpfSenha);
    }

    private String limparCpf(String cpf) {
        if (cpf == null) {
            return "";
        }

        return cpf.replaceAll("[^0-9]", "");
    }

    private void limparCamposLogin() {
        login = null;
        senha = null;
    }

    private void addMsg(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.clienteService = new ClienteService();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getAcesso() {
        return acesso;
    }

    public void setAcesso(String acesso) {
        if (acesso == null || acesso.trim().isEmpty()) {
            this.acesso = "geral";
            return;
        }

        if ("cliente".equalsIgnoreCase(acesso)) {
            this.acesso = "cliente";
        } else if ("funcionario".equalsIgnoreCase(acesso)) {
            this.acesso = "funcionario";
        } else {
            this.acesso = "geral";
        }
    }
    private void registrarSessao(String tipo) {
    FacesContext.getCurrentInstance()
            .getExternalContext()
            .getSessionMap()
            .put("usuarioLogado", true);

    FacesContext.getCurrentInstance()
            .getExternalContext()
            .getSessionMap()
            .put("tipoUsuario", tipo);
}

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public Cliente getClienteLogado() {
        return clienteLogado;
    }

    public boolean isLogado() {
        return logado;
    }
}
