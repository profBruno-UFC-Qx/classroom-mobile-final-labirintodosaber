# Labirinto do Saber

Plataforma mobile para educadores aplicarem atividades personalizadas, acompanharem o progresso de alunos e gerarem relatórios de desempenho.

**Stack:** Android · Kotlin · Jetpack Compose · Hilt · Room · Retrofit · minSdk 26

---

## Como rodar o app

**Pré-requisitos:** Android SDK, `adb` no PATH, Java 17, dispositivo físico ou emulador conectado.

```bash
# Build + instalar no dispositivo conectado
.\gradlew.bat installDebug

# Abrir o app via ADB
adb shell am start -n com.labirintodosaber/.MainActivity

# Só compilar (sem instalar)
.\gradlew.bat assembleDebug
# APK gerado em: app/build/outputs/apk/debug/app-debug.apk
```

---

## Como rodar os testes

Todos os testes são JVM unit tests — sem emulador.

```bash
.\gradlew.bat :app:testDebugUnitTest
```

Relatórios gerados em:
- `app/build/test-results/testDebugUnitTest/` (XML)
- `app/build/reports/tests/testDebugUnitTest/` (HTML)

### Cobertura atual: 90 testes, 0 falhas

| Suíte | Testes | O que cobre |
|---|---|---|
| `AddStudentViewModelTest` | 19 | 8 regras de validação do cadastro, filtros de input, sucesso e erro de API |
| `ForgotPasswordViewModelTest` | 19 | `maskedEmail`, sendCode multi-step, filtro alfanumérico do token, resetPassword |
| `ApiCallerTest` | 22 | Classificação de todos os códigos HTTP, IOException, corpos JSON, exceção genérica |
| `LoginViewModelTest` | 7 | Validação de campos, login, erro de API, toggles |
| `SessionRunUiStateTest` | 14 | `timerLabel`, `isLastTask`, `isFinished`, `currentTask` |
| `StudentRepositoryTest` | 2 | Cache hit, cache miss com erro de rede |
| `FlexibleEducatorSerializerTest` | 3 | Deserialização de UUID string e objeto completo |
| `TaskCategoryExtTest` | 3 | Labels em português, cores por categoria |

---

## Principais fluxos

### Autenticação
Login com email/senha. Recuperação de senha em 3 etapas: informar email → receber código → definir nova senha.

### Cadastro de aluno
Formulário com nome, idade, gênero, endereço, telefone, temas de aprendizagem e foto. 8 regras de validação antes do envio.

### Criação de conteúdo
Na aba Atividades, o botão **+** abre um modal bottom sheet com três opções:
- **Atividade** — questão com enunciado, imagem/áudio opcional e alternativas (mín. 2, exatamente 1 correta)
- **Grupo de Atividades** — agrupa atividades por categoria
- **Caderno** — agrupa grupos com uma descrição e categoria, usado como unidade de sessão

### Sessão ao vivo
`Configurar sessão → Selecionar aluno → Quiz cronometrado → Relatório`

O educador seleciona o conteúdo (tarefas avulsas, grupos ou cadernos), escolhe o aluno e executa o quiz. Cada resposta registra acerto e tempo de resposta. Ao final, exibe taxa de acerto por categoria.

### Perfil do aluno
Histórico de sessões com taxa de acerto, nível de progressão automático (Iniciante / Em desenvolvimento / Avançado, baseado em 40% e 70% de acerto) e geração de relatório PDF individual.

### Dashboard
Visão geral do dia: sessões realizadas hoje, últimas sessões (aluno, data, duração, % de acerto) e contagem de atendimentos agendados.

### Agenda
Atendimentos agrupados por data com cabeçalho contextual (Hoje / Amanhã / data). Status derivado automaticamente: Pendente para datas futuras, Realizado para passadas.

### Relatórios
Análise de desempenho por período (7 dias, 30 dias, 3 meses, 12 meses ou intervalo customizado) com precisão por aluno e exportação em PDF salvo em Downloads.

---

## Como agrega valor

Educadores que atendem alunos individualmente — reforço escolar, fonoaudiologia, pedagogia clínica — precisam criar atividades, aplicá-las em sessão, registrar respostas e acompanhar evolução. O Labirinto do Saber centraliza esse ciclo inteiro num único app mobile:

- **Banco de conteúdo reutilizável** — atividades criadas uma vez são reutilizadas em múltiplos alunos via grupos e cadernos
- **Registro fiel da sessão** — tempo de resposta por questão e acerto ficam salvos automaticamente, sem preenchimento manual
- **Acompanhamento longitudinal** — taxa de acerto acumulada por categoria revela onde o aluno avança e onde trava
- **Comunicação com responsáveis** — relatório em PDF gerado no dispositivo, pronto para compartilhar após cada ciclo
- **Agenda integrada** — sem precisar de agenda externa; os atendimentos ficam no mesmo lugar que os dados dos alunos
