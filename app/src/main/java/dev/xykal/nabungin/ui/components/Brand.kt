package dev.xykal.nabungin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.xykal.nabungin.R
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

/** One mark everywhere: launcher, header and empty goal artwork. */
@Composable
fun BrandMark(size: Dp = 42.dp, modifier: Modifier = Modifier) {
    val colors = LocalNabunginColors.current
    Box(modifier.size(size).clip(RoundedCornerShape(size / 3)).background(colors.accentSoft),
        contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.brand_mark), contentDescription = "Nabungin",
            modifier = Modifier.size(size))
    }
}
