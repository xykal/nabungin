package dev.xykal.nabungin

import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatTest {

    @Test
    fun `parseDigits ambil angka dari input kotor`() {
        assertEquals(1_500_000L, Money.parseDigits("Rp 1.500.000"))
        assertEquals(0L, Money.parseDigits("abc"))
        assertEquals(25_000L, Money.parseDigits("25 000"))
    }

    @Test
    fun `parseDigits tidak overflow walau input panjang`() {
        assertEquals(999_999_999_999_999L, Money.parseDigits("999999999999999999"))
    }

    @Test
    fun `format pakai prefix rupiah`() {
        assertTrue(Money.format(1_500_000L).startsWith("Rp"))
        assertTrue(Money.format(1_500_000L).contains("1"))
    }

    @Test
    fun `formatCompact ringkas ke satuan indonesia`() {
        assertEquals("850 rb", Money.formatCompact(850_000L))
        assertEquals("1,5 jt", Money.formatCompact(1_500_000L))
        assertEquals("2 jt", Money.formatCompact(2_000_000L))
        assertEquals("3 M", Money.formatCompact(3_000_000_000L))
    }

    @Test
    fun `label relatif untuk hari ini dan kemarin`() {
        assertEquals("Hari ini", Dates.relative(LocalDate.now()))
        assertEquals("Kemarin", Dates.relative(LocalDate.now().minusDays(1)))
    }

    @Test
    fun `label hari tersisa memakai satuan yang masuk akal`() {
        assertEquals("Hari terakhir", Dates.daysLeftLabel(LocalDate.now()))
        assertEquals("5 hari lagi", Dates.daysLeftLabel(LocalDate.now().plusDays(5)))
        assertTrue(Dates.daysLeftLabel(LocalDate.now().plusDays(70)).contains("bulan"))
    }
}
