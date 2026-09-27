package dev.xykal.nabungin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

@Composable
fun NabunginTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalNabunginColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconSquareButton(icon = AppIcons.Back, onClick = onBack, contentDescription = "Kembali")
            Spacer(Modifier.width(14.dp))
        }
        BrandMark(size = 34.dp)
        Spacer(Modifier.width(9.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
fun AmountDisplay(
    amount: Long,
    modifier: Modifier = Modifier,
    fontSize: Int = 40,
    showCurrency: Boolean = true,
) {
    val colors = LocalNabunginColors.current
    Text(
        text = if (showCurrency) Money.format(amount) else Money.formatCompact(amount),
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize.sp,
        color = if (amount > 0L) colors.onSurface else colors.muted,
        maxLines = 1,
    )
}

@Composable
fun LabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = LocalNabunginColors.current.onSurface,
) {
    val colors = LocalNabunginColors.current
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = valueColor)
    }
}

@Composable
fun SourceBadge(isAuto: Boolean) {
    val colors = LocalNabunginColors.current
    val bg = if (isAuto) colors.accentSoft else colors.surfaceAlt
    val fg = if (isAuto) colors.accent else colors.muted
    Box(
        modifier = Modifier
            .clip(cardShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = if (isAuto) "AUTO" else "MANUAL",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

@Composable
fun SwatchRow(
    colors: List<androidx.compose.ui.graphics.Color>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEachIndexed { index, color ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (selected) 34.dp else 28.dp)
                    .clip(cardShape(10.dp))
                    .background(color)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmOverlay(
    visible: Boolean,
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val colors = LocalNabunginColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(cardShape(20.dp))
                .background(colors.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            Spacer(Modifier.height(2.dp))
            NButton(text = confirmLabel, onClick = onConfirm, tone = ButtonTone.Danger)
            NButton(text = "Batal", onClick = onDismiss, tone = ButtonTone.Ghost)
        }
    }
}
