Preciso que você escreva um documento em PDF: uma especificação de trabalho de back end, no
formato que uma empresa entregaria a um desenvolvedor como demanda formal. Quem vai executar
o trabalho sou eu, um desenvolvedor Java de nível intermediário, e vou implementar tudo à mão.
O documento precisa me dizer o que construir, em que ordem e como saber que cada parte está
pronta, sem que eu precise adivinhar nada. A parte de banco de dados é a mais importante: quero
ela completa, com todos os relacionamentos explicados.

Gere o arquivo PDF. Se você não conseguir gerar arquivos, entregue o documento completo e
formatado, pronto para eu exportar como PDF.

## Contexto do produto

O Sobra é um app Android de finanças pessoais. A ideia central é mostrar quanto "sobra" da
renda do mês depois dos gastos e ajudar a pessoa a guardar esse valor em objetivos. O app
está em português do Brasil e trabalha só com reais.

O front end (Kotlin + Jetpack Compose) já está pronto, com as 24 telas navegáveis, mas roda
com dados fictícios fixos no código. Não existe nenhum back end ainda. O trabalho descrito no
documento é construir esse back end, o banco de dados e o contrato que o app vai consumir.

Stack do back end: [PREENCHER: linguagem e framework. Se estiver em branco, escreva sem
amarrar a uma tecnologia e assuma uma API REST com JSON.]

Banco de dados: [PREENCHER: por exemplo PostgreSQL ou MySQL. Se estiver em branco, assuma um
banco relacional, escreva o SQL no padrão ANSI e avise onde os bancos mais comuns diferem.]

## O que o app precisa do back end

Estas são as telas que dependem de dados e o que cada uma consome ou envia. Use isto como a
fonte dos requisitos funcionais.

Autenticação
- Cadastro com nome completo, e-mail, senha e confirmação de senha.
- Login com e-mail e senha.
- Recuperação de senha: a pessoa informa o e-mail e recebe um link de instrução.
- Logout ("Sair da conta").
- Sessão expirada: quando a sessão vence, o app mostra uma tela própria e manda a pessoa para
  o login. O back end precisa sinalizar esse caso de forma distinta de um erro comum.

Configuração inicial, logo após o cadastro
- Passo 1: renda mensal da pessoa.
- Passo 2: categorias de gasto com um valor estimado por mês em cada uma. Uma categoria pode
  ficar sem valor (inativa).
- As categorias são fixas, sete ao todo: Moradia, Alimentação, Transporte, Saúde, Educação,
  Lazer e Outros.

Início (Home)
- Primeiro nome da pessoa e foto de avatar.
- Renda do mês, total de gastos e valor disponível.
- Lista de quanto foi gasto em cada categoria no mês.

Gastos
- Lista de despesas de um mês escolhido e o total desse mês.
- Cada despesa tem: identificador, descrição, categoria, data e valor.
- Criação de despesa com valor, descrição, categoria e data.

Objetivos
- Lista de objetivos, cada um com: identificador, nome, valor já acumulado, valor da meta e
  uma imagem de miniatura. A lista pode vir vazia (o app tem uma tela para isso).
