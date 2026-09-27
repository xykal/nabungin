package dev.xykal.nabungin.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.xykal.nabungin.R
import androidx.compose.ui.res.painterResource

@Composable
fun GoalArtwork(photo: String, modifier: Modifier = Modifier, fallback: Int = R.drawable.ill_target_sprout) {
    val bitmap = remember(photo) {
        if (photo.isBlank()) null else runCatching {
            val bytes = Base64.decode(photo, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
    Box(modifier = modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF32694C)), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap, contentDescription = "Gambar tujuan tabungan",
            modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
        else Image(painterResource(fallback), contentDescription = "Ilustrasi tujuan tabungan",
            modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
    }
}
