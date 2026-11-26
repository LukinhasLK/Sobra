# Auditoria completa (10 lentes): Sobra — app Android + API Spring Boot

**Data**: 2026-11-26
**Modo**: Branch vs main
**Fonte**: branch `claude/youthful-darwin-9ml80l` comparada com `main` (11 commits, ~155 arquivos)
**Lentes executadas**: 10/10 — Design, UX, Frontend (Android/Compose), Backend (Java/Spring), Estrutura, Manutenção do repositório, Prontidão para produção, DX, Desempenho, Acessibilidade
**Lentes puladas**: nenhuma

**Como foi feito**: as 10 lentes rodaram em 5 agentes independentes, todos só de leitura, cada um seguindo a skill da sua lente (as skills foram escritas para React/Rust; os critérios foram traduzidos para Android/Compose e Spring Boot). O app não pôde ser compilado (não há Android SDK no ambiente); o back end foi testado de verdade (80 testes, 0 falhas) e vários achados de back end foram confirmados com chamadas reais à API.

**Correção já aplicada depois da auditoria**: o único erro de compilação encontrado (`Pickers.kt` usava `.height(...)` sem o import) foi corrigido. Uma varredura em todos os arquivos não achou outro import de layout faltando.

---

## Veredito geral: APROVAR COM MUDANÇAS

O projeto está bem construído: back end acima da média (consultas parametrizadas, isolamento entre usuários, Argon2id, token só como hash, formato único de erro, 80 testes passando) e app com arquitetura clara (telas sem estado, ViewModels, estados de carregando/erro/vazio, dinheiro sempre em BigDecimal). Nenhuma lente encontrou problema estrutural que exija refazer o trabalho. Mas há **um punhado de bugs reais e localizados** — navegação que leva a tela em branco, dados de outra conta reaparecendo, registros duplicados em rotação, configuração inicial que pode ficar incompleta, um 500 no back end e campos cortados com fonte grande — que devem ser corrigidos, e **o app precisa ser compilado e testado no emulador** antes da apresentação.

## Veredito por lente

| # | Lente | Veredito | Críticos | Recomendações |
|---|---|---|---|---|
| 1 | Design Engineer | APROVAR COM MUDANÇAS | 1 | 13 |
| 2 | UX Engineer | APROVAR COM MUDANÇAS | 3 | 10 |
| 3 | Frontend Engineer (Android/Compose) | APROVAR COM MUDANÇAS | 3 (+1 erro de compilação, já corrigido) | 10 |
| 4 | Backend Engineer (Java/Spring) | APROVAR COM MUDANÇAS | 1 | 11 |
| 5 | Structural Engineer | APROVAR COM MUDANÇAS | 0 | 5 |
| 6 | Repo Maintainer | APROVAR COM MUDANÇAS | 0 | 10 |
| 7 | Production Readiness | APROVAR COM MUDANÇAS (apresentação) / REFAZER (produção) | 1 | 11 |
| 8 | DX Engineer | APROVAR COM MUDANÇAS | 0 | 9 |
| 9 | Performance Engineer | APROVAR COM MUDANÇAS | 0 | 7 |
| 10 | Accessibility Engineer | APROVAR COM MUDANÇAS | 2 | 13 |

## Todos os problemas críticos (corrigir)

Juntados de todas as lentes, sem repetição, do mais grave para o menos grave.

| # | Problema | Lente(s) | Arquivo(s) | Impacto | Correção |
|---|---|---|---|---|---|
| 1 | **App nunca compilado nem rodado.** Erro de compilação em `Pickers.kt` (import de `height`) — **já corrigido**. | Frontend, Prontidão | todo `app/src/main` | Pode falhar na hora da apresentação | Build no Android Studio, `./gradlew :app:testDebugUnitTest :app:lintDebug` e o `docs/roteiro-de-teste.md` inteiro, pelo menos um dia antes |
| 2 | **Dados de outra conta reaparecem** depois de sair e entrar com outra: as abas salvas (`saveState`/`restoreState`) devolvem o ViewModel antigo; no Perfil, nome e e-mail da conta anterior ficam na tela | Frontend | `SobraNavHost.kt:123-137, 558-564`; `ProfileViewModel.kt:23-26` | Vazamento de dados entre contas no mesmo aparelho | No logout, sessão expirada e login: `nav.clearBackStack(...)` das abas, e `simulation.reset()` também no login |
| 3 | **"Voltar" leva a tela em branco** quando Login ou Renda é a única tela da pilha (depois de sair, sessão expirada ou login com configuração pendente) | UX, Frontend, Design | `SobraNavHost.kt:212, 275` + `navigateClearingStack` | Beco sem saída: a pessoa precisa fechar o app | Esconder o "Voltar" quando `nav.previousBackStackEntry == null`, ou fechar o app |
| 4 | **Registro duplicado se a tela girar durante o salvamento**: o callback guardado no ViewModel navega num `NavController` antigo; a pessoa fica no formulário e salva de novo | Frontend, Prontidão, UX | `NewExpenseViewModel`, `NewGoalViewModel`, `SimulationViewModel`, `GoalDetailViewModel`, `AuthViewModel`, `SetupViewModel`; usos no `SobraNavHost` | Despesas, objetivos e simulações duplicados (e não há como apagar) | Trocar o callback por um evento observado na tela (`LaunchedEffect(vm.saved)`) |
| 5 | **Configuração inicial pode ficar incompleta para sempre**: quem cria a conta e fecha o app na Renda ou nas Categorias entra direto na Home com renda R$ 0,00 | UX, Prontidão | `SobraNavHost.kt:176` (Splash só olha se há token) | O número principal do app ("quanto sobra") fica errado sem explicação | Na Splash, com token, chamar `GET /configuracao` (já existe na API) e ir para Renda se `concluida = false` |
| 6 | **Voltar na Renda devolve ao Cadastro** de uma conta que já existe; tocar em "Criar conta" de novo dá "e-mail já cadastrado" | UX, Frontend | `SobraNavHost.kt:246` | A pessoa acha que o cadastro falhou | `navigateClearingStack(Routes.INCOME)` depois do cadastro |
| 7 | **Back end: criar objetivo com meta pequena e prazo longo dá erro 500** (ex.: R$ 1,00 em 360 meses): o aporte arredonda para 0,00 e o banco recusa | Backend | `ObjetivoService.java:82-93`, `RegrasObjetivo.java:20-22`, `V001:94`, `TratadorErros.java:45-62` | Funcionalidade quebrada para entrada válida, e o log grava dados pessoais | Validar no domínio e devolver 422; mapear `ck_objetivo_aporte_mensal` no `TratadorErros` |
| 8 | **Campos de texto cortam o que é digitado com fonte do sistema a partir de 115%** (altura fixa 48dp − padding 2×14dp) | Design, Acessibilidade | `Fields.kt:140, 143` | Quem aumenta a fonte não lê o que digita em Nova despesa, Novo objetivo e Simular | `heightIn(min = 48.dp)` e sem padding vertical |
| 9 | **Mensagens de erro não são anunciadas pelo leitor de tela** e ficam abaixo da dobra | Acessibilidade | `Pickers.kt:257-261` (`ErrorText`), `AuthScreens.kt:70-74` (`FormError`) | Usuário de TalkBack erra a senha e não recebe retorno | `semantics { liveRegion = LiveRegionMode.Polite }` no `ErrorText`; apagar `FormError` duplicado |

## Problemas transversais (apontados por 2 ou mais lentes)

| Problema | Apontado por | Causa | Correção unificada |
|---|---|---|---|
| Navegação "Voltar" que não volta (tela em branco, volta ao cadastro, "Voltar" do erro que recarrega) | UX, Frontend, Design | `navigateClearingStack` + `BackLink` sempre visível; `onBack = vm::load` nas abas | Uma regra só: o "Voltar" aparece apenas quando há tela anterior; cadastro limpa a pilha |
| Registros duplicados | Prontidão, Frontend, UX | Callback de navegação no ViewModel; resultado salvo continua com "Salvar" ativo; POST com timeout | Eventos observados na tela; marcar a simulação como salva; idempotência em produção |
| Configuração inicial incompleta | UX, Prontidão | Splash decide só pelo token | `GET /configuracao` na Splash |
| Taxa da simulação em 3 lugares (servidor, `Finance.kt`, `strings.xml`) | Estrutura, DX | Prévia local e texto fixo; `taxa_mensal` da API é ignorada | Ler `taxa_mensal` da API e mostrar no Resultado; documentar a prévia como estimativa |
| Sair da conta pode abrir "Sessão expirada" | UX, Frontend | O logout passa pelo mesmo `call()` que dispara `onSessionExpired` | `notifyExpiry = false` no logout |
| Erros silenciosos (sem log no app, sem aviso de atualização falha, sem anúncio ao leitor de tela) | DX, Prontidão, UX, Acessibilidade | `loadResult` engole exceções; `keepOnError` descarta falhas; sem `liveRegion` | `Log.w` em debug com `id_requisicao`; aviso discreto "Não foi possível atualizar"; `liveRegion` |
| Evento de sessão expirada pode se perder (comentário errado no `SessionStore`) | Manutenção, Frontend | `MutableSharedFlow` sem `replay` descarta sem coletor | `replay = 1` + `resetReplayCache()`, ou `Channel(CONFLATED)` |
| Limite de login trava a demonstração (5 por IP a cada 15 min, contando logins certos) | Prontidão, DX, Backend | Emulador sempre do mesmo IP; limite também conta sucessos | Relaxar `login-por-ip` no perfil `dev`; contar só falhas; reiniciar a API zera o limite |
| Limite por IP burlável em produção (`X-Forwarded-For`) | Backend, Prontidão | `forward-headers-strategy: framework` confia no cliente | `native` + `internal-proxies` do Tomcat |
| Valor máximo diferente: app aceita 11 dígitos (R$ 999 mi), API só 99.999.999,99 | Estrutura, DX | `MAX_MONEY_DIGITS = 11` | Usar 10 dígitos |
| Fonte grande corta texto em contêineres de altura fixa | Design, Acessibilidade | `height(...)` em vez de `heightIn(min = ...)` | Trocar em `WhiteField`, chips, tiles, navegação, linhas do Perfil e pílulas de mês |
| Release não conversa com a API (URL fixa `10.0.2.2` e HTTP só no debug) | Prontidão, Frontend, DX | `ApiConfig.kt:10` + network config só em `src/debug` | `buildConfigField` por build type e HTTPS no release; apresentar com o build debug |
| Mensagens de erro do app fixas no código | Manutenção, DX | Validações escritas nos ViewModels | Passar para `strings.xml` ou documentar a convenção |
| Simulação descarta a mensagem de validação da API | Manutenção, DX, Frontend | `SimulationViewModel.calculate` usa `loadResult` | Usar `e.displayMessage` do `ApiException` |
| Rotas e TODOs que a branch já resolveu | Manutenção, UX | `GOALS_EMPTY`, `HOME_SKELETON`, `ERROR`; "TODO(back end)" antigos | Remover |
| Fuso do aparelho diferente do servidor (America/Sao_Paulo) | Prontidão | `LocalDate.now()` no app | Fixar o fuso do emulador ou usar `ZoneId.of("America/Sao_Paulo")` |

## Principais recomendações (por área)

| # | Recomendação | Lente | Arquivo(s) | Correção |
|---|---|---|---|---|
| 1 | Dados pessoais vazam no log pelo Hibernate (e-mail em violação de unicidade) | Backend | `application.yml:38-41` | `logging.level."org.hibernate.orm.jdbc.error": OFF` |
| 2 | Dois PUTs simultâneos de renda/categorias dão 500 | Backend | `ConfiguracaoService.java:81-86, 133-138` | Upsert `INSERT ... ON CONFLICT DO UPDATE` |
| 3 | Gravações concorrentes podem desfazer revogação de sessão ou troca de senha | Backend | `SessaoService`, `PerfilService`, `AuthService` | UPDATEs JPQL pontuais ou `@DynamicUpdate`/`@Version` |
| 4 | Token de recuperação aceita dois usos simultâneos | Backend | `AuthService.java:176-184` | `UPDATE ... WHERE usado_em IS NULL` e checar 1 linha |
| 5 | `prazo_meses: 1e400` dá 500; número enorme é truncado | Backend | `Validacao.java:223-231` | Rejeitar infinito/NaN; `longValueExact()` |
| 6 | Argon2 roda segurando conexão do pool | Backend | `AuthService.java:59, 89, 163` | Hash fora da transação; limitar hashes simultâneos |
| 7 | Rendimento em verde (2,54:1) no Resultado | Design, Acessibilidade | `SimulateScreens.kt:396` | `TealDark` (5,47:1), dentro da paleta |
| 8 | Tela de erro mostra a aba "Início" ativa em qualquer aba | Design, Acessibilidade | `StateScreens.kt:225` | Parâmetro `bottomTab` |
| 9 | Sino da Home sem ação | Design, Acessibilidade | `SobraNavHost.kt:325` | Esconder até existir a tela |
| 10 | Diálogos não rolam (aporte não cabe em paisagem/fonte grande) | Acessibilidade | `Pickers.kt:49-61` | `verticalScroll` no card |
| 11 | Rótulo do campo não é lido junto com o campo; sem títulos semânticos; grupos sem posição | Acessibilidade | `Fields.kt`, `Headers.kt`, chips e navegação | `mergeDescendants`, `heading()`, `selectableGroup()` |
| 12 | Não há como alterar renda e categorias depois | UX | — | Linha no Perfil reaproveitando as telas 08 e 09 |
| 13 | Depois de salvar despesa, quem veio de Gastos cai na Home | UX | `SobraNavHost.kt:365` | Voltar à tela de origem e abrir o modal por cima |
| 14 | Erro ao refazer simulação salva não aparece | UX, Prontidão | `SobraNavHost.kt:482-485` | Erro e carregando na própria lista |
| 15 | Busca de novo a cada troca de aba e rotação; buscas concorrentes sem cancelamento | Desempenho | `SobraNavHost.kt:307, 334, 379, 494`; VMs | Janela de validade + "dados sujos"; `job?.cancel()` |
| 16 | Camada `data` depende de `ui.model` (Compose e `R`) | Estrutura | `Models.kt`, repositórios | Modelos em `domain/model`; mapeamento visual na `ui` |
| 17 | Recuperação de senha não testável em dev (link não aparece no log) | DX, Prontidão | `EnvioEmail.java` | Logar o link só no perfil `dev` |
| 18 | README sem pré-requisitos (JDK 21, Docker, porta 5432) e sem "problemas comuns" | DX | `README.md` | Seções novas |
| 19 | Perfil `prod` do back end com padrões perigosos (perfil `dev` padrão, senha `sobra`) | Prontidão | `application.yml:9, 12-14, 48, 81` | Sem valores padrão em `prod`; banco separado |
| 20 | Autoria dos commits aparece como "Claude" | Manutenção | `git log` | Decisão do dono (squash/merge com seu autor) |

### Decisões que são do dono do projeto (paleta fixa)
- Vermelho como cor de texto (mensagens de erro 3,55:1 e "Sair da conta" 3,76:1) fica abaixo do mínimo 4,5.
- Placeholder cinza sobre o fundo Soft (4,18:1).
- Série "sem rendimento" do gráfico (1,7:1), compensada pelos números em texto.

---

# Relatórios completos das lentes

Os relatórios abaixo são a saída completa de cada agente. Cada agente cobriu duas ou três lentes; a ordem segue as lentes 1 a 10.


---

## Lentes 1 e 10 — Design Engineer e Accessibility Engineer

# Auditoria do app Android "Sobra": design e acessibilidade

**Branch:** `claude/youthful-darwin-9ml80l`. **Escopo:** `git diff main...HEAD -- app/` (59 arquivos, +3296/−389). As telas foram avaliadas como estão hoje.
**Método:** carreguei as skills `audit-as-design-eng` e `audit-as-a11y-eng` e traduzi os critérios para Android/Compose.

Avisos antes de tudo:
- **Nada foi gravado.** As skills mandam gravar em `reviews/*.md`, mas a restrição de só leitura vale mais. O relatório completo está só aqui.
- **Sem compilação** (não há Android SDK). Tudo vem da leitura do código.
- **Contrastes** calculados com python3 pela fórmula WCAG.
- **Altura de linha** medida no próprio arquivo `res/font/manrope_regular.ttf`: ascent 2132 e descent 600 em 2000 unidades, ou seja 1,366 em. Um texto de 15sp ocupa 20,5dp.

---

# LENTE 1 — Design Engineering Audit: refino visual das telas (referência Stoic) + ligação com a API

**Perspectiva:** Design Engineer · **Data:** 2026-11-26 · **"Plano":** diff `main...HEAD` em `app/`

## Resumo
O que mudou visualmente:
- A Home foi reestruturada: tiles de Renda e Gastos, card "Disponível" em TealDark e um carrossel de objetivos.
- Gastos ganhou uma fileira de meses e uma lista agrupada por dia.
- Nova despesa, Novo objetivo, Novo aporte e Simular passaram a usar chips (`SelectChip`, `DateChips`).
- Há banners TealDark em Gastos, Objetivos e Resultado.
- Todo texto que era Teal virou TealDark, e a aba ativa ganhou uma pílula TealLight.
- Surgiram diálogos novos (`MessageDialog`, `AddContributionDialog`, `CategoryAmountDialog`, DatePicker) e os estados de carregamento, vazio e erro foram ligados à API.

## Arquivos revisados
| Arquivo | Papel visual | Risco |
|---|---|---|
| ui/theme/Color.kt, Type.kt, Dimens.kt, Theme.kt | Tokens (paleta, tipografia, espaços, raios) | Baixo |
| ui/components/Buttons.kt | Botões primário, secundário e compacto (TealDark) | Médio (sem estados) |
| ui/components/Chips.kt (novo) | `ChipGroup`, `SelectChip`, `DateChips` | Baixo |
| ui/components/Fields.kt | `SoftField`, `WhiteField`, campo de dinheiro | **Alto** (altura fixa, sem foco) |
| ui/components/Pickers.kt (novo) | Diálogos, `TypeTile`, `ErrorText` | Alto |
| ui/components/Headers.kt, Indicators.kt, Rows.kt, BottomNav.kt, AmountCard.kt, LoadingScreen.kt, Layout.kt, Surfaces.kt | Primitivas | Médio |
| ui/screens/home/HomeScreen.kt | Home refinada | Médio |
| ui/screens/expenses/ExpensesScreens.kt | Gastos e Nova despesa | Médio |
| ui/screens/goals/GoalsScreens.kt | Objetivos, Novo objetivo, Detalhe | Médio |
| ui/screens/simulate/SimulateScreens.kt | Simular, Resultado, Salvas | Médio |
| ui/screens/profile/ProfileScreen.kt, setup/SetupScreens.kt, auth/AuthScreens.kt, states/StateScreens.kt | Perfil, configuração inicial, login, estados | Médio |
| navigation/SobraNavHost.kt | Qual estado aparece em cada tela | Médio |

## Veredito: **APPROVE WITH CHANGES**

## Problemas críticos (corrigir antes do merge)
| # | Problema | Arquivo(s) | Impacto | Correção |
|---|---|---|---|---|
| D-C1 | O `WhiteField` tem altura fixa de 48dp e padding vertical de 14dp. Sobram 20dp para o conteúdo, e o texto 15sp em Manrope ocupa 20,5dp. Com fonte do sistema a partir de 115% (23,6dp; 26,6dp em 130%), o texto digitado é cortado, porque o `sobraCard` recorta. | Fields.kt:140 (`height(WhiteFieldHeight)`) e :143 (`padding(vertical = Space14)`) | Afeta Descrição (Nova despesa), Nome e Valor (Novo objetivo), Valor inicial e Aporte mensal (Simular). O campo fica ilegível justamente para quem aumenta a fonte. É o mesmo achado A-C1 da lente 2. | Trocar `.height(48.dp)` por `.heightIn(min = 48.dp)` e tirar o padding vertical (o `verticalAlignment = CenterVertically` já centraliza). Com fonte em 100% o visual fica idêntico ao Figma. |

