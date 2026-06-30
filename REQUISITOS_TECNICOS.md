# Labirinto do Saber — Requisitos Técnicos Implementados

Documento técnico que demonstra como cada requisito foi atendido, com referências diretas aos arquivos e trechos de código.

---

## 1. Múltiplas telas com navegação

**Arquivo principal:** `ui/navigation/AppNavGraph.kt`

O app usa Jetpack Navigation Compose com um `NavHostController` centralizado. A estrutura de rotas é declarada como uma `sealed class`:

```kotlin
// ui/navigation/AppNavGraph.kt
sealed class AppDestination(val route: String) {
    data object Login          : AppDestination("login")
    data object Register       : AppDestination("register")
    data object ForgotPassword : AppDestination("forgot-password")
    data object Dashboard      : AppDestination("dashboard")
    data object Students       : AppDestination("students")
    data object AddStudent     : AppDestination("add-student")
    data object StudentProfile : AppDestination("student-profile/{studentId}") {
        fun createRoute(studentId: String) = "student-profile/$studentId"
    }
    data object Activities     : AppDestination("activities")
    data object CreateActivity : AppDestination("create-activity")
    data object CreateNotebook : AppDestination("create-notebook")
    data object CreateTaskGroup: AppDestination("create-task-group")
    data object Reports        : AppDestination("reports")
    data object Agenda         : AppDestination("agenda")
    data object SessionSelectStudent : AppDestination("session/select-student")
    data object SessionConfigure     : AppDestination("session/configure/{studentId}") { ... }
    data object SessionRun           : AppDestination("session/run/{studentId}/{contentIds}/{sessionName}") { ... }
    data object SessionReport        : AppDestination("session-report/{sessionId}/{studentId}") { ... }
    // ... demais rotas
}
```

São **20+ destinos** registrados no `NavHost`. Rotas com parâmetros usam `navArgument`:

```kotlin
// ui/navigation/AppNavGraph.kt
composable(
    route = AppDestination.StudentProfile.route,
    arguments = listOf(navArgument("studentId") { type = NavType.StringType }),
) { backStackEntry ->
    val studentId = backStackEntry.arguments?.getString("studentId").orEmpty()
    StudentProfileScreen(
        onBackClick = { navController.popBackStack() },
        onSessionClick = { sessionId ->
            navController.navigate(AppDestination.SessionReport.createRoute(sessionId, studentId))
        },
    )
}
```

A navegação global das telas autenticadas é feita por um `ModalNavigationDrawer` (menu hamburguer) que envolve todo o `NavHost`:

```kotlin
// ui/navigation/AppNavGraph.kt
ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = drawerEnabled && !showProfileSheet,
    drawerContent = {
        AppMenuDrawer(
            currentRoute = currentRoute,
            isDarkTheme = profileUiState.isDarkTheme,
            onToggleTheme = { profileViewModel.onAction(UserProfileAction.OnToggleDarkTheme) },
            onNavigate = { route -> ... },
            onClose = { scope.launch { drawerState.close() } },
        )
    },
) { NavHost(...) }
```

Telas autenticadas que levam a sub-telas passam callbacks de navegação injetados pelo `AppNavGraph` — os ViewModels nunca conhecem rotas.

---

## 2. Cadastro, Edição e Exclusão (CRUD)

### Alunos

**Arquivo:** `data/repository/StudentRepository.kt`

```kotlin
interface StudentRepository {
    suspend fun create(educatorId: String, student: StudentCreateRequest): ApiResult<Student>
    suspend fun getAll(educatorId: String): ApiResult<List<Student>>
    suspend fun getById(studentId: String): ApiResult<Student>
    suspend fun update(studentId: String, request: StudentUpdateRequest): ApiResult<Student>
    suspend fun delete(studentId: String): ApiResult<Unit>
}
```

O **Create** é acionado pelo `AddStudentViewModel` após 8 validações:

```kotlin
// ui/screen/addstudent/AddStudentViewModel.kt
private fun save() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val result = studentRepository.create(
            educatorId = educatorId,
            student = StudentCreateRequest(
                name = state.name.trim(),
                age = state.age.toInt(),
                gender = Gender.fromDisplayName(state.gender),
                zipcode = state.cep.trim(),
                road = state.street.trim(),
                housenumber = state.number.trim(),
                phonenumber = state.phone.trim(),
                topics = state.topics.split(",").map { it.trim() }.filter { it.isNotBlank() },
            )
        )
        when (result) {
            is ApiResult.Success -> _uiState.update { it.copy(saveSuccess = true) }
            is ApiResult.Error   -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }
}
```

### Atividades (Create + Read)

**Arquivo:** `data/repository/TaskRepository.kt` / `data/remote/service/TaskApi.kt`

```kotlin
// TaskApi.kt — endpoints Retrofit
@POST("tasks")
suspend fun createTask(@Body request: CreateTaskRequest): TaskDto

@GET("tasks/educator/{educatorId}")
suspend fun getTasksByEducator(@Path("educatorId") educatorId: String): List<TaskDto>

@DELETE("tasks/{taskId}")
suspend fun deleteTask(@Path("taskId") taskId: String)
```

