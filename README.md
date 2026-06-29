# ✈️ TEM Aerovendas

Marketplace mobile para compra e venda de aeronaves executivas e corporativas — jatos, turboélices, helicópteros e mais. Desenvolvido nativamente em **Kotlin + Jetpack Compose**, com backend em **Firebase**.

> Mercado Executivo e Corporativo de Aeronaves

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)
![Hilt](https://img.shields.io/badge/DI-Hilt-1B6AC6?style=flat)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-success?style=flat)

---

## 📱 Sobre o projeto

O **TEM Aerovendas** conecta compradores e vendedores de aeronaves em um único app: anúncios com fichas técnicas completas, favoritos, publicação de aeronaves para venda e contato direto entre as partes — tudo com autenticação segura e notificações em tempo real.

## ✨ Funcionalidades

- **Comprar** — vitrine de aeronaves com busca e filtro por categoria (jato executivo, turboélice, pistão, helicóptero, jato leve/pesado, ultra long range).
- **Ficha técnica detalhada** — registro, ano, horas disponíveis, ciclos, motores, configuração, aviônica, fotos e condições de pagamento (entrada + parcelas).
- **Vender** — formulário de cadastro de aeronave para anúncio, com fluxo de aprovação.
- **Favoritos** — salvar aeronaves de interesse, com cache local (Room) para acesso offline.
- **Perfil** — dados do usuário, meus anúncios e lista de favoritos em um só lugar.
- **Autenticação** — login com Google ou e-mail/senha (Firebase Auth), com cadastro de novo usuário.
- **Termo de responsabilidade** — disclaimer obrigatório de inspeção pré-compra antes de qualquer solicitação de proposta.
- **Notificações push** — alertas em tempo real via Firebase Cloud Messaging.

## 🏗️ Arquitetura

Clean Architecture + MVVM, organizada em três camadas:

```
ui/         → Composables, ViewModels e estado de tela (Jetpack Compose)
domain/     → Modelos, casos de uso e contratos de repositório (puro Kotlin)
data/       → Implementação dos repositórios, Firebase, Room e serviços
```

A camada `domain` não conhece Android nem Firebase — apenas interfaces, o que mantém a lógica de negócio testável e independente de infraestrutura.

## 🛠️ Stack técnica

| Camada              | Tecnologia                                      |
|---------------------|--------------------------------------------------|
| UI                  | Jetpack Compose, Material 3, Navigation Compose   |
| Injeção de dependência | Hilt                                           |
| Autenticação        | Firebase Auth (Google Sign-In + e-mail/senha)     |
| Banco remoto        | Cloud Firestore                                   |
| Armazenamento       | Firebase Storage (fotos das aeronaves)            |
| Cache local         | Room                                              |
| Notificações        | Firebase Cloud Messaging                          |
| Imagens             | Coil                                              |
| Concorrência        | Kotlin Coroutines + Flow                          |

## 📂 Estrutura de pastas

```
app/src/main/java/com/temaerovendas/
├── data/
│   ├── local/          # Room (entities, DAO, database)
│   └── repository/     # Implementações dos repositórios
├── di/                  # Módulos Hilt
├── domain/
│   ├── model/           # Aircraft, User, Disclaimer
│   ├── repository/      # Interfaces de repositório
│   └── usecase/         # Casos de uso (regras de negócio)
├── navigation/           # NavGraph e definição de rotas
├── notifications/        # Serviço de push (FCM)
└── ui/
    ├── components/       # Componentes reutilizáveis
    ├── screens/          # splash, login, signup, list, detail, register, profile, disclaimer
    └── theme/             # Cores, tipografia e tema Material 3
```

## 🚀 Como executar

1. Disponível na PlayStore: https://play.google.com/store/apps/details?id=com.temaerovendas
2. Apenas em teste Alpha no momento

## 📋 Pré-requisitos

- Android Studio (versão atual recomendada)
- JDK 17+
- Conta Firebase configurada com Firestore, Authentication, Storage e Cloud Messaging habilitados

## 🗺️ Roadmap

- [ ] Filtros avançados (faixa de preço, ano, localização)
- [ ] Painel administrativo de aprovação de anúncios
- [ ] Painel de usuários

## 📄 Licença

MIT License

Copyright (c) 2026 Matheus Guerra

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
---

Desenvolvido por Matheus Guerra.
