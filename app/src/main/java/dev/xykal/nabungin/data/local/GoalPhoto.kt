package dev.xykal.nabungin.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Store a bounded thumbnail in Room and JSON backup, not an ephemeral document URI. */
object GoalPhoto {
    private const val MAX_INPUT = 12 * 1024 * 1024
    private const val MAX_ENCODED = 700_000

    suspend fun import(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri) ?: error("Gambar tidak bisa dibuka")
        val bytes = input.use { stream ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = stream.read(buffer)
                if (n < 0) break
                out.write(buffer, 0, n)
                require(out.size() <= MAX_INPUT) { "Gambar terlalu besar (maks. 12 MB)" }
            }
            out.toByteArray()
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth in 1..12000 && bounds.outHeight in 1..12000) { "Format atau ukuran gambar tidak didukung" }
        var sample = 1
        while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sample }) ?: error("Gambar rusak")
        try {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.WEBP, 78, output)
            require(output.size() <= MAX_ENCODED) { "Gambar terlalu rumit, pilih gambar lain" }
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } finally { bitmap.recycle() }
    }

    fun valid(value: String): Boolean = value.length <= 1_000_000 &&
        (value.isEmpty() || runCatching { Base64.decode(value, Base64.DEFAULT).size <= MAX_ENCODED }.getOrDefault(false))
}
