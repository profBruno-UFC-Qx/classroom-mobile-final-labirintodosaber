package com.labirintodosaber.ui.screen.reportpdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.labirintodosaber.R
import com.labirintodosaber.ui.theme.TealPrimary
import com.labirintodosaber.ui.theme.TextPrimary
import com.labirintodosaber.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private sealed interface PdfRenderState {
    data object Loading : PdfRenderState
    data class Ready(val pages: List<ImageBitmap>) : PdfRenderState
    data object Error : PdfRenderState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPdfScreen(
    filePath: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by produceState<PdfRenderState>(PdfRenderState.Loading, filePath) {
        value = withContext(Dispatchers.IO) {
            runCatching { PdfRenderState.Ready(renderPdfPages(filePath)) }
                .getOrDefault(PdfRenderState.Error)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.report_pdf_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back_button), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color(0xFF525659),
    ) { padding ->
        when (val s = state) {
            PdfRenderState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }

            PdfRenderState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.report_pdf_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }

            is PdfRenderState.Ready -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(s.pages) { pageBitmap ->
                    Image(
                        bitmap = pageBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                    )
                }
            }
        }
    }
}

/** Renderiza cada página do PDF em um bitmap (fundo branco) para exibição. */
private fun renderPdfPages(path: String): List<ImageBitmap> {
    val file = File(path)
    if (!file.exists()) return emptyList()
    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(pfd)
    val targetWidth = 1240
    val pages = ArrayList<ImageBitmap>(renderer.pageCount)
    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val scale = targetWidth.toFloat() / page.width
        val height = (page.height * scale).toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawColor(AndroidColor.WHITE)
        val matrix = Matrix().apply { setScale(scale, scale) }
        page.render(bmp, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pages.add(bmp.asImageBitmap())
    }
    renderer.close()
    pfd.close()
    return pages
}
