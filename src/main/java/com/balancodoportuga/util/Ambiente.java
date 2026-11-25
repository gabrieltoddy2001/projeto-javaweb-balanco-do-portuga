package com.balancodoportuga.util;

/**
 * Lê configurações de variáveis de ambiente, com valores padrão para
 * rodar localmente sem configurar nada.
 *
 * Variáveis usadas:
 *  DB_PATH        caminho do arquivo SQLite (padrão: balancodoportuga.db)
 *  UPLOAD_DIR     pasta das fotos dos veículos (padrão: ~/BalancoDoPortugaWeb/uploads/veiculos)
 *  ADMIN_USER     usuário do funcionário administrador (padrão: admin)
 *  ADMIN_PASSWORD senha do funcionário administrador
 *  DEMO_MODE      "true" para popular dados de exemplo e mostrar os acessos de teste na tela de login
 */
public final class Ambiente {

    private Ambiente() {
    }

    public static String get(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }

    public static boolean modoDemo() {
        return "true".equalsIgnoreCase(get("DEMO_MODE", "false"));
    }

    public static String adminUsuario() {
        return get("ADMIN_USER", "admin");
    }

    public static String adminSenha() {
        return get("ADMIN_PASSWORD", "362651");
    }
}
