package com.labirintodosaber.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Arquivo a ser enviado em endpoints `multipart/form-data`. Carrega os bytes já
 * resolvidos (ex.: lidos de um `Uri` via `ContentResolver` na camada de UI), mantendo
 * o repositório livre de dependências de Android.
 */
data class FileUpload(
    val fileName: String,
    val bytes: ByteArray,
    val mimeType: String,
) {
    // equals/hashCode customizados por causa do array (data class compara referência).
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileUpload) return false
        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

/** Helpers para montar partes de uma requisição `multipart/form-data`. */
object MultipartFactory {

    /** Campo escalar de formulário (chega como string no servidor). */
    fun textPart(value: String): RequestBody =
        value.toRequestBody(TEXT_PLAIN)

    /** Mapa de campos escalares prontos para `@PartMap`. */
    fun textParts(values: Map<String, String?>): Map<String, RequestBody> =
        values.filterValues { it != null }
            .mapValues { (_, value) -> textPart(value!!) }

    /** Parte de arquivo nomeada (`name` deve casar com o campo esperado pela API). */
    fun filePart(name: String, file: FileUpload): MultipartBody.Part {
        val body = file.bytes.toRequestBody(file.mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.fileName, body)
    }

    private val TEXT_PLAIN = "text/plain".toMediaTypeOrNull()
}
