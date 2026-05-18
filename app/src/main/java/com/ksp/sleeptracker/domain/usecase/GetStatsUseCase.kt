package com.ksp.sleeptracker.domain.usecase

import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.data.repository.ProfileRepository
import com.ksp.sleeptracker.data.repository.SleepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

enum class StatsPeriod(val days: Int, val label: String) {
    WEEK(7, "Week"),
    MONTH(30, "Month"),
    QUARTER(90, "3 Months"),
    YEAR(365, "Year")
}

data class DayPoint(
    val date: LocalDate,
    val xLabel: String,
    val totalHours: Float,
    val deepHours: Float,
    val record: SleepRecord?
)

data class StageDistribution(
    val lightMin: Int,
    val deepMin: Int,
    val remMin: Int,
    val awakeMin: Int
) {
    val totalMin: Int get() = lightMin + deepMin + remMin + awakeMin
}

data class StatsData(
    val period: StatsPeriod,
    val points: List<DayPoint>,
    val distribution: StageDistribution,
    val avgTotalMinutes: Int,
    val consistencyScore: Int,
    val avgOnsetMin: Int,
    val longestStreakDays: Int,
    val bestNight: SleepRecord?,
    val debtMinutes: Int,
    val goalMinutes: Int
)

class GetStatsUseCase @Inject constructor(
    private val sleepRepo: SleepRepository,
    private val profileRepo: ProfileRepository
) {
    operator fun invoke(period: StatsPeriod, today: LocalDate = LocalDate.now()): Flow<StatsData> {
        val zone = ZoneId.systemDefault()
        val start = today.minusDays((period.days - 1).toLong())
        val from = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        return combine(
            sleepRepo.observeRange(from, to),
            profileRepo.observe()
        ) { records, profile -> compute(period, today, records, profile) }
    }

    private fun compute(
        period: StatsPeriod,
        today: LocalDate,
        records: List<SleepRecord>,
        profile: UserProfile?
    ): StatsData {
        val zone = ZoneId.systemDefault()
        val byDay = records.associateBy {
            java.time.Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
        }

        val labelFormatter = when (period) {
            StatsPeriod.WEEK -> DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
            StatsPeriod.MONTH -> DateTimeFormatter.ofPattern("d", Locale.getDefault())
            StatsPeriod.QUARTER -> DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            StatsPeriod.YEAR -> DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        }

        val points = (0 until period.days).map { offset ->
            val date = today.minusDays((period.days - 1 - offset).toLong())
            val record = byDay[date]
            DayPoint(
                date = date,
                xLabel = labelFormatter.format(date),
                totalHours = record?.totalMinutes?.div(60f) ?: 0f,
                deepHours = record?.deepMinutes?.div(60f) ?: 0f,
                record = record
            )
        }

        val withRecords = records
        val distribution = StageDistribution(
            lightMin = withRecords.sumOf { it.lightMinutes },
            deepMin = withRecords.sumOf { it.deepMinutes },
            remMin = withRecords.sumOf { it.remMinutes },
            awakeMin = withRecords.sumOf { it.awakeMinutes }
        )
        val avgTotalMinutes = if (withRecords.isEmpty()) 0
        else withRecords.sumOf { it.totalMinutes } / withRecords.size

        val consistency = consistencyScore(withRecords)
        val avgOnset = if (withRecords.isEmpty()) 0
        else withRecords.map { (it.awakeMinutes * 0.3f + 8f).toInt() }.average().toInt()

        val goal = profile?.goalMinutes ?: 480
        val streak = longestStreak(points, goal)
        val best = withRecords.maxByOrNull { it.score }
        val debt = (period.days * goal) - withRecords.sumOf { it.totalMinutes }

        return StatsData(
            period = period,
            points = points,
            distribution = distribution,
            avgTotalMinutes = avgTotalMinutes,
            consistencyScore = consistency,
            avgOnsetMin = avgOnset,
            longestStreakDays = streak,
            bestNight = best,
            debtMinutes = -debt,
            goalMinutes = goal
        )
    }

    private fun consistencyScore(records: List<SleepRecord>): Int {
        if (records.size < 2) return 0
        val totals = records.map { it.totalMinutes.toDouble() }
        val mean = totals.average()
        val variance = totals.map { (it - mean) * (it - mean) }.average()
        val std = kotlin.math.sqrt(variance)
        return (100 - (std / 60.0 * 12).coerceAtMost(100.0)).toInt().coerceIn(0, 100)
    }

    private fun longestStreak(points: List<DayPoint>, goalMinutes: Int): Int {
        val goalH = goalMinutes / 60f
        var max = 0
        var cur = 0
        for (p in points) {
            if (p.totalHours >= goalH) { cur++; max = maxOf(max, cur) } else cur = 0
        }
        return max
    }
}

fun Int.formatDuration(): String {
    val abs = this.absoluteValue
    val h = abs / 60
    val m = abs % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}