- Criação de objetivo com nome, valor desejado e prazo em meses.
- Detalhe de um objetivo: os dados acima, o aporte mensal, a previsão de conclusão (mês e
  ano) e o histórico de aportes. Cada aporte tem identificador, título (por exemplo "Aporte
  Mensal" ou "Aporte Extra"), data e valor.
- Registro de um novo aporte em um objetivo.

Simular
- Entrada: valor inicial, aporte mensal e prazo em meses.
- Resultado: total investido, total dos aportes, estimativa final, rendimento estimado e duas
  séries mês a mês para o gráfico (saldo com rendimento e saldo sem rendimento), do mês zero
  ao último.
- Salvar uma simulação e listar as salvas. Cada simulação salva tem identificador, data,
  valor inicial, aporte mensal, prazo em meses e resultado.

Perfil
- Nome completo, e-mail e iniciais da pessoa.
- Preferência de notificações (ligado ou desligado), que a pessoa pode alterar.

Estados que o app já sabe mostrar e que o back end precisa permitir distinguir
- Carregando (a Home tem uma tela de skeleton).
- Erro ao carregar, com botão de tentar novamente.
- Lista vazia.
- Sessão expirada.

## Regras de negócio já fixadas no app

Trate estas regras como requisitos e diga onde cada uma deve ser calculada (banco, servidor,
app ou mais de um lugar), justificando a escolha.

- Dinheiro é sempre decimal exato com duas casas, nunca ponto flutuante. No app é BigDecimal.
- Gastos do mês = soma dos gastos das categorias. Disponível = renda − gastos.
  Exemplo do protótipo: renda 5.200, gastos 3.480, disponível 1.720.
- Progresso de um objetivo = acumulado ÷ meta, limitado entre 0 e 100%.
  Exemplo: 8.400 de 15.000 = 56%.
- Aporte mensal sugerido ao criar um objetivo = valor desejado ÷ prazo em meses, arredondado
  para duas casas com meia unidade para cima. Exemplo: 15.000 ÷ 18 = 833,33.
- Datas trafegam como data sem hora para despesas e aportes, e como mês e ano para o mês de
  referência e a previsão de conclusão.
- Cada pessoa só enxerga e altera os próprios dados.

## Banco de dados: modelo de partida

O modelo abaixo é o ponto de partida, derivado do que as telas usam. Mantenha estas entidades
e relacionamentos. Você pode acrescentar campos ou tabelas de apoio se justificar, e deve
apontar qualquer problema que encontrar em vez de corrigi-lo em silêncio.

Entidades e campos

- usuario: id, nome_completo, email (único), senha_hash, avatar_url (opcional),
  renda_mensal, notificacoes_ativas, criado_em, atualizado_em.
- categoria: id, codigo (único: MORADIA, ALIMENTACAO, TRANSPORTE, SAUDE, EDUCACAO, LAZER,
  OUTROS), nome. É uma tabela de referência com sete linhas fixas, carregadas na criação do
  banco. Usuários não criam categorias.
- orcamento_categoria: usuario_id, categoria_id, valor_estimado. Guarda o valor que a pessoa
  estimou para cada categoria na configuração inicial.
- despesa: id, usuario_id, categoria_id, descricao, valor, data, criado_em.
- objetivo: id, usuario_id, nome, valor_meta, prazo_meses, imagem_url (opcional), criado_em.
- aporte: id, objetivo_id, titulo, valor, data, criado_em.
- simulacao: id, usuario_id, valor_inicial, aporte_mensal, prazo_meses, resultado, criado_em.
- sessao: id, usuario_id, token_hash, expira_em, revogada_em (opcional), criado_em.
- recuperacao_senha: id, usuario_id, token_hash, expira_em, usado_em (opcional), criado_em.

Relacionamentos

- usuario 1:N despesa. Uma despesa pertence a exatamente um usuário.
- categoria 1:N despesa. Toda despesa tem exatamente uma categoria.
- usuario N:N categoria, resolvido pela tabela orcamento_categoria, que carrega o atributo
  valor_estimado. Cada par usuário e categoria aparece no máximo uma vez.
- usuario 1:N objetivo.
- objetivo 1:N aporte. Um aporte pertence a exatamente um objetivo e, por ele, a um usuário.
- usuario 1:N simulacao.
- usuario 1:N sessao.
- usuario 1:N recuperacao_senha.

Valores que as telas mostram e que não estão como coluna no modelo acima

- Valor acumulado de um objetivo: é a soma dos aportes dele.
- Total de gastos do mês e gasto por categoria no mês: são somas sobre despesa.
- Disponível do mês: renda menos o total de gastos.
- Previsão de conclusão e aporte mensal de um objetivo: dependem de decisões pendentes.

## O que quero na seção de banco de dados

Esta seção deve ser a mais detalhada do documento. Inclua tudo isto:

1. Diagrama entidade-relacionamento com todas as tabelas, chaves primárias, chaves
   estrangeiras e cardinalidades, na notação pé de galinha. Depois do diagrama, explique cada
   relacionamento em uma ou duas frases em português simples, dizendo o que ele significa no
   produto ("um usuário tem várias despesas; apagar o usuário apaga as despesas dele").
2. Dicionário de dados: uma tabela por entidade com nome do campo, tipo, tamanho ou precisão,
   se aceita nulo, valor padrão, restrição e descrição. Use tipo decimal com precisão e escala
   explícitas para dinheiro e diga qual precisão escolheu e por quê.
3. Chaves: para cada tabela, a chave primária e o tipo dela (sequencial ou UUID), com a
   justificativa da escolha. Para orcamento_categoria, diga se a chave é composta ou
   substituta e por quê.
4. Integridade referencial: para cada chave estrangeira, o comportamento ao apagar e ao
   atualizar o registro pai (cascata, restringir ou anular), com o motivo. Deixe claro que
   uma categoria com despesas não pode ser apagada.
5. Restrições: unicidade (e-mail, código da categoria, par usuário e categoria), regras de
   checagem (valores positivos, prazo maior que zero, datas coerentes) e campos obrigatórios.
6. Índices: quais criar e qual consulta cada um atende. As consultas que as telas fazem são:
   despesas de um usuário em um mês, soma por categoria de um usuário em um mês, objetivos de
   um usuário, aportes de um objetivo do mais recente para o mais antigo, simulações de um
   usuário da mais recente para a mais antiga, e busca de sessão pelo token.
7. Normalização: diga em que forma normal o modelo está e aponte onde vale a pena guardar um
   valor calculado. Discuta especificamente se o valor acumulado do objetivo deve ser sempre
   somado dos aportes ou guardado em uma coluna, com prós e contras, e recomende um caminho.
8. Histórico da renda: a Home mostra a renda "do mês" e a pessoa pode mudar a renda com o
   tempo. Compare guardar a renda como um campo do usuário com guardar um histórico por mês em
   tabela própria, mostre o que muda no modelo em cada caso e recomende um caminho.
9. Script de criação completo (DDL) no banco indicado, na ordem correta de dependência, com
   tabelas, chaves, restrições e índices, mais o script que insere as sete categorias.
10. Dados de exemplo: um script de inserção que reproduza o protótipo, para eu testar. Use
    renda de 5.200; gastos por categoria de 1.200 (Moradia), 850 (Alimentação), 430
    (Transporte), 350 (Saúde), 350 (Lazer) e 300 (Outros); os objetivos "Reserva de
    emergência" (8.400 de 15.000), "Viagem Europa" (3.200 de 12.000) e "Notebook novo" (1.800
    de 4.500); e, para o primeiro objetivo, três aportes entre os que somam 8.400: 500 em
    15/09/2026, 500 em 10/08/2026 e 1.200 em 15/07/2026.