### Atendimentos (Agenda) — CRUD completo

```kotlin
// AppointmentFormViewModel.kt
fun onAction(action: AppointmentFormAction) = when (action) {
    is AppointmentFormAction.OnSave   -> save()    // POST ou PUT dependendo do appointmentId
    is AppointmentFormAction.OnDelete -> delete()  // DELETE
    ...
}
```

---

## 3. Persistência local de dados (DataStore)

O app usa **Jetpack DataStore** (não Room) para dois fins distintos.

### 3a. Preferências do usuário

**Arquivo:** `data/local/UserPreferencesStore.kt`

```kotlin
@Singleton
class UserPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.createDataStore("user_prefs")

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    val isDarkTheme: Flow<Boolean> =
        dataStore.data.map { it[DARK_THEME_KEY] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }
}
```

A preferência é lida na `MainActivity` e alimenta o tema de toda a aplicação:

```kotlin
// MainActivity.kt
val isDarkTheme by userPreferencesStore.isDarkTheme
    .collectAsState(initial = false)

setContent {
    LabirintodoSaberTheme(darkTheme = isDarkTheme) {
        AppNavGraph()
    }
}
```

### 3b. Token JWT de autenticação

**Arquivo:** `data/remote/auth/AuthTokenStore.kt`

```kotlin
@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val cached = AtomicReference<String?>(null)
    private val TOKEN_KEY = stringPreferencesKey("auth_jwt_token")

    val tokenFlow: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    fun currentToken(): String? =
        cached.get() ?: runBlocking { dataStore.data.first()[TOKEN_KEY].also { cached.set(it) } }

    suspend fun saveToken(token: String) {
        cached.set(token)
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clearToken() {
        cached.set(null)
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
```

O token é limpado automaticamente quando a API retorna 401 (sessão expirada), via interceptor:

```kotlin
// data/remote/interceptor/AuthInterceptor.kt
val response = chain.proceed(request)
if (response.code == 401) {
    runBlocking { tokenStore.clearToken() }
}
return response
```

---

## 4. MaterialTheme com modo claro/escuro

**Arquivo:** `ui/theme/Theme.kt`

Dois `ColorScheme` completos são definidos — um para cada modo:

```kotlin
// ui/theme/Theme.kt
private val LightColorScheme = lightColorScheme(
    primary          = TealPrimary,       // #5CC8C0
    background       = GradientBottom,    // #F4F9F8
    surface          = Color.White,
    surfaceVariant   = InputBackground,   // #F8FAFA
    onSurface        = TextPrimary,       // #1A1A1A
    onSurfaceVariant = TextSecondary,     // #9CA3AF
)

private val DarkColorScheme = darkColorScheme(
    primary          = TealPrimary,
    background       = DarkBackground,    // #121212
    surface          = DarkSurface,       // #1E1E1E
    surfaceVariant   = DarkSurfaceVariant,// #2C2C2C
    onSurface        = DarkOnSurface,     // #E1E1E1
    onSurfaceVariant = DarkTextSecondary, // #9E9E9E
)

@Composable
fun LabirintodoSaberTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
```

O toggle de tema fica no menu hamburguer (`AppMenuDrawer`) e persiste no DataStore. As telas de autenticação (Login, Recuperar Senha) são envolvidas em `LabirintodoSaberTheme(darkTheme = false)` para sempre renderizar no modo claro:

```kotlin
// ui/navigation/AppNavGraph.kt
composable(AppDestination.Login.route) {
    LabirintodoSaberTheme(darkTheme = false) {
        LoginScreen(...)
    }
}
```

Todos os composables usam tokens do tema (`MaterialTheme.colorScheme.*`) — sem cores fixas nos componentes autenticados:

```kotlin
// Exemplo de uso correto em qualquer Screen
Scaffold(containerColor = MaterialTheme.colorScheme.background) { ... }
Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { ... }
```

---

## 5. Integração com API externa

**Arquivo:** `data/remote/ApiCaller.kt`

Toda chamada à API passa por um wrapper que normaliza os resultados em `ApiResult<T>`:

```kotlin
// data/remote/ApiCaller.kt
suspend fun <T> call(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: IOException) {
    ApiResult.Error(type = ApiErrorType.NETWORK, message = "Sem conexão com o servidor.")
} catch (e: HttpException) {
    toError(e)
} catch (e: Exception) {
    ApiResult.Error(type = ApiErrorType.UNKNOWN, message = defaultMessage(ApiErrorType.UNKNOWN))
}

private fun toError(e: HttpException): ApiResult.Error {
    val type = when (e.code()) {
        400       -> ApiErrorType.BAD_REQUEST
        401       -> ApiErrorType.UNAUTHORIZED
        403       -> ApiErrorType.FORBIDDEN
        404       -> ApiErrorType.NOT_FOUND
        409, 422, 429 -> ApiErrorType.CONFLICT
        in 500..599   -> ApiErrorType.SERVER
        else          -> ApiErrorType.UNKNOWN
    }
    return ApiResult.Error(type = type, message = parseBody(e) ?: defaultMessage(type))
}
```

