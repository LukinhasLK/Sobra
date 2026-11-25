# Sobra

App Android de finanças pessoais: mostra quanto **sobra** da renda do mês depois dos gastos e ajuda a
guardar esse valor em objetivos. Em português do Brasil, só em reais.

| Pasta | O que é |
|---|---|
| `app/` | App Android (Kotlin + Jetpack Compose), 24 telas |
| `backend/` | API REST (Java 21 + Spring Boot + PostgreSQL + Flyway). Detalhes em [`backend/README.md`](backend/README.md) |
| `docs/` | Documentos de apoio |

## Como rodar tudo junto

**1. Back end** (precisa do Docker, ou de um PostgreSQL 16 com os bancos `sobra` e `sobra_test`):

```bash
cd backend
docker compose up -d
./mvnw spring-boot:run        # sobe na porta 8084
```

**2. App**: abra a pasta raiz no Android Studio, aguarde o Gradle sincronizar e rode no **emulador**.
No emulador, `10.0.2.2` é o computador onde a API está rodando (`app/.../data/ApiConfig.kt`).
A versão debug libera HTTP só para esse endereço.

**3. Conta de teste**: crie uma pela tela de cadastro (a API valida a senha: 8 a 64 caracteres, sem senhas comuns).

## O que o app faz com a API

| Tela | Rotas |
|---|---|
| Cadastro, login, sair | `POST /auth/cadastro`, `/auth/login`, `/auth/logout` |
| Recuperar senha | `POST /auth/recuperar-senha` (hoje o e-mail só é registrado no log do servidor) |
| Renda e categorias | `PUT /configuracao/renda`, `/configuracao/categorias` |
| Home | `GET /home` |
| Gastos e nova despesa | `GET /despesas`, `POST /despesas` |
| Objetivos, detalhe, aporte | `GET/POST /objetivos`, `GET /objetivos/{id}`, `POST /objetivos/{id}/aportes` |
| Simular e salvas | `POST /simulacoes/calcular`, `POST/GET /simulacoes` |
| Perfil | `GET /perfil`, `PUT /perfil/notificacoes` |

Sessão vencida ou token desconhecido leva à tela "Sessão expirada".

## Limites conhecidos

- Editar ou apagar despesa, objetivo e aporte não existe (a API só cria e lista, decisão D-11).
- O envio real de e-mail da recuperação de senha ainda não existe no back end.
- As imagens dos objetivos são as 3 do app; a API guarda `imagem_url`, mas o catálogo ainda não foi decidido.
