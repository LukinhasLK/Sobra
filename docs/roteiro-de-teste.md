# Roteiro de teste no emulador

Marque cada item ao testar. Se algo falhar, anote a tela, o que você fez e o que apareceu.

## Antes de começar

- [ ] Back end rodando: `cd backend && docker compose up -d && ./mvnw spring-boot:run`
- [ ] Teste rápido da API no navegador do computador: <http://localhost:8084/api/v1/saude> deve mostrar `{"status":"ok"}`
- [ ] App aberto no Android Studio, Gradle sincronizado sem erro, rodando no **emulador**

> Se o app abrir mas toda tela mostrar "Algo deu errado" ou "Não foi possível conectar", a API não está
> acessível. Confira se o back end está no ar e a porta (8084).

## 1. Conta e configuração inicial

- [ ] Splash e onboarding aparecem como no banner
- [ ] **Cadastro** com senhas diferentes mostra "A confirmação não confere com a senha." (ou "As senhas não são iguais.") em vermelho
- [ ] **Cadastro** com e-mail e senha válidos (senha de 8 a 64 caracteres, não óbvia) segue para a renda
- [ ] **Renda** em zero mostra "Informe sua renda mensal."
- [ ] **Renda** 5.200,00 → Continuar vai para as categorias
- [ ] **Categorias**: tocar em um chip abre o diálogo; informar 900 e salvar mostra `R$ 900` no chip; "Sem valor" volta a "Adicionar"
- [ ] **Concluir** leva à Home

## 2. Home e gastos

- [ ] Home de conta nova: renda 5.200, gastos 0, disponível 5.200, sem categorias
- [ ] **Gastos** → botão + → **Nova despesa**
- [ ] Tocar em **Categoria** abre a lista com 7 opções; escolher uma atualiza o campo
- [ ] Tocar em **Data** abre o calendário; datas futuras não podem ser escolhidas
- [ ] Salvar sem valor mostra "Informe o valor da despesa."; sem descrição mostra "Informe uma descrição."
- [ ] Salvar com valor 120,50 mostra o modal "Gasto adicionado!" sobre a Home
- [ ] A **Home** já mostra gastos 120,50 e a categoria escolhida (sem precisar reabrir o app)
- [ ] Em **Gastos**, a despesa aparece na lista e o total do mês é 120,50
- [ ] Seletor de mês: escolher um mês anterior mostra a lista vazia; voltar ao atual mostra a despesa
- [ ] Gastar mais do que a renda mostra o disponível como `-R$ ...` (com o sinal antes do R$)

## 3. Objetivos

- [ ] **Objetivos** de conta nova mostra o estado vazio (tela 20) com o botão "Criar objetivo"
- [ ] **Novo objetivo**: nome vazio mostra "Dê um nome ao objetivo."; valor zero mostra "Informe o valor desejado."
- [ ] Criar "Viagem Europa", 12.000, prazo 24 meses (o campo Prazo abre a lista)
- [ ] O aporte sugerido muda ao trocar o prazo (12.000 ÷ 24 = R$ 500,00 / mês)
- [ ] O objetivo aparece na lista com 0%
- [ ] **Detalhe** → "Adicionar aporte" → valor 1.200, tipo Extra → salvar
- [ ] Anel de progresso passa a 10%, "R$ 1.200 de R$ 12.000" e o histórico mostra "Aporte Extra"
- [ ] Voltar à lista mostra 10%

## 4. Simular

- [ ] Valores sugeridos (1.000 inicial, 500 por mês, 24 meses) → **Simular** mostra estimativa final **R$ 14.680,67**
- [ ] Rendimento estimado **+ R$ 1.680,67**; total investido R$ 13.000,00
- [ ] **Girar o aparelho** (Ctrl+seta no emulador) na tela de resultado: o resultado continua o mesmo
- [ ] **Salvar** abre "Simulações salvas" com a simulação na lista
- [ ] Arrastar o controle de prazo muda os meses

## 5. Perfil e sessão

- [ ] **Perfil** mostra o nome, o e-mail e as iniciais da conta criada (não "Lucas Costa")
- [ ] Ligar/desligar **Notificações** muda na hora e continua assim ao reabrir a tela
- [ ] **Sair da conta** leva ao login; o botão voltar do sistema não volta ao app
- [ ] **Login** com senha errada mostra "E-mail ou senha incorretos."
- [ ] **Login** com a conta criada: conta com configuração completa vai direto à Home
- [ ] **Recuperar senha**: informar o e-mail mostra a mensagem em Teal (o e-mail não é enviado de verdade)
- [ ] **Fechar e reabrir o app** logado: vai direto à Home, sem onboarding nem login

## 6. Falhas (importante para a apresentação)

- [ ] Parar o back end e abrir a Home: aparece a tela de erro com "Tentar novamente"; religar o back end e tocar no botão carrega os dados
- [ ] Sessão expirada: no banco, `UPDATE sessao SET expira_em = now() - interval '1 day';` e navegar no app abre a tela "Sessão expirada"

## 7. Aparência

- [ ] Nenhum texto entre colchetes (`[...]`) aparece em nenhuma das 24 telas
- [ ] O visual (cores, fontes, espaçamentos) continua igual ao banner mostrado ao professor
