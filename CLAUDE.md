# Labirinto do Saber — Convenções do Projeto

Projeto Android nativo em Kotlin + Jetpack Compose.  
Package: `com.labirintodosaber` | minSdk 26 | targetSdk 35

---

- Build + instalar: `.\gradlew.bat installDebug`
- Abrir o app: `adb shell am start -n com.labirintodosaber/.MainActivity`
- Só build (sem instalar): `.\gradlew.bat assembleDebug`

## Estrutura de Pacotes

```
app/src/main/java/com/labirintodosaber/
├── ui/
│   ├── components/       # Composables reutilizáveis (sem estado próprio)
│   ├── navigation/       # AppNavGraph, AppDestination
│   ├── screen/<tela>/    # <Tela>Screen.kt + <Tela>ViewModel.kt + UiState + Action
│   └── theme/            # Color.kt, Theme.kt, Type.kt
├── data/
│   ├── model/            # Data classes de domínio
│   └── repository/       # Interfaces e implementações de repositório
├── domain/
│   └── usecase/          # Casos de uso (lógica de negócio pura)
├── LabirintodoSaberApp.kt
└── MainActivity.kt
```

---

## 1. Convenções de Nomenclatura

### Composables

- **CamelCase e obrigatoriamente um substantivo.**
- Adjetivos descritivos são permitidos como prefixo.

```kotlin
// Correto
fun PrimaryButton(...)    // substantivo
fun RoundIcon(...)        // adjetivo + substantivo
fun LoadingOverlay(...)   // substantivo

// Errado — nunca usar
fun DrawTextField(...)    // verbo
fun TextFieldWithLink(...)// preposição substantiva
fun Clickable(...)        // adjetivo isolado
```

---

## 2. Padrões de Sintaxe Kotlin

### Imutabilidade

Preferir `val` a `var`. Usar `var` somente quando a mutação é inevitável e local.

```kotlin
// Correto
val userId = "abc-123"
val items = listOf("a", "b")

// Evitar
var counter = 0  // só aceitável quando realmente mutado no mesmo escopo
```

### Null-Safety

Usar tipos anuláveis, operador de chamada segura e Elvis explicitamente.

```kotlin
val name: String? = user?.profile?.name
val displayName: String = name ?: "Anônimo"

// Smart cast — Kotlin infere após verificação
if (name != null) {
    println(name.length)  // sem !! necessário
}
```

### if/when como expressão

Nunca operador ternário ou switch. `if` e `when` retornam valor.

```kotlin
val label = if (isLoading) "Carregando..." else "Pronto"

val message = when (errorCode) {
    404 -> "Não encontrado"
    500 -> "Erro interno"
    else -> "Erro desconhecido"
}
```

### Trailing Lambda

Quando o último parâmetro é uma lambda, ela fica fora dos parênteses.

```kotlin
// Correto
Button(onClick = { doSomething() }) {
    Text("Clique")
}

items.forEach { item ->
    process(item)
}
```

---

## 3. Padrões de UI — Jetpack Compose

### Parâmetro Modifier

**Toda** função `@Composable` deve aceitar `modifier: Modifier = Modifier` e passá-lo ao primeiro filho.

```kotlin
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,   // sempre presente, sempre com default
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(label)
    }
}
```

### Ordem dos Modificadores

A ordem importa: cada `Modifier` altera o retorno do anterior.

```kotlin
// padding ANTES de clickable → área de clique inclui o padding
Modifier.padding(16.dp).clickable { }

// clickable ANTES de padding → área de clique NÃO inclui o padding
Modifier.clickable { }.padding(16.dp)
```

### String Resources

Nunca hardcode de strings na UI. Usar `stringResource`.

```kotlin
// Correto
Text(text = stringResource(R.string.home_title))

// Errado
Text(text = "Labirinto do Saber")
```

### Acessibilidade