## Recomendações
| # | Problema | Arquivo(s) | Por quê | Correção |
|---|---|---|---|---|
| D-R1 | Outros contêineres de altura fixa cortam texto com fonte ampliada: `CategoryChip` 59dp (texto cortado a partir de ~115%), `TypeTile` 88dp (~115%), item da navegação 43dp (~130%, a legenda perde a base), `SettingsRow` 52/64dp, pílula de mês 56dp (~150%). | Rows.kt:250; Pickers.kt:220/230; BottomNav.kt:37/93; ProfileScreen.kt:48-49/173; ExpensesScreens.kt:88-89/189 | Mesmo motivo do D-C1. O próprio código registra o TODO em Type.kt:82. | Usar `heightIn(min = …)` em vez de `height(…)`. Na navegação, usar `heightIn(min = 43.dp)` no item e no `BarHeight` (BottomNav.kt:35/75). |
| D-R2 | O Resultado mostra o "Rendimento" em `Green` (#10B981) sobre branco: contraste **2,54:1**. Os outros valores positivos do app (aporte, `toBrlPositive`) usam TealDark. | SimulateScreens.kt:396 | Falha AA e quebra a consistência ("+ R$" é TealDark em ContributionRow, Rows.kt:163). | Usar `SobraColors.TealDark` (5,47:1). Fica dentro da paleta. |
| D-R3 | O ErrorScreen fixa `bottomTab = BottomTab.Home`, mas é usado em Gastos, Objetivos, Perfil, Detalhe do objetivo e Simulações salvas. A aba errada aparece ativa. O Detalhe do objetivo, que não tem navegação, ganha uma no erro. | StateScreens.kt:225; SobraNavHost.kt:336, 382, 418, 477, 499 | Quebra a orientação ("onde estou?"). O TalkBack anuncia "Início, selecionado" dentro de Objetivos. | Aceitar `bottomTab: BottomTab?` como parâmetro e passar a aba de cada rota (`null` no Detalhe e nas Salvas). |
| D-R4 | O chevron "Voltar" do ErrorScreen, nas raízes das abas, recarrega a tela em vez de voltar (`onBack = vm::load`). | SobraNavHost.kt:312, 336, 382, 499 | O controle diz uma coisa e faz outra, e duplica o "Tentar novamente". | Nas raízes, não mostrar o chevron (tornar `onBack` opcional no ErrorScreen). |
| D-R5 | O sino da Home não faz nada (`onNotificationsClick = { /* TODO */ }`). | SobraNavHost.kt:325; HomeScreen.kt:218-223 | Um controle morto, com cara e anúncio de botão. O próprio código adotou essa regra no lápis do Detalhe (GoalsScreens.kt:281-283). | Esconder o sino enquanto não houver tela, como já foi feito com o lápis. |
| D-R6 | Os campos não têm estado de foco. Só o cursor indica o campo ativo (TODO em Fields.kt:56). | Fields.kt:84-90, 137-143 | Sem foco visível, a navegação por teclado ou Switch Access fica às cegas, e falta um estado no sistema. | Usar `interactionSource.collectIsFocusedAsState()` e, no foco, uma borda de 1–2dp em `Teal` (3,53:1 sobre Background, passa o 3:1 de elemento não textual). |
| D-R7 | O banner TealDark (label TealLight, valor OnTeal) foi copiado 4 vezes com medidas diferentes: Home (padding 24, raio 24, valor 32), Gastos (20/20/28), Objetivos (20/20/28), Resultado (20/20/32). | HomeScreen.kt:264-303; ExpensesScreens.kt:132-151; GoalsScreens.kt:141-158; SimulateScreens.kt:335-353 | Fragmenta o sistema: a próxima mudança de padding vai divergir. | Extrair um `TealBanner(label, value, caption?)` em `ui/components` com um raio só. A Home pode manter o seu tamanho por parâmetro. |
| D-R8 | `FormError` (AuthScreens.kt:70-74) duplica `ErrorText` (Pickers.kt:257-261). | idem | São dois componentes para a mesma coisa, e a correção de A-C2 teria de ser feita duas vezes. | Apagar `FormError` e usar `ErrorText`. |
| D-R9 | Texto `TextSecondary` sobre fundo claro abaixo de 4,5:1: dica do card de sugestão sobre TealLight (4,26), "Novo objetivo" sobre Soft (4,18, 14sp). | GoalsScreens.kt:258; HomeScreen.kt:393 | Falha AA em texto pequeno. | Dentro da paleta: `TealDark` (4,86 sobre TealLight; 4,77 sobre Soft) ou `TextPrimary`. |
| D-R10 | O total estimado "acima da renda" tem a mesma aparência do total normal. | SetupScreens.kt:114-121 | O alerta passa despercebido; só o texto muda. | No estado acima da renda, usar `TextPrimary` + peso W700 (não usar Red como texto: ver D-R11). |
| D-R11 | `Red` como cor de texto: mensagens de erro (3,55:1 sobre Background) e "Sair da conta" (3,76:1 sobre branco). | Pickers.kt:259; AuthScreens.kt:72; ProfileScreen.kt:133 | Falha AA. A paleta é fixa, então é **decisão do dono**. | Opções dentro da paleta: (a) texto em `TextPrimary` acompanhado de um ícone Red (vale para os erros); (b) manter o Red, sabendo que falha AA. |
| D-R12 | "Histórico" sem estado vazio: o título aparece sobre uma área em branco quando ainda não há aporte. | GoalsScreens.kt:351-361 | Falta o estado vazio. | Se `contributions` estiver vazio, mostrar um texto `TextSecondary` ("Nenhum aporte ainda"). |
| D-R13 | Na tela Objetivos com itens, há dois botões "Adicionar objetivo" (o do header e o FAB). Gastos usa só o FAB. | GoalsScreens.kt:90-103 | Redundância e inconsistência entre abas; o TalkBack lê a ação duas vezes. | Manter um só: o FAB, para seguir o padrão de Gastos. |

## Detalhes opcionais
| # | Sugestão | Motivo |
|---|---|---|
| D-N1 | No `EmptyMonth`, aplicar `textAlign = TextAlign.Center` nos textos (ExpensesScreens.kt:263-264). | A coluna está centralizada, mas o texto de várias linhas fica alinhado à esquerda. |
| D-N2 | O estado vazio de "Simulações salvas" usa o ícone de alvo (`ic_target_64`, SimulateScreens.kt:526), que é o de Objetivos. | Mistura os conceitos. Não existe hoje um ícone de gráfico de 64; seria preciso criar. |
| D-N3 | Os carregamentos são diferentes entre telas: skeleton na Home, spinner embutido em Gastos, `LoadingScreen` em tela cheia nas outras, e no Detalhe sem header nem voltar. | No Detalhe, mostrar ao menos o `BackHeader` durante o carregamento. |
| D-N4 | Os botões não têm estado desabilitado nem de carregamento; só o texto muda ("Salvando…"). TODO em Buttons.kt:31. | O texto já resolve o feedback, e o ViewModel ignora toques repetidos. Um `alpha` menor enquanto salva reforçaria. |
| D-N5 | Os botões usam `maxLines = 1` sem `TextOverflow.Ellipsis` (Buttons.kt:83). | Um rótulo longo com fonte grande é cortado seco. |

## Conformidade com o sistema de design
- **Paleta:** nenhuma `Color(0x…)` fora de `theme/` (verificado com grep). TealDark ficou restrito a texto e a fundos com texto branco; ícones, barras, anéis e FAB continuam em Teal, como pedido.
- **Theme.kt:13:** `primary = TealDark` foi uma boa decisão, porque o DatePicker do Material põe texto branco sobre primary (5,47:1).
- **Dimens:** usados com disciplina. Há medidas locais nomeadas por componente (`ChipHeight 44`, `MonthPill 64×56` etc.), o que é aceitável.
- **Raios:** uma deriva leve, com a Home em 24 e as demais telas em 20 nos mesmos papéis (ver D-R7).
- **Tipografia:** `manrope()` local em cada tela. Funciona, mas os estilos de banner e de valor se repetem com tamanhos diferentes (D-R7).

## Cobertura de estados visuais
| Componente | Toque (ripple) | Foco | Pressionado | Desabilitado | Carregando | Vazio | Erro | Dark mode |
|---|---|---|---|---|---|---|---|---|
| PrimaryButton / SecondaryButton | Sim | Ripple fraco | Ripple | **Não** | Só texto | n/a | n/a | N/A (só tema claro) |
| SoftField / WhiteField | n/a | **Não** | n/a | **Não** | n/a | Placeholder no SoftField; nenhum no WhiteField | Texto separado, sem marcar o campo | N/A |
| SelectChip / DateChips | Sim | Ripple fraco (quase invisível no TealDark) | Ripple | n/a | n/a | n/a | n/a | N/A |
| BottomNav | Sim | Ripple | Ripple | n/a | n/a | n/a | Aba errada no erro (D-R3) | N/A |
| Home | n/a | n/a | n/a | n/a | Skeleton | Sim (gastos e objetivos) | ErrorScreen | N/A |
| Gastos | n/a | n/a | n/a | n/a | Spinner + "—" | Sim (EmptyMonth) | ErrorScreen | N/A |
| Objetivos / Detalhe | n/a | n/a | n/a | n/a | LoadingScreen | Lista: sim. Histórico: **não** (D-R12) | ErrorScreen | N/A |
| Simular / Resultado / Salvas | n/a | n/a | n/a | n/a | Texto do botão | Salvas: sim | ErrorText / ErrorScreen | N/A |
| Diálogos (aporte, categoria, mensagem) | Sim | n/a | n/a | n/a | Aporte: toque ignorado, sem visual | n/a | ErrorText interno | N/A |
| Perfil | Sim | Ripple | Ripple | n/a | LoadingScreen | n/a | ErrorScreen | N/A |

## Detalhe do veredito
- **Fidelidade aos tokens: PASS.** A paleta é respeitada e TealDark está bem aplicado. Restam D-R2, D-R9 e D-R11 (contraste dentro da paleta).
- **Cobertura de estados: CONCERNS.** Faltam o foco dos campos, o estado desabilitado e o vazio do histórico, e o ErrorScreen mostra a aba errada.
- **Dark mode: N/A.** O app só tem tema claro (Theme.kt; o Figma só define o claro). Registrado, sem nada inventado.
- **Animação: N/A, com uma exceção.** A única animação é o shimmer do skeleton da Home (StateScreens.kt:90-108, infinito, 1200ms, só gradiente). As transições são as padrão do Navigation. Não há movimento relevante a auditar.
- **Consistência visual: CONCERNS.** Banners duplicados (D-R7), duas ações iguais em Objetivos (D-R13) e um controle morto (D-R5).

---

# LENTE 2 — Accessibility Audit: mesmo escopo

**Perspectiva:** Accessibility Engineer · **Data:** 2026-11-26

## Resumo
O ganho de contraste com TealDark é real. Quase toda a semântica básica está certa: `Role.Button`, `selectable` com `Role.RadioButton` ou `Role.Tab`, `toggleable` com `Role.Switch`, `contentDescription` nos botões só de ícone e um slider com `setProgress`.

Os riscos estão em três frentes:
1. Texto ampliado (alturas fixas).
2. Anúncios: não existe nenhum `liveRegion`, `heading()`, `selectableGroup` ou `mergeDescendants` explícito no app (grep sem resultado).
3. Rótulos de campo desassociados.

## Arquivos revisados
| Arquivo | Papel de acessibilidade | Risco |
|---|---|---|
| Fields.kt | Campos de texto e dinheiro | **Alto** |
| Pickers.kt | Diálogos e `ErrorText` | **Alto** |
| AuthScreens.kt | Formulários de login, cadastro e recuperação | Alto (erros) |
| ProfileScreen.kt | Linha + interruptor | Médio |
| SimulateScreens.kt | Slider, gráfico | Médio |
| BottomNav.kt, ExpensesScreens.kt (MonthStrip), Chips.kt | Grupos de seleção | Médio |
| Rows.kt | Linhas de lista | Médio |
| StateScreens.kt | Skeleton, erro, modal | Médio |
| Headers.kt, Buttons.kt, Indicators.kt, HomeScreen.kt, GoalsScreens.kt, SetupScreens.kt | Rótulos e áreas de toque | Baixo a médio |

## Veredito: **APPROVE WITH CHANGES**
Os dois críticos se resolvem em 2 componentes compartilhados (`WhiteField`, `ErrorText`), sem retrabalho de telas. Mesmo assim, devem entrar antes do merge.

## Problemas críticos (corrigir antes do merge)
| # | Problema | Critério WCAG | Arquivo(s) | Impacto | Correção |
|---|---|---|---|---|---|
| A-C1 | O texto digitado no `WhiteField` é cortado a partir de 115% de fonte. Conta: 48dp − 2×14dp = 20dp disponíveis; 15sp × 1,366 = 20,5dp em 100%, 23,6dp em 115%, 26,6dp em 130%. | 1.4.4 Redimensionar texto | Fields.kt:140, :143 | Quem usa fonte grande (muitos usuários mais velhos) não consegue ler o que digita em Nova despesa, Novo objetivo e Simular. | `heightIn(min = 48.dp)` e sem padding vertical (igual ao D-C1). |
| A-C2 | As mensagens de erro (`ErrorText` e `FormError`) aparecem sem `liveRegion` e ficam no fim do conteúdo rolável, abaixo dos campos. O botão volta de "Entrando…" para "Entrar" sem nenhum anúncio. | 4.1.3 Mensagens de status; 3.3.1 Identificação de erro | Pickers.kt:257-261; AuthScreens.kt:70-74 e :141/:247/:296; ExpensesScreens.kt:364; GoalsScreens.kt:234; SetupScreens.kt:86/154; SimulateScreens.kt:158/400 | Um usuário de TalkBack que erra a senha, ou cuja despesa é recusada pela API, não recebe nenhum retorno. O fluxo trava sem explicação. Em telas longas, quem enxerga também pode não ver o erro, que fica abaixo da dobra. | No `ErrorText`: `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`. Aplicar o mesmo à `infoMessage` da recuperação de senha (AuthScreens.kt:298-300). Opcional: um `BringIntoViewRequester` para rolar até o erro quando ele aparece. |

## Recomendações
| # | Problema | Critério WCAG | Correção |
|---|---|---|---|
| A-R1 | Os rótulos dos campos são `Text` irmãos do `BasicTextField`, sem associação. Tocando direto no campo (explorar por toque), o TalkBack diz só "Caixa de edição, 0,00", sem "Valor" ou "Descrição". O `WhiteTextField` e o campo de dinheiro não têm nem placeholder. (Fields.kt:81/93, 136-146, 223-241; AmountCard.kt:42/55) | 1.3.1, 4.1.2, 3.3.2 | Na `Column` do campo, aplicar `Modifier.semantics(mergeDescendants = true) {}`, para que rótulo e campo virem um só nó. Alternativa: `semantics { contentDescription = label }` no `BasicTextField`. |
| A-R2 | Os diálogos (`PickerDialog`) não rolam. O `AddContributionDialog` tem cerca de 560dp de conteúdo (valor, dois tiles de 88dp, chips de data, erro e 2 botões de 54dp). Em paisagem (a orientação não está travada no Manifest), com fonte de 150–200% ou com o teclado aberto, "Salvar" fica fora de alcance. (Pickers.kt:49-61, 174-218) | 1.4.10 Refluxo; 1.4.4 | `Column(Modifier.verticalScroll(rememberScrollState()))` dentro do card, ou rolar só o conteúdo e fixar os botões. |
| A-R3 | Interruptor de notificações com foco duplo. A linha é `clickable(Role.Button)` e anuncia "Notificações, …, botão", sem estado. O `SobraSwitch` aninhado anuncia "Ativado, interruptor", sem rótulo. (ProfileScreen.kt:114-121, :174; Indicators.kt:157) | 4.1.2 Nome, Função, Valor | Na linha: `toggleable(value = notificationsEnabled, role = Role.Switch, onValueChange = …)`. No switch: um parâmetro para desligar o `toggleable` próprio quando a linha cuida dele (ou `clearAndSetSemantics {}`). |
| A-R4 | Grupos de seleção sem `selectableGroup()`: navegação inferior (Role.Tab), fileira de meses (Role.Tab), chips de categoria, data, prazo e tipo (Role.RadioButton). O TalkBack não anuncia a posição ("2 de 7") nem o limite do grupo. (BottomNav.kt:72; ExpensesScreens.kt:179; Chips.kt:41; Pickers.kt:198; GoalsScreens.kt:219; SimulateScreens.kt:197) | 1.3.1 | `Modifier.selectableGroup()` no `Row`, `LazyRow` ou `FlowRow` que contém as opções. |
| A-R5 | A pílula de mês lê o texto abreviado: "Set 2026" pode ser falado como a palavra inglesa "set", e "Mai" fica truncado. (ExpensesScreens.kt:186-207; Formatters.kt:83) | 2.4.6, 1.3.1 | No item: `semantics(mergeDescendants = true) { contentDescription = month.toMonthYear() }` (ex.: "setembro de 2026"). |
| A-R6 | A linha de despesa não é agrupada: são 5 paradas por item (título, categoria, "·" lido como "ponto", data, valor). A lista inteira vira um caminho longo para o TalkBack. (Rows.kt:112-137; também ContributionRow :149-165, AmountTile HomeScreen.kt:237-256, InfoLine e BreakdownLine) | 1.3.1, 2.4.3 | `Modifier.semantics(mergeDescendants = true) {}` na `Row` e `clearAndSetSemantics {}` no separador "·". |
| A-R7 | Não há títulos semânticos: títulos de tela ("Gastos", "Objetivos"…), seções ("Seus objetivos", "Gastos por categoria", "Histórico") e cabeçalhos de dia. A navegação por títulos do TalkBack não funciona. (Headers.kt TitleHeader/AuthHeader/ChevronTitleHeader; HomeScreen.kt:170-175, :321; GoalsScreens.kt:352; ExpensesScreens.kt:234) | 1.3.1, 2.4.6 | `Modifier.semantics { heading() }` nesses `Text`. Fazer nos componentes de header garante a maior parte de uma vez. |
| A-R8 | O slider de prazo informa `contentDescription` + `progressBarRangeInfo`, e o TalkBack fala a porcentagem ("19%"), que aqui não significa nada. A trilha tem 22dp de altura, o que torna arrastar difícil para quem tem pouca coordenação motora. (SimulateScreens.kt:211-234) | 4.1.2; 2.5.8 (AAA/2.2) | Trocar `contentDescription` por `stateDescription = termDescription` (o TalkBack passa a falar "24 meses"). Altura do toque com `heightIn(min = 48.dp)`, mantendo a trilha visual de 10dp. Os chips já oferecem uma alternativa sem arraste, o que é bom. |
| A-R9 | Contraste de texto abaixo de AA dentro da paleta: Green 2,54 (Resultado), TextSecondary/TealLight 4,26, TextSecondary/Soft 4,18 (placeholder do SoftField, Fields.kt:110, e "Novo objetivo" da Home). O Red como texto (3,55/3,76) é **decisão do dono**. | 1.4.3 | Ver D-R2, D-R9 e D-R11. O placeholder de SoftField sobre Soft (4,18) também é **decisão do dono**, porque o fundo Soft vem do Figma. O prejuízo é menor porque o rótulo fica acima do campo. |
| A-R10 | O skeleton da Home é mudo: o TalkBack não encontra nada para ler enquanto carrega. O `LoadingScreen` e o spinner de Gastos não têm rótulo. O "—" do banner é lido como "travessão". (StateScreens.kt:122; LoadingScreen.kt:19; ExpensesScreens.kt:144/160) | 4.1.3 | Container com `semantics { contentDescription = "Carregando"; liveRegion = Polite }`. No banner, usar `contentDescription = "Carregando"` em vez do "—". |
| A-R11 | Os indicadores de foco são o ripple padrão (overlay de cerca de 10% da cor do conteúdo). Sobre TealDark (chip, pílula ou mês selecionado) o foco por teclado ou Switch Access fica praticamente invisível. Os campos não têm foco visual (D-R6). | 2.4.7 Foco visível | Borda de foco em Teal ou TextPrimary via `interactionSource` nos componentes base (SelectChip, SobraButton, WhiteField, SoftField). |
| A-R12 | O sino sem ação é anunciado como "Notificações, botão" (D-R5). | 4.1.2 | Esconder o sino. |
| A-R13 | O ErrorScreen anuncia a aba errada como selecionada (D-R3). | 1.3.1, 2.4.8 | Ver D-R3. |

## Detalhes opcionais
| # | Sugestão | Motivo |
|---|---|---|
| A-N1 | Shimmer infinito (StateScreens.kt:90-108): conferir no aparelho com "Remover animações" ligado. | Não confirmei pela leitura se a `InfiniteTransition` para sozinha quando a escala de animação é 0. Se não parar, ler `Settings.Global.ANIMATOR_DURATION_SCALE` e deixar o gradiente parado. Não pisca (não há risco de convulsão). |
| A-N2 | O gráfico do Resultado (Canvas) não tem semântica. | Os números estão no card de composição, então não há perda de informação. Um `contentDescription` resumido ("Com rendimento chega a R$ X; sem rendimento, R$ Y") ajudaria. A série "sem rendimento" tem contraste de 1,7:1, cor vinda do Figma (decisão do dono), mas a legenda e os números textuais compensam. |
| A-N3 | O "Mostrar/Ocultar" da senha é anunciado sem contexto. | `contentDescription` "Mostrar senha" / "Ocultar senha" (AuthScreens.kt:128-133, 230-235). |
| A-N4 | O `MoneyTextField` força o cursor no fim a cada recomposição (Fields.kt:233). | Os gestos de mover o cursor do TalkBack não funcionam. É aceitável para um campo de centavos, mas vale saber. |
| A-N5 | Diálogos sem `paneTitle`. | `semantics { paneTitle = title }` no card do `PickerDialog`, para o TalkBack anunciar a abertura com o título. |

## Áreas de toque
- O comentário em Headers.kt:34 está **correto**. O Compose estende o toque de qualquer `pointerInput`/`clickable` menor que 48dp (hit-test "near" com `minimumTouchSize` do `ViewConfiguration`) sem mudar o layout. Por isso o fechar de 26dp (que ainda tem um Box de 48), o sino de 36dp, o "+" de 34dp, o chevron de 24dp e os chips de 44dp funcionam ao toque.
- **Ressalva:** o Accessibility Scanner vai acusar os limites semânticos menores que 48dp, e o retângulo de foco do TalkBack fica pequeno. Não é problema funcional.

## Navegação por teclado / TalkBack (percurso)
| Passo | Ação | Foco esperado | Anúncio esperado | Código atende? |
|---|---|---|---|---|
| 1 | Login: deslizar | "Voltar" → título → descrição → campo E-mail | "Voltar, botão"; "E-mail"; "Caixa de edição, seu@email.com" | Parcial: rótulo e campo são nós separados (A-R1) |
| 2 | Tecla "Próximo" do teclado | Campo Senha | "Caixa de edição, senha" | Sim (ação padrão do ImeAction.Next) |
| 3 | Entrar com a senha errada | Fica no botão; o erro é anunciado | "Credenciais inválidas" | **Não** (A-C2) |
| 4 | Home: navegação por títulos | "Seus objetivos", "Gastos por categoria" | "…, título" | **Não** (A-R7) |
| 5 | Carrossel de objetivos | Cada card | "Viagem, 56%, botão" | Sim (o `clickable` agrupa) |
| 6 | Sino | Botão | "Notificações, botão" → nada acontece | **Não** (A-R12) |
| 7 | Navegação inferior | 5 abas | "Gastos, guia, 2 de 5, não selecionado" | Parcial: sem `selectableGroup` não diz "2 de 5" (A-R4) |
| 8 | Gastos: fileira de meses | Pílula | "setembro de 2026, selecionado" | Parcial: lê "Set 2026" (A-R5) |
| 9 | Lista de despesas | Uma linha por item | "Mercado, Alimentação, 18/09, R$ 120,00" | **Não**: 5 paradas por item (A-R6) |
| 10 | FAB "+" | FAB | "Adicionar despesa, botão" | Sim |
| 11 | Nova despesa: Valor | Campo | "Valor, caixa de edição, 0,00" | **Não**: sem rótulo (A-R1) |
| 12 | Chips de categoria | Chip | "Alimentação, botão de opção, selecionado, 2 de 7" | Parcial: sem posição (A-R4) |
| 13 | "Outra data" | Abre o DatePicker do Material | Grade de dias acessível | Sim |
| 14 | Salvar com erro da API | Erro anunciado | Mensagem | **Não** (A-C2) |
| 15 | Confirmação (diálogo) | O foco entra na janela; o Voltar fecha | "Despesa salva…; OK, botão" | Sim |
| 16 | Detalhe → "Adicionar aporte" | O foco vai para o diálogo | Título do diálogo | Parcial: sem `paneTitle` (A-N5); não rola (A-R2) |
| 17 | Simular: slider de prazo | Slider ajustável | "24 meses, ajustável" | Parcial: fala porcentagem (A-R8) |
| 18 | Perfil: Notificações | Uma parada | "Notificações, interruptor, ativado" | **Não**: duas paradas (A-R3) |
| 19 | Sair da conta | Abre o diálogo de confirmação | "Sair da conta?…" | Sim |
| 20 | Teclado físico / Switch Access em chip selecionado | Foco visível | — | **Fraco** (A-R11) |

## Checklist de semântica (equivalente ao ARIA)
| Elemento | Papel | Rótulo | Estados | Teclado/gesto | Observações |
|---|---|---|---|---|---|
| SobraButton | Role.Button | Texto | — | Sim | Sem desabilitado |
| TextLink / BackLink | Role.Button | Texto | — | Sim | "Mostrar" sem contexto |
| CircleIconButton / Fab / chevron | Role.Button | `contentDescription` | — | Sim | OK |
| SelectChip / TypeTile | Role.RadioButton (`selectable`) | Texto | selected | Sim | Falta `selectableGroup` |
| NavItem / pílula de mês | Role.Tab | Texto | selected | Sim | Falta grupo; mês abreviado |
| SobraSwitch | Role.Switch | **Sem rótulo** | on/off | Sim | Duplicado com a linha |
| SettingsRow | Role.Button | Texto | — | Sim | OK, exceto a de notificações |
| SoftField / WhiteField / MoneyTextField | Campo editável | **Não associado** | — | IME OK | A-R1 |
| Slider de prazo | Range + setProgress | `contentDescription` | valor | Gesto e volume | Usar `stateDescription` |
| ProgressTrack / ProgressRing | Nenhum | O % ao lado é texto | — | — | OK (decorativo + texto) |
| ErrorText | Texto | — | — | — | **Sem liveRegion** |
| Diálogos | Janela | Título visual | — | O Voltar fecha | Sem `paneTitle`; sem rolagem |
| Imagens (avatar, thumbs) | — | `null` (decorativas) | — | — | Correto |

## Detalhe do veredito
- **Compatibilidade com leitor de tela: CONCERNS.** Faltam erros anunciados, rótulos de campo, títulos e agrupamento de linhas.
- **Navegação por teclado / Switch Access: CONCERNS.** Tudo é focável e acionável, mas o foco é pouco visível.
- **Gerenciamento de foco: PASS com ressalvas.** Os diálogos recebem foco e o Voltar fecha. Falta rolar ou anunciar o erro.
- **Contraste: CONCERNS.** O ganho com TealDark é real: texto em TealDark e branco sobre TealDark (5,47:1) passam. Ainda falham Green, TextSecondary sobre TealLight e sobre Soft, e Red (este, decisão do dono).
- **Movimento: PASS / N/A.** Só tema claro, sem animações relevantes além do shimmer (confirmar A-N1).
- **Texto grande (fontScale): CONCERNS → crítico no WhiteField** (A-C1) e alto nos demais contêineres de altura fixa (D-R1).

---

## O que corrigir primeiro
1. **Fields.kt:140/143:** `heightIn` em vez de `height` e sem padding vertical. Resolve D-C1/A-C1.
2. **Pickers.kt:257:** `liveRegion` no `ErrorText`, e apagar o `FormError`. Resolve A-C2 e D-R8.
3. **SimulateScreens.kt:396:** Green → TealDark.
4. **Rolagem nos diálogos** (Pickers.kt:49).
5. **Interruptor do Perfil.**
6. **`selectableGroup` e `heading()`** nos componentes base.
7. **ErrorScreen** com a aba certa e sem o "Voltar" que recarrega.

**Decisões do dono (paleta fixa):**
- Red como cor de texto (erros, "Sair da conta").
- Placeholder TextSecondary sobre o fundo Soft (4,18:1).
- Série "sem rendimento" em 1,7:1.

---

## Lentes 2 e 8 — UX Engineer e DX Engineer

# Auditoria UX + DX do app Sobra (Android/Compose + API Spring)

Branch `claude/youthful-darwin-9ml80l`, comparada com `main`. Só li o código: nenhum arquivo foi criado ou editado. As skills mandam gravar em `reviews/*.md`, mas a sua regra de não escrever no repositório prevalece, então o relatório vai inteiro aqui. Não compilei nem rodei nada, porque não há Android SDK.

**Vereditos:** UX = **APPROVE WITH CHANGES**. DX = **APPROVE WITH CHANGES**.

O fluxo principal está bem feito: estados de carregando, erro e vazio, ViewModels, mensagens em pt-BR e confirmação ao sair. Há um ponto em que a pessoa fica presa para valer (configuração inicial incompleta), alguns botões "Voltar" que levam a lugar nenhum, e falhas de DX que vão atrapalhar quem testar app e back end juntos.

---

# LENTE 1 — UX Engineer

## Resumo
Esta branch liga as 24 telas à API real. Mudam para a pessoa: login e cadastro de verdade, configuração em dois passos, Home com skeleton e erro, despesas por mês, objetivos com aportes, simulação calculada no servidor e salva, perfil com saída confirmada, e a tela de sessão expirada disparada pela API.

## Mapa da jornada do usuário
(OK = claro · WARN = ambíguo · FAIL = falta ou trava)

**Primeiro acesso**
1. Splash (1,2 s): sem token vai ao onboarding, com token vai à Home (`SobraNavHost.kt:176`). **OK**, mas veja o C1.
2. Onboarding com 3 páginas, "Pular" e "Próximo" vão ao Login. **OK**
3. Login → "Criar conta" → Cadastro. Validação local, botão "Criando conta…", erro do servidor abaixo do formulário. **OK**
4. Cadastro → Renda (`:246`, `nav.navigate` sem limpar a pilha). **WARN**: o "Voltar" da Renda volta ao Cadastro de uma conta que já existe (C3).
5. Renda: zero mostra "Informe sua renda mensal.", o botão vira "Salvando…" e segue para Categorias. **OK**
6. Categorias: toque abre diálogo de valor, "Concluir" vai à Home limpando a pilha. **OK**
7. Fechar o app no passo 5 ou 6 e reabrir: Splash vê o token e vai à Home com Renda R$ 0,00, sem caminho para terminar a configuração. **FAIL** (C1)

**Registrar despesa**
8. Gastos → "+" → Nova despesa. Valor, descrição, categoria e data; o calendário bloqueia datas futuras. **OK**
9. Salvar mostra "Salvando…", volta à Home e abre o modal "Gasto adicionado!". **WARN**: quem veio da aba Gastos cai na Home (R2).
10. A Home se atualiza em silêncio. **WARN**: se essa atualização falhar, a Home mostra os números antigos sem aviso, enquanto o modal afirma que o gasto "já aparece" (R4).

**Objetivo e aporte**
11. Objetivos vazio: estado vazio com "Criar objetivo". **OK**
12. Novo objetivo: validações locais, "Salvando…", depois volta. **WARN**: não há confirmação, ao contrário da despesa (consistência).
13. Detalhe → "Adicionar aporte" abre um diálogo, o anel e o histórico se atualizam. **WARN**: o botão não mostra "Salvando…" (R7).

**Simular e salvar**
14. Simular mostra uma prévia ao vivo; "Simular" vira "Calculando…" e abre o Resultado. **OK**
15. Resultado → "Salvar" → Simulações salvas. **WARN**: voltar devolve ao Resultado com "Salvar" ativo, e a pessoa pode duplicar a simulação (R5).
16. Salvas → tocar numa simulação recalcula. **FAIL** se der erro: nada aparece nesta tela (R3).

**Perfil e sair**
17. Perfil → "Sair da conta" pede confirmação ("Continuar conectado" é a ação principal) e vai ao Login com a pilha limpa. **OK**
18. No Login que aparece depois de sair, o link "Voltar" fica visível, mas não há tela anterior. **FAIL** (C2)
19. Perfil → Segurança → "Trocar senha" abre a tela 07. **WARN**: o texto é de quem esqueceu a senha, há um botão "Voltar ao login" que leva ao Perfil, e o e-mail não vem preenchido (R8).

**Sessão expirada**
20. A API responde 401 SESSAO_EXPIRADA ou NAO_AUTENTICADO, o app abre a tela 24 com a pilha limpa, e o botão leva ao Login. **OK**
21. Caso raro: sair com um token que o servidor não conhece leva à tela "Sessão expirada" logo depois de a pessoa ter saído por vontade própria (R9).

**Servidor fora do ar**
22. Abas sem dados em cache mostram ErrorScreen com "Tentar novamente". **OK**
23. Abas que já tinham dados continuam mostrando o que havia, sem aviso (R4).
24. Formulários mostram "Não foi possível conectar ao servidor. Verifique a conexão." **OK**

## Problemas críticos (corrigir)

| # | Problema | Passo | Impacto | Correção |
|---|---|---|---|---|
| C1 | A Splash decide só pela existência do token (`SobraNavHost.kt:176`). Quem criou a conta e fechou o app na Renda ou nas Categorias entra direto na Home com Renda R$ 0,00 (`HomeRepository.kt:76`). Não há entrada para a configuração no Perfil nem na Home. A única saída é sair e entrar de novo, porque o login devolve `configuracao_concluida` (`:218`), e ninguém vai descobrir isso. | 7 | O número central do app ("quanto sobra") fica errado para sempre, sem explicação. | Na Splash, com token, chamar `GET /configuracao`, que já existe e o app não usa (`ConfiguracaoController.java:29`). Com `concluida=false`, ir para INCOME limpando a pilha. Se a rede falhar, ir à Home como hoje. Além disso, quando `renda` vier nula, a Home deveria mostrar "Informe sua renda" com link para a tela 08. |
| C2 | `navigateClearingStack` (`:134-138`) deixa o Login, ou a Renda depois de um login sem configuração (`:218`), como única tela da pilha. O link "Voltar" continua visível (`AuthHeader` em `Headers.kt:91`) e chama `nav.popBackStack()` (`:212`, `:275`). Sem tela anterior, o toque esvazia a pilha: a tela fica em branco ou, no mínimo, o botão não faz nada útil. | 18 (também depois de "Sessão expirada" e em 4/5) | Beco sem saída: a pessoa precisa fechar o app. | Quando `nav.previousBackStackEntry == null`, esconder o BackLink, ou fazer o Voltar do Login ir ao onboarding e o da Renda pedir confirmação para sair da conta. |
| C3 | O cadastro navega para a Renda sem limpar a pilha (`:246`). "Voltar" ou o voltar do sistema na Renda devolve ao Cadastro com nome e e-mail preenchidos. Tocar em "Criar conta" de novo dá "Este e-mail já está cadastrado." | 4 | A pessoa acha que o cadastro falhou e não entende o motivo. | `nav.navigateClearingStack(Routes.INCOME)` depois do cadastro, como no login. Junto com o C2, o Voltar da Renda some quando ela for a raiz. |

## Recomendações

| # | Problema | Impacto | Correção |
|---|---|---|---|
| R1 | Não há como alterar renda ou categorias depois da configuração. A API aceita (`PUT /configuracao/renda` substitui o valor no mês), mas o app não oferece entrada. | Num app de finanças, um erro de digitação na renda ou um aumento salarial não pode ser corrigido. | Linha "Renda e categorias" no Perfil, reaproveitando as telas 08 e 09 com `GET /configuracao` para preencher os campos. |
| R2 | Depois de salvar uma despesa, `popBackStack(Routes.HOME)` (`:365`) tira a aba Gastos da pilha. Quem veio de Gastos cai na Home, e o mês escolhido em Gastos se perde. | Quebra o modelo mental ("salvei, quero ver na lista"). | Voltar à tela de origem (`popBackStack()`) e abrir o modal por cima dela. O texto do modal ("já aparece nos seus gastos") continua valendo. |
| R3 | Simulações salvas → toque chama `simulation.calculate` (`:482-485`). Se der erro, `error` é gravado no ViewModel compartilhado (`SimulationViewModel.kt:55`), mas esta tela não mostra erro nem "carregando". A mensagem aparece depois, fora de contexto, na aba Simular. Além disso, `prefill` sobrescreve sem aviso o que a pessoa tinha digitado em Simular. | O toque parece não fazer nada, e o erro surge na hora errada. | Indicador de carregando no item e erro nesta tela. Limpar `error` ao sair. |
| R4 | `refresh()` com `keepOnError=true` (`HomeViewModel.kt:35,48`, também em Expenses e Goals) descarta a falha em silêncio. | Dados velhos aparecem como atuais. O passo 6 do roteiro ("Parar o back end e abrir a Home…") só funciona se o app for reaberto. | Manter os dados, mas mostrar um aviso discreto ("Não foi possível atualizar · Tentar de novo"). |
| R5 | Resultado → Salvar → Salvas (`:466`). Voltar mostra o mesmo Resultado com "Salvar" ativo, e um segundo toque cria uma duplicata. | Lista poluída com simulações repetidas. | Marcar o resultado como salvo no ViewModel e trocar o botão por "Salva" desativado, ou usar `popUpTo(SIMULATE)` ao abrir Salvas. |
| R6 | ErrorScreen nas abas raiz usa `onBack = vm::load` (`:312`, `:336`, `:382`, `:499`). A seta "Voltar" recarrega em vez de voltar. | Ação diferente do rótulo. | Esconder a seta nas raízes (a barra inferior já serve de navegação). |
| R7 | `AddContributionDialog` (`Pickers.kt:211`) não troca o texto para "Salvando…" e ignora toques repetidos sem aviso. Em Nova despesa, Novo objetivo, Simular e aporte, o erro não some quando a pessoa edita, ao contrário de Login e Renda. | Feedback inconsistente; o erro antigo fica na tela depois da correção. | Usar `if (saving) action_saving` e limpar `error` nos `change*` dos ViewModels. |
| R8 | Perfil → Segurança reaproveita a tela 07 (`:534`). Ali estão "Voltar ao login" (`:265`, que leva ao Perfil), o texto "Insira seu e-mail cadastrado…" e o campo vazio, mesmo com o e-mail conhecido. O texto de `profile_security_body` (`strings.xml:294`) diz que a troca "encerra as sessões abertas em outros aparelhos", mas `redefinirSenha` revoga **todas**, inclusive a atual (`AuthService.java:183`). | A pessoa não sabe para onde volta e depois é derrubada sem esperar. | Passar o e-mail do perfil e um modo "trocar senha" sem o link de login. Corrigir o texto para "encerra todas as sessões, inclusive esta". |
| R9 | O logout manda o token antigo ao servidor (`AuthRepository.kt:41`). Se o servidor não conhece o token (banco recriado em dev), a resposta NAO_AUTENTICADO dispara `onSessionExpired` (`ApiClient.kt:57`) e abre "Sessão expirada" por cima do Login. | Raro, mas confuso: a pessoa acabou de sair. | No logout, não acionar `onSessionExpired`, por exemplo com um parâmetro `notifyExpiry=false` em `call`. |
| R10 | O interruptor de Notificações volta à posição anterior sem dizer nada quando a API recusa (`ProfileViewModel.kt:35-39`). | A pessoa não entende por que o interruptor "pulou". | Snackbar curto: "Não foi possível salvar. Tente de novo." |

## Desejáveis

| # | Sugestão | Motivo |
|---|---|---|
| N1 | Confirmação leve (snackbar) ao criar objetivo e ao salvar simulação | Hoje só a despesa tem confirmação. |
| N2 | Rotas mortas `GOALS_EMPTY`, `HOME_SKELETON`, `ERROR` (`:571-592`) e os TODOs "TODO(back end): navegar para cá" (`:581`, `:585`, `:601`), já resolvidos | Confundem quem lê o código e o roteiro. |
| N3 | Os Termos dizem "Você pode pedir a exclusão da conta a qualquer momento" (`strings.xml:298`), mas não dizem como | É uma promessa sem caminho. |
| N4 | O diálogo de aporte guarda o estado com `remember` (`Pickers.kt:180-183`): girar a tela apaga o valor digitado | Use `rememberSaveable`. |

## Checagem de consistência de interação
- **Carregando**: Home usa skeleton; Objetivos, Perfil e Salvas usam LoadingScreen com barra inferior; Detalhe usa LoadingScreen sem barra; Gastos usa indicador inline. Dá para defender, mas são quatro padrões diferentes.
- **Envio**: os botões trocam o texto ("Entrando…", "Salvando…", "Calculando…") e o ViewModel bloqueia toques repetidos. Duas exceções: o diálogo de aporte e o toque em simulação salva.
- **Erro em formulário**: o texto vermelho abaixo do formulário é consistente. Ele some ao editar só em Login, Cadastro, Recuperar e Renda.
- **Confirmação**: só a despesa tem modal de sucesso. A saída da conta tem confirmação com a ação segura como principal, o que está certo.
- **Voltar**: em detalhe e subtelas, volta de fato. Na ErrorScreen das abas, recarrega (R6). No Login e na Renda como raiz, não leva a lugar nenhum (C2).
- **Ações irreversíveis**: não existem (a API não permite editar nem apagar), então não há risco de perda acidental. A contrapartida é que um erro de digitação numa despesa é permanente, sem nenhum aviso antes de salvar. Vale uma linha no formulário ("Despesas não podem ser editadas depois") até a D-11 mudar.

## Detalhes do veredito (UX)
- Descoberta: PASS
- Fluxo completo: CONCERNS (C1, C2, C3, R1)
- Recuperação de erro: CONCERNS (R3, R4, R9)
- Feedback e status: CONCERNS (R4, R7, R10)
- Consistência: CONCERNS (leves)

---

# LENTE 2 — DX Engineer
O "desenvolvedor" aqui é o dono do projeto (Java intermediário aprendendo Spring Boot) ou quem for rodar app e back end juntos.

## Resumo
Rodar tudo deve funcionar com três comandos e o emulador. O README, o `exemplos.http`, o `/saude` e os logs JSON com `id_requisicao` são bons. Os atritos aparecem no limite de login em dev, no teste da recuperação de senha, nos pré-requisitos que faltam e no app, que não registra nada no Logcat.

## Arquivos revisados

| Arquivo | Papel para o DX | Risco |
|---|---|---|
| `README.md`, `backend/README.md`, `docs/roteiro-de-teste.md` | Como rodar e testar | Médio |
| `backend/src/main/resources/application.yml` | Padrões de dev, limites | Médio |
| `backend/docker-compose.yml` | Banco | Baixo |
| `app/.../data/ApiConfig.kt`, `app/src/debug/res/xml/network_security_config.xml` | Endereço da API, cleartext | Médio |
| `app/.../data/remote/ApiClient.kt`, `ApiException.kt`, `ui/viewmodel/Load.kt` | Erros que chegam ao app | Médio |
| `backend/.../controller/TratadorErros.java`, `seguranca/RequisicaoFilter.java`, `service/EnvioEmail.java` | Logs e erros da API | Médio |

## Problemas críticos
Nenhum bloqueia rodar o projeto. Os itens abaixo são recomendações de prioridade alta.

## Recomendações

| # | Problema | Por quê | Correção |
|---|---|---|---|
| D1 | O limite de login é de **5 por IP a cada 15 min, contando também os logins certos** (`application.yml:64-66`, `AuthService.java:105`). Todo o tráfego do emulador chega do mesmo IP. O roteiro já faz login errado, login certo, sair e entrar; somando o contorno do C1, a pessoa chega ao 429 "Muitas tentativas. Aguarde e tente novamente." sem saber quanto esperar. O limite fica em memória, então reiniciar a API zera o contador, mas isso não está escrito em lugar nenhum. | O testador acha que o app quebrou no meio da apresentação. | No perfil `dev`, relaxar `login-por-ip` como o perfil `test` já faz (`:97-99`). Documentar no README que "reiniciar a API zera os limites". No app, ler `Retry-After` e mostrar "Tente de novo em N min". |
| D2 | Em dev, a recuperação de senha não pode ser testada de ponta a ponta. `EnvioEmail` registra só o assunto (`EnvioEmail.java:39`), e o link com o token (`AuthService.java:152`) não aparece em lugar nenhum. O README diz "o e-mail só é registrado no log", o que sugere que o link estaria lá. A página `redefinir-senha.html` fica inalcançável à mão. | Uma funcionalidade inteira (D-08) fica sem teste manual. | Só no perfil `dev`, logar o link completo (com um aviso de "apenas dev"), ou expor `GET /dev/emails`. Corrigir a frase do README. |
| D3 | O app não escreve nada no Logcat. `ApiClient` converte qualquer `IOException` (porta errada, conexão recusada, timeout de 15 s, cleartext bloqueado num celular físico) na mesma frase (`ApiClient.kt:59-60`). `loadResult` engole tudo sem registrar (`Load.kt:16-23`). O `X-Id-Requisicao` que a API devolve é ignorado. | O dev não distingue "API desligada", "IP errado" e "HTTP bloqueado", nem liga o erro do app à linha de log do servidor. | `Log.w("SobraApi", "$method $path -> $status $codigo id=$idRequisicao", e)` em `ApiClient`, só em debug (`BuildConfig.DEBUG`). Guardar `idRequisicao` em `ApiException`. |
| D4 | Faltam pré-requisitos no README raiz: JDK 21 (o `mvnw` falha com JDK antigo e uma mensagem difícil de entender), Docker em execução, `mvnw.cmd` no Windows, e o que fazer se a porta 5432 já estiver em uso por um Postgres local. O `docker compose up -d` devolve o controle antes de o Postgres aceitar conexões (o compose não tem `healthcheck`), então na primeira vez o `spring-boot:run` pode falhar com uma pilha longa de erros do Hikari e do Flyway. | O primeiro "não funciona" de quem está aprendendo Spring vem do ambiente, não do código. | Uma seção "Pré-requisitos" e uma seção "Problemas comuns" (porta ocupada, banco ainda subindo: espere alguns segundos ou use `docker compose up -d --wait` com healthcheck `pg_isready`). |
| D5 | Rodar num celular físico exige mudar dois lugares, `ApiConfig.kt:10` e `network_security_config.xml` (que só libera `10.0.2.2`). O README menciona só o emulador. | Quem tentar no próprio celular recebe "Não foi possível conectar" sem pista. | Mover a URL para `buildConfigField` ou `local.properties` e documentar os dois passos (IP da máquina e domínio no network config). |
| D6 | Erros 500 registram só a classe da exceção e o primeiro frame (`TratadorErros.java:121-124`, `RequisicaoFilter.java:62`). Para um NullPointerException dentro de um service, isso quase sempre aponta para dentro do JDK ou do Spring. | Quem está aprendendo Spring precisa da pilha completa para depurar. | No perfil `dev`, `LOG.error(..., e)` com a pilha completa. Em prod, manter como está (RNF-08). |
| D7 | A taxa da simulação está repetida em três lugares: `application.yml:50` (servidor), `Finance.kt:17` (prévia) e `strings.xml:173`, que aparece até como "Taxa usada" no resultado (`SimulateScreens.kt:398`). A API já grava e devolve a taxa (`taxa_mensal`). | Mudar `sobra.taxa-simulacao`, como o README sugere (D-01), deixa a prévia e o texto "Taxa usada" errados sem nenhum aviso. | Mostrar no Resultado a taxa vinda da API e anotar no README e no yml que `Finance.kt` também precisa mudar. Melhor ainda: a API devolver a taxa num GET. |
| D8 | As mensagens de erro do app estão escritas direto nos ViewModels (`AuthViewModel.kt`, `NewExpenseViewModel.kt`, `SimulationViewModel.kt:55`, `Load.kt:33`) e não em `strings.xml`. | Inconsistente com o resto das strings e mais difícil de revisar ou traduzir. | Passar para `strings.xml` (o ViewModel expõe um `@StringRes` ou um código). |
| D9 | No roteiro: (a) o item 17 aceita duas mensagens, mas o app sempre mostra a local ("As senhas não são iguais."); (b) o item da sessão expirada (`:69`) dá o SQL sem dizer onde rodar; (c) o item do servidor fora do ar (`:68`) só funciona com o app recém-aberto (R4); (d) não avisa do limite de 5 logins (D1). | O testador marca falha onde não há. | (a) Deixar só a mensagem local. (b) `docker compose exec banco psql -U sobra -d sobra -c "UPDATE sessao SET expira_em = now() - interval '1 day';"`. (c) "Feche o app, pare o back end e reabra". (d) Uma nota sobre o 429. |

## Desejáveis

| # | Sugestão | Motivo |
|---|---|---|
| DN1 | `README.md` raiz: um link para `backend/docs/exemplos.http` e o atalho do usuário Lucas (`token-dev-lucas`) | Permite ver dados prontos no app sem cadastrar uma conta. |
| DN2 | Atualizar os TODOs "TODO(back end)" já resolvidos em `SobraNavHost.kt` (N2) | Um TODO velho engana quem está aprendendo. |
| DN3 | `runSubmit` (`Load.kt:29-34`) não captura `NumberFormatException` nem `DateTimeParseException`, ao contrário de `loadResult`. Hoje os submits quase não fazem parse da resposta, mas um futuro parse de dinheiro numa resposta de POST derrubaria o app. | Deixa os dois helpers simétricos. |

## Auditoria de mensagens de erro (usuário final e desenvolvedor)

| Cenário | Mensagem atual | O que a pessoa precisa saber | Mensagem sugerida |
|---|---|---|---|
| API desligada ou IP errado (formulários) | "Não foi possível conectar ao servidor. Verifique a conexão." (`ApiClient.kt:60`) | Está bom para o usuário. O dev precisa da causa no Logcat (D3). | Manter, e acrescentar log em debug. |
| API desligada (telas com dados) | "Algo deu errado / Não foi possível carregar os dados. Verifique sua conexão e tente novamente." | Está bom. Mas o mesmo texto aparece para 404 (objetivo inexistente) e 500, em que "conexão" está errado. | Separar por `isNetwork`: rede vs. "O servidor teve um problema. Tente de novo em instantes." |
| Timeout de 15 s | O mesmo de "sem conexão", depois de 15 s mostrando só o texto do botão | Que o servidor está lento, não fora do ar. | "O servidor demorou para responder. Tente novamente." (capturar `SocketTimeoutException` antes de `IOException`) |
| 429 no login | "Muitas tentativas. Aguarde e tente novamente." | Quanto tempo esperar. | "Muitas tentativas. Tente de novo em %d min." (usar `Retry-After`) |
| 422 de campo sem nome do campo (`ApiException.kt:23` usa só a primeira mensagem) | "Campo obrigatório." / "Use de 2 a 150 caracteres." (nome com 1 letra no cadastro, `AuthService.java:64`) | Qual campo está errado. | Incluir o rótulo: "Nome completo: use de 2 a 150 caracteres." (mapear `campo` para o rótulo no app, ou o servidor já mandar a frase completa). |
| Valor acima do máximo. O campo aceita 11 dígitos, ou seja até 999.999.999,99 (`Formatters.kt:23`); a API aceita até 99.999.999,99. | "O valor máximo é 99999999.99." | O limite no formato brasileiro, ou o campo nem deixar passar. | Limitar a 10 dígitos no app. Na API: "O valor máximo é R$ 99.999.999,99." |
| Formato técnico que vaza para o usuário | "Use o formato 1234.56." / "Use o formato AAAA-MM-DD." / "Use true ou false." | O usuário nunca digita nesses formatos; se aparecer, é bug do app. | O app trata `FORMATO_INVALIDO` e `DATA_INVALIDA` como erro interno: "Não foi possível salvar. Tente novamente." e registra no log. |
| E-mail já cadastrado | "Este e-mail já está cadastrado." | O que fazer em seguida. | "Este e-mail já tem conta. Faça login ou recupere a senha." |
| Simulação recusada (ex.: prazo fora de 1–120) | "Não foi possível calcular a simulação. Verifique os valores e a conexão." (`SimulationViewModel.kt:55`, que descarta a mensagem da API) | Qual dos dois aconteceu. | Usar `displayMessage` quando houver `ApiException` com campos; a frase genérica fica só para rede. |
| Simulação salva que falha ao recalcular | (nenhuma mensagem na tela; aparece depois em Simular) | Feedback imediato. | Erro em linha em Salvas (R3). |
| 500 | "Ocorreu um erro inesperado. Tente novamente." | Para o dev: o id para achar no log. | Manter para o usuário; em debug, registrar `X-Id-Requisicao` no Logcat. |
| Resposta fora do formato | "Resposta inesperada do servidor." | Para o dev: que a URL pode estar apontando para outro serviço. | Manter, e registrar status e início do corpo em debug. |
| Notificações recusadas | (nenhuma; o interruptor volta sozinho) | Que não foi salvo. | "Não foi possível salvar a preferência." (R10) |
| Recuperação de senha em dev | "Se o e-mail estiver cadastrado, você receberá as instruções em instantes." | Para o testador: que em dev nada chega e onde achar o link. | Manter no app; resolver no back end (D2). |
| Sessão expirada | Tela 24: "Por segurança, sua sessão foi encerrada…" | Está claro. | — |
| Senhas diferentes (local) | "As senhas não são iguais." | Está claro. | — |

## Jornada do desenvolvedor
- **Primeira vez**: clona o projeto, roda `docker compose up -d` e `./mvnw spring-boot:run`, depois o app no emulador. Pontos de atrito: JDK 21 não citado, porta 5432 ocupada, banco ainda subindo (D4). O `curl /saude` é um ótimo teste de fumaça. No app, cadastrar e configurar funciona.
- **Uso diário (roteiro de teste)**: o atrito aparece no 6º login em 15 minutos (D1), na recuperação de senha que não dá para testar (D2) e no item "servidor fora do ar", que depende de reabrir o app (D9c). Quando algo falha, o servidor tem bons logs (método, rota, status, duração e id), mas o app não registra nada, e o dev não consegue ligar os dois lados (D3).
- **Uso avançado**: o `exemplos.http`, o token do Lucas, o `verificar_restricoes.sql` e os testes com relógio fixo são muito bons para aprender Spring. Mudar a taxa ou rodar num celular físico exige caçar trechos espalhados (D5, D7), e um 500 sem a pilha completa trava o aprendizado (D6).

## Detalhes do veredito (DX)
- Qualidade dos erros: CONCERNS (campo sem nome, formato técnico, 429 sem tempo, app sem log)
- Padrões e configuração: CONCERNS (limite de login em dev, URL fixa no código, taxa repetida)
- Teclado e IME: PASS (ImeAction Next/Done e envio pelo teclado nos formulários de autenticação; não revisei os demais em profundidade)
- Descoberta: PASS (README claro, `/saude`, `exemplos.http`)
- Integração com o fluxo de trabalho: CONCERNS (falta seção de problemas comuns; o roteiro tem alguns passos imprecisos)

---

## Prioridade sugerida
C1 → C3 → C2 → D1 → D3 → R1 → R3/R4/R5 → D2 → o restante.

Os principais arquivos a mexer são `app/src/main/java/com/unasp/sobra/navigation/SobraNavHost.kt`, `app/src/main/java/com/unasp/sobra/data/remote/ApiClient.kt`, `backend/src/main/resources/application.yml` e `docs/roteiro-de-teste.md`.

---

## Lentes 3 e 9 — Frontend Engineer (Android/Compose) e Performance Engineer

# Auditoria do app Android (branch `claude/youthful-darwin-9ml80l`, `git diff main...HEAD -- app/ gradle/`)

Fiz só leitura e não alterei nada no repositório. Não consegui compilar, porque não há Android SDK. Por isso os erros de compilação abaixo têm grau de certeza explícito.

**Resumo em 4 linhas:**
- Há **1 erro de compilação quase certo**: falta um import em `Pickers.kt`.
- Há **3 bugs de comportamento reais**:
  - dados de uma conta reaparecem depois de trocar de conta;
  - a seta "voltar" leva a uma tela em branco;
  - uma resposta que chega durante a rotação faz a navegação se perder e pode criar registros duplicados.
- O resto é dívida pequena.
- Desempenho está adequado para o tamanho do app.

---

## 0. Erros de compilação prováveis (listados à parte)

| # | Arquivo:linha | Problema | Certeza | Correção |
|---|---|---|---|---|
| C1 | `ui/components/Pickers.kt:230` (`TypeTile`: `.height(TileHeight)`) | `androidx.compose.foundation.layout.height` **não está importado**. Os imports do arquivo têm só `Arrangement, Column, Row, fillMaxWidth, padding`, e não há import com `*` nem uma `Modifier.height` própria no pacote. O resultado é *Unresolved reference: height*. | **Alta (~95%)** | Adicionar `import androidx.compose.foundation.layout.height`. |
| C2 | `ui/screens/simulate/SimulateScreens.kt:447` | Não é erro de compilação, mas é **crash em runtime**: `result.seriesWithYield.first()` lança `NoSuchElementException` se a API devolver `serie: []`. | Média | `firstOrNull() ?: return`, ou validar no `SimulationRepository`. |

Verifiquei e não encontrei problemas no resto:
- **APIs:** `LocalActivity` (activity-compose 1.13) existe. Também existem `ContentType.*` e `Modifier.semantics { contentType = … }` (ui 1.8 ou superior, BOM 2026.02), `KeyboardOptions(autoCorrectEnabled=)` (foundation 1.7 ou superior), `rememberDatePickerState(yearRange=, selectableDates=)`, `dialog(dialogProperties=)`, `ProgressBarRangeInfo(steps=)` e `setProgress`.
- **Construções de linguagem:** são válidos `return@composable` e `return@SobraScreen`, o `items(items, key=…)` com sombra de nome, o default param `token = tokenProvider()` e o `GoalDetailViewModel(SavedStateHandle)` com `viewModel()` (a factory padrão da entry suporta, e `NavType.LongType` entrega `Long`).
- **Recursos:** todos os `R.string`/`R.plurals` e `R.drawable` usados existem (cruzei uso contra definição).
- **Dependências:** `lifecycle-viewmodel-compose` usa a mesma versão de `lifecycle-runtime-ktx` e é lançado junto. minSdk 26 cobre `java.time`.

Há **avisos** sem impacto (imports não usados em `SimulateScreens.kt:3-13`, por exemplo `toBrlNumber`/`Role` duplicados). `floatingAction = if (isEmpty) null else { { … } }` em `GoalsScreens.kt:90` já existia na `main`, então ficou fora do escopo.

---

# LENTE 1 — Frontend Engineer (Compose/ViewModel/Kotlin)

## Resumo do que mudou
O app deixou de usar `DemoData` e passou a usar uma API real:
- `ApiClient` (HttpURLConnection + org.json em `Dispatchers.IO`);
- `SessionStore` (SharedPreferences + `SharedFlow` de expiração);
- `Graph` (DI manual);
- 11 ViewModels com `mutableStateOf` e `Load<T>`;
- NavHost com escopos mistos: Activity para `SimulationViewModel`/`SetupViewModel` e back stack entry para os demais.

## Arquivos revisados
| Arquivo | Papel | Risco |
|---|---|---|
| `data/remote/ApiClient.kt`, `ApiException.kt` | HTTP, erros | Médio |
| `data/SessionStore.kt`, `Graph.kt`, `AuthRepository.kt` | sessão/DI/logout | Médio |
| `data/*Repository.kt` | parsing JSON | Baixo-médio |
| `ui/viewmodel/*.kt`, `Load.kt` | estado, corrotinas | Médio |
| `navigation/SobraNavHost.kt` | rotas, escopos, back stack | **Alto** |
| `ui/components/Pickers.kt`, `Chips.kt`, `Fields.kt` | diálogos, chips | **Alto (C1)** |
| `ui/screens/**` | apresentação | Baixo |
| `build.gradle.kts`, manifests, `network_security_config.xml` | build/rede | Baixo-médio |

## Veredito: **APPROVE WITH CHANGES** (C1 + F1–F3 antes de merge)

## Problemas críticos (corrigir)

| # | Problema | Arquivo(s) | Por que quebra | Correção |
|---|---|---|---|---|
| F1 | **Estado de outra conta reaparece depois do logout/login**, porque a Navigation guarda as abas salvas. | `SobraNavHost.kt:123-131` (`popUpTo(HOME){saveState=true}` + `restoreState=true`), `:134-137` (`navigateClearingStack`), `:558-564` (logout), `ProfileViewModel.kt:23-26` | Ao sair de uma aba pela barra inferior, o NavController guarda a entry *e o ViewModelStore dela*. `popUpTo(graph.id){inclusive=true}` sem `saveState` **não apaga** esses estados salvos. Depois do login, tocar em Perfil/Gastos/Objetivos executa `restoreState` e devolve o ViewModel antigo. No Perfil, `refresh()` retorna cedo quando já é `Success`, então **o nome e o e-mail da conta anterior ficam na tela**. Em Gastos/Objetivos os dados antigos aparecem até a nova busca chegar, e permanecem se a rede falhar (`keepOnError`). | Ao sair da sessão (logout, sessão expirada e login): `listOf(Routes.EXPENSES, Routes.GOALS, Routes.PROFILE, Routes.SIMULATE).forEach { nav.clearBackStack(it) }` antes de navegar. Alternativa mais robusta: `activity.recreate()` / reiniciar o `NavHost` com `key(sessionId)`. Também chamar `simulation.reset()` no login (hoje só `setup.reset()`, em `:216`). |
| F2 | **Tela em branco ao tocar em "voltar"** quando Login ou Renda é a única entry da pilha. | `SobraNavHost.kt:212` (Login `onBack = popBackStack()`), `:275` (Renda), combinado com `navigateClearingStack` em `:218`, `:563` e `:603` | Depois de logout, sessão expirada ou login com setup pendente (`navigateClearingStack(INCOME)`), a tela fica sozinha na pilha. O `AuthHeader`/`BackLink` sempre aparece. `popBackStack()` então esvazia a pilha e o NavHost não desenha nada. | `onBack = { if (!nav.popBackStack()) activity?.finish() }`, ou esconder o BackLink quando `nav.previousBackStackEntry == null`. |
| F3 | **Callback `onDone` guardado no ViewModel captura um `NavController` velho** (rotação ou mudança de configuração no meio da requisição). | `AuthViewModel.kt:51-86`, `NewExpenseViewModel.kt:34`, `NewGoalViewModel.kt:30`, `SetupViewModel.kt:55`, `SimulationViewModel.kt:46/60`, `GoalDetailViewModel.kt:45`; uso em `SobraNavHost.kt:215, 244, 276, 291, 362, 404, 447, 466, 484` | `viewModelScope` sobrevive à rotação, mas `rememberNavController()` é recriado a partir do estado salvo. A lambda chama `navigate` no controller antigo, já desligado da UI, e a navegação some. Em **Nova despesa**, **Novo objetivo** e **Salvar simulação** o registro é criado no servidor, o usuário continua no formulário com o botão habilitado e, ao tocar de novo, **cria um duplicado**. | Trocar o callback por estado de evento no VM (ex.: `var saved by mutableStateOf(false)`) e, na tela, `LaunchedEffect(vm.saved) { if (vm.saved) { vm.consumeSaved(); nav.popBackStack() } }`. Outra opção é `Channel` + `collect` no composable. |

## Recomendações

| # | Problema | Arquivo(s) | Por quê | Correção |
|---|---|---|---|---|
| R1 | O comentário de `SharedFlow` está errado e o evento pode se perder. | `SessionStore.kt:14-15`, `SobraNavHost.kt:162-164` | Com `replay = 0` e **nenhum assinante**, `tryEmit` descarta o valor: `extraBufferCapacity` só vale quando há assinante. Durante a recriação da Activity, o 401 que chega nessa janela é perdido, e a tela atual mostra "Erro" em vez da tela de sessão expirada. | `MutableSharedFlow(replay = 1)` + `resetReplayCache()` depois de navegar, ou um `StateFlow<Boolean>`/`Channel(CONFLATED)` consumido com `receiveAsFlow()`. |
| R2 | Logout em segundo plano pode abrir a tela de sessão expirada por cima do Login. | `AuthRepository.kt:37-43` + `ApiClient.kt:57` | Se o token já tiver vencido no servidor, o `POST /auth/logout` responde `SESSAO_EXPIRADA`/`NAO_AUTENTICADO`. Isso dispara `onSessionExpired()`, que leva a `navigateClearingStack(SESSION_EXPIRED)` logo depois de a pessoa ter saído de propósito. | Parâmetro `notifyExpiry = false` em `call()` para o logout, ou uma chamada separada. |
| R3 | Várias emissões de expiração (`async` duplo da Home) navegam duas vezes. | `HomeViewModel.kt:42-46`, `SobraNavHost.kt:163` | Duas respostas 401 geram duas navegações (cintilação). | `launchSingleTop = true` em `navigateClearingStack`, ou um `distinctUntilChanged`/debounce no collector. |
| R4 | `loadResult` e `runSubmit` têm cobertura de exceções diferente. | `Load.kt:14-34` | `runSubmit` não captura `NumberFormatException`/`DateTimeParseException`. Hoje nenhuma gravação faz parse, mas `AuthViewModel.submit` faz (`saveSession`) e só captura `JSONException`. Uma mudança futura derruba o app sem aviso. | Centralizar: capturar `ApiException`, `JSONException` e `IllegalArgumentException` (`NumberFormatException` e `DateTimeParseException` são subclasses em ambos os casos). |
| R5 | `SimulationViewModel.calculate` descarta a mensagem de validação da API. | `SimulationViewModel.kt:51-56` | Usa `loadResult`, que perde o `displayMessage` do 422 e mostra texto genérico. | Capturar `ApiException` e usar `e.displayMessage`. |
| R6 | Depois do cadastro, "voltar" na tela Renda volta para o Cadastro. | `SobraNavHost.kt:246` | Tentar cadastrar de novo dá erro de e-mail repetido. | `navigateClearingStack(Routes.INCOME)` (com a correção de F2). |
| R7 | O formulário se perde com a morte do processo. | `NewExpenseViewModel`, `NewGoalViewModel`, `SetupViewModel`, `SimulationViewModel` | Esses campos não vão para `SavedStateHandle`. Em `SIMULATION_RESULT` há um fallback (`:457-459`); nos outros o formulário volta vazio. O diálogo `adding` (`:415`) e `editing` (`:283`) usam `remember`, então fecham na rotação. | Campos simples em `SavedStateHandle` (BigDecimal como String) e `rememberSaveable` nos flags de diálogo. Prioridade baixa. |
| R8 | Release fica sem rede. | `ApiConfig.kt:10` + `src/debug/res/xml/network_security_config.xml` | `http://10.0.2.2` está fixo no código e o cleartext só é liberado no debug. No release (targetSdk 37), toda chamada vira "Sem conexão". | `buildConfigField("String","BASE_URL",…)` por buildType (ativar `buildFeatures.buildConfig`) e HTTPS no release. |
| R9 | `ProfileViewModel.setNotifications` com toques rápidos pode reverter para o valor errado. | `ProfileViewModel.kt:29-41` | Cada toque captura um `current` diferente, e as reversões ficam fora de ordem. | Guardar um `Job` e cancelar o anterior, ou ignorar toques com uma flag `saving`. |
| R10 | O parsing tolera pouco. | `HomeRepository.kt:25`, `GoalsRepository.kt:46-47`, `ProfileRepository.kt:19-22` | `getString`/`getBoolean` falham com campo ausente. Isso é tratado como `Load.Error` (ok), mas um campo opcional novo quebra a tela inteira. | `optString`/`optBoolean` com default onde o campo é opcional. |

## Melhorias opcionais
| # | Sugestão | Motivo |
|---|---|---|
| N1 | `Graph` com `lateinit` global → inicializar em `Application.onCreate` | Evita `UninitializedPropertyAccessException` se algum componente (WorkManager, Service) rodar antes da Activity. |
| N2 | `GoalDetailViewModel.goalId ?: -1L` | Um id inválido vira GET `/objetivos/-1`. Melhor `checkNotNull`. |
| N3 | `TermControl` (`SimulateScreens.kt:189`): `x / width` com `width == 0` dá NaN, e `roundToInt()` lança exceção | É teórico (não há toque sem layout), mas `if (width <= 0) return months` custa nada. |

## Análise do caminho de renderização/recomposição
- **Gatilhos:** os `mutableStateOf` dos ViewModels são lidos dentro de cada lambda `composable(...)`. A invalidação fica restrita ao destino, e `SobraNavHost` em si não lê estado observável fora dos destinos, então não recompõe o grafo inteiro.
- **Digitação (Login/Cadastro/formulários):** cada tecla recompõe a tela de formulário inteira (`LoginScreen`, `NewExpenseScreen`). É barato: são poucos nós, e o Kotlin 2.2 tem strong skipping ativo, então os filhos com parâmetros iguais são pulados.
- **Simular:** cada tecla ou arraste recalcula `simulate(...)` em `remember(initial, monthly, months)` (`SimulateScreens.kt:130`). São até 120 iterações BigDecimal, menos de 1 ms. `TermControl` recompõe a cada mudança de mês arredondado. `mutableIntStateOf` não invalida para o mesmo valor, então arrastar sem mudar de mês não recompõe. O `offset(x = …)` poderia ser `offset { IntOffset(...) }` para mover só na fase de layout (melhoria opcional).
- **`LaunchedEffect(Unit) { vm.refresh() }`** (Home, Gastos, Objetivos, Perfil) roda toda vez que o destino entra na composição, **inclusive na rotação**. Ver perf P1.
- **`when (vm.state)` fora do `LaunchedEffect`:** está correto, a troca Loading→Success não reinicia o efeito.
- **Listas:** todas as `LazyColumn`/`LazyRow` novas têm `key` estável (`it.id`, `"dia-$day"`, `"resumo"`, `"add"`, `YearMonth.toString()`), sem colisão entre tipos e salváveis em Bundle. `groupByDay` usa `remember(expenses)`. O total por dia (`ExpensesScreens.kt:235`) é recalculado na composição do cabeçalho, o que é trivial.

## Avaliação de tipos
- `Load<T>` como `sealed interface` com `data object` é bom: os `when` são exaustivos. Em `SobraNavHost.kt:337` o `else` em Gastos e o `state == Load.Loading` funcionam, mas um `is` explícito seria mais claro.
- Dinheiro é tratado ponta a ponta em `BigDecimal`/String com 2 casas (`toMoney()`) e nunca em Double. Isso está correto.
- O ponto fraco é a fronteira org.json: não há schema, e cada `getX` é um ponto de exceção. Está mitigado em `loadResult` (veja R4/R10).
- **Nulos:** `forecast` e `monthlyRate` são tratados com `isNull`, e `renda` nula vira ZERO (`HomeRepository.kt:27`). O `GoalCard` "Faltam/Meta atingida" trata `remaining <= 0`.
- O cast de `savedStateHandle["goalId"]` está seguro, graças a `NavType.LongType` (`SobraNavHost.kt:411`).

## Detalhes do veredito
- Arquitetura de componentes: **PASS**. As telas são stateless e o NavHost faz a cola, um padrão consistente.
- Gerenciamento de estado: **CONCERNS** (F1, F3, R1).
- Segurança de tipos: **PASS**, com ressalvas em R4/R10.
- Desempenho de renderização: **PASS**.
- Dependências/Gradle: **PASS**. Só uma lib nova (`lifecycle-viewmodel-compose`), sem Retrofit/OkHttp/kotlinx-serialization, e o `minSdk` caiu de 37 para 26 de forma compatível. O problema de release está em R8.

---

# LENTE 2 — Performance Engineer

## Resumo
O desempenho é dominado por **rede**: uma ou duas requisições por exibição de tela, sem cache. CPU e alocações na composição são pequenas. Não encontrei vazamento de memória que cresça com o tempo de uso.

## Veredito: **APPROVE WITH CHANGES** (nenhum item crítico de perf; P1 e P2 recomendados)

## Problemas críticos
Nenhum. O único problema de recurso com efeito visível para o usuário (requisições duplicadas que geram registros duplicados) é o F3, já listado.

## Recomendações

| # | Problema | Arquivo(s) | Impacto | Correção |
|---|---|---|---|---|
| P1 | **Busca de novo a cada retorno de tela e a cada rotação.** | `SobraNavHost.kt:307, 334, 379, 494` | Cada troca de aba dispara uma busca: Home faz 2 GETs (`/home` + `/objetivos`), Gastos 1 e Objetivos 1. A rotação também, porque o `LaunchedEffect` é recriado e o VM não. Home e Objetivos repetem `GET /objetivos`. Com servidor local é imperceptível; no 3G são 2 a 4 requisições a mais por navegação. | Janela de validade no VM (`if (now - lastFetch < 30s && !dirty) return`) e uma flag "suja" marcada por quem cria despesa/objetivo/aporte (ex.: `Graph` com `MutableStateFlow<Int>` de versão). Também resolve a rotação. |
| P2 | **Buscas concorrentes sem `Job`.** | `HomeViewModel.kt:38-51`, `GoalsViewModel.kt:27-33`, `ProfileViewModel.kt:18-21`, `GoalDetailViewModel.kt:36-39`, `SavedSimulationsViewModel.kt:21-24` | "Tentar novamente" durante um `refresh` gera duas requisições em paralelo, e vence a última que termina (o resultado é o mesmo, mas é desperdício). Só o `ExpensesViewModel` cancela o `Job` anterior. | O mesmo padrão `job?.cancel()` do `ExpensesViewModel`. |
| P3 | **I/O bloqueante não é cancelável.** | `ApiClient.kt:31-66` | `job.cancel()` não interrompe `HttpURLConnection`: a thread de IO fica presa até 15 s de conexão + 15 s de leitura. O resultado obsoleto é descartado corretamente pela checagem de cancelamento do `withContext`. Com poucas requisições isso não esgota o pool de 64 threads. | Aceitável. Se crescer, usar OkHttp (`call.cancel()` via `invokeOnCancellation`). |
| P4 | **`conn.disconnect()` em toda requisição.** | `ApiClient.kt:64` | No Android, `disconnect()` pode fechar o socket e impedir o keep-alive: é um handshake TCP por chamada. Com HTTPS no release isso vira um handshake TLS por chamada, cerca de 100 a 300 ms em rede móvel. | Ler o stream até o fim e fechá-lo sem chamar `disconnect()`, para deixar o pool reaproveitar, ou usar OkHttp. |
| P5 | **`DecimalFormat` novo a cada formatação.** | `ui/format/Formatters.kt:25-28, 31, 39` (`formatMoney` → `decimalFormat(pattern)`) | Cada `toBrl()`/`toBrlShort()` faz o parse do padrão e aloca. Por recomposição da Home são cerca de 10 chamadas; numa lista de Gastos com 50 itens visíveis, cerca de 60. São dezenas de µs no total, sem jank, mas é alocação evitável no caminho de composição de lista. | Dois formatadores pré-criados em `ThreadLocal<DecimalFormat>` (`DecimalFormat` não é thread-safe), ou cache por padrão. Os testes JVM continuam válidos. |
| P6 | **Token lido na main thread.** | `ApiClient.kt:26, 29` (o default `token = tokenProvider()` é avaliado na thread chamadora, a Main), `SobraNavHost.kt:176` | `SharedPreferences.getString` bloqueia até a primeira carga do arquivo. O arquivo é minúsculo e a carga começa no `Graph.init`, então o efeito é desprezível. StrictMode pode acusar. | Resolver o token dentro do `withContext(IO)` (já há o fallback `tokenOverride ?: tokenProvider()`), deixando o default como `null`. |
| P7 | **Gráfico aloca a cada desenho.** | `SimulateScreens.kt:447-492` | `fractions()` roda a cada recomposição, e dois `Path` + `PathEffect` são criados a cada draw. É uma tela estática, sem impacto. | `remember(result)` nas frações; `drawWithCache` para os Paths. Opcional. |

## Melhorias opcionais
| # | Sugestão | Motivo |
|---|---|---|
| N4 | `AuthRepository.appScope` (`AuthRepository.kt:14`) | **Não é vazamento**: vive dentro do singleton `Graph`, com a mesma vida do processo, e só faz um `launch` por logout. Para limpeza e testabilidade, injetar um `applicationScope` no `Graph` em vez de criá-lo dentro do repositório. |
| N5 | `listSaved` com `limite=50` fixo | Basta para o uso atual. Paginar se crescer. |

## Orçamento de recursos
| Recurso | Antes (main, DemoData) | Depois | Preocupação? |
|---|---|---|---|
| Requisições por abrir o app (token válido) | 0 | 2 em paralelo (Home) | Não |
| Requisições por troca de aba | 0 | 1–2 (Home 2, Gastos 1, Objetivos 1, Perfil 0 depois da 1ª) | **Sim, leve (P1)** |
| Requisições por rotação | 0 | 1–2 (mesmo do retorno de tela) | **Sim, leve (P1)** |
| Requisições duplicadas no mesmo instante | — | Home e Objetivos repetem `/objetivos`; retry com refresh em curso duplica | Leve (P1/P2) |
| Conexões TCP | — | 1 por requisição (sem reuso, P4) | Leve; maior no release com TLS |
| Listeners/coletores | — | 1 collector de `session.expired` (vida da composição raiz) | Não |
| CoroutineScopes próprios | — | 1 (`AuthRepository.appScope`, vida do processo) | Não |
| Crescimento de memória por hora | — | Estáveis: VMs de abas salvas retidos (F1), listas limitadas ao mês/50 itens | Não (mas F1 é um problema de correção) |
| Startup | — | `Graph.init` antes de `super.onCreate`: constrói objetos e abre SharedPreferences de forma assíncrona; splash de 1,2 s mascara | Não |
| APK/dependências | — | +`lifecycle-viewmodel-compose` (pequena); sem libs HTTP/JSON | Não |

## Análise do caminho quente
1. **Digitação em campos de dinheiro e no Simular.** `parseMoneyInput` é O(n) em ≤ 11 dígitos. A recomposição do formulário inclui `simulate()` (≤ 120 iterações BigDecimal DECIMAL64) e cerca de 3 `DecimalFormat`. O total fica bem abaixo de 1 ms por tecla.
2. **Arraste da trilha de prazo.** Cada evento de ponteiro chama `currentOnChange`, mas o estado só muda quando o mês arredondado muda (no máximo 120 mudanças por arraste completo). Cada mudança recompõe `TermControl`, `PreviewCard` (com `simulate`) e `pluralStringResource`. É aceitável.
3. **Rolagem da lista de Gastos.** Por item: `ExpenseRow` + `toBrl()`/`toDayMonth()`, que alocam `DecimalFormat`/`String.format` (P5). Os cabeçalhos de dia somam o grupo. Não há trabalho pesado na rolagem: o agrupamento está em `remember`.
4. **Rede.** Tudo roda em `Dispatchers.IO`, e o parsing org.json também acontece em IO dentro do repositório, já que o `suspend` continua no contexto de quem chama `api.get`… **Atenção:** o parse dos repositórios (`getJSONArray` e o `map`) roda **depois** que o `withContext(IO)` retorna, ou seja, na **Main** (viewModelScope). Com payloads de 50 itens é desprezível, mas cresce linearmente. Se as listas crescerem, envolver os repositórios em `withContext(Dispatchers.Default)`.
5. **Shimmer do skeleton.** A animação infinita é recomposta ou redesenhada só enquanto `Load.Loading`. Ela é descartada ao sair da tela.

## Detalhes do veredito
- Gerenciamento de memória: **PASS** (sem acumulação; F1 é de correção, não de memória).
- Eficiência de rede ("IPC"): **CONCERNS** (P1, P2, P4).
- Padrões de I/O: **PASS** (IO fora da main; P6 é menor).
- Impacto no startup: **PASS**.
- Estabilidade em sessões longas: **PASS** (timeouts de 15 s, nenhum escopo que cresça).

---

## Prioridade sugerida de correção
1. **C1**: import de `height` em `Pickers.kt` (impede o build).
2. **F1**: limpar os estados salvos das abas no logout/expiração/login e chamar `simulation.reset()` no login.
3. **F3**: trocar os callbacks `onDone` por eventos observados (evita duplicatas).
4. **F2**: proteger o `popBackStack()` quando a tela está sozinha na pilha.
5. R1/R2 (expiração de sessão), R8 (URL/HTTPS no release), P1/P2 (refetch e cancelamento).

Este relatório só aponta problemas; não editei, criei nem commitei nada no repositório.

---

## Lente 4 — Backend Engineer (Java/Spring)

# Auditoria de Backend: Sobra (API REST Spring Boot)

**Perspectiva**: engenheiro de backend sênior (lente traduzida de Rust/Tauri/IPC para Java/Spring/JPA/HTTP)
**Data**: 2026-11-26
**Alvo**: `/home/user/Sobra/backend`. Revisei o código inteiro, não um plano. Não alterei nada: `git status` continua limpo e os testes temporários ficaram em `/tmp/claude-0/`.

## Resumo

É uma API bem acima da média para quem está aprendendo. As consultas são sempre parametrizadas, todo acesso filtra por `usuario_id`, o token é opaco e só o hash fica no banco, a senha usa Argon2id, o formato de erro é único e o limite de 10 KB vale até para envio em partes (*chunked*). Os 80 testes passam.

Os problemas que achei estão nas bordas: entradas válidas que viram 500, corridas entre requisições, dados pessoais vazando no log pelo Hibernate e o limite por IP em produção.

## Testes executados

`mvn -q -B test` terminou com **EXIT=0** e **80 testes, 0 falhas**:

| Classe | Testes |
|---|---|
| AutenticacaoApiTest | 15 |
| ConfiguracaoApiTest | 6 |
| ContratoApiTest | 7 |
| HomeEGastosApiTest | 9 |
| IsolamentoApiTest | 2 |
| ObjetivosApiTest | 9 |
| SimulacaoApiTest | 5 |
| ValidacaoTest | 4 |
| RegrasNegocioTest | 23 |

Também subi a aplicação com o perfil `test` na porta 18084 (banco sobra_test) e confirmei os achados abaixo com curl. Depois derrubei a aplicação.

## Arquivos revisados

| Arquivo | Papel | Risco |
|---|---|---|
| `seguranca/AutenticacaoFilter`, `RequisicaoFilter`, `LimiteTaxa`, `Tokens`, `SenhaHasher`, `RespostaErro` | Autenticação, limite de corpo, log, limite de taxa | Médio |
| `service/AuthService`, `SessaoService`, `EnvioEmail` | Cadastro, login, recuperação de senha, sessão deslizante | Alto |
| `service/ObjetivoService`, `ConfiguracaoService`, `DespesaService`, `HomeService`, `SimulacaoService`, `PerfilService` | Regras de negócio e transações | Médio |
| `comum/Validacao`, `Parametros`, `ErroApi`, `JsonErro`, `controller/TratadorErros` | Validação e caminhos de erro | Médio |
| `dominio/Dinheiro`, `RegrasObjetivo`, `Simulador`, `Nomes` | Contas com BigDecimal | Baixo |
| `model/*`, `repository/*` | JPA e consultas | Baixo |
| `db/migration/V001`, `V002`, `db/exemplo/V003`, `application.yml`, `pom.xml` | Esquema e configuração | Médio |
| `src/test/**` | Testes | — |

## Veredito: APPROVE WITH CHANGES

---

## Problemas críticos (corrigir antes de publicar)

| # | Problema | Arquivo(s) | O que acontece | Correção |
|---|---|---|---|---|
| C1 | **Criar objetivo com meta pequena e prazo longo dá 500.** O aporte sugerido é arredondado para 0.00, e o banco recusa esse valor pelo CHECK `aporte_mensal > 0`. A validação aceita a entrada, mas o banco não. | `ObjetivoService.java:82-93`, `RegrasObjetivo.java:20-22`, `V001__criar_esquema.sql:94`. O `TratadorErros.java:45-62` também não mapeia `ck_objetivo_aporte_mensal`. | **Confirmado**: `POST /objetivos {"valor_meta":"1.00","prazo_meses":360}` → 500 ERRO_INTERNO. Acontece sempre que a meta é menor que prazo × 0,005 (por exemplo, R$ 1,79 em 360 meses). É uma funcionalidade quebrada para entrada válida, e de quebra o log grava a linha inteira (ver R1). | Na validação: se `aporteSugerido(meta, prazo)` der zero, devolver 422 em `valor_meta`/`prazo_meses` com o código FORA_DO_LIMITE. Isso deve ficar na regra do domínio, e não depender do banco. Adicione `ck_objetivo_aporte_mensal` ao mapa `RESTRICOES` como rede de segurança e crie um teste para esse caso. |

Só esse caso se encaixa na sua definição de crítico. Os itens abaixo são reais (vários confirmados), mas nenhum causa perda de dados nem é uma falha explorável de forma direta.

## Recomendações (ordem de prioridade)

### R1 (alta) – Dados pessoais no log, apesar da RNF-08

**Onde:** logger do Hibernate `org.hibernate.orm.jdbc.error`; configuração em `application.yml:38-41`.

**O que acontece (confirmado):** quando o banco recusa uma linha, o Hibernate registra em WARN a mensagem do PostgreSQL com o detalhe. Vi no log:
- `Detail: Key (email)=(corrida-7199@exemplo.com) already exists.`, em duas requisições de cadastro simultâneas com o mesmo e-mail;
- `Failing row contains (14, 58, Cofrinho secreto, 1.00, 360, ...)`, no caso do C1.

O `TratadorErros` foi escrito para não registrar a mensagem, mas o Hibernate registra antes dele. Isso é um problema de LGPD.

**Correção:** em `application.yml`, `logging.level."org.hibernate.orm.jdbc.error": OFF` (o `TratadorErros` já registra a classe da exceção e o `id_requisicao`). Crie um teste que force uma corrida no cadastro e verifique, com um *appender* de captura, que o e-mail não aparece no log.

### R2 (alta, depende do deploy) – Limite por IP burlável em produção

**Onde:** `application.yml:108` (`forward-headers-strategy: framework`) junto com `req.getRemoteAddr()` em `AuthController.java:38,44,58,67`.

**Por quê:** o `ForwardedHeaderFilter` do Spring usa o primeiro valor de `X-Forwarded-For`, que quem controla é o cliente. Se o proxy apenas acrescenta valores ao cabeçalho (o comum em Render, Heroku e similares), um atacante manda um IP novo a cada requisição e os limites por IP (login 5/15 min, rotas públicas 30/15 min) deixam de valer. Isso permite *password spraying*, ou seja, testar a mesma senha em muitos e-mails, e enumeração em massa pelo 409 do cadastro. Cada IP falso também cria uma entrada nova no `LimiteTaxa` (ver R8).

**Correção:** `forward-headers-strategy: native` com `server.tomcat.remoteip.internal-proxies` apontando só para o IP do proxy (a `RemoteIpValve` do Tomcat ignora valores de origem não confiável), ou um proxy que sobrescreva o cabeçalho. Teste no ambiente real.

### R3 (média) – Dois PUTs simultâneos de renda ou categorias dão 500

**Onde:** `ConfiguracaoService.java:81-86` e `133-138`.

**O que acontece (confirmado):** com 8 `PUT /configuracao/renda` em paralelo, 1 recebeu 200 e 7 receberam 500. Com 6 `PUT /configuracao/categorias`, 1 recebeu 200 e 5 receberam 500. O padrão "busca, se não existir insere" corre entre duas transações, e a violação de `pk_renda_vigencia` / `pk_orcamento_categoria` não está mapeada. Um toque duplo no app já basta. Não há perda de dados (uma das gravações vence), mas o usuário vê erro.

**Correção:** um *upsert* nativo, `INSERT ... ON CONFLICT (usuario_id, mes_inicio) DO UPDATE SET valor = EXCLUDED.valor` (o mesmo vale para `orcamento_categoria`).

### R4 (média) – Gravações concorrentes podem desfazer revogação de sessão ou troca de senha

**Onde:** `SessaoService.java:65-67` (renovação), `82-85` (logout), `PerfilService.java:40-44`, `AuthService.java:181`.

**Por quê:** sem `@DynamicUpdate` nem `@Version`, o Hibernate grava todas as colunas no UPDATE. Dois exemplos:
- Uma requisição que está renovando a sessão (no máximo uma vez por hora) leu `revogada_em = null`. Se o logout ou a redefinição de senha fizer o commit no meio, essa requisição grava `revogada_em = NULL` por cima e a revogação se perde.
- Um `PUT /perfil/notificacoes` concorrente com a redefinição de senha pode gravar o `senha_hash` antigo de volta.

A janela é pequena, mas o efeito é de segurança.

**Correção:** trocar as gravações pontuais por UPDATEs JPQL, como `UPDATE Sessao SET expiraEm=:n WHERE id=:id AND revogadaEm IS NULL`. Outra opção é `@DynamicUpdate` em `Usuario` e `Sessao`, ou `@Version` (bloqueio otimista).

### R5 (média) – Token de recuperação aceita dois usos simultâneos

**Onde:** `AuthService.java:176-184`.

**Por quê:** duas requisições com o mesmo token leem `usado_em = null` e as duas trocam a senha (a última vence). Isso quebra o "uso único" da D-08.

**Correção:** `UPDATE RecuperacaoSenha r SET r.usadoEm=:agora WHERE r.id=:id AND r.usadoEm IS NULL` e seguir só se o número de linhas afetadas for 1. Alternativa: `@Lock(PESSIMISTIC_WRITE)` no `findByTokenHash`.

### R6 (média) – Números extremos no JSON derrubam a validação

**Onde:** `Validacao.java:223-231`.

**O que acontece (confirmado):**
- `prazo_meses: 1e400` chega como Double Infinity, `new BigDecimal("Infinity")` lança NumberFormatException e a resposta é 500. Testei em `POST /objetivos` e em `/simulacoes/calcular`.
- `prazo_meses: 18446744073709551628` (2^64 + 12) foi **aceito como 12**, porque `BigDecimal.longValue()` trunca em silêncio.

**Correção:** no ramo `Number`, rejeitar Double/Float infinito ou NaN e usar `bd.longValueExact()` dentro de try/catch (NumberFormatException e ArithmeticException), devolvendo FORMATO_INVALIDO ou FORA_DO_LIMITE.

### R7 (média) – Argon2 roda dentro da transação, segurando uma conexão do pool

**Onde:** `AuthService.java:59` (cadastro), `89` (login) e `163` (redefinição). O pool é o Hikari, 10 conexões por padrão.

**Por quê:** com o `JpaTransactionManager`, a conexão é pega no início do método. Cada hash usa 19 MiB e dezenas de milissegundos. Uma rajada de logins esgota as conexões, e com isso todas as rotas param com 503. A memória também sobe: 200 threads × 19 MiB dá cerca de 3,8 GB.

**Correção:**
- Validar e gerar o hash fora da transação: mover o `@Transactional` para um método menor ou usar `TransactionTemplate`.
- Ou adiar a conexão com `spring.datasource.hikari.auto-commit=false` mais `hibernate.connection.provider_disables_autocommit=true`.
- Limitar quantos hashes rodam ao mesmo tempo com um `Semaphore` no `SenhaHasher`.

### R8 (baixa/média) – LimiteTaxa: corrida na limpeza e varredura O(n)

**Onde:** `LimiteTaxa.java:27-40` e `43-50`.

**Por quê:** `limpar()` pode remover a fila que outra thread acabou de obter com `computeIfAbsent`. A tentativa vai para uma fila órfã e não é contada. Além disso, com mais de 50 mil chaves ainda dentro da janela, cada requisição varre o mapa inteiro, o que é fácil de provocar junto com o R2.

**Correção:** usar `compute()` para remover de forma atômica, limpar periodicamente com `@Scheduled` em vez de a cada requisição, ou usar Caffeine/Bucket4j com tamanho máximo.

### R9 (baixa) – Envio de e-mail dentro da transação e diferença de tempo na recuperação

**Onde:** `AuthService.java:136-157` e `185`.

**Por quê:** hoje o envio é só um log. Quando virar SMTP síncrono:
- o e-mail pode sair e o commit falhar depois, deixando um link com token que não existe no banco;
- o tempo de resposta passa a revelar se o e-mail existe (quando não existe, o método retorna na hora).

**Correção:** publicar um evento e enviar com `@TransactionalEventListener(phase = AFTER_COMMIT)` mais `@Async`.

### R10 (baixa) – Migração de exemplo V003 vai colidir com uma futura V003 real

**Onde:** `db/exemplo/V003__dados_exemplo.sql` e `application.yml:81,91`.

**Por quê:** quando você criar `db/migration/V003__...`, o Flyway em dev e teste para com "Found more than one migration with version 3".

**Correção:** renomear o exemplo para uma versão alta (`V900__dados_exemplo.sql`) ou para uma migração repetível (`R__dados_exemplo.sql`).

### R11 (baixa) – Tabelas de sessão e recuperação crescem para sempre

**Onde:** `sessao` e `recuperacao_senha`.

**Por quê:** nada apaga as linhas vencidas, revogadas ou usadas. É dado pessoal guardado além do necessário (LGPD) e um índice que só cresce.

**Correção:** um job `@Scheduled` diário que apague o que venceu há mais de N dias.

## Extras (opcionais)

| # | Sugestão | Motivo |
|---|---|---|
| E1 | Aplicar um limite por IP também a tokens inválidos (`AutenticacaoFilter.java:69-73`) | Hoje um token inventado custa uma consulta ao banco sem limite. Força bruta é inviável (256 bits), mas a carga existe. |
| E2 | `ddl-auto: validate` (`application.yml:19`) | Faz a aplicação falhar ao subir se a entidade e a migração divergirem, em vez de dar erro em tempo de execução. |
| E3 | `SimulacaoService.java:92`: `setScale(6)` sem arredondamento lança exceção se alguém configurar uma taxa com 7 casas | Dá 500 por erro de configuração. Melhor validar a taxa na inicialização. |
| E4 | `Simulador.java:37`: a escala cresce 6 casas por mês (cerca de 720 dígitos em 120 meses) | Hoje está ok. Se o prazo subir até o limite do CHECK (600 meses), usar `MathContext(34)` ou `setScale(20, HALF_UP)` a cada passo. O resultado em 2 casas não muda. |
| E5 | O limite por e-mail no login conta também os logins bem-sucedidos (`AuthService.java:105-106`) | Seis logins legítimos em 15 minutos dão 429. Contar só as falhas é mais justo. |
| E6 | Métodos só de teste no código de produção (`LimiteTaxa.zerar`, `EnvioEmail.enviados`) | Melhor um bean `@Profile("test")` ou `@TestConfiguration`. |
| E7 | Mapear também `ck_simulacao_resultado`, `ck_simulacao_taxa` e `ck_usuario_email_minusculo` no `TratadorErros` | Completa a rede de segurança de restrições. |
| E8 | CORS: não há configuração. Hoje está correto para o app Android | Se um dia houver cliente web, liste as origens explicitamente e nunca use `*` com o cabeçalho Authorization. |

---

## Análise dos caminhos de erro

1. **Validação** (`Validacao`, `Parametros`): junta os erros e lança `ErroApi` 422/400, que o `TratadorErros` converte no formato único. Está bom. Os furos são o R6 (1e400 vira 500, números enormes são truncados) e o C1 (a regra do domínio deixa passar um valor que o banco recusa).
2. **Filtros** (`RequisicaoFilter`, `AutenticacaoFilter`): `ErroApi`, `DataAccessException` e `TransactionException` são tratados por `RespostaErro.escrever`, com 401/503 e os cabeçalhos certos. O `catch (Exception)` final devolve 500 sem detalhes. `resetBuffer` e `isCommitted` estão protegidos. Está bom.
3. **JSON e corpo**: um corpo acima de 10 KB lança IOException dentro do Jackson, o Spring transforma em `HttpMessageNotReadableException` e a resposta é 400. **Confirmado** com chunked de 20 KB. Está bom.
4. **Banco**: `DataIntegrityViolationException` tem o nome da restrição procurado em `RESTRICOES`, gerando 409/422, ou vira 500 sem a mensagem. **Mas o Hibernate já registrou a mensagem com os dados antes (R1).** Nomes de restrição que faltam no mapa (`ck_objetivo_aporte_mensal`, `pk_renda_vigencia`, `pk_orcamento_categoria`) resultam em 500 nos casos C1 e R3. Banco fora do ar gera 503 (`CannotCreateTransactionException` / `DataAccessResourceFailureException`). Não há teste para esse caminho.
5. **Exceções inesperadas**: `TratadorErros.inesperado` registra a classe e o primeiro *frame* da pilha, sem a mensagem, e devolve 500. É a decisão certa para a LGPD.
6. **Rollback**: `ErroApi` estende RuntimeException, então qualquer erro de negócio desfaz a transação. Os `saveAndFlush` forçam o erro de restrição a aparecer dentro do método. Está bom.

## Ciclo de vida dos recursos

| Recurso | Criado em | Liberado em | Liberação no caminho de erro | Risco |
|---|---|---|---|---|
| Conexão JDBC (Hikari) | Início do `@Transactional`, e no filtro para `validar()` | Fim da transação | Rollback devolve a conexão | Médio: segura a conexão durante o Argon2 (R7) |
| Transação | `@Transactional` nos serviços; `readOnly` nas leituras; `open-in-view: false` | Commit ou rollback | Rollback em RuntimeException | Baixo: a do filtro e a do controller são separadas, o que está ok |
| Sessão (linha em `sessao`) | `SessaoService.criar` | Revogação lógica (logout ou troca de senha) ou validade de 30 dias / teto de 90 | Nunca é apagada | Médio: perda de atualização (R4) e crescimento sem fim (R11) |
| Token de recuperação | `pedirRecuperacao` | `usado_em` ou expiração em 30 min; o pedido anterior é invalidado | Nunca é apagado | Médio: dois usos simultâneos (R5) |
| Token em claro | Gerado em memória e devolvido uma única vez | Não é guardado; no banco só fica o SHA-256 | — | Baixo |
| `LimiteTaxa` (mapa em memória) | `consumir()` | Limpeza só acima de 50 mil chaves | — | Médio: corrida e varredura O(n) (R8), chaves sem limite com IP falso (R2); só serve para uma instância |
| Lista `EnvioEmail.enviados` | Só com `guardar-em-memoria=true` | Nunca | — | Baixo: crescimento sem limite se a opção for ligada fora dos testes |
| Memória do Argon2 (19 MiB por hash) | Cada login, cadastro ou redefinição | Fim do hash | — | Médio: sem limite de execuções simultâneas (R7) |

## Tópicos pedidos

- **Concorrência**:
  - Aportes simultâneos estão corretos. O acumulado é sempre `SUM` e não um saldo gravado, então não há atualização perdida. Confirmei com 20 aportes paralelos de 10.01: acumulado 200.20, 20 linhas.
  - Na sessão deslizante, só a renovação concorrente com logout é problema (R4).
  - No cadastro simultâneo, o 409 funciona (1×201 e 7×409 confirmados), mas vaza o e-mail no log (R1).
- **Segurança (OWASP)**:
  - Injeção: nenhuma. Todo SQL e JPQL usa parâmetros.
  - IDOR (acesso a dados de outro usuário): bem resolvido. Todas as consultas filtram por `usuario_id`, o dono do objetivo é conferido antes da validação, recurso alheio dá o mesmo 404 que recurso inexistente e o `usuario_id` mandado pelo cliente é ignorado.
  - Rotas: tentei `//`, `/./`, `/../` e `;param`. Nenhuma burlou o filtro de autenticação; todas deram 401 ou 404.
  - Senha: Argon2id com os parâmetros mínimos da OWASP. O token usa 32 bytes de `SecureRandom` e é guardado como SHA-256, o que é adequado para um segredo de alta entropia.
  - Enumeração de e-mail: o login tem tempo equalizado. O 409 do cadastro revela o e-mail por exigência da especificação, contido só pelo limite por IP (que depende do R2). O R9 é um risco futuro.
  - Tamanho de corpo: está ok.
  - CORS: ausente, o que é correto hoje.
  - Logs: o log de requisição só tem metadados, o que é bom. O vazamento vem do Hibernate (R1).
- **@Transactional**: o uso geral está correto, com `readOnly` nas leituras e `saveAndFlush` onde importa. As ressalvas são o R7 e o R9.
- **N+1 e índices**: não há N+1 (`JOIN FETCH` na despesa e agregações no próprio SQL). Todas as consultas têm índice: `ix_despesa_usuario_data`, `ix_objetivo_usuario_criado`, `ix_aporte_objetivo_data`, `ix_simulacao_usuario_criado`, os índices únicos dos tokens e as PKs compostas.
- **BigDecimal e dinheiro**: está correto. Nunca usa double, as colunas são DECIMAL(12,2), o arredondamento é HALF_UP igual ao `ROUND` do PostgreSQL para valores positivos, a validação limita a 2 casas e teto de 99.999.999,99, e `resultado` é DECIMAL(15,2), o que comporta o pior caso (cerca de 2,3e10). As ressalvas são E3 e E4.
- **Flyway**: o esquema é só das migrações (`ddl-auto: none`) e os dados de exemplo ficam fora de produção. As ressalvas são R10 e E2.
- **Qualidade dos testes**: cobrem bem a especificação (casos numéricos, isolamento entre usuários, contrato, sessão, limites) com relógio fixo e o aplicativo de verdade. Faltam testes de:
  - concorrência (R3, R4, R5);
  - banco fora do ar (503);
  - ausência de dados pessoais no log (R1);
  - limites numéricos (C1, R6).

  Os testes também acumulam usuários no `sobra_test` a cada execução. Funciona, mas vale limpar de vez em quando.

## Detalhes do veredito

- Tratamento de erros: **CONCERNS** (C1, R6 e mapeamento de restrições incompleto)
- Segurança de concorrência: **CONCERNS** (R3, R4, R5, R8)
- Gestão de recursos: **CONCERNS** (R7, R11)
- Contrato HTTP/JSON: **PASS**
- Segurança e privacidade: **CONCERNS** (R1, R2)

---

## Lentes 5, 6 e 7 — Structural Engineer, Repo Maintainer e Production Readiness

# Auditoria do Sobra em três lentes: branch `claude/youthful-darwin-9ml80l` comparada com `main`

**Data:** 2026-11-26. **Escopo:** 11 commits, 155 arquivos (+10.287/−389). Inclui o app Android em `app/` e a API Spring Boot em `backend/`, que foi copiada de outro repositório e não foi modificada.

**Como foi feito:** só leitura. Nada foi editado, criado ou commitado. Carreguei as três skills (`audit-as-structural-eng`, `audit-as-repo-maintainer`, `audit-as-prod-readiness`). Elas pedem que o relatório seja gravado em `reviews/*.md`, mas isso iria contra a sua regra de não criar arquivos, então o relatório completo está aqui.

**Resumo:**
- **Lente 1 (estrutura):** APPROVE WITH CHANGES.
- **Lente 2 (manutenção do repositório):** APPROVE WITH CHANGES.
- **Lente 3 (prontidão):** APPROVE WITH CHANGES para a apresentação; NEEDS REWORK para produção.
- **Nenhum achado bloqueia a apresentação.** O maior risco é o app nunca ter sido compilado aqui.

---

## LENTE 1: Structural Engineer

### Resumo
O app passou de dados de exemplo (DemoData) para a API real. As camadas `ui/screens` → `ui/viewmodel` → `data/*Repository` → `data/remote/ApiClient` foram criadas, e a injeção de dependência é manual, pelo `Graph`. O back end segue controller → service → repository → model, com `dominio/` puro.

### Arquivos revisados
| Arquivo | Papel | Camada | Risco |
|---|---|---|---|
| `app/.../data/remote/ApiClient.kt` | Cliente HTTP, erros e sessão | remote | Médio |
| `app/.../data/Graph.kt` | DI manual (service locator) | data | Baixo |
| `app/.../data/*Repository.kt` (8) | JSON → modelos | data | Médio |
| `app/.../ui/model/Models.kt` | Modelos usados por data e ui | ui (na prática, domínio) | Médio |
| `app/.../domain/Finance.kt` | Regras locais (simulação, progresso, aporte) | domain | Médio |
| `app/.../ui/viewmodel/*.kt` (12) | Estado das telas | viewmodel | Baixo |
| `app/.../navigation/SobraNavHost.kt` (634 linhas) | Rotas e ligação das ViewModels | ui | Médio |
| `backend/.../controller, service, repository, dominio, seguranca` | API | back end | Baixo |

### Veredito: APPROVE WITH CHANGES

### Problemas críticos
Nenhum. A estrutura funciona e é legível para um projeto acadêmico. Os pontos abaixo são de acoplamento e duplicação, não de quebra.

### Recomendações
| # | Problema | Arquivo:linha | Impacto | Correção |
|---|---|---|---|---|
| S1 | **Regra da simulação nos dois lados, com a taxa fixa em três lugares.** O servidor calcula com a taxa configurável `sobra.taxa-simulacao`. O app repete a fórmula numa prévia ao vivo e fixa a taxa em `domain/Finance.kt:17` e no texto de `strings.xml:173` ("0,96% ao mês"). A tela de Resultado mostra essa string fixa (`SimulateScreens.kt:397-398`). A taxa que o servidor manda em `calcular` (`taxa_mensal`) é ignorada (`SimulationRepository.kt:17-31`). | `SimulateScreens.kt:130`, `Finance.kt:13-17,56-84`, `strings.xml:173`, `SimulationRepository.kt:20-30` | Se alguém mudar `taxa-simulacao` no `application.yml`, a prévia, o cartão da taxa e a linha "Taxa" do resultado passam a mostrar outro valor que não o calculado. Hoje os dois dão 14.680,67, então não há bug visível. | Adicionar `monthlyRate` em `SimulationResult`, ler `taxa_mensal` do JSON e mostrá-la no Resultado. Na prévia, escolher entre (a) remover, (b) rotulá-la como estimativa ou (c) usar a última taxa recebida do servidor. Atualizar o TODO desatualizado de `Finance.kt:13-16`: a regra já está definida no servidor (RN-07/D-01). |
| S2 | **A camada `data` depende de `ui.model`, que depende de Compose e de `R`.** `Category` carrega `Color` do Compose e ids de `R.string`/`R.drawable` (`Models.kt:18-33`). Os repositórios importam `ui.model.*`. `GoalsRepository.goalThumbnail` escolhe um drawable de tela (`GoalsRepository.kt:83-91`). | `Models.kt:3-7,18-33`, `GoalsRepository.kt:3,74,83-91`, `ExpensesRepository.kt:4-5` | A dependência está invertida: data aponta para ui. Os repositórios não podem ser testados com JUnit puro, e uma mudança visual em `Category` afeta a camada de dados. | Mover os modelos para `domain/model` sem `Color` nem `R`. Fazer o mapeamento categoria → ícone/cor/rótulo numa extensão em `ui/`, e a escolha da miniatura no ViewModel ou na tela. |
| S3 | **Valores que o servidor já calcula são recalculados no app.** A Home ignora `gastos` e `disponivel` do `/home` e soma as categorias no app (`HomeScreen.kt:127-128`). `ExpensesData.total` é lido do JSON (`ExpensesRepository.kt:29`) mas a tela soma de novo (`ExpensesScreens.kt:121`). | idem | Hoje é inofensivo porque as somas batem. Se o servidor mudar uma regra de negócio, a mudança precisará ser feita nos dois lados. | Usar os totais do servidor (`HomeData.expenses/available`, `ExpensesData.total`) e deixar `availableAmount` e `total()` só para as prévias. |
| S4 | **As ViewModels acessam o `Graph` global diretamente** (`Graph.auth`, `Graph.goals` etc.). | todos os `ui/viewmodel/*.kt` (ex.: `HomeViewModel.kt:43-44`) | É um service locator: não dá para testar uma ViewModel com um repositório falso sem mexer no singleton. | Receber o repositório no construtor com valor padrão (`class HomeViewModel(private val home: HomeRepository = Graph.home, …)`). Melhora os testes sem usar biblioteca. |
| S5 | **Limites do servidor copiados no app.** `PRAZO_MAX = 120` (`SimulateScreens.kt:98`) repete `prazo-simulacao-max`. O máximo de dinheiro do app, `MAX_MONEY_DIGITS = 11` (`Formatters.kt:23`), permite R$ 999.999.999,99, enquanto o servidor aceita até 99.999.999,99 (`Validacao.java:29`). | idem | Só a parte do dinheiro diverge. A pessoa consegue digitar um valor que o servidor recusa com erro 422, cuja mensagem aparece na tela. | Usar 10 dígitos e citar a constante do servidor num comentário. |

### Melhorias opcionais
| # | Sugestão | Justificativa |
|---|---|---|
| S6 | Dividir `SobraNavHost.kt` (634 linhas) por área: auth, setup, abas. | Está virando um "arquivo-deus". Hoje ainda é legível. |
| S7 | Simplificar o token no `ApiClient`: hoje `post` resolve o token no parâmetro padrão (`ApiClient.kt:26`) e `call` resolve de novo (`:33-34`). | Duas fontes de verdade para o mesmo valor. Basta `token: String? = null` e `tokenOverride ?: tokenProvider()`. |

### Mapa de dependências
```
MainActivity ──> Graph.init ──> SessionStore (SharedPreferences)
                         └────> ApiClient(BASE_URL de ApiConfig, tokenProvider, onSessionExpired)
                                   ^
SobraNavHost ──> ViewModels ──(Graph.*)──> Repositories ──> ApiClient ──HTTP/JSON──> backend
     │                │                        │
     │                │                        ├──> ui.model (Category: Color + R)   <-- invertido (S2)
     │                │                        └──> R.drawable (GoalsRepository)     <-- invertido (S2)
     │                └──> domain (SimulationResult)
     └──> ui.screens ──> domain.simulate()  (prévia local, taxa fixa)                <-- duplicação (S1)
                    └──> data.DemoData / data.CATEGORY_ORDER (previews e chips)

backend: controller ─> service ─> repository ─> model
                         ├─> dominio (Simulador, RegrasObjetivo, Dinheiro: puros)
                         ├─> dto, comum (Validacao, ErroApi), config (Relogio, SobraProperties)
         seguranca (filtros) ─> service (SessaoService)
```
No back end não há ciclos nem acesso de controller direto a repository. A camada `dominio` não depende de nada.

### Análise de fronteiras
| Fronteira | De → Para | Limpa? | Observações |
|---|---|---|---|
| Contrato JSON (snake_case em português) | app → API | Sim | As chaves batem com os DTOs: `HomeDtos`, `SimulacaoDtos`, `AuthDtos:56`. |
| Dinheiro como texto | app ↔ API | Sim | `Money.kt:7` (`setScale(2,HALF_UP).toPlainString()`) espelha `Dinheiro.texto`. A leitura usa `BigDecimal(String)`. |
| Datas | app ↔ API | Parcial | `LocalDate`/`YearMonth.toString()` estão corretos. Mas o app usa o fuso do aparelho e o servidor usa America/Sao_Paulo (ver P6). |
| Códigos de erro | API → app | Sim | `{"erro":{codigo,mensagem,campos}}` é lido em `ApiClient.kt:69-84`. `SESSAO_EXPIRADA` e `NAO_AUTENTICADO` levam à tela 24 (`ApiException.kt:19`). O logout é rota pública (`AutenticacaoFilter.java:38`), então sair com token vencido não dispara a tela de sessão expirada. Correto. |
| Taxa da simulação | API → app | Não | `taxa_mensal` é ignorada (S1). |
| Totais da Home e de Gastos | API → app | Parcial | Recalculados no app (S3). |
| Imagens | API → app | Parcial e documentado | `imagem_url` e `avatar_url` são ignoradas (`GoalsRepository.kt:78-82`, `SobraNavHost.kt:318`). |

### Detalhe do veredito
- Direção das dependências: CONCERNS (S2)
- Acoplamento: CONCERNS (S4, Graph global)
- Coesão: PASS
- Fronteiras entre camadas: CONCERNS (S1, S3)
- Propagação de mudanças: CONCERNS (a taxa está em três lugares)

---

## LENTE 2: Repo Maintainer

### Resumo
Muito código novo e coerente: o padrão `Load`/`loadResult`/`runSubmit` é usado em todas as ViewModels e os comentários didáticos são bons. Ficaram para trás código e rotas de antes da integração, TODOs que a própria branch já resolveu e algumas duplicações pequenas.

### Arquivos revisados
| Arquivo | Situação das convenções | Risco |
|---|---|---|
| `navigation/SobraNavHost.kt` | Rotas mortas e TODOs desatualizados | Baixo |
| `data/DemoData.kt` | Só serve para @Preview, mas vai no APK | Baixo |
| `data/Mappers.kt` | Duplica a ordem do enum | Baixo |
| `res/values/strings.xml` | 10 strings sem uso | Baixo |
| `ui/viewmodel/*.kt` | Mensagens fixas no código | Baixo |
| `domain/Finance.kt` | TODO desatualizado | Baixo |

### Veredito: APPROVE WITH CHANGES

### Problemas críticos
Nenhum.

### Recomendações
| # | Problema | Arquivo:linha | Impacto | Correção |
|---|---|---|---|---|
| R1 | **Rotas que nada navega até elas:** `GOALS_EMPTY`, `HOME_SKELETON` e `ERROR`. Os estados correspondentes agora são renderizados dentro de Home e Objetivos. | `SobraNavHost.kt:97-99,570-592` | Código morto. O professor pode perguntar "para que serve isso?". | Remover as três rotas (as telas continuam em uso). |
| R2 | **TODOs que a própria branch já resolveu.** | `SobraNavHost.kt:581,585,589,601` ("navegar para cá quando o token expirar" já está feito em `:162-164`), `DemoData.kt:19` ("substituir cada uso em SobraNavHost" já foi feito), `Finance.kt:13-16,54` (a fórmula já está definida no servidor) | Confunde quem lê: parece trabalho pendente. | Apagar ou reescrever. Há 40 TODOs no total; a maioria é pergunta legítima ao design ("o Figma não define…"), e esses ficam. |
| R3 | **Strings sem uso:** `months_count`, `expenses_total_label`, `date_today`, `new_goal_deadline_value`, `result_final_estimate`, `picker_category_title`, `picker_month_title`, `picker_term_title`, `expenses_count`, `goals_total_caption`. | `strings.xml:32,124,135,154,181,230,231,237,254,267` | Gera aviso no lint (UnusedResources). | Remover. |
| R4 | **Mensagens de validação escritas direto nas ViewModels**, enquanto as telas usam `strings.xml`. | `AuthViewModel.kt:36,53,61,65,81`, `NewExpenseViewModel.kt:37,41`, `NewGoalViewModel.kt:33,37`, `SetupViewModel.kt:41`, `GoalDetailViewModel.kt:48`, `SimulationViewModel.kt:55`, `Load.kt:33`, `ApiClient.kt:60,62,82` | Duas convenções no mesmo app. | Aceitável no escopo acadêmico se for documentado ("mensagens de validação ficam na ViewModel"). Senão, expor um `@StringRes` no estado. |
| R5 | **Nomes misturando português e inglês.** O app usa identificadores em inglês, exceto `ApiException.codigo` e `campos` (`ApiException.kt:9,12`), as variáveis `erro` e `lista` em `ApiClient.kt:71-77`, `TAXA_MENSAL_PROVISORIA` (`Finance.kt:17`) e `PRAZO_MAX`/`PRAZO_MIN` (`SimulateScreens.kt:98`). O back end é todo em português, com o pacote `com.Sobra.Aplicativo` em maiúsculas. | idem | Inconsistência pequena. A regra implícita é razoável: o app em inglês e o contrato da API em português. | Escrever essa regra no README e renomear os 4 casos (`code`, `fields`, `PROVISIONAL_MONTHLY_RATE`, `TERM_MAX`). Não mexer no pacote do back end agora (foi copiado e não deve ser modificado). Vale saber que pacote Java com letra maiúscula foge da convenção, caso o professor pergunte. |
| R6 | **`DemoData` e `ThemePreview` vão para o APK de release**, porque estão em `src/main` e são importados por @Preview. | `DemoData.kt`, `Rows.kt:31`, `GoalsScreens.kt:32`, `SimulateScreens.kt:61`, `ProfileScreen.kt:32`, `ExpensesScreens.kt:35` | Peso morto pequeno. Com a otimização de release desligada (`build.gradle.kts:24-26`), nada é removido. | Aceitável. Se quiser separar, mover os dados de exemplo para `src/debug`. Exige cuidado, porque os @Preview estão em `src/main`. |
| R7 | `ExampleInstrumentedTest.kt` é o placeholder do template. | `app/src/androidTest/.../ExampleInstrumentedTest.kt` | Ruído. | Apagar, ou trocar por um teste real de navegação. |
| R8 | **Arquivos do IDE versionados** (`.idea/deploymentTargetSelector.xml`, `misc.xml` etc.). | `.idea/*` | O seletor de dispositivo é específico de cada máquina e gera conflito. | Colocar `.idea/deploymentTargetSelector.xml` no `.gitignore`. |
| R9 | **Comentário enganoso em `SessionStore.kt:14`.** Ele diz que `extraBufferCapacity = 1` garante que o aviso "não se perde se ninguém estiver escutando". Num `MutableSharedFlow` sem `replay`, um `tryEmit` sem nenhum coletor é descartado. | `SessionStore.kt:14-15` | Raro na prática, porque o NavHost coleta o tempo todo. Mas a explicação está errada, e o professor pode perguntar. | Corrigir o comentário, ou usar `replay = 1` (exigiria `resetReplayCache` após navegar). |
| R10 | **Autoria dos commits.** Os 11 commits da branch estão como "Claude <noreply@anthropic.com>". | `git log main..HEAD` | Num trabalho acadêmico, a autoria pode importar para a avaliação. | Decisão sua: considere fazer o squash/merge com o seu autor e um `Co-Authored-By`. |

### Melhorias opcionais
| # | Sugestão | Justificativa |
|---|---|---|
| N1 | Função `JSONArray.objects(): List<JSONObject>` | O mesmo `(0 until array.length()).map {}` se repete 7 vezes nos repositórios. |
| N2 | Usar o `ApiException` já disponível no `calculate` da simulação | `SimulationViewModel.kt:51-56` usa `loadResult` e descarta a mensagem do servidor (ex.: 422 "valor máximo"). Trocar por `runSubmit` ou um `try/catch` aproveita `displayMessage`. |
| N3 | Um `fetch` comum para Home, Gastos e Objetivos | O padrão `load`/`refresh`/`fetch(keepOnError)` está copiado em três ViewModels. Só Gastos cancela o job anterior (`ExpensesViewModel.kt:40`); `GoalsViewModel.kt:27-33` não cancela. |

### Checklist de convenções (traduzido para Android/Kotlin)
| Convenção | Status | Observações |
|---|---|---|
| Localização dos arquivos (pacotes ui/viewmodel/data/remote/domain) | OK, com uma ressalva | `ui/model` deveria ser `domain/model` (S2). |
| Nomes | Parcial | Mistura de português e inglês (R5). |
| "Barrel exports" (não existe em Kotlin; equivale a visibilidade) | OK | `internal` em `loadResult`/`runSubmit`, `private` nos helpers. |
| Ordem dos imports | Parcial | `SobraNavHost.kt:3-71` está fora de ordem (Compose misturado com componentes do app). Rodar "Optimize imports". |
| Recursos de texto | Parcial | R3 e R4. |
| Sem código morto | Não | R1, R2, R3, R7. |
| Log estruturado | Ausente no app | Nenhum `Log.*`. `loadResult` engole a exceção sem registrar (ver P9). O back end loga em formato JSON. |
| Sem supressões de lint | OK | Nenhum `@Suppress` foi encontrado nos arquivos novos. |
| Documentação | OK | O README da raiz e `docs/roteiro-de-teste.md` estão bons e honestos sobre os limites. |
| `.gitignore` | OK, com uma ressalva | Cobre `build/` (app) e `target/` (back end). Falta R8. |
| Commits | OK | Mensagens descritivas em português, por etapa. Ver R10. |

### Oportunidades de reuso
- `CATEGORY_ORDER` (`Mappers.kt:17-20`) tem exatamente a ordem de `Category.entries` (`Models.kt:24-32`). Pode ser substituído por `Category.entries`.
- `categoryFromCode` e `categoryToCode` (`Mappers.kt:6-31`) são dois `when` espelhados. Basta um campo `apiCode` no enum e `entries.firstOrNull { it.apiCode == code } ?: Other`.
- `GoalsScreens.kt:142-143` e `SetupScreens.kt:107` usam `fold(BigDecimal.ZERO)…`, mas `domain/total()` já existe e é usado em `ExpensesScreens.kt:121`.
- O `submit` de `AuthViewModel.kt:71-86` repete o que `runSubmit` (`Load.kt:27`) já faz.

### Detalhe do veredito
- Organização dos arquivos: PASS, com S2
- Consistência de nomes: CONCERNS (R5)
- Higiene dos imports: CONCERNS (pequena)
- Reuso de código: CONCERNS (pequena)
- Lint: CONCERNS (R3 e imports; nunca rodado, porque o app nunca compilou)

---

## LENTE 3: Production Readiness

### Resumo
O app conversa por HTTP com a API em `10.0.2.2:8084` (só no emulador e só no build debug). O tratamento de servidor fora, de sessão vencida e de rotação está bem feito. Os riscos são de ambiente: o app nunca foi compilado, o release não consegue falar com a API e o perfil `prod` do back end tem valores padrão perigosos.

### Arquivos revisados
| Arquivo | Papel em produção | Risco |
|---|---|---|
| `data/ApiConfig.kt:10` | URL fixa `http://10.0.2.2:8084/api/v1` | Alto para produção, ok na apresentação |
| `src/debug/res/xml/network_security_config.xml` | HTTP liberado só para 10.0.2.2 e só em debug | Médio |
| `app/build.gradle.kts` | Sem assinatura, sem configuração por build type | Médio |
| `data/remote/ApiClient.kt` | Timeouts e tradução de erros | Baixo |
| `data/SessionStore.kt` | Token em SharedPreferences | Baixo (acadêmico) |
| `backend/src/main/resources/application.yml` | Perfis e Flyway | Médio para produção |
| `backend/.../service/EnvioEmail.java` | Não envia e-mail | Conhecido |

### Veredito
- **Para a apresentação: APPROVE WITH CHANGES.** Basta compilar, rodar o roteiro e conhecer os limites listados abaixo.
- **Para produção: NEEDS REWORK.** Faltam URL por ambiente, HTTPS, assinatura, envio de e-mail e configuração segura do perfil `prod`.

### Problemas críticos (corrigir antes de apresentar)
| # | Problema | Arquivo:linha | Falha | Correção |
|---|---|---|---|---|
| P1 | **O app nunca foi compilado nem rodado** (não há Android SDK aqui). São cerca de 3.000 linhas novas de Kotlin/Compose com AGP 9.3.3 e compileSdk 37. Há APIs sensíveis a versão: `LocalActivity`, `@param:` em anotações, `SavedStateHandle` via `viewModel()` e `mutableIntStateOf`. | todo `app/src/main` | Erro de compilação ou crash na hora de apresentar. | No Android Studio: Build > Make, depois `./gradlew :app:testDebugUnitTest :app:lintDebug`, depois o `docs/roteiro-de-teste.md` inteiro no emulador. Fazer isso pelo menos um dia antes. |

### Recomendações
| # | Problema | Arquivo:linha | Impacto | Correção |
|---|---|---|---|---|
| P2 | **O build release não consegue falar com a API.** A URL é `http://…` fixa, e o release não tem `networkSecurityConfig`. Com targetSdk ≥ 28 o Android bloqueia HTTP sem criptografia, e o erro (`UnknownServiceException`, que é uma `IOException`) aparece como "Não foi possível conectar". | `ApiConfig.kt:10`, `src/debug/AndroidManifest.xml:5`, `src/main/AndroidManifest.xml` | Quem gerar um APK de release para mandar ao professor terá um app que não funciona. | Apresentar com o build debug. Para produção: `buildConfigField("String","API_URL",…)` por build type, com HTTPS no release. |
| P3 | **Só funciona no emulador.** Num celular de verdade, 10.0.2.2 não existe, e o debug só libera HTTP para esse endereço. | `ApiConfig.kt:7-10`, `network_security_config.xml:4-6` | Não dá para demonstrar no celular sem mudar duas coisas. | Se for apresentar no celular: trocar o IP nos dois arquivos (ou ler de `local.properties` via `buildConfigField`). |
| P4 | **Limite de login:** 5 tentativas por IP a cada 15 minutos, e 30 requisições públicas por IP a cada 15 minutos. No emulador, todo acesso vem do mesmo IP. | `application.yml:64-70` | Se o professor errar a senha 5 vezes, ou se você cadastrar e entrar várias vezes durante o ensaio, o login trava por até 15 minutos com 429 "Muitas tentativas". | No ensaio, reiniciar o back end zera o limite (ele fica em memória, em `LimiteTaxa.java:21`). Para a demo, aumentar `login-por-ip` no perfil `dev`. |
| P5 | **"Esqueci a senha" não envia e-mail.** O log também não mostra o link nem o token. | `EnvioEmail.java:12-16,35-40` | A recuperação não pode ser concluída na demo. Já está documentado em `README.md` e no roteiro. | Avisar na apresentação. Para produção: `spring-boot-starter-mail`. |
| P6 | **Fuso do aparelho diferente do fuso do servidor.** "Hoje" no app é `LocalDate.now()` do aparelho (`NewExpenseViewModel.kt:22`, `ExpensesViewModel.kt:16`, `HomeViewModel.kt:43`). O servidor usa America/Sao_Paulo e recusa data futura (`Validacao.java:264`, `Relogio.java:35`). | idem | Com o emulador em UTC, uma despesa salva depois das 21h (horário de Brasília) usa a data de amanhã e recebe 422 "A data deve estar entre 2000-01-01 e hoje". Na virada do mês, a Home pede o mês errado. | Configurar o fuso do emulador para São Paulo, ou usar `ZoneId.of("America/Sao_Paulo")` no app. |
| P7 | **O app desiste antes do servidor quando o banco cai.** O app espera 15 s (`ApiClient.kt:38-39,87`). O pool de conexões (Hikari) espera 30 s pelo padrão, e nenhum timeout de conexão está definido no `application.yml`. | idem | Com o banco fora, o app mostra "Não foi possível conectar ao servidor" em vez do 503 "Serviço indisponível", e a mensagem fica enganosa. Ele se recupera com "Tentar novamente". | `spring.datasource.hikari.connection-timeout: 5000`. |
| P8 | **Despesas ou aportes duplicados.** Um POST que estoura o tempo pode ter sido gravado. Aparece erro de rede, a pessoa toca em Salvar de novo e grava duas vezes. Salvar a simulação, voltar e salvar de novo também duplica. | `NewExpenseViewModel.kt:46-50`, `GoalDetailViewModel.kt:53-64`, `SobraNavHost.kt:466` | Dados duplicados. Não há como apagar, porque a API só cria e lista (D-11). | Baixo para a apresentação. Para produção: chave de idempotência, ou rota de exclusão. |
| P9 | **Nenhum log no app.** `loadResult` e `runSubmit` engolem a exceção. | `Load.kt:14-34` | Na demo, se algo der errado, o Logcat não mostra nada. | `Log.w("Sobra", "falha", e)` nos `catch`. |
| P10 | **Perfil `prod` do back end inseguro por padrão:** (a) o perfil padrão é `dev` (`application.yml:9`), e um deploy sem `SPRING_PROFILES_ACTIVE=prod` carrega os dados de exemplo da V003; (b) usuário e senha do banco caem em `sobra`/`sobra` se as variáveis faltarem (`:12-14`); (c) `url-publica` padrão é `http://localhost:8084` (`:48`), e é ela que monta o link de redefinição; (d) com o mesmo banco usado antes em `dev`, o Flyway do `prod` encontra uma V003 aplicada que não está na pasta dele e falha na validação ao subir. | `application.yml:9,12-14,48,81` | Dados falsos ou senha fraca em produção, ou um servidor que não sobe. | No perfil `prod`, sem valores padrão (`${SOBRA_DB_SENHA}`, para falhar cedo). Banco separado para produção. Documentar `SPRING_PROFILES_ACTIVE=prod`. |
| P11 | **Processo morto no meio da configuração inicial.** O token é salvo no cadastro. Se o app morrer na tela de Renda ou de Categorias, a Splash vê o token e vai direto para a Home (`SobraNavHost.kt:176`), pulando a configuração. | idem | Conta sem renda e sem categorias. A Home mostra renda 0. Dá para recuperar entrando de novo, porque o login respeita `configuracao_concluida`. | Guardar `configuracao_concluida` no `SessionStore` e checar na Splash. |
| P12 | **Erro invisível em Simulações salvas.** Tocar numa simulação salva recalcula. Se falhar, o erro vai para `simulation.error`, que só aparece na tela Simular, e não na lista. | `SobraNavHost.kt:482-485` | O toque parece não fazer nada. | Mostrar o erro na própria lista (snackbar ou texto). |

### Melhorias opcionais
| # | Sugestão | Justificativa |
|---|---|---|
| N4 | Guardar o token com EncryptedSharedPreferences ou com DataStore + Keystore | O token está em texto puro em `MODE_PRIVATE`. O backup desligado (`AndroidManifest.xml:8`) ajuda e já é aceitável no escopo acadêmico. |
| N5 | `signingConfig` para release e `versionCode` automático | `build.gradle.kts:22-28` não tem assinatura. |
| N6 | Paginação de despesas e objetivos | Só as simulações têm limite (`?limite=50`). Para o volume acadêmico, está ok. |
| N7 | Um teste de ViewModel com repositório falso (depende de S4) | Os testes atuais (Money, ApiException, Finance, Formatters) cobrem funções puras. Nada cobre o fluxo. |

### Tabela de modos de falha
| Cenário | O que falha | Impacto para a pessoa | Recuperação | O código trata? |
|---|---|---|---|---|
| Servidor desligado | `IOException` → `SEM_CONEXAO` | Tela de erro com "Tentar novamente"; nos formulários, mensagem vermelha | Tocar em "Tentar novamente" | Sim (`ApiClient.kt:59-60`, `SobraNavHost.kt:311-315`) |
| Banco fora | Servidor espera 30 s pelo pool; app desiste em 15 s | Mensagem "não foi possível conectar" (enganosa) | "Tentar novamente" | Parcial (P7) |
| Sessão vencida ou token desconhecido | 401 `SESSAO_EXPIRADA`/`NAO_AUTENTICADO` | Tela 24 com a pilha de navegação limpa | Fazer login | Sim (`ApiException.kt:19`, `SobraNavHost.kt:162-164`) |
| Logout com token já vencido | Nada (a rota é pública e idempotente) | Vai para o login | — | Sim |
| Rede lenta | Timeout de 15 s | Botão em "Salvando…"; depois, erro | Tentar de novo (risco de duplicar) | Parcial (P8) |
| Toques repetidos em Salvar | Bloqueados por `if (loading) return` | — | — | Sim |
| Rotação da tela | ViewModels sobrevivem; `LaunchedEffect` chama `refresh()` de novo, sem piscar | Nenhum | — | Sim |
| Morte do processo (Simular → Resultado) | `result` perdido | Volta para o formulário | — | Sim (`SobraNavHost.kt:457-459`) |
| Morte do processo durante a configuração inicial | A Splash pula para a Home | Configuração incompleta | Login de novo | Não (P11) |
| Morte do processo em Categorias | O `SetupViewModel` perde a renda (já salva no servidor) | A tela mostra renda 0 na comparação | Voltar para Renda | Parcial (baixo) |
| Fuso do aparelho diferente de America/Sao_Paulo | Data e mês "de hoje" divergem | 422 ao salvar despesa à noite | Escolher outra data | Não (P6) |
| Relógio do aparelho adiantado | Idem, com data futura | 422 | — | Não |
| Muitas tentativas de login | 429 | "Muitas tentativas…" por até 15 minutos | Esperar ou reiniciar o back end | Mensagem sim, demo frágil (P4) |
| Valor acima do limite do servidor | 422 `FORA_DO_LIMITE` | Mensagem do servidor | Corrigir o valor | Sim (exceto em Simular, N2) |
| Resposta fora do formato | `JSONException`, `NumberFormatException` ou `DateTimeParseException` | Tela de erro | — | Sim (`Load.kt:14-24`) |
| Muitos dados | Listas sem paginação | Lentidão em contas enormes | — | Aceitável (N6) |
| Recuperação de senha | E-mail não sai | Fluxo não conclui | — | Documentado (P5) |

### Checklist de ambientes
| Ponto | Dev | Prod | Falta algo? |
|---|---|---|---|
| URL da API | `http://10.0.2.2:8084` fixa (`ApiConfig.kt:10`) | Não existe | **Sim** (P2, P3) |
| HTTP x HTTPS | HTTP liberado só para 10.0.2.2 no debug | Release bloqueia HTTP; servidor `prod` exige HTTPS via proxy | **Sim** |
| Build debug x release | Debug funciona | Release sem assinatura, sem URL e com otimização desligada | **Sim** |
| minSdk | 26 (`java.time` disponível desde a API 26) | idem | Não. O diff baixou de 37 para 26, e foi correto. |
| Nunca compilado | — | — | **Sim** (P1) |
| Variáveis de ambiente do back end | Valores padrão `sobra/sobra`, localhost | Os mesmos padrões, que deveriam falhar | **Sim** (P10) |
| Perfis Spring | `dev` é o padrão, com V003 | `prod` precisa ser ativado à mão | **Sim** (P10) |
| Flyway | V001, V002 + V003 (`db/exemplo`) em dev e test | Só V001 e V002 | Banco compartilhado entre dev e prod quebra a validação (P10) |
| Fuso | O app usa o do aparelho | O servidor usa America/Sao_Paulo (`application.yml:44`) | **Sim** (P6) |
| E-mail | Só registra no log | Precisa de SMTP | **Sim** (P5) |
| Limite de requisições | Em memória, 5 logins por IP | Em memória: não funciona com mais de uma instância | Aceitável para uma instância |
| Banco | `docker compose` com Postgres 16 | Sem plano de backup | Fora do escopo |

### O que falta para a apresentação (em ordem)
1. Compilar e rodar os testes e o lint (P1).
2. Rodar `docs/roteiro-de-teste.md` inteiro, incluindo a seção 6 (falhas).
3. Fixar o fuso do emulador em São Paulo (P6) e aumentar `login-por-ip` no perfil `dev` ou reiniciar o back end antes (P4).
4. Ter uma resposta pronta para "o e-mail de recuperação não chega" (P5) e para "por que a taxa aparece no app e no servidor" (S1).
5. Opcional, mas barato: limpar R1, R2 e R3.

### O que falta para produção
P2, P3, P5, P7, P8, P10, N4 e N5. Também uma URL por ambiente via `buildConfigField` e a taxa vindo só do servidor (S1).

### Detalhe do veredito
- Fluxo de ponta a ponta: CONCERNS (nunca compilado)
- Recuperação de erros: PASS, com P7, P8 e P12
- Integridade dos dados: CONCERNS (duplicação em P8; o resto está bem: dinheiro em BigDecimal/texto e regras validadas no servidor)
- Paridade entre ambientes: CONCERNS (P2, P3, P6, P10)
- Observabilidade: CONCERNS no app (P9); PASS no back end

---

## Arquivos-chave
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/data/remote/ApiClient.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/data/ApiConfig.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/domain/Finance.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/ui/screens/simulate/SimulateScreens.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/navigation/SobraNavHost.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/ui/model/Models.kt`
- `/home/user/Sobra/app/src/main/java/com/unasp/sobra/data/SessionStore.kt`
- `/home/user/Sobra/app/src/main/res/values/strings.xml`
- `/home/user/Sobra/app/build.gradle.kts`
- `/home/user/Sobra/backend/src/main/resources/application.yml`
- `/home/user/Sobra/backend/src/main/java/com/Sobra/Aplicativo/service/EnvioEmail.java`