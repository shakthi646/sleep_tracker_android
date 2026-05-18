package com.ksp.sleeptracker.domain.usecase

import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.repository.SleepRepository
import com.ksp.sleeptracker.ui.components.DayBar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class GetWeeklySleepUseCase @Inject constructor(
    private val repo: SleepRepository
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<List<DayBar>> {
        val zone = ZoneId.systemDefault()
        val monday = today.with(DayOfWeek.MONDAY)
        val from = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = monday.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()

        return repo.observeRange(from, to).map { records ->
            (0 until 7).map { offset ->
                val day = monday.plusDays(offset.toLong())
                val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val record = records.firstOrNull { it.startTime in dayStart until dayEnd }
                val hours = record?.totalMinutes?.div(60f) ?: 0f
                DayBar(
                    label = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    hours = hours,
                    isToday = day == today,
                    isFuture = day.isAfter(today) || (day == today && record == null)
                )
            }
        }
    }
}

fun SleepRecord.localStartDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(startTime).atZone(zone).toLocalDate()