`contentDescription` é obrigatório em imagens e ícones.  
Passar `null` apenas se o elemento for puramente decorativo (a ferramenta de acessibilidade ignora).

```kotlin
Icon(
    imageVector = Icons.Default.Person,
    contentDescription = stringResource(R.string.profile_icon_desc),
)

// Decorativo — null explícito
Icon(
    imageVector = Icons.Default.Circle,
    contentDescription = null,
)
```

---

## 4. Arquitetura e Gerenciamento de Estado

### Padrão por Tela

Cada tela tem três artefatos dentro do pacote `ui/screen/<tela>/`:

| Arquivo                 | Responsabilidade                                                                                    |
| ----------------------- | --------------------------------------------------------------------------------------------------- |
| `<Tela>Screen.kt`       | Composable **stateful** (conecta ao ViewModel) + Composable **stateless** (recebe estado/callbacks) |
| `<Tela>ViewModel.kt`    | Lógica de negócio, `StateFlow<UiState>`, função `onAction(action)`                                  |
| _(dentro do ViewModel)_ | `data class <Tela>UiState` e `sealed interface <Tela>Action`                                        |

### State Hoisting

Mover estado para o chamador. O composable stateless é testável de forma isolada.

```kotlin
// Stateful — conecta ao ViewModel (não testável unitariamente)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onAction = viewModel::onAction)
}

// Stateless — recebe tudo por parâmetro (fácil de testar e fazer Preview)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) { ... }
```

### Single Source of Truth

Estado deve ser elevado ao ancestral comum mais próximo que precise lê-lo ou alterá-lo.  
Nunca duplicar estado entre composables — um lê, o outro recebe por parâmetro.

### Persistência de Estado

| Situação                                         | Ferramenta                                |
| ------------------------------------------------ | ----------------------------------------- |
| Sobreviver a recomposições                       | `remember { }`                            |
| Sobreviver a rotação de tela / morte do processo | `rememberSaveable { }`                    |
| Estado complexo / objetos grandes                | `ViewModel` (nunca no `rememberSaveable`) |

```kotlin
// Simples — sobrevive à recomposição
val expanded = remember { mutableStateOf(false) }

// Persistente — sobrevive à rotação
val query = rememberSaveable { mutableStateOf("") }

// Evitar: objetos grandes no rememberSaveable causam TransactionTooLargeException
```

---

## 5. Convenções do Ciclo de Vida

### Gerenciamento de Recursos

| Recurso                    | Iniciar      | Liberar     | Motivo                                            |
| -------------------------- | ------------ | ----------- | ------------------------------------------------- |
| Câmera, GPS (interativos)  | `onResume()` | `onPause()` | Libera em multi-janela quando a janela perde foco |
| Animações, observers de UI | `onStart()`  | `onStop()`  | Libera quando a Activity fica invisível           |

### Lógica nas Activities

Nunca colocar lógica de negócio diretamente em callbacks da Activity.  
Usar `ViewModel` para estado e lógica, componentes lifecycle-aware para observações.

```kotlin
// Errado
override fun onResume() {
    super.onResume()
    fetchUserData()  // lógica de negócio na Activity
}

// Correto — Activity apenas navega/seta content
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { LabirintodoSaberTheme { AppNavGraph() } }
}
```

---

## 6. Padrões de Navegação

### Callbacks de navegação são responsabilidade do chamador

Ações que causam navegação **não entram no ViewModel**. O `AppNavGraph` injeta lambdas diretamente no composable raiz da tela.

```kotlin
// AppNavGraph — quem decide para onde vai
composable(AppDestination.Login.route) {
    LoginScreen(
        onLoginSuccess = { navController.navigate(...) },
        onRegisterClick = { navController.navigate(...) },
    )
}

// LoginScreen — apenas declara o contrato
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    ...
)
```

Regra prática: se o resultado da ação é "mudar de tela", é callback. Se é "alterar estado ou chamar API", é `Action` no ViewModel.

