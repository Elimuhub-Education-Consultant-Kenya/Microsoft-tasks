/*
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.repeats

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.WeekDay
import org.tasks.R
import org.tasks.compose.collectAsStateLifecycleAware
import org.tasks.compose.edit.RepeatRow
import org.tasks.repeats.BasicRecurrenceDialog
import org.tasks.repeats.RecurrenceUtils.newRecur
import org.tasks.repeats.RepeatRuleToString
import org.tasks.time.DateTime
import org.tasks.time.DateTimeUtils.currentTimeMillis
import org.tasks.ui.TaskEditControlFragment
import javax.inject.Inject

@AndroidEntryPoint
class RepeatControlSet : TaskEditControlFragment() {
    @Inject lateinit var repeatRuleToString: RepeatRuleToString

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_RECURRENCE) {
            if (resultCode == RESULT_OK) {
                viewModel.recurrence.value = data?.getStringExtra(BasicRecurrenceDialog.EXTRA_RRULE)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onDueDateChanged() {
        viewModel.recurrence.value?.takeIf { it.isNotBlank() }?.let { recurrence ->
            val recur = newRecur(recurrence)
            if (recur.frequency == Recur.Frequency.MONTHLY && recur.dayList.isNotEmpty()) {
                val weekdayNum = recur.dayList[0]
                val dateTime =
                    DateTime(this.viewModel.dueDate.value.let { if (it > 0) it else currentTimeMillis() })
                val num: Int
                val dayOfWeekInMonth = dateTime.dayOfWeekInMonth
                num = if (weekdayNum.offset == -1 || dayOfWeekInMonth == 5) {
                    if (dayOfWeekInMonth == dateTime.maxDayOfWeekInMonth) -1 else dayOfWeekInMonth
                } else {
                    dayOfWeekInMonth
                }
                recur.dayList.let {
                    it.clear()
                    it.add(WeekDay(dateTime.weekDay, num))
                }
                viewModel.recurrence.value = recur.toString()
            }
        }
    }

    override fun createView(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenResumed {
            viewModel.dueDate.collect {
                onDueDateChanged()
            }
        }
    }

    override fun bind(parent: ViewGroup?): View =
        (parent as ComposeView).apply {
            setContent {
                MdcTheme {
                    RepeatRow(
                        recurrence = viewModel.recurrence.collectAsStateLifecycleAware().value?.let {
                            repeatRuleToString.toString(it)
                        },
                        repeatAfterCompletion = viewModel.repeatAfterCompletion.collectAsStateLifecycleAware().value,
                        onClick = {
                            BasicRecurrenceDialog.newBasicRecurrenceDialog(
                                this@RepeatControlSet,
                                REQUEST_RECURRENCE,
                                viewModel.recurrence.value,
                                viewModel.dueDate.value.let { if (it > 0) it else currentTimeMillis() }
                            )
                                .show(parentFragmentManager, FRAG_TAG_BASIC_RECURRENCE)
                        },
                        onRepeatFromChanged = { viewModel.repeatAfterCompletion.value = it }
                    )
                }
            }
        }

    override fun controlId() = TAG

    companion object {
        const val TAG = R.string.TEA_ctrl_repeat_pref
        private const val FRAG_TAG_BASIC_RECURRENCE = "frag_tag_basic_recurrence"
        private const val REQUEST_RECURRENCE = 10000
    }
}
