package com.balancodoportuga.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Popula o banco com dados fictícios para a versão de demonstração online.
 * Só roda quando DEMO_MODE=true e o banco ainda não tem clientes.
 */
public final class DadosDemo {

    public static final String CLIENTE_EMAIL = "ana.souza@exemplo.com";
    public static final String CLIENTE_CPF = "123.456.789-09";

    private DadosDemo() {
    }

    static void popularSeVazio(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM clients")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("INSERT INTO clients(nome, cpf, cnh, email, telefone, endereco, numero, bairro, cidade, estado) VALUES "
                    + "('Ana Souza', '" + CLIENTE_CPF + "', '01234567890', '" + CLIENTE_EMAIL + "', '(71) 90000-0001', 'Rua das Flores', '100', 'Barra', 'Salvador', 'BA'),"
                    + "('Bruno Lima', '987.654.321-00', '09876543210', 'bruno.lima@exemplo.com', '(71) 90000-0002', 'Avenida Sete de Setembro', '250', 'Centro', 'Salvador', 'BA')");

            st.executeUpdate("INSERT INTO veiculos(modelo, marca, placa, ano, diaria, tipo, foto) VALUES "
                    + "('Onix LTZ', 'Chevrolet', 'BRA2E19', 2022, 150.0, 'CARRO', 'imagens/veiculos/68025082-ca23-4d12-9337-d5d524a3b807.jpg'),"
                    + "('Corolla XEi', 'Toyota', 'PTG4A21', 2023, 280.0, 'CARRO', 'imagens/veiculos/96a4cc8a-bb20-4a6d-aa3f-9ef35a265861.jpg'),"
                    + "('Fazer 250', 'Yamaha', 'MOT1B23', 2023, 110.0, 'MOTO', 'imagens/veiculos/9cf6bc12-789a-4ea7-8507-2e90b6a694e1.jpg'),"
                    + "('CG 160 Start', 'Honda', 'CGX5C67', 2022, 80.0, 'MOTO', 'imagens/veiculos/f7422b4b-bd2d-40af-8728-219d7f255767.jpg')");

            st.executeUpdate("INSERT INTO reservas(cliente_id, veiculo_id, data_inicio, data_fim, total, status, status_pagamento) VALUES "
                    + "(1, 1, date('now', '+3 days'), date('now', '+6 days'), 450.0, 'SOLICITADA', 'Não Pago'),"
                    + "(1, 4, date('now', '-20 days'), date('now', '-15 days'), 400.0, 'CONCLUIDA', 'Pago'),"
                    + "(2, 2, date('now', '-1 days'), date('now', '+4 days'), 1400.0, 'EM_ANDAMENTO', 'Parcial')");

            st.executeUpdate("INSERT INTO pagamentos(reserva_id, valor, data_pagamento, metodo_pagamento, tipo) VALUES "
                    + "(2, 400.0, date('now', '-20 days'), 'PIX', 'Pagamento'),"
                    + "(3, 700.0, date('now', '-1 days'), 'Cartão de Crédito', 'Pagamento')");

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }
}