### Navegação pós-login com limpeza de pilha

Ao navegar para uma tela autenticada, remover as telas de auth da backstack:

```kotlin
navController.navigate(AppDestination.Dashboard.route) {
    popUpTo(AppDestination.Login.route) { inclusive = true }
}
```

### Voltar com popBackStack

Telas que têm botão "Voltar" usam `navController.popBackStack()` — não `navigate` para a tela anterior.

---

## 7. Padrões Visuais Estabelecidos

### Paleta de cores (`ui/theme/Color.kt`)

| Token | Hex | Uso |
|---|---|---|
| `GradientTop` | `#A9DFDB` | Topo do fundo das telas de auth |
| `GradientMid` | `#CBEAE6` | Meio do gradiente |
| `GradientBottom` | `#F4F9F8` | Base do gradiente / fundo de telas logadas |
| `TealPrimary` | `#5CC8C0` | Cor principal, botões de ação |
| `TealLight` | `#A9DFDB` | Ponta clara de gradientes de botão |
| `TealDark` | `#3FADA5` | Ponta escura de gradientes de botão |
| `WarmButtonStart` | `#EDD898` | Botões de etapas intermediárias (ex: recuperar senha) |
| `WarmButtonEnd` | `#F5E6C8` | Ponta clara do botão warm |
| `TextPrimary` | `#1A1A1A` | Texto principal |
| `TextSecondary` | `#9CA3AF` | Texto secundário, placeholders |
| `InputBackground` | `#F8FAFA` | Fundo dos campos de texto |
| `InputBorder` | `#E5E7EB` | Borda dos campos de texto |

### Fundo das telas de autenticação

Gradiente vertical com `Brush.verticalGradient`:

```kotlin
Modifier.background(
    brush = Brush.verticalGradient(
        colors = listOf(GradientTop, GradientMid, GradientBottom),
    )
)
```

### Botão de ação principal (gradiente teal)

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.horizontalGradient(colors = listOf(TealDark, TealLight)))
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
) {
    Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
}
```

### Botão "Voltar" flutuante (pill)

Usado em telas sobre o gradiente (Register, ForgotPassword):

```kotlin
Surface(
    onClick = onBackClick,
    shape = RoundedCornerShape(50.dp),
    color = Color.White,
    shadowElevation = 2.dp,
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
        Text(stringResource(R.string.back_button))
    }
}
```

### Campos de texto

`OutlinedTextField` com `shape = RoundedCornerShape(12.dp)` e cores:

```kotlin
OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = InputBorder,
    focusedContainerColor = InputBackground,
    unfocusedContainerColor = InputBackground,
)
```

### Card branco (conteúdo de telas de auth)

```kotlin
Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
)
```

---

## 8. Padrões de Fluxos Multi-Etapa

Fluxos com passos sequenciais (ex: recuperação de senha com 3 etapas) usam **um único ViewModel** com um `enum` de passo:

```kotlin
enum class ForgotPasswordStep { EMAIL, CODE, NEW_PASSWORD }

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    ...
)
```

O Screen renderiza conteúdo diferente com `when (uiState.step)`. Não criar rotas separadas para cada etapa do mesmo fluxo.

---

## 9. Cores em Data Models

Data classes que representam itens de lista (sessões, atividades) armazenam cores como `Long` (hex ARGB) para não acoplar o modelo ao Compose. A conversão acontece no composable:

```kotlin
// No modelo
data class SessionItem(val borderColorHex: Long, ...)

// No composable
val borderColor = Color(session.borderColorHex)
```

---

## Build

- **Gradle version catalog**: todas as dependências em `gradle/libs.versions.toml`
- **KSP** (não KAPT) para processamento de anotações (Hilt)
- **Java 17** para compilação
- Gradle Wrapper (`gradlew.bat`) já commitado — não precisa do Android Studio para buildar
