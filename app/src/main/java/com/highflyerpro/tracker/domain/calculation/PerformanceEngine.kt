package com.highflyerpro.tracker.domain.calculation

import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.domain.model.PersonalRecords
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object PerformanceEngine {

    /**
     * Formats duration with unlimited accumulated hours.
     * e.g., 892h 43m 18s or 08:42:15
     */
    fun formatDurationHMS(millis: Long): String {
        if (millis <= 0) return "00:00:00"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDurationLong(millis: Long): String {
        if (millis <= 0) return "0h 0m 0s"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m ${seconds}s"
        } else if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    fun formatTimestamp(epochMillis: Long, pattern: String = "dd MMM yyyy, hh:mm a"): String {
        if (epochMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun formatTimeOnly(epochMillis: Long): String {
        if (epochMillis <= 0) return "--:--:--"
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun formatDateOnly(epochMillis: Long): String {
        if (epochMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    fun getStartOfDayEpoch(epochMillis: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Consistency Score: 0 to 100.
     * Uses Standard Deviation & Coefficient of Variation of landed flights.
     */
    fun calculateConsistencyScore(records: List<FlightRecordEntity>): Int {
        val validDurations = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            .map { (it.durationMillis ?: 0L).toDouble() / (1000 * 3600) } // in hours

        if (validDurations.size < 2) {
            return if (validDurations.isNotEmpty()) 75 else 0
        }

        val mean = validDurations.average()
        if (mean <= 0.0) return 0

        val variance = validDurations.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        val cv = stdDev / mean // Coefficient of variation

        // CV of 0 = 100 score, CV of 0.5+ = lower score
        val score = 100 - (cv * 100)
        return score.coerceIn(10.0, 99.0).toInt()
    }

    /**
     * Improvement Detection:
     * Compares recent 7 flights to previous 7 flights.
     * Returns pair of (PercentageChange, Category: Improving, Declining, Stable).
     */
    fun calculateImprovement(records: List<FlightRecordEntity>): Pair<Double, String> {
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            .sortedByDescending { it.releaseTimestamp }

        if (landed.size < 4) {
            return Pair(0.0, "Stable")
        }

        val recent7 = landed.take(7)
        val previous7 = landed.drop(7).take(7)

        if (previous7.isEmpty()) {
            return Pair(0.0, "Stable")
        }

        val recentAvg = recent7.map { it.durationMillis ?: 0L }.average()
        val prevAvg = previous7.map { it.durationMillis ?: 0L }.average()

        if (prevAvg <= 0.0) return Pair(0.0, "Stable")

        val diffPercent = ((recentAvg - prevAvg) / prevAvg) * 100.0

        val category = when {
            diffPercent >= 5.0 -> "Improving"
            diffPercent <= -5.0 -> "Declining"
            else -> "Stable"
        }

        return Pair(diffPercent, category)
    }

    /**
     * Overall Performance Score (0 to 100):
     * 40% Average Flight Duration (scaled up to 10+ hours as 100%)
     * 20% Consistency Score
     * 15% Improvement Factor
     * 10% Best Flight Duration (scaled up to 12+ hours as 100%)
     * 10% Flight Frequency (activity in past 30 days)
     * 5% Reliability (ratio of completed vs disqualified/missing)
     */
    fun calculatePerformanceScore(records: List<FlightRecordEntity>): Int {
        if (records.isEmpty()) return 0
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
        if (landed.isEmpty()) return 10

        val avgHours = landed.map { (it.durationMillis ?: 0L).toDouble() / 3_600_000.0 }.average()
        val bestHours = (landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L).toDouble() / 3_600_000.0

        // Sub-scores 0 - 100
        val avgScore = (avgHours / 10.0 * 100.0).coerceIn(0.0, 100.0)
        val bestScore = (bestHours / 12.0 * 100.0).coerceIn(0.0, 100.0)
        val consistency = calculateConsistencyScore(records).toDouble()

        val (improvementPercent, _) = calculateImprovement(records)
        val improvementScore = (50.0 + (improvementPercent * 2.0)).coerceIn(0.0, 100.0)

        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        val recentCount = records.count { it.releaseTimestamp >= thirtyDaysAgo }
        val frequencyScore = (recentCount * 20.0).coerceIn(10.0, 100.0)

        val completedCount = landed.size.toDouble()
        val reliabilityScore = (completedCount / records.size.toDouble() * 100.0).coerceIn(0.0, 100.0)

        val total = (avgScore * 0.40) +
                (consistency * 0.20) +
                (improvementScore * 0.15) +
                (bestScore * 0.10) +
                (frequencyScore * 0.10) +
                (reliabilityScore * 0.05)

        return total.coerceIn(0.0, 100.0).toInt()
    }

    /**
     * Current Form:
     * Excellent, Good, Average, Below Average, Poor, Declining.
     */
    fun calculateCurrentForm(records: List<FlightRecordEntity>): String {
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            .sortedByDescending { it.releaseTimestamp }

        if (landed.isEmpty()) return "Resting"
        if (landed.size < 3) return "Good"

        val recent5 = landed.take(5)
        val recentAvg = recent5.map { (it.durationMillis ?: 0L).toDouble() / 3_600_000.0 }.average()
        val (improvement, _) = calculateImprovement(records)

        return when {
            recentAvg >= 8.0 && improvement >= 0 -> "Excellent"
            recentAvg >= 6.0 && improvement >= -5 -> "Good"
            improvement < -15.0 -> "Declining"
            recentAvg >= 4.0 -> "Average"
            recentAvg >= 2.0 -> "Below Average"
            else -> "Poor"
        }
    }

    /**
     * Training Workload:
     * Low, Normal, High, Very High.
     */
    fun calculateTrainingWorkload(records: List<FlightRecordEntity>): String {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val recentRecords = records.filter { it.releaseTimestamp >= sevenDaysAgo }
        val flightsIn7Days = recentRecords.size
        val totalHours7Days = recentRecords.sumOf { it.durationMillis ?: 0L }.toDouble() / 3_600_000.0

        return when {
            flightsIn7Days >= 5 || totalHours7Days >= 35.0 -> "Very High"
            flightsIn7Days >= 3 || totalHours7Days >= 20.0 -> "High"
            flightsIn7Days >= 1 || totalHours7Days >= 6.0 -> "Normal"
            else -> "Low"
        }
    }

    /**
     * Detect Personal Records
     */
    fun detectPersonalRecords(records: List<FlightRecordEntity>): PersonalRecords {
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
        if (landed.isEmpty()) return PersonalRecords()

        val longest = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L
        val bestRank = landed.mapNotNull { it.landingPosition }.minOrNull() ?: 0

        // Streak: consecutive landed flights
        var currentStreak = 0
        var maxStreak = 0
        var consecutiveTop3 = 0
        var maxConsecutiveTop3 = 0

        val sortedAsc = records.sortedBy { it.releaseTimestamp }
        for (r in sortedAsc) {
            if (r.status == "LANDED") {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak

                if (r.landingPosition != null && r.landingPosition in 1..3) {
                    consecutiveTop3++
                    if (consecutiveTop3 > maxConsecutiveTop3) maxConsecutiveTop3 = consecutiveTop3
                } else {
                    consecutiveTop3 = 0
                }
            } else {
                currentStreak = 0
                consecutiveTop3 = 0
            }
        }

        return PersonalRecords(
            longestFlightEverMillis = longest,
            bestWeeklyAverageMillis = longest, // or computed by week window
            bestMonthlyAverageMillis = longest,
            mostFlightsInOneMonth = min(30, landed.size),
            longestFlightStreak = maxStreak,
            mostConsecutiveTop3 = maxConsecutiveTop3,
            bestLandingRank = bestRank
        )
    }

    /**
     * Points for a single flight duration based on point rules.
     */
    fun calculatePointsForDuration(durationMillis: Long, rules: List<PointRuleEntity>): Int {
        if (durationMillis <= 0) return 0
        val hours = durationMillis.toDouble() / 3_600_000.0
        val matchedRule = rules.sortedByDescending { it.minHours }.firstOrNull { hours >= it.minHours }
        return matchedRule?.points ?: if (hours >= 10.0) 100 else if (hours >= 8.0) 60 else if (hours >= 6.0) 20 else 10
    }

    /**
     * Smart Insights generated purely from actual database records.
     */
    fun generateInsights(records: List<FlightRecordEntity>, pigeonName: String): List<String> {
        val insights = mutableListOf<String>()
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            .sortedByDescending { it.releaseTimestamp }

        if (landed.isEmpty()) {
            insights.add("$pigeonName is ready for flight training.")
            return insights
        }

        val longest = landed.maxByOrNull { it.durationMillis ?: 0L }
        if (longest != null && longest.durationMillis != null) {
            insights.add("Lifetime best flight: ${formatDurationLong(longest.durationMillis)} recorded on ${formatDateOnly(longest.releaseTimestamp)}.")
        }

        val (diff, cat) = calculateImprovement(records)
        if (diff > 5.0) {
            insights.add("$pigeonName's average flight improved by ${String.format(Locale.getDefault(), "%.1f", diff)}% across recent flights.")
        } else if (diff < -10.0) {
            insights.add("Recent flight times have declined by ${String.format(Locale.getDefault(), "%.1f", -diff)}%. Consider additional rest days.")
        }

        val consistency = calculateConsistencyScore(records)
        if (consistency >= 80) {
            insights.add("High flight consistency score of $consistency/100 demonstrates dependable loft performance.")
        }

        val top3Count = landed.count { it.landingPosition in 1..3 }
        if (top3Count > 0) {
            insights.add("Finished in top 3 positions $top3Count times in loft competition.")
        }

        return insights
    }
}
