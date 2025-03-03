package com.todoroo.astrid.alarms

import com.todoroo.andlib.utility.DateUtilities
import com.todoroo.astrid.data.Task
import org.tasks.data.Alarm
import org.tasks.jobs.AlarmEntry
import org.tasks.preferences.Preferences
import org.tasks.reminders.Random
import org.tasks.time.DateTimeUtils.withMillisOfDay
import javax.inject.Inject

class AlarmCalculator(
    private val random: Random,
    private val defaultTimeProvider: () -> Int,
){
    @Inject
    internal constructor(
        preferences: Preferences
    ) : this(Random(), { preferences.defaultDueTime })

    fun toAlarmEntry(task: Task, alarm: Alarm): AlarmEntry? {
        val trigger = when (alarm.type) {
            Alarm.TYPE_SNOOZE,
            Alarm.TYPE_DATE_TIME ->
                alarm.time
            Alarm.TYPE_REL_START ->
                when {
                    task.hasStartTime() ->
                        task.hideUntil + alarm.time
                    task.hasStartDate() ->
                        task.hideUntil.withMillisOfDay(defaultTimeProvider()) + alarm.time
                    else ->
                        AlarmService.NO_ALARM
                }
            Alarm.TYPE_REL_END ->
                when {
                    task.hasDueTime() ->
                        task.dueDate + alarm.time
                    task.hasDueDate() ->
                        task.dueDate.withMillisOfDay(defaultTimeProvider()) + alarm.time
                    else ->
                        AlarmService.NO_ALARM
                }
            Alarm.TYPE_RANDOM ->
                calculateNextRandomReminder(random, task, alarm.time)
            else ->
                AlarmService.NO_ALARM
        }
        return when {
            trigger <= AlarmService.NO_ALARM ->
                null
            trigger > task.reminderLast || alarm.type == Alarm.TYPE_SNOOZE ->
                AlarmEntry(alarm.id, alarm.task, trigger, alarm.type)
            alarm.repeat > 0 -> {
                val past = (task.reminderLast - trigger) / alarm.interval
                val next = trigger + (past + 1) * alarm.interval
                if (past < alarm.repeat && next > task.reminderLast) {
                    AlarmEntry(alarm.id, alarm.task, next, alarm.type)
                } else {
                    null
                }
            }
            else ->
                null
        }
    }

    /** Schedules alarms for a single task  */
    /**
     * Calculate the next alarm time for random reminders.
     *
     *
     * We take the last reminder time and add approximately the reminder period. If it's still in
     * the past, we set it to some time in the near future.
     */
    private fun calculateNextRandomReminder(random: Random, task: Task, reminderPeriod: Long): Long {
        if (reminderPeriod > 0) {
            var `when` = task.reminderLast
            if (`when` == 0L) {
                `when` = task.creationDate
            }
            `when` += (reminderPeriod * (0.85f + 0.3f * random.nextFloat())).toLong()
            if (`when` < DateUtilities.now()) {
                `when` =
                    DateUtilities.now() + ((0.5f + 6 * random.nextFloat()) * DateUtilities.ONE_HOUR).toLong()
            }
            return `when`
        }
        return AlarmService.NO_ALARM
    }
}