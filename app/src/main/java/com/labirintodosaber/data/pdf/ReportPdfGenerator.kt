package com.labirintodosaber.data.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// ── Dados de entrada (já formatados; o gerador não conhece modelos de domínio) ──

data class ReportPdfCategory(val label: String, val percent: Int, val colorInt: Int)
data class ReportPdfSession(val name: String, val date: String, val score: String)
data class ReportPdfObservation(val sessionName: String, val text: String)

data class ReportPdfData(
    val studentName: String,
    val periodLabel: String,
    val generatedAt: String,
    val includeMetrics: Boolean,
    val includeQualitative: Boolean,
    val overallCorrect: Int,
    val overallTotal: Int,
    val overallAccuracyPercent: Int,
    val categories: List<ReportPdfCategory>,
    val sessions: List<ReportPdfSession>,
    val observations: List<ReportPdfObservation>,
)

data class GeneratedPdf(val file: File, val savedToDownloads: Boolean)

/**
 * Gera o PDF do relatório localmente (sem depender do backend), grava uma cópia em
 * cache para renderização in-app e salva o arquivo em Downloads.
 */
@Singleton
class ReportPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun generate(data: ReportPdfData, fileName: String): GeneratedPdf {
        val bytes = buildPdfBytes(data)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val cacheFile = File(dir, fileName)
        cacheFile.outputStream().use { it.write(bytes) }

        val saved = saveToDownloads(bytes, fileName)
        return GeneratedPdf(cacheFile, saved)
    }

    private fun buildPdfBytes(data: ReportPdfData): ByteArray {
        val doc = PdfDocument()
        val drawer = PdfDrawer(doc)

        drawer.line(data.studentName.ifBlank { "Relatório" }, TITLE)
        drawer.line("Relatório de Desempenho", H2.apply { color = TEAL })
        drawer.line(data.periodLabel, MUTED)
        drawer.line("Gerado em ${data.generatedAt}", MUTED, gapAfter = 10f)
        drawer.divider()

        if (data.includeMetrics) {
            drawer.line("Desempenho Geral", H2)
            drawer.line("${data.overallAccuracyPercent}% de acerto", BIG)
            drawer.line("${data.overallCorrect} de ${data.overallTotal} questões corretas", MUTED, gapAfter = 12f)

            if (data.categories.isNotEmpty()) {
                drawer.line("Taxa de Acerto por Categoria", H2)
                data.categories.forEach { cat ->
                    drawer.labelValue(cat.label, "${cat.percent}%", BODY)
                    drawer.bar(cat.percent, cat.colorInt)
                }
                drawer.gap(6f)
            }
            drawer.divider()
        }

        drawer.line("Sessões (${data.sessions.size})", H2)
        if (data.sessions.isEmpty()) {
            drawer.line("Nenhuma sessão no período.", MUTED, gapAfter = 10f)
        } else {
            data.sessions.forEach { s ->
                drawer.labelValue(s.name, s.score, BODY_BOLD)
                drawer.line(s.date, MUTED, gapAfter = 8f)
            }
        }

        if (data.includeQualitative && data.observations.isNotEmpty()) {
            drawer.divider()
            drawer.line("Observações", H2)
            data.observations.forEach { obs ->
                drawer.line(obs.sessionName, BODY_BOLD)
                drawer.wrapped(obs.text, BODY, gapAfter = 8f)
            }
        }

        drawer.finish()

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun saveToDownloads(bytes: ByteArray, fileName: String): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } else {
            // Pré-Android 10: grava na pasta de Downloads específica do app (sem permissão).
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
            File(dir, fileName).outputStream().use { it.write(bytes) }
            true
        }
    }.getOrDefault(false)

    // ── Desenho paginado ────────────────────────────────────────────────────────

    private class PdfDrawer(private val doc: PdfDocument) {
        private var pageNum = 1
        private var page = doc.startPage(pageInfo(pageNum))
        private var canvas = page.canvas
        private var y = MARGIN

        private fun newPageIfNeeded(need: Float) {
            if (y + need > PAGE_H - MARGIN) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(pageInfo(pageNum))
                canvas = page.canvas
                y = MARGIN
            }
        }

        fun gap(h: Float) { y += h }

        fun line(text: String, paint: Paint, x: Float = MARGIN, gapAfter: Float = 6f) {
            val lh = paint.fontSpacing
            newPageIfNeeded(lh)
            canvas.drawText(text, x, y + paint.textSize, paint)
            y += lh + gapAfter
        }

        /** Rótulo à esquerda + valor à direita na mesma linha. */
        fun labelValue(label: String, value: String, paint: Paint, gapAfter: Float = 4f) {
            val lh = paint.fontSpacing
            newPageIfNeeded(lh)
            val maxLabel = CONTENT_W - paint.measureText(value) - 12f
            val shownLabel = ellipsize(label, paint, maxLabel)
            canvas.drawText(shownLabel, MARGIN, y + paint.textSize, paint)
            canvas.drawText(value, PAGE_W - MARGIN - paint.measureText(value), y + paint.textSize, paint)
            y += lh + gapAfter
        }

        fun wrapped(text: String, paint: Paint, x: Float = MARGIN, gapAfter: Float = 6f) {
            val maxWidth = PAGE_W - MARGIN - x
            var current = ""
            text.split(" ").forEach { word ->
                val test = if (current.isEmpty()) word else "$current $word"
                if (paint.measureText(test) > maxWidth) {
                    line(current, paint, x, 0f)
                    current = word
                } else {
                    current = test
                }
            }
            if (current.isNotEmpty()) line(current, paint, x, 0f)
            y += gapAfter
        }

        fun bar(percent: Int, colorInt: Int, height: Float = 8f) {
            newPageIfNeeded(height + 6f)
            val bg = Paint().apply { color = TRACK; isAntiAlias = true }
            canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + height, 4f, 4f, bg)
            val fg = Paint().apply { color = colorInt; isAntiAlias = true }
            val w = CONTENT_W * (percent / 100f).coerceIn(0f, 1f)
            canvas.drawRoundRect(MARGIN, y, MARGIN + w, y + height, 4f, 4f, fg)
            y += height + 10f
        }

        fun divider() {
            newPageIfNeeded(12f)
            val p = Paint().apply { color = TRACK }
            canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 1f, p)
            y += 14f
        }

        fun finish() {
            doc.finishPage(page)
        }

        private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
            if (maxWidth <= 0f) return text
            if (paint.measureText(text) <= maxWidth) return text
            var end = text.length
            while (end > 0 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
            return text.substring(0, end) + "…"
        }
    }

    private companion object {
        const val PAGE_W = 595
        const val PAGE_H = 842
        const val MARGIN = 40f
        const val CONTENT_W = PAGE_W - 2 * MARGIN

        const val DARK = 0xFF1A1A1A.toInt()
        const val GRAY = 0xFF6B7280.toInt()
        const val TEAL = 0xFF3FADA5.toInt()
        const val TRACK = 0xFFE5E7EB.toInt()

        fun pageInfo(num: Int): PdfDocument.PageInfo =
            PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, num).create()

        val TITLE get() = Paint().apply { color = DARK; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        val H2 get() = Paint().apply { color = DARK; textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        val BIG get() = Paint().apply { color = TEAL; textSize = 20f; isFakeBoldText = true; isAntiAlias = true }
        val BODY get() = Paint().apply { color = DARK; textSize = 11f; isAntiAlias = true }
        val BODY_BOLD get() = Paint().apply { color = DARK; textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        val MUTED get() = Paint().apply { color = GRAY; textSize = 10f; isAntiAlias = true }
    }
}