Os endpoints são interfaces Retrofit. Exemplo do `EducatorApi`:

```kotlin
// data/remote/service/EducatorApi.kt
interface EducatorApi {
    @POST("auth/login")
    @Headers("X-No-Auth: true")
    suspend fun signIn(@Body request: SignInRequest): SignInResponse

    @GET("educators/me")
    suspend fun me(): Educator

    @PUT("educators/me")
    suspend fun updateEducator(@Body request: UpdateEducatorRequest): Educator

    @Multipart
    @PUT("educators/me/photo")
    suspend fun updateProfilePicture(@Part photo: MultipartBody.Part): Educator
}
```

O token JWT é injetado automaticamente em toda requisição autenticada pelo `AuthInterceptor`:

```kotlin
// data/remote/interceptor/AuthInterceptor.kt
override fun intercept(chain: Interceptor.Chain): Response {
    val token = tokenStore.currentToken()
    val request = original.newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
    val response = chain.proceed(request)
    if (response.code == 401) {
        runBlocking { tokenStore.clearToken() }  // limpa token expirado
    }
    return response
}
```

---

## 6. Integração com câmera e galeria

**Arquivo:** `ui/screen/addstudent/AddStudentScreen.kt` e `ui/screen/userprofile/UserProfileScreen.kt`

O cadastro de aluno e o perfil do educador permitem selecionar foto da galeria ou tirar foto com a câmera. O fluxo usa `ActivityResultContracts`:

```kotlin
// ui/screen/addstudent/AddStudentScreen.kt
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success) onAction(AddStudentAction.OnPhotoCaptured(cameraUri))
}

val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri ->
    uri?.let { onAction(AddStudentAction.OnPhotoSelected(it)) }
}
```

A foto selecionada é convertida em `FileUpload` e enviada para a API via `multipart/form-data`:

```kotlin
// data/remote/MultipartFactory.kt
fun filePart(name: String, file: FileUpload): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        name,
        file.name,
        file.bytes.toRequestBody(file.mimeType.toMediaType()),
    )
```

O endpoint que recebe a foto:

```kotlin
// data/remote/service/EducatorApi.kt
@Multipart
@PUT("educators/me/photo")
suspend fun updateProfilePicture(@Part photo: MultipartBody.Part): Educator
```

---

## 7. Testes unitários

**Pasta:** `app/src/test/java/com/labirintodosaber/`

Cobertura atual: **90 testes, 0 falhas**. Stack: JUnit 4 + MockK + `kotlinx-coroutines-test`.

| Suíte | Testes | O que cobre |
|---|---|---|
| `AddStudentViewModelTest` | 19 | 8 regras de validação, filtros de input (apenas dígitos em telefone/CEP), sucesso e erro de API |
| `ForgotPasswordViewModelTest` | 19 | `maskedEmail` (5 casos), fluxo multi-step (EMAIL→CODE→NEW_PASSWORD), filtro alfanumérico do token |
| `ApiCallerTest` | 22 | Todos os códigos HTTP (400/401/403/404/409/500+), IOException, corpo JSON com `message`/`error`/`validationErrors` |
| `LoginViewModelTest` | 7 | Validação de campos, login bem-sucedido, erro de API, toggle de senha |
| `SessionRunUiStateTest` | 14 | `timerLabel` (formatação mm:ss), `isLastTask`, `isFinished`, `currentTask` com índice out-of-bounds |
| `StudentRepositoryTest` | 2 | Cache hit (não bate a API), cache miss com erro de rede |
| `FlexibleEducatorSerializerTest` | 3 | Deserialização de ID como UUID string e como objeto aninhado |
| `TaskCategoryExtTest` | 3 | Labels em português por categoria, cores associadas |

Exemplo de teste de ViewModel com coroutines:

```kotlin
// AddStudentViewModelTest.kt
@Test
fun `blank name shows error`() = runTest {
    viewModel.onAction(AddStudentAction.OnNameChange(""))
    viewModel.onAction(AddStudentAction.OnSaveClick)
    advanceUntilIdle()
    assertEquals("Nome é obrigatório.", viewModel.uiState.value.errorMessage)
}
```

Exemplo de teste do `ApiCaller`:

```kotlin
// ApiCallerTest.kt
@Test
fun `HTTP 401 maps to UNAUTHORIZED`() = runTest {
    val result = apiCaller.call<Unit> { throw httpException(401) }
    val error = result as ApiResult.Error
    assertEquals(ApiErrorType.UNAUTHORIZED, error.type)
}
```

Para rodar:

```bash
.\gradlew.bat :app:testDebugUnitTest
# Relatório HTML: app/build/reports/tests/testDebugUnitTest/index.html
```
