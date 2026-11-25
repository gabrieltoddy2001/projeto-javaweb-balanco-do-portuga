# Balanço do Portuga Web

## Sobre o Projeto

O **Balanço do Portuga Web** é um sistema web desenvolvido como projeto acadêmico com o objetivo de simular a gestão de uma franquia de aluguel de veículos.

A aplicação permite o controle de clientes, veículos, reservas e pagamentos, oferecendo dois tipos de acesso: **área do funcionário** e **área do cliente**. O sistema busca representar um fluxo real de locadora, desde o cadastro dos dados até o acompanhamento financeiro das reservas.

O projeto também conta com upload de fotos dos veículos, controle de pagamentos parciais, aplicação de multas, histórico financeiro e layout responsivo para melhor adaptação em diferentes tamanhos de tela.

## Demonstração Online

A aplicação está publicada para testes em: **https://balanco-do-portuga.onrender.com** _(link definitivo após o primeiro deploy no Render)_

- Os acessos de teste (funcionário e cliente) aparecem na própria tela de login.
- Os dados são fictícios e voltam ao estado inicial sempre que o servidor reinicia.
- No plano gratuito o servidor "dorme" após alguns minutos sem uso: o primeiro acesso pode levar cerca de 1 minuto.

> Existe também a versão desktop (Java Swing) do mesmo sistema: [projeto-java-balanco-do-portuga](https://github.com/gabrieltoddy2001/projeto-java-balanco-do-portuga).

---

## Objetivo

O objetivo principal do projeto é desenvolver uma aplicação web funcional para gerenciamento de aluguel de veículos, aplicando conceitos de programação web, banco de dados, regras de negócio, organização visual, responsividade e controle de acesso.

A proposta também busca demonstrar a integração entre interface, lógica de sistema e persistência de dados, utilizando tecnologias compatíveis com o ambiente Java Web.

---

## Tecnologias Utilizadas

- Java
- Jakarta Faces / JSF
- PrimeFaces
- HTML5
- CSS3
- JavaScript
- SQLite
- Maven
- GlassFish Server
- NetBeans IDE

---

## Funcionalidades

## Landing Page

A landing page apresenta a franquia, informações sobre o sistema, áreas de acesso, integrantes do projeto e canais de contato.

Principais recursos:

- Apresentação da franquia
- Explicação do sistema
- Área de acesso para clientes
- Área de acesso para funcionários
- Seção de integrantes
- Seção de contato e redes sociais
- Layout responsivo

---

## Área do Funcionário

A área do funcionário permite gerenciar as principais operações internas da locadora.

Funcionalidades disponíveis:

- Dashboard do funcionário
- Cadastro, edição, listagem e exclusão de clientes
- Cadastro, edição, listagem e exclusão de veículos
- Upload e exibição de fotos dos veículos
- Gerenciamento de reservas
- Filtros por cliente, status e período
- Controle de pagamentos
- Registro de pagamentos parciais ou totais
- Aplicação de multas
- Visualização de detalhes financeiros da reserva
- Histórico de pagamentos e multas
- Controle de acesso restrito

---

## Área do Cliente

A área do cliente permite que o usuário acompanhe suas próprias informações dentro do sistema.

Funcionalidades disponíveis:

- Página inicial do cliente
- Consulta de reservas
- Criação de nova reserva
- Visualização da foto do veículo selecionado
- Estimativa de valor da reserva
- Seleção de datas por calendário
- Histórico de reservas
- Consulta de pagamentos
- Registro de pagamentos
- Acompanhamento do progresso financeiro da reserva
- Histórico financeiro com pagamentos e multas
- Contato com suporte

---

## Controle de Acesso

O sistema possui controle de acesso para impedir que páginas internas sejam acessadas diretamente sem login.

Regras aplicadas:

- Usuários sem login não acessam páginas internas.
- Funcionários acessam apenas a área administrativa.
- Clientes acessam apenas a área do cliente.
- A landing page e a tela de login são públicas.
- O filtro de autenticação redireciona o usuário para a tela correta conforme o tipo de acesso.

---

## Banco de Dados

O projeto utiliza banco de dados **SQLite** para armazenar as informações do sistema.

O banco contém dados relacionados a:

- Clientes
- Veículos
- Reservas
- Pagamentos

O sistema também possui uma estrutura de atualização automática para adicionar colunas novas quando necessário, como campos de foto de veículos e tipo de movimentação financeira.

---

## Upload de Imagens dos Veículos

O sistema permite cadastrar uma foto para cada veículo.

As imagens são salvas fora da pasta `target`, evitando que sejam apagadas após o **Clean and Build**.

Por padrão, as imagens são armazenadas em:

```text
C:\Users\<usuario>\BalancoDoPortugaWeb\uploads\veiculos
```

No código, esse caminho é gerado automaticamente a partir de:

```java
System.getProperty("user.home")
```

Isso evita que o sistema dependa de um caminho fixo do computador do desenvolvedor.

Se a variável de ambiente `UPLOAD_DIR` estiver definida (como na versão online), as imagens são salvas nessa pasta.

### Observação importante

Caso o projeto seja enviado para outro computador com imagens já cadastradas, é necessário copiar também a pasta de uploads para o mesmo caminho do novo usuário:

```text
C:\Users\<usuario>\BalancoDoPortugaWeb\uploads\veiculos
```

Se a pasta não existir, o sistema cria automaticamente ao cadastrar uma nova imagem.

---

## Status Padronizados

Para evitar erros de filtro, exibição e regras de negócio, os status das reservas foram padronizados em letras maiúsculas, sem acentos e sem espaços.

### Status de Reserva

```text
SOLICITADA
EM_ANDAMENTO
CONCLUIDA
CANCELADA
```

Na interface, esses status são exibidos de forma amigável:

```text
Solicitada
Em andamento
Concluída
Cancelada
```

### Status de Pagamento

```text
Pendente
Parcial
Pago
```

### Tipos de Movimentação Financeira

```text
Pagamento
Multa
```

As multas aparecem no histórico financeiro separadas dos pagamentos, permitindo melhor visualização das movimentações de cada reserva.

---

## Regras de Negócio

O sistema possui regras básicas para garantir o funcionamento correto da locadora.

Exemplos:

- Não é permitido criar reserva com data inicial anterior ao dia atual.
- Não é permitido criar reserva com data final anterior à data inicial.
- A reserva deve ter pelo menos 1 dia de duração.
- Não é permitido reservar um veículo já reservado no mesmo período.
- Não é permitido registrar pagamento com valor menor ou igual a zero.
- Não é permitido registrar pagamento maior que o valor restante da reserva.
- Clientes só visualizam suas próprias reservas e pagamentos.
- Funcionários podem gerenciar clientes, veículos, reservas e pagamentos.
- Reservas podem receber pagamentos parciais ou totais.
- Multas são registradas como movimentações financeiras.
- O progresso financeiro considera os pagamentos realizados em relação ao valor total da reserva.
- O status da reserva é atualizado de acordo com o fluxo do sistema.
- O status de pagamento é atualizado conforme os valores pagos.

---

## Responsividade

O sistema possui ajustes responsivos para melhorar a utilização em telas menores.

Foram aplicadas melhorias em:

- Menus laterais
- Dashboards
- Tabelas
- Cards
- Formulários
- Modais
- Tela de reservas do cliente
- Tela de pagamentos do cliente
- Telas de clientes e veículos

Em telas menores, tabelas com muitas colunas utilizam rolagem horizontal interna para evitar que toda a página quebre.

---

## Como Executar o Projeto

### Pré-requisitos

Antes de executar o projeto, é necessário ter instalado:

- JDK 21
- NetBeans IDE
- GlassFish Server
- Maven

---

### Passos para execução

1. Abrir o NetBeans.
2. Importar o projeto Maven.
3. Verificar se o GlassFish está configurado corretamente.
4. Executar o comando **Clean and Build**.
5. Rodar o projeto no servidor.
6. Acessar a aplicação pelo navegador.

Exemplo de acesso:

```text
http://localhost:8080/BalancoDoPortugaWeb/
```

O banco `balancodoportuga.db` é criado automaticamente na primeira execução (ele não fica no repositório).

---

### Executar com Docker

Também é possível rodar exatamente como na versão online, sem instalar GlassFish ou Maven:

```bash
docker build -t balanco-do-portuga .
docker run -p 8080:8080 balanco-do-portuga
```

Acesse `http://localhost:8080/`. O container usa o **Payara Micro** (servidor Jakarta EE 10 derivado do GlassFish) e já vem com dados de demonstração.

---

### Variáveis de Ambiente

Todas são opcionais; sem elas o sistema funciona como no NetBeans.

| Variável | Para que serve | Padrão |
|----------|----------------|--------|
| `DB_PATH` | Caminho do arquivo SQLite | `balancodoportuga.db` |
| `UPLOAD_DIR` | Pasta das fotos dos veículos | `~/BalancoDoPortugaWeb/uploads/veiculos` |
| `ADMIN_USER` | Usuário do funcionário | `admin` |
| `ADMIN_PASSWORD` | Senha do funcionário | `362651` |
| `DEMO_MODE` | `true` popula dados fictícios e mostra os acessos de teste na tela de login | `false` |

---

### Publicar no Render

O repositório já contém o `Dockerfile` e o `render.yaml`:

1. Em [render.com](https://render.com), clique em **New → Blueprint**.
2. Selecione este repositório e confirme.
3. Aguarde o build (alguns minutos) e acesse a URL gerada.

---

## Login de Teste

### Funcionário

```text
Usuário: admin
Senha: 362651
```

Na versão online, o usuário e a senha são os definidos no `render.yaml` e aparecem na tela de login.

### Cliente

O cliente acessa utilizando:

```text
Login: e-mail cadastrado
Senha: CPF cadastrado
```

Exemplo (dados fictícios da demonstração):

```text
E-mail: ana.souza@exemplo.com
CPF: 123.456.789-09
```

---

## Estrutura Geral do Projeto

```text
BalancoDoPortugaWeb/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/balancodoportuga/
│       │       ├── bean/
│       │       ├── dao/
│       │       ├── filter/
│       │       ├── model/
│       │       ├── servlet/
│       │       ├── service/
│       │       └── util/
│       └── webapp/
│           ├── resources/
│           │   └── css/
│           ├── WEB-INF/
│           │   └── includes/
│           │       ├── cliente/
│           │       └── funcionario/
│           ├── index.xhtml
│           ├── login.xhtml
│           ├── cliente-menu.xhtml
│           └── funcionario-menu.xhtml
├── pom.xml
├── demo/veiculos/          (fotos usadas nos dados de demonstração)
├── Dockerfile
├── render.yaml
├── .gitignore
└── README.md
```

---

## Organização das Telas

O projeto utiliza uma estrutura com menus principais e includes internos.

### Área do Funcionário

```text
funcionario-menu.xhtml
WEB-INF/includes/funcionario/dashboard.xhtml
WEB-INF/includes/funcionario/clientes.xhtml
WEB-INF/includes/funcionario/veiculos.xhtml
WEB-INF/includes/funcionario/reservas.xhtml
WEB-INF/includes/funcionario/pagamentos.xhtml
```

### Área do Cliente

```text
cliente-menu.xhtml
WEB-INF/includes/cliente/inicio.xhtml
WEB-INF/includes/cliente/reservas.xhtml
WEB-INF/includes/cliente/pagamentos.xhtml
```

A navegação interna é controlada pelo `PainelBean`, evitando a abertura de várias páginas separadas e mantendo a experiência mais próxima de um painel administrativo.

---

## Integrantes do Projeto

### Gabriel Silva

**Função:** Interface e Organização Visual

Responsável pela estrutura visual das páginas, ajustes de layout, responsividade, organização da landing page, telas internas e apoio na experiência do usuário.

### Gabryel Rosa

**Função:** Banco de Dados

Responsável pela modelagem do banco, criação das tabelas, organização dos relacionamentos e apoio na persistência dos dados.

### Antonino

**Função:** Regras de Negócio

Responsável pelas funcionalidades relacionadas às reservas, controle dos veículos, pagamentos e comportamento principal do sistema.

### Vitor

**Função:** Testes e Documentação

Responsável pelos testes das funcionalidades, revisão dos fluxos, identificação de melhorias e apoio na documentação e apresentação acadêmica.

---

## Observações para Entrega

Antes de compactar o projeto para entrega, recomenda-se:

1. Fazer **Clean and Build**.
2. Testar o login de funcionário.
3. Testar o login de cliente.
4. Criar um cliente.
5. Criar um veículo com foto.
6. Criar uma reserva pelo cliente.
7. Registrar pagamento pelo funcionário.
8. Aplicar multa.
9. Conferir o histórico financeiro.
10. Testar a tela em tamanho reduzido.
11. Remover a pasta `target` antes de compactar o projeto-fonte.

A pasta `target` é gerada automaticamente pelo Maven e não precisa ser enviada como parte do código-fonte.

---

## Considerações Finais

O projeto **Balanço do Portuga Web** foi desenvolvido com foco acadêmico, buscando aplicar conceitos de desenvolvimento web com Java, organização de interface, controle de acesso, banco de dados, responsividade e regras de negócio.

A aplicação simula um sistema de aluguel de veículos, permitindo que funcionários gerenciem a operação da locadora e que clientes acompanhem suas reservas, pagamentos e movimentações financeiras de forma simples e organizada.
