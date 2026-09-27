package dev.xykal.nabungin.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Ikon dibuat manual sebagai ImageVector (path murni), bukan icon pack bawaan.
 * Semua viewport 24x24 supaya scaling konsisten dan bebas layout shift.
 */
object AppIcons {

    private const val K = 0.5523f

    private fun filled(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero, pathBuilder = block).build()

    private fun stroked(name: String, width: Float = 1.8f, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        ).build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float, clockwise: Boolean = true) {
        val k = r * K
        if (clockwise) {
            moveTo(cx, cy - r)
            curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy)
            curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r)
            curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy)
            curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r)
        } else {
            moveTo(cx, cy - r)
            curveTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy)
            curveTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r)
            curveTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy)
            curveTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r)
        }
        close()
    }

    private fun PathBuilder.ring(cx: Float, cy: Float, outer: Float, inner: Float) {
        circle(cx, cy, outer, clockwise = true)
        circle(cx, cy, inner, clockwise = false)
    }

    val Coins: ImageVector = filled("coins") {
        moveTo(4f, 7.5f); lineTo(20f, 7.5f); lineTo(20f, 10.5f); lineTo(4f, 10.5f); close()
        moveTo(4f, 11.4f); lineTo(20f, 11.4f); lineTo(20f, 14.4f); lineTo(4f, 14.4f); close()
        moveTo(5.6f, 3.5f); lineTo(18.4f, 3.5f); lineTo(18.4f, 6.5f); lineTo(5.6f, 6.5f); close()
    }

    val Plus: ImageVector = filled("plus") {
        moveTo(10.8f, 4f); lineTo(13.2f, 4f); lineTo(13.2f, 10.8f); lineTo(20f, 10.8f)
        lineTo(20f, 13.2f); lineTo(13.2f, 13.2f); lineTo(13.2f, 20f); lineTo(10.8f, 20f)
        lineTo(10.8f, 13.2f); lineTo(4f, 13.2f); lineTo(4f, 10.8f); lineTo(10.8f, 10.8f); close()
    }

    val Minus: ImageVector = filled("minus") {
        moveTo(4f, 10.8f); lineTo(20f, 10.8f); lineTo(20f, 13.2f); lineTo(4f, 13.2f); close()
    }

    val Check: ImageVector = stroked("check", 2.2f) {
        moveTo(4.5f, 12.5f); lineTo(9.5f, 17.5f); lineTo(19.5f, 6.5f)
    }

    val Back: ImageVector = stroked("back", 2.0f) {
        moveTo(14.5f, 5f); lineTo(7.5f, 12f); lineTo(14.5f, 19f)
    }

    val Close: ImageVector = stroked("close", 2.0f) {
        moveTo(6f, 6f); lineTo(18f, 18f)
        moveTo(18f, 6f); lineTo(6f, 18f)
    }

    val Target: ImageVector = filled("target") {
        ring(12f, 12f, 10f, 7.6f)
        ring(12f, 12f, 5.6f, 3.2f)
        circle(12f, 12f, 1.6f)
    }

    val Calendar: ImageVector = filled("calendar") {
        moveTo(4f, 5.5f); lineTo(20f, 5.5f); lineTo(20f, 9f); lineTo(4f, 9f); close()
        moveTo(4f, 10.2f); lineTo(20f, 10.2f); lineTo(20f, 20f); lineTo(4f, 20f); close()
        moveTo(7.5f, 2.6f); lineTo(9.5f, 2.6f); lineTo(9.5f, 7f); lineTo(7.5f, 7f); close()
        moveTo(14.5f, 2.6f); lineTo(16.5f, 2.6f); lineTo(16.5f, 7f); lineTo(14.5f, 7f); close()
    }

    val Clock: ImageVector = filled("clock") {
        ring(12f, 12f, 9.6f, 8.0f)
        moveTo(11.2f, 6.6f); lineTo(12.8f, 6.6f); lineTo(12.8f, 12.6f); lineTo(11.2f, 12.6f); close()
        moveTo(11.8f, 11.4f); lineTo(16.4f, 11.4f); lineTo(16.4f, 13f); lineTo(11.8f, 13f); close()
    }

    val Bell: ImageVector = filled("bell") {
        moveTo(12f, 3f)
        curveTo(9f, 3f, 7f, 5.2f, 7f, 8f)
        lineTo(7f, 12.2f)
        lineTo(5.2f, 15.6f)
        lineTo(18.8f, 15.6f)
        lineTo(17f, 12.2f)
        lineTo(17f, 8f)
        curveTo(17f, 5.2f, 15f, 3f, 12f, 3f)
        close()
        moveTo(10.2f, 17f); lineTo(13.8f, 17f); lineTo(13.8f, 19.4f); lineTo(10.2f, 19.4f); close()
    }

    val Lock: ImageVector = filled("lock") {
        ring(12f, 9.6f, 4.2f, 2.8f)
        moveTo(5f, 11.4f); lineTo(19f, 11.4f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        circle(12f, 15.6f, 1.6f)
    }

    val Fingerprint: ImageVector = stroked("fingerprint", 1.7f) {
        moveTo(8f, 12f)
        curveTo(8f, 9.8f, 9.8f, 8f, 12f, 8f)
        curveTo(14.2f, 8f, 16f, 9.8f, 16f, 12f)
        moveTo(5.4f, 12.4f)
        curveTo(5.4f, 8.4f, 8.4f, 5.2f, 12f, 5.2f)
        curveTo(15.6f, 5.2f, 18.6f, 8.4f, 18.6f, 12.4f)
        moveTo(12f, 11.6f); lineTo(12f, 16.4f)
        moveTo(9.2f, 13.6f); lineTo(9.2f, 18f)
        moveTo(14.8f, 13.6f); lineTo(14.8f, 18.4f)
    }

    val Shield: ImageVector = filled("shield") {
        moveTo(12f, 2.2f)
        lineTo(20f, 5.6f)
        lineTo(20f, 11.4f)
        curveTo(20f, 16.4f, 16.6f, 20.4f, 12f, 22f)
        curveTo(7.4f, 20.4f, 4f, 16.4f, 4f, 11.4f)
        lineTo(4f, 5.6f)
        close()
    }

    val Trash: ImageVector = filled("trash") {
        moveTo(9.4f, 3f); lineTo(14.6f, 3f); lineTo(14.6f, 4.6f); lineTo(9.4f, 4.6f); close()
        moveTo(4.4f, 5.4f); lineTo(19.6f, 5.4f); lineTo(19.6f, 7f); lineTo(4.4f, 7f); close()
        moveTo(6.4f, 8.2f); lineTo(17.6f, 8.2f); lineTo(17.6f, 20.4f); lineTo(6.4f, 20.4f); close()
        moveTo(9.6f, 10.4f); lineTo(11f, 10.4f); lineTo(11f, 18.2f); lineTo(9.6f, 18.2f); close()
        moveTo(13f, 10.4f); lineTo(14.4f, 10.4f); lineTo(14.4f, 18.2f); lineTo(13f, 18.2f); close()
    }

    val Pencil: ImageVector = filled("pencil") {
        moveTo(16.4f, 2.8f); lineTo(21.2f, 7.6f); lineTo(9.6f, 19.2f); lineTo(3.4f, 20.6f)
        lineTo(4.8f, 14.4f); close()
    }

    val Sliders: ImageVector = filled("sliders") {
        moveTo(4f, 6.2f); lineTo(20f, 6.2f); lineTo(20f, 7.8f); lineTo(4f, 7.8f); close()
        moveTo(4f, 11.2f); lineTo(20f, 11.2f); lineTo(20f, 12.8f); lineTo(4f, 12.8f); close()
        moveTo(4f, 16.2f); lineTo(20f, 16.2f); lineTo(20f, 17.8f); lineTo(4f, 17.8f); close()
        moveTo(8f, 4.4f); lineTo(10.6f, 4.4f); lineTo(10.6f, 9.6f); lineTo(8f, 9.6f); close()
        moveTo(14.6f, 9.4f); lineTo(17.2f, 9.4f); lineTo(17.2f, 14.6f); lineTo(14.6f, 14.6f); close()
        moveTo(7.2f, 14.4f); lineTo(9.8f, 14.4f); lineTo(9.8f, 19.6f); lineTo(7.2f, 19.6f); close()
    }

    val Chart: ImageVector = filled("chart") {
        moveTo(4.6f, 13.4f); lineTo(7.8f, 13.4f); lineTo(7.8f, 21f); lineTo(4.6f, 21f); close()
        moveTo(10.4f, 8.6f); lineTo(13.6f, 8.6f); lineTo(13.6f, 21f); lineTo(10.4f, 21f); close()
        moveTo(16.2f, 3.4f); lineTo(19.4f, 3.4f); lineTo(19.4f, 21f); lineTo(16.2f, 21f); close()
    }

    val Ledger: ImageVector = filled("ledger") {
        circle(6.4f, 6.4f, 1.9f)
        circle(6.4f, 12f, 1.9f)
        circle(6.4f, 17.6f, 1.9f)
        moveTo(10.4f, 5.4f); lineTo(20.2f, 5.4f); lineTo(20.2f, 7.4f); lineTo(10.4f, 7.4f); close()
        moveTo(10.4f, 11f); lineTo(20.2f, 11f); lineTo(20.2f, 13f); lineTo(10.4f, 13f); close()
        moveTo(10.4f, 16.6f); lineTo(20.2f, 16.6f); lineTo(20.2f, 18.6f); lineTo(10.4f, 18.6f); close()
    }

    val Download: ImageVector = filled("download") {
        moveTo(10.9f, 3f); lineTo(13.1f, 3f); lineTo(13.1f, 12.4f); lineTo(17.2f, 12.4f)
        lineTo(12f, 18.4f); lineTo(6.8f, 12.4f); lineTo(10.9f, 12.4f); close()
        moveTo(4.6f, 19.2f); lineTo(19.4f, 19.2f); lineTo(19.4f, 21.2f); lineTo(4.6f, 21.2f); close()
    }

    val Upload: ImageVector = filled("upload") {
        moveTo(12f, 3f); lineTo(17.2f, 9f); lineTo(13.1f, 9f); lineTo(13.1f, 16.6f)
        lineTo(10.9f, 16.6f); lineTo(10.9f, 9f); lineTo(6.8f, 9f); close()
        moveTo(4.6f, 19.2f); lineTo(19.4f, 19.2f); lineTo(19.4f, 21.2f); lineTo(4.6f, 21.2f); close()
    }

    val Car: ImageVector = filled("car") {
        moveTo(5.6f, 6.4f); lineTo(18.4f, 6.4f); lineTo(20.6f, 12f); lineTo(20.6f, 17.4f)
        lineTo(3.4f, 17.4f); lineTo(3.4f, 12f); close()
        circle(7.2f, 18.8f, 1.9f)
        circle(16.8f, 18.8f, 1.9f)
    }

    val Home: ImageVector = filled("home") {
        moveTo(12f, 3f); lineTo(21f, 10.6f); lineTo(18.4f, 10.6f); lineTo(18.4f, 20.4f)
        lineTo(13.6f, 20.4f); lineTo(13.6f, 14.4f); lineTo(10.4f, 14.4f); lineTo(10.4f, 20.4f)
        lineTo(5.6f, 20.4f); lineTo(5.6f, 10.6f); lineTo(3f, 10.6f); close()
    }

    val Book: ImageVector = filled("book") {
        moveTo(5f, 3.4f); lineTo(19f, 3.4f); lineTo(19f, 20.6f); lineTo(5f, 20.6f); close()
        moveTo(7f, 6f); lineTo(17f, 6f); lineTo(17f, 7.4f); lineTo(7f, 7.4f); close()
        moveTo(7f, 9.6f); lineTo(17f, 9.6f); lineTo(17f, 11f); lineTo(7f, 11f); close()
        moveTo(7f, 13.2f); lineTo(14f, 13.2f); lineTo(14f, 14.6f); lineTo(7f, 14.6f); close()
    }

    val Plane: ImageVector = filled("plane") {
        moveTo(12f, 2.4f); lineTo(14.4f, 9.4f); lineTo(21.4f, 12f); lineTo(14.4f, 14.6f)
        lineTo(12f, 21.6f); lineTo(9.6f, 14.6f); lineTo(2.6f, 12f); lineTo(9.6f, 9.4f); close()
    }

    val Grad: ImageVector = filled("grad") {
        moveTo(12f, 3.2f); lineTo(22f, 8f); lineTo(12f, 12.8f); lineTo(2f, 8f); close()
        moveTo(6f, 10.6f); lineTo(8f, 10.6f); lineTo(8f, 15.6f); lineTo(6f, 15.6f); close()
        moveTo(16f, 10.6f); lineTo(18f, 10.6f); lineTo(18f, 15.6f); lineTo(16f, 15.6f); close()
        moveTo(8f, 15.4f); lineTo(16f, 15.4f); lineTo(16f, 17.4f); lineTo(8f, 17.4f); close()
    }

    val Heart: ImageVector = filled("heart") {
        moveTo(12f, 20.4f)
        curveTo(4.4f, 15.2f, 2.4f, 11.6f, 3.6f, 8.2f)
        curveTo(4.8f, 5f, 9.2f, 4.2f, 12f, 7.6f)
        curveTo(14.8f, 4.2f, 19.2f, 5f, 20.4f, 8.2f)
        curveTo(21.6f, 11.6f, 19.6f, 15.2f, 12f, 20.4f)
        close()
    }

    val byKey: Map<String, ImageVector> = mapOf(
        "coins" to Coins,
        "shield" to Shield,
        "car" to Car,
        "home" to Home,
        "book" to Book,
        "plane" to Plane,
        "grad" to Grad,
        "heart" to Heart,
        "target" to Target,
        "chart" to Chart,
    )

    fun of(key: String): ImageVector = byKey[key] ?: Coins

    val pickerKeys: List<String> = byKey.keys.toList()
}
