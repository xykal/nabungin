package dev.xykal.nabungin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.DayTotal
import dev.xykal.nabungin.ui.theme.GoalAccents
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

@Composable
fun cardShape(radius: Dp = 20.dp) = RoundedCornerShape(radius)

/** Kartu dasar: flat, hairline border, tanpa shadow default Material. */
@Composable
fun NabunginCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalNabunginColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape())
            .background(colors.surface)
            .border(1.dp, accent ?: colors.hairline, cardShape())
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalNabunginColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

enum class ButtonTone { Primary, Ghost, Danger, Quiet }

@Composable
fun NButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
) {
    val colors = LocalNabunginColors.current
    val bg = when (tone) {
        ButtonTone.Primary -> colors.accent
        ButtonTone.Ghost -> Color.Transparent
        ButtonTone.Danger -> colors.danger.copy(alpha = 0.12f)
        ButtonTone.Quiet -> colors.surfaceAlt
    }
    val fg = when (tone) {
        ButtonTone.Primary -> colors.accentInk
        ButtonTone.Ghost -> colors.onSurface
        ButtonTone.Danger -> colors.danger
        ButtonTone.Quiet -> colors.onSurface
    }
    val border = when (tone) {
        ButtonTone.Ghost -> colors.hairline
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier.widthIn(min = 96.dp))
            .heightIn(min = 50.dp)
            .clip(cardShape(14.dp))
            .background(bg)
            .border(1.dp, border, cardShape(14.dp))
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun IconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color = LocalNabunginColors.current.accent,
    size: Dp = 38.dp,
    iconSize: Dp = 19.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(cardShape(size / 2))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun IconSquareButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalNabunginColors.current.onSurface,
) {
    val colors = LocalNabunginColors.current
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(cardShape(12.dp))
            .background(colors.surfaceAlt)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun AmountText(
    amount: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    color: Color = LocalNabunginColors.current.onSurface,
) {
    Text(
        text = if (compact) Money.formatCompact(amount) else Money.format(amount),
        modifier = modifier,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = if (compact) 15.sp else 20.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Progress ring digambar sendiri, tanpa komponen bawaan OS. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    stroke: Dp = 9.dp,
    trackColor: Color = LocalNabunginColors.current.surfaceAlt,
    progressColor: Color = LocalNabunginColors.current.accent,
    content: (@Composable () -> Unit)? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "ring",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        content?.invoke()
    }
}

/** Bar chart 30 hari, murni Canvas. */
@Composable
fun BarChart(
    data: List<DayTotal>,
    modifier: Modifier = Modifier,
    barColor: Color = LocalNabunginColors.current.accent,
    height: Dp = 110.dp,
) {
    val colors = LocalNabunginColors.current
    val max = (data.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (data.isEmpty()) return@Canvas
        val gap = 3f
        val barWidth = ((size.width - gap * (data.size - 1)) / data.size).coerceAtLeast(1.5f)
        data.forEachIndexed { index, item ->
            val ratio = item.total.toFloat() / max.toFloat()
            val barHeight = (size.height * ratio).coerceAtLeast(if (item.total > 0L) 4f else 1.5f)
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = if (item.total > 0L) barColor else colors.hairline,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

@Composable
fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNabunginColors.current
    Box(
        modifier = modifier
            .heightIn(min = 34.dp)
            .clip(cardShape(10.dp))
            .background(if (selected) colors.accentSoft else colors.surface)
            .border(1.dp, if (selected) colors.accent else colors.hairline, cardShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) colors.accent else colors.onSurface,
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    valueColor: Color = LocalNabunginColors.current.onSurface,
) {
    val colors = LocalNabunginColors.current
    Column(modifier = modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
    }
}

@Composable
fun NabunginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxChars: Int = 60,
) {
    val colors = LocalNabunginColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape(14.dp))
            .background(colors.surfaceAlt)
            .border(1.dp, if (focused) colors.accent else colors.hairline, cardShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.muted)
        }
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            ),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Bottom sheet custom: scrim + panel naik dari bawah, tanpa ModalBottomSheet bawaan. */
@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalNabunginColors.current
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.scrim)
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(colors.background)
                    .pointerInput(Unit) { detectTapGestures(onTap = { }) },
            ) {
                Column(modifier = Modifier.padding(20.dp)) { content() }
            }
        }
    }
}

@Composable
fun SheetTitle(title: String, subtitle: String? = null, onClose: () -> Unit) {
    val colors = LocalNabunginColors.current
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            }
        }
        IconSquareButton(icon = dev.xykal.nabungin.ui.icons.AppIcons.Close, onClick = onClose)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalNabunginColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBubble(icon = icon, size = 56.dp, iconSize = 26.dp)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
fun Keypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    val colors = LocalNabunginColors.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { digit ->
                    KeypadKey(
                        label = digit,
                        modifier = Modifier.weight(1f),
                        onClick = { onDigit(digit.first()) },
                        keyColor = colors.onSurface,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (trailingLabel != null && onTrailing != null) {
                KeypadKey(label = trailingLabel, modifier = Modifier.weight(1f), onClick = onTrailing)
            } else {
                Spacer(Modifier.weight(1f))
            }
            KeypadKey(label = "0", modifier = Modifier.weight(1f), onClick = { onDigit('0') })
            KeypadKey(
                label = "<",
                modifier = Modifier.weight(1f),
                onClick = onBackspace,
                keyColor = colors.muted,
            )
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyColor: Color = LocalNabunginColors.current.onSurface,
) {
    val colors = LocalNabunginColors.current
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(cardShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.hairline, cardShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            color = keyColor,
        )
    }
}

@Composable
fun accentOf(index: Int): Color = GoalAccents[index.coerceIn(0, GoalAccents.lastIndex)]
