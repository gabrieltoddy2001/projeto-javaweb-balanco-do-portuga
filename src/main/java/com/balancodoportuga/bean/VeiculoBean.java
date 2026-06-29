package com.balancodoportuga.bean;

import com.balancodoportuga.model.Veiculo;
import com.balancodoportuga.service.VeiculoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import com.balancodoportuga.util.UploadConfig;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("veiculoBean")
@ViewScoped
public class VeiculoBean implements Serializable {

    private VeiculoService veiculoService;

    private List<Veiculo> veiculos;
    private List<Veiculo> veiculosFiltrados;
    private Veiculo veiculoSelecionado;
    private Veiculo novoVeiculo;

    private String filtro;
    private String filtroMarca;

    private Part fotoUpload;
    private Part fotoUploadEdicao;

    public VeiculoBean() {
        this.veiculoService = new VeiculoService();
    }

    @PostConstruct
    public void init() {
        novoVeiculo = new Veiculo();
        novoVeiculo.setTipo("CARRO");
        carregarVeiculos();
    }

    public void carregarVeiculos() {
        try {
            veiculos = veiculoService.listarTodos();
            veiculosFiltrados = veiculos;
        } catch (Exception e) {
            veiculos = new ArrayList<>();
            veiculosFiltrados = new ArrayList<>();
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar veículos: " + e.getMessage());
        }
    }

    public void filtrar() {
        List<Veiculo> resultado = veiculos != null ? veiculos : new ArrayList<>();

        if (filtro != null && !filtro.isBlank()) {
            String termo = filtro.trim().toLowerCase();

            resultado = resultado.stream()
                    .filter(v -> contem(v.getModelo(), termo)
                    || contem(v.getMarca(), termo)
                    || contem(v.getPlaca(), termo)
                    || contem(v.getTipo(), termo))
                    .collect(Collectors.toList());
        }

        if (filtroMarca != null && !filtroMarca.isBlank()) {
            resultado = resultado.stream()
                    .filter(v -> v.getMarca() != null && v.getMarca().equalsIgnoreCase(filtroMarca))
                    .collect(Collectors.toList());
        }

        veiculosFiltrados = resultado;
    }

    public void limparFiltro() {
        filtro = null;
        filtroMarca = null;
        veiculosFiltrados = veiculos != null ? veiculos : new ArrayList<>();
    }

    public void prepararNovo() {
        novoVeiculo = new Veiculo();
        novoVeiculo.setTipo("CARRO");
        fotoUpload = null;
    }

    public void prepararEdicao() {
        if (veiculoSelecionado == null) {
            addFalha(FacesMessage.SEVERITY_WARN, "Selecione um veículo!");
            return;
        }

        if (veiculoSelecionado.getTipo() == null || veiculoSelecionado.getTipo().isBlank()) {
            veiculoSelecionado.setTipo("CARRO");
        }

        fotoUploadEdicao = null;
    }

    public void salvarNovo() {
        try {
            validarTipo(novoVeiculo);

            String caminhoFoto = salvarFoto(fotoUpload);

            if (caminhoFoto != null) {
                novoVeiculo.setFoto(caminhoFoto);
            }

            veiculoService.salvar(novoVeiculo);

            carregarVeiculos();

            novoVeiculo = new Veiculo();
            novoVeiculo.setTipo("CARRO");
            fotoUpload = null;

            addMsg(FacesMessage.SEVERITY_INFO, "Veículo cadastrado com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar: " + e.getMessage());
        }
    }

    public void salvarEdicao() {
        try {
            if (veiculoSelecionado == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Selecione um veículo!");
                return;
            }

            validarTipo(veiculoSelecionado);

            String caminhoFoto = salvarFoto(fotoUploadEdicao);

            if (caminhoFoto != null) {
                veiculoSelecionado.setFoto(caminhoFoto);
            }

            veiculoService.salvar(veiculoSelecionado);

            carregarVeiculos();

            fotoUploadEdicao = null;

            addMsg(FacesMessage.SEVERITY_INFO, "Veículo atualizado com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro ao atualizar: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            if (veiculoSelecionado == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Selecione um veículo!");
                return;
            }

            veiculoService.excluir(veiculoSelecionado.getId());

            carregarVeiculos();

            veiculoSelecionado = null;

            addMsg(FacesMessage.SEVERITY_INFO, "Veículo excluído com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro ao excluir: " + e.getMessage());
        }
    }

private String salvarFoto(Part arquivo) throws IOException {
    if (arquivo == null || arquivo.getSize() == 0) {
        return null;
    }

    String nomeOriginal = arquivo.getSubmittedFileName();

    if (nomeOriginal == null || nomeOriginal.isBlank()) {
        return null;
    }

    String contentType = arquivo.getContentType();

    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
        throw new IOException("O arquivo enviado precisa ser uma imagem.");
    }

    String extensao = obterExtensao(nomeOriginal);
    String nomeArquivo = UUID.randomUUID().toString() + extensao;

    Path pastaUpload = UploadConfig.pastaVeiculos();

    Files.createDirectories(pastaUpload);

    Path destino = pastaUpload.resolve(nomeArquivo);

    try (InputStream input = arquivo.getInputStream()) {
        Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    return "imagens/veiculos/" + nomeArquivo;
}

    private String obterExtensao(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf(".");

        if (ponto == -1) {
            return ".jpg";
        }

        String extensao = nomeArquivo.substring(ponto).toLowerCase();

        if (!extensao.equals(".jpg")
                && !extensao.equals(".jpeg")
                && !extensao.equals(".png")
                && !extensao.equals(".webp")) {
            return ".jpg";
        }

        return extensao;
    }

    private void validarTipo(Veiculo v) {
        if (v.getTipo() == null || v.getTipo().isBlank()) {
            v.setTipo("CARRO");
            return;
        }

        if (!"CARRO".equalsIgnoreCase(v.getTipo()) && !"MOTO".equalsIgnoreCase(v.getTipo())) {
            v.setTipo("CARRO");
        }
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase().contains(termo);
    }

    private void addMsg(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
    }

    private void addFalha(FacesMessage.Severity severity, String msg) {
        addMsg(severity, msg);
        FacesContext.getCurrentInstance().validationFailed();
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public List<Veiculo> getVeiculosFiltrados() {
        return veiculosFiltrados;
    }

    public Veiculo getVeiculoSelecionado() {
        return veiculoSelecionado;
    }

    public void setVeiculoSelecionado(Veiculo v) {
        this.veiculoSelecionado = v;
    }

    public Veiculo getNovoVeiculo() {
        return novoVeiculo;
    }

    public void setNovoVeiculo(Veiculo v) {
        this.novoVeiculo = v;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public String getFiltroMarca() {
        return filtroMarca;
    }

    public void setFiltroMarca(String filtroMarca) {
        this.filtroMarca = filtroMarca;
    }

    public Part getFotoUpload() {
        return fotoUpload;
    }

    public void setFotoUpload(Part fotoUpload) {
        this.fotoUpload = fotoUpload;
    }

    public Part getFotoUploadEdicao() {
        return fotoUploadEdicao;
    }

    public void setFotoUploadEdicao(Part fotoUploadEdicao) {
        this.fotoUploadEdicao = fotoUploadEdicao;
    }

    public List<String> getMarcas() {
        if (veiculos == null) {
            return new ArrayList<>();
        }

        return veiculos.stream()
                .map(Veiculo::getMarca)
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}