11. Consultas de cada tela: o SQL que responde cada tela do app (resumo da Home, lista de
    gastos do mês com total, lista de objetivos com acumulado e percentual, detalhe do
    objetivo com histórico, simulações salvas), com o resultado esperado sobre os dados de
    exemplo.
12. Segurança dos dados: senha guardada só como hash (indique o algoritmo), tokens guardados
    só como hash, quais campos são dados pessoais pela LGPD e como garantir, no nível das
    consultas, que um usuário nunca leia dados de outro.
13. Evolução do banco: como versionar as mudanças de esquema (migrações) e uma convenção de
    nomes para tabelas, colunas, chaves e índices.

## Decisões que ainda não foram tomadas

O design não define os itens abaixo. Não invente respostas para eles. Crie no documento uma
seção de decisões pendentes e, para cada item, explique por que a decisão importa, apresente
duas ou três opções com prós e contras, diga qual você recomenda e mostre o que muda no banco
de dados em cada opção.

- Fórmula da simulação: taxa, capitalização, momento do aporte e impostos. O app usa hoje, de
  forma provisória, juros compostos mensais de 0,96% com aporte no fim de cada mês. No exemplo
  do protótipo (1.000 iniciais, 24 aportes de 500) isso resulta em 14.680,67, e o protótipo
  mostra 14.680,00. Diga também se a taxa usada deve ser gravada junto com a simulação salva.
- Limites do prazo da simulação e do prazo de um objetivo.
- Regras de validação: formato de e-mail, força da senha, valores mínimos e máximos.
- Como a previsão de conclusão de um objetivo é calculada.
- Como o aporte mensal de um objetivo existente é definido.
- Se o título do aporte é texto livre ou um tipo fixo (mensal ou extra).
- Política de expiração da sessão e de nova tentativa após erro.
- O que acontece depois que a pessoa pede a recuperação de senha.
- Se as estimativas por categoria da configuração inicial funcionam como orçamento (limite)
  ou só como referência.
- De onde vêm as imagens de avatar e de miniatura dos objetivos, e onde ficam guardadas.
- Se despesas, objetivos e aportes podem ser editados e apagados, e se a exclusão é
  definitiva ou lógica. O app hoje só cria e lista.

## Estrutura do documento

Escreva em português do Brasil, em tom profissional e direto, como uma demanda interna de
engenharia. Prefira texto corrido para explicar e tabelas para o que é comparável (campos,
endpoints, códigos de erro). Estruture assim:

1. Capa: título, nome do projeto (Sobra), versão do documento e data.
2. Resumo executivo: o que será construído e por quê, em no máximo meia página.
3. Escopo: o que está dentro e o que está fora deste trabalho.
4. Requisitos funcionais, agrupados pelos módulos acima, cada um com identificador (RF-01,
   RF-02 e assim por diante) e critério de aceite verificável.
5. Requisitos não funcionais: segurança, desempenho esperado, tratamento de erros e registro
   de logs.
6. Banco de dados, com os treze itens pedidos acima, na ordem em que estão.
7. Contrato da API: para cada operação, método e caminho, quem pode chamar, corpo da
   requisição, corpo da resposta com exemplo em JSON, erros possíveis e quais tabelas ela lê
   ou grava. Inclua um formato único de resposta de erro e a forma de sinalizar sessão
   expirada.
8. Regras de negócio, com os exemplos numéricos acima como casos de teste.
9. Decisões pendentes, no formato descrito na seção anterior.
10. Plano de entregas: divida o trabalho em etapas pequenas e ordenadas, cada uma com o que
    entrega, do que depende e como eu verifico que está pronta. Comece pelo banco de dados e
    faça a primeira etapa de API permitir trocar os dados fictícios de uma tela do app por
    dados reais o quanto antes.
11. Plano de testes: o que testar em cada etapa, incluindo as restrições do banco (tentar
    violar cada uma) e os exemplos numéricos.
12. Glossário dos termos do produto (sobra, aporte, objetivo, disponível) e dos termos de
    banco usados no documento (chave primária, chave estrangeira, cardinalidade, índice,
    forma normal, migração).

Como sou intermediário e vou implementar sozinho, explique o motivo das escolhas técnicas em
uma ou duas frases quando ele não for óbvio. Fora os scripts SQL pedidos na seção de banco de
dados, não inclua código de implementação: o documento define o que fazer e o contrato, e a
implementação é comigo.
