# 📅 Roadmap de Desenvolvimento - Calendário Brasil

Este documento detalha o progresso e as futuras implementações do aplicativo, baseando-se na filosofia de **simplicidade, privacidade e funcionalidade offline**.

---

## ✅ Implementado (Fácil/Médio)
- [x] **Visão Combinada:** Calendário mensal com detalhes do dia selecionado na mesma tela.
- [x] **Visão em Lista:** Alternância para visualização de agenda em formato de lista contínua.
- [x] **Personalização Visual:** Paleta de cores dinâmica e suporte a temas (Claro/Escuro/Sistema).
- [x] **Legenda de Feriados:** Identificação visual por cores para feriados Nacionais, Estaduais e Municipais.
- [x] **Labels de Estado:** Identificação automática da sigla do estado em feriados regionais.
- [x] **Persistência Local:** Implementação robusta com armazenamento seguro e suporte a datas ISO-8601.
- [x] **Estrutura de Eventos:** Suporte inicial para recorrência e horários compatíveis com Google Calendar.

---

qdo eu clico no primeiro icone que parece um calendario nada acontece, nenhuma acao.
E no pesquisar quando eu digito algo deveria mostrar resultados ao que eu digitei. Nao esta acontecendo, nada acontece.
e no utlimo icone os feriados
Qdo clicar em um feriado deveria mostrar informacoes, se ele é nacional, municipal( de qual cidade) e estadual(de qual estado)

## ⏳ Próximas Etapas (Médio/Alto Risco)

### 🚀 Funcionalidades de Gerenciamento
- [ ] **Sistema de Lembretes:**
    - [ ] Notificações locais em horários específicos.
    - [ ] Opção de "Soneca" (Snooze).
    - [ ] Personalização de som e vibração.
- [ ] **Importação e Exportação:**
    - [ ] Importar arquivos `.ics` (iCalendar).
    - [ ] Exportar eventos do usuário para `.ics`.
    - [ ] Backup de configurações em `.txt`.

### 🔄 Conectividade e Sincronismo
- [ ] **Sincronização CalDAV:**
    - [ ] Integração com Google Calendar API.
    - [ ] Suporte a Microsoft Outlook e Nextcloud.
    - [ ] Implementação de lógica de resolução de conflitos (Online vs Local).

### 🎨 UX/UI & Acessibilidade
- [ ] **Widgets de Tela Inicial:**
    - [ ] Widget de visualização mensal.
    - [ ] Widget de "Próximos Eventos".
- [ ] **Internacionalização:**
    - [ ] Tradução da interface para mais de 40 idiomas (i18n).
- [ ] **Aprimoramento de Busca:**
    - [ ] Barra de pesquisa para localizar eventos rapidamente por título ou descrição.

---

## 🔒 Segurança e Performance
- [ ] **Auditoria Open Source:** Revisão do código para garantir que nenhuma permissão desnecessária seja solicitada.
- [ ] **Otimização de Performance:** Garantir fluidez em dispositivos com 2GB de RAM ou menos.
- [ ] **Modo Offline Estrito:** Garantir que todas as funções (exceto sincronização externa) operem sem qualquer pacote de dados.

---

## 🛠️ Notas Técnicas
- **Data Format:** ISO-8601 (YYYY-MM-DD) - *Mantido para compatibilidade total com sistemas externos.*
- **Storage:** Persistência de dados nativa.
- **Architecture:** MVVM (Model-View-ViewModel).
- **UI Framework:** Jetpack Compose.
