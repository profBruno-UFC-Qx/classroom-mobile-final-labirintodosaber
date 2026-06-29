package com.labirintodosaber.ui.screen.sessionrun

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRunUiStateTest {

    private fun aTask(id: String = "t1") = SessionTaskItem(
        id = id,
        imageUrl = null,
        hasAudio = false,
        audioDurationLabel = null,
        prompt = "Enunciado",
        alternatives = listOf(
            SessionAlternative("a1", "Alternativa A", isCorrect = true),
            SessionAlternative("a2", "Alternativa B", isCorrect = false),
        ),
    )

    // ── timerLabel ───────────────────────────────────────────────────────────

    @Test
    fun `timerLabel formats zero seconds as 00 colon 00`() {
        val state = SessionRunUiState(elapsedSeconds = 0)
        assertEquals("00:00", state.timerLabel)
    }

    @Test
    fun `timerLabel formats 59 seconds correctly`() {
        val state = SessionRunUiState(elapsedSeconds = 59)
        assertEquals("00:59", state.timerLabel)
    }

    @Test
    fun `timerLabel formats 61 seconds as 01 colon 01`() {
        val state = SessionRunUiState(elapsedSeconds = 61)
        assertEquals("01:01", state.timerLabel)
    }

    @Test
    fun `timerLabel formats exactly 60 minutes`() {
        val state = SessionRunUiState(elapsedSeconds = 3600)
        assertEquals("60:00", state.timerLabel)
    }

    @Test
    fun `timerLabel pads single-digit seconds with zero`() {
        val state = SessionRunUiState(elapsedSeconds = 605)
        assertEquals("10:05", state.timerLabel)
    }

    // ── isLastTask ───────────────────────────────────────────────────────────

    @Test
    fun `isLastTask is false when not at the last index`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask("t1"), aTask("t2"), aTask("t3")),
            currentTaskIndex = 1,
        )
        assertFalse(state.isLastTask)
    }

    @Test
    fun `isLastTask is true when at the last index`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask("t1"), aTask("t2"), aTask("t3")),
            currentTaskIndex = 2,
        )
        assertTrue(state.isLastTask)
    }

    @Test
    fun `isLastTask is true for a single-task list at index 0`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask()),
            currentTaskIndex = 0,
        )
        assertTrue(state.isLastTask)
    }

    // ── isFinished ───────────────────────────────────────────────────────────

    @Test
    fun `isFinished is false while within task range`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask("t1"), aTask("t2")),
            currentTaskIndex = 1,
        )
        assertFalse(state.isFinished)
    }

    @Test
    fun `isFinished is true when index equals task count`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask("t1"), aTask("t2")),
            currentTaskIndex = 2,
        )
        assertTrue(state.isFinished)
    }

    @Test
    fun `isFinished is false when task list is empty`() {
        val state = SessionRunUiState(tasks = emptyList(), currentTaskIndex = 0)
        assertFalse(state.isFinished)
    }

    // ── currentTask ──────────────────────────────────────────────────────────

    @Test
    fun `currentTask returns the task at currentTaskIndex`() {
        val task2 = aTask("t2")
        val state = SessionRunUiState(
            tasks = listOf(aTask("t1"), task2, aTask("t3")),
            currentTaskIndex = 1,
        )
        assertEquals(task2, state.currentTask)
    }

    @Test
    fun `currentTask returns null when index is out of bounds`() {
        val state = SessionRunUiState(
            tasks = listOf(aTask()),
            currentTaskIndex = 5,
        )
        assertNull(state.currentTask)
    }

    @Test
    fun `currentTask returns null for empty task list`() {
        val state = SessionRunUiState(tasks = emptyList(), currentTaskIndex = 0)
        assertNull(state.currentTask)
    }
}
