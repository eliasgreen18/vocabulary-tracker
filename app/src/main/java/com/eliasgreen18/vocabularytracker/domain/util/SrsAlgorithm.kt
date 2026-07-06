package com.eliasgreen18.vocabularytracker.domain.util

import java.time.Instant
import java.time.temporal.ChronoUnit

object SrsAlgorithm {

    /**
     * Calculates the next review date and interval based on user recall performance.
     * 
     * If remembered:
     * - 0 -> 1 day
     * - 1 -> 3 days
     * - 3 -> 7 days
     * - 7 -> 14 days
     * - 14 -> 28 days
     * - X -> X * 2 days
     * 
     * If forgotten:
     * - Reset interval to 1 day.
     */
    fun calculateNextReview(
        currentIntervalDays: Int,
        remembered: Boolean,
        now: Instant = Instant.now()
    ): Pair<Instant, Int> {
        val nextInterval = if (remembered) {
            when (currentIntervalDays) {
                0 -> 1
                1 -> 3
                3 -> 7
                7 -> 14
                14 -> 28
                else -> currentIntervalDays * 2
            }
        } else {
            1
        }

        val nextDate = now.plus(nextInterval.toLong(), ChronoUnit.DAYS)
        return nextDate to nextInterval
    }
}
