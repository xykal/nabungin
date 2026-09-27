package dev.xykal.nabungin

import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.DepositSource
import dev.xykal.nabungin.domain.model.Goal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsMathTest {

    private fun goal(
        target: Long = 10_000_000L,
        saved: Long = 0L,
        createdDaysAgo: Long = 10L,
        deadlineInDays: Long? = null,
    ) = Goal(
        id = 1L,
        name = "Dana Darurat",
        targetAmount = target,
        deadline = deadlineInDays?.let { LocalDate.now().plusDays(it) },
        createdAtMillis = System.currentTimeMillis() - ChronoUnit.DAYS.toMillis(createdDaysAgo),
        saved = saved,
    )

    private fun deposit(daysAgo: Long, amount: Long = 100_000L) = Deposit(
        goalId = 1L,
        amount = amount,
        day = LocalDate.now().minusDays(daysAgo),
        source = DepositSource.MANUAL,
    )

    @Test
    fun `progress dihitung dari saved dibagi target`() {
        val g = goal(target = 1_000_000L, saved = 250_000L)
        assertEquals(0.25f, g.progress, 0.0001f)
    }

    @Test
    fun `progress tidak lebih dari satu walau setoran melebihi target`() {
        val g = goal(target = 1_000_000L, saved = 5_000_000L)
        assertEquals(1f, g.progress, 0.0001f)
        assertTrue(g.isReached)
        assertEquals(0L, g.remaining)
    }

    @Test
    fun `kebutuhan harian membagi sisa dengan hari tersisa`() {
        val g = goal(target = 3_000_000L, saved = 0L, deadlineInDays = 30L)
        // 3.000.000 / 30 hari = 100.000
        assertEquals(100_000L, SavingsMath.pace(g).dailyNeeded)
    }

    @Test
    fun `deadline lewat membuat kebutuhan harian sebesar sisa target`() {
        val g = goal(target = 2_000_000L, saved = 500_000L, deadlineInDays = -3L)
        assertEquals(1_500_000L, SavingsMath.pace(g).dailyNeeded)
    }

    @Test
    fun `eta null kalau belum ada setoran`() {
        assertNull(SavingsMath.etaFromPace(saved = 0L, target = 1_000_000L, perDay = 0L))
    }

    @Test
    fun `eta nol kalau target sudah tercapai`() {
        assertEquals(0L, SavingsMath.etaFromPace(saved = 1_000_000L, target = 1_000_000L, perDay = 0L))
    }

    @Test
    fun `eta membulatkan ke atas`() {
        assertEquals(4L, SavingsMath.etaFromPace(saved = 600_000L, target = 1_000_000L, perDay = 100_000L))
    }

    @Test
    fun `streak menghitung hari beruntun termasuk hari ini`() {
        val deposits = listOf(deposit(0), deposit(1), deposit(2), deposit(5))
        assertEquals(3, SavingsMath.streak(deposits))
    }

    @Test
    fun `streak tetap jalan kalau hari ini belum setor tapi kemarin setor`() {
        val deposits = listOf(deposit(1), deposit(2))
        assertEquals(2, SavingsMath.streak(deposits))
    }

    @Test
    fun `streak nol kalau terakhir setor dua hari lalu`() {
        assertEquals(0, SavingsMath.streak(listOf(deposit(2), deposit(3))))
    }

    @Test
    fun `total per hari lengkap walau ada hari kosong`() {
        val today = LocalDate.now()
        val series = SavingsMath.totalsByDay(
            deposits = listOf(deposit(0, 50_000L), deposit(2, 25_000L)),
            from = today.minusDays(4),
            to = today,
        )
        assertEquals(5, series.size)
        assertEquals(50_000L, series.last().second)
        assertEquals(25_000L, series[2].second)
        assertEquals(0L, series[3].second)
    }

    @Test
    fun `total bulan ini hanya menghitung bulan berjalan`() {
        val deposits = listOf(deposit(0, 100_000L), deposit(0, 25_000L))
        assertEquals(125_000L, SavingsMath.monthTotal(deposits))
    }
}
