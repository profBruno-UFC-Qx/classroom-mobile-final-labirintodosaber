package com.labirintodosaber.ui.screen.activities

import com.labirintodosaber.data.model.TaskCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskCategoryExtTest {

    @Test
    fun `displayName returns correct Portuguese label for each category`() {
        assertEquals("Leitura", TaskCategory.READING.displayName())
        assertEquals("Escrita", TaskCategory.WRITING.displayName())
        assertEquals("Vocabulário", TaskCategory.VOCABULARY.displayName())
        assertEquals("Compreensão", TaskCategory.COMPREHENSION.displayName())
    }

    @Test
    fun `each category has a distinct color`() {
        val colors = TaskCategory.entries.map { it.colorHex() }
        assertEquals(TaskCategory.entries.size, colors.distinct().size)
    }

    @Test
    fun `colorHex returns expected values`() {
        assertEquals(0xFF5CC8C0L, TaskCategory.READING.colorHex())
        assertEquals(0xFF50C878L, TaskCategory.WRITING.colorHex())
        assertEquals(0xFFE94B8FL, TaskCategory.VOCABULARY.colorHex())
        assertEquals(0xFFE5A820L, TaskCategory.COMPREHENSION.colorHex())
    }
}
