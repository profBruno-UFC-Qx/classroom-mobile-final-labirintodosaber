package com.labirintodosaber.ui.screen.activities

import com.labirintodosaber.data.model.TaskCategory

object ActivitiesMockData {

    val tasks: List<MockTask> = listOf(
        MockTask(
            id = "t1",
            prompt = "Qual é a sílaba inicial da palavra mostrada na imagem?",
            category = TaskCategory.READING,
            alternatives = listOf(
                MockAlternative("a1", "BA", isCorrect = false),
                MockAlternative("a2", "MA", isCorrect = true),
                MockAlternative("a3", "LA", isCorrect = false),
                MockAlternative("a4", "CA", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t2",
            prompt = "Identifique a letra que inicia o nome do animal da figura.",
            category = TaskCategory.READING,
            alternatives = listOf(
                MockAlternative("a1", "G", isCorrect = false),
                MockAlternative("a2", "P", isCorrect = false),
                MockAlternative("a3", "C", isCorrect = true),
                MockAlternative("a4", "T", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t3",
            prompt = "Qual palavra rima com 'PATO'?",
            category = TaskCategory.VOCABULARY,
            alternatives = listOf(
                MockAlternative("a1", "BOLO", isCorrect = false),
                MockAlternative("a2", "GATO", isCorrect = true),
                MockAlternative("a3", "MURO", isCorrect = false),
                MockAlternative("a4", "FITA", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t4",
            prompt = "Complete a frase: 'O menino _____ no parque.'",
            category = TaskCategory.COMPREHENSION,
            alternatives = listOf(
                MockAlternative("a1", "BRINCA", isCorrect = true),
                MockAlternative("a2", "DORME", isCorrect = false),
                MockAlternative("a3", "COME", isCorrect = false),
                MockAlternative("a4", "LÊ", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t5",
            prompt = "Ordene as sílabas para formar uma palavra: BO - LA",
            category = TaskCategory.WRITING,
            alternatives = listOf(
                MockAlternative("a1", "BOLA", isCorrect = true),
                MockAlternative("a2", "LABO", isCorrect = false),
                MockAlternative("a3", "OLAB", isCorrect = false),
                MockAlternative("a4", "ABOL", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t6",
            prompt = "Qual imagem representa a palavra 'ESCOLA'?",
            category = TaskCategory.VOCABULARY,
            alternatives = listOf(
                MockAlternative("a1", "Casa", isCorrect = false),
                MockAlternative("a2", "Parque", isCorrect = false),
                MockAlternative("a3", "Prédio com alunos", isCorrect = true),
                MockAlternative("a4", "Mercado", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t7",
            prompt = "Quantas sílabas tem a palavra 'BORBOLETA'?",
            category = TaskCategory.READING,
            alternatives = listOf(
                MockAlternative("a1", "2", isCorrect = false),
                MockAlternative("a2", "3", isCorrect = false),
                MockAlternative("a3", "4", isCorrect = true),
                MockAlternative("a4", "5", isCorrect = false),
            ),
        ),
        MockTask(
            id = "t8",
            prompt = "Qual é o antônimo de 'FELIZ'?",
            category = TaskCategory.COMPREHENSION,
            alternatives = listOf(
                MockAlternative("a1", "Alegre", isCorrect = false),
                MockAlternative("a2", "Triste", isCorrect = true),
                MockAlternative("a3", "Animado", isCorrect = false),
                MockAlternative("a4", "Contente", isCorrect = false),
            ),
        ),
    )

    // Cadernos contêm atividades (tasks)
    val notebooks: List<MockNotebook> = listOf(
        MockNotebook(
            id = "n1",
            title = "Reconhecimento de Letras",
            subtitle = "Identificação de letras do alfabeto",
            categories = listOf(TaskCategory.READING),
            iconColorHex = 0xFF5CC8C0,
            taskIds = listOf("t1", "t2"),
        ),
        MockNotebook(
            id = "n2",
            title = "Formação de Sílabas",
            subtitle = "Combinação de letras para formar sílabas",
            categories = listOf(TaskCategory.READING, TaskCategory.WRITING),
            iconColorHex = 0xFF7EC8C8,
            taskIds = listOf("t1", "t5", "t7"),
        ),
        MockNotebook(
            id = "n3",
            title = "Rimas e Vocabulário",
            subtitle = "Atividades lúdicas com rimas e palavras",
            categories = listOf(TaskCategory.VOCABULARY),
            iconColorHex = 0xFFE94B8F,
            taskIds = listOf("t3", "t6"),
        ),
        MockNotebook(
            id = "n4",
            title = "Compreensão de Textos",
            subtitle = "Interpretação e compreensão de frases",
            categories = listOf(TaskCategory.COMPREHENSION),
            iconColorHex = 0xFFE5A820,
            taskIds = listOf("t4", "t8"),
        ),
        MockNotebook(
            id = "n5",
            title = "Produção Escrita",
            subtitle = "Exercícios de escrita e ortografia",
            categories = listOf(TaskCategory.WRITING),
            iconColorHex = 0xFF50C878,
            taskIds = listOf("t5", "t7"),
        ),
    )

    // Grupos contêm cadernos (notebooks)
    val groups: List<MockGroup> = listOf(
        MockGroup(
            id = "g1",
            name = "Alfabetização Divertida",
            description = "Atividades de reconhecimento de letras e sons",
            category = TaskCategory.READING,
            iconColorHex = 0xFF5CC8C0,
            notebookIds = listOf("n1", "n2"),
        ),
        MockGroup(
            id = "g2",
            name = "Histórias Ilustradas",
            description = "Compreensão de histórias curtas com imagens",
            category = TaskCategory.COMPREHENSION,
            iconColorHex = 0xFF7EC8C8,
            notebookIds = listOf("n3", "n4"),
        ),
        MockGroup(
            id = "g3",
            name = "Rimas e Poesias",
            description = "Atividades lúdicas com rimas e vocabulário",
            category = TaskCategory.VOCABULARY,
            iconColorHex = 0xFFF4A0A0,
            notebookIds = listOf("n3", "n5"),
        ),
    )

    fun taskById(id: String) = tasks.find { it.id == id }
    fun groupById(id: String) = groups.find { it.id == id }
    fun notebookById(id: String) = notebooks.find { it.id == id }

    fun tasksForNotebook(notebookId: String): List<MockTask> {
        val notebook = notebookById(notebookId) ?: return emptyList()
        return notebook.taskIds.mapNotNull { taskById(it) }
    }

    fun notebooksForGroup(groupId: String): List<MockNotebook> {
        val group = groupById(groupId) ?: return emptyList()
        return group.notebookIds.mapNotNull { notebookById(it) }
    }
}

data class MockTask(
    val id: String,
    val prompt: String,
    val category: TaskCategory,
    val alternatives: List<MockAlternative>,
)

data class MockAlternative(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)

data class MockGroup(
    val id: String,
    val name: String,
    val description: String,
    val category: TaskCategory,
    val iconColorHex: Long,
    val notebookIds: List<String>,
)

data class MockNotebook(
    val id: String,
    val title: String,
    val subtitle: String,
    val categories: List<TaskCategory>,
    val iconColorHex: Long,
    val taskIds: List<String>,
)
