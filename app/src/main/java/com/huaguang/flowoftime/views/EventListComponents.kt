package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventWithSubEvents
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import java.time.Duration

@Composable
fun EventList(
    viewModel: EventsViewModel,
    listState: LazyListState,
    eventsWithSubEvents: List<EventWithSubEvents>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = listState
        ) {
            items(eventsWithSubEvents) { (event, subEvents) ->
                EventItem(event, subEvents, viewModel)
            }
        }
    }
}

@Composable
fun EventItem(
    event: Event,
    subEvents: List<Event> = listOf(),
    viewModel: EventsViewModel
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            EventItemRow(event = event, showTime = true, viewModel)

            // 插入的临时事件的 UI
            for (subEvent in subEvents) {
                EventItemRow(
                    event = subEvent,
                    showTime = false,
                    viewModel = viewModel,  // 添加了一些左侧的 padding 以便缩进
                    modifier = Modifier.padding(start = 30.dp)
                )
            }
        }
    }
}

@Composable
fun EventItemRow(
    event: Event,
    showTime: Boolean,
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier
) {
    Log.i("打标签喽", "EventItemRow 重组执行！id = ${event.id}")
    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var duration by remember { mutableStateOf(event.duration) }
    val selectedEventIdsMap by viewModel.selectedEventIdsMap.observeAsState(mutableMapOf())

    LaunchedEffect(event.endTime, event.duration) {
        endTime = event.endTime
        duration = event.duration
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showTime) {
            DraggableText(
                text = formatLocalDateTime(startTime),
                modifier = Modifier.padding(end = 5.dp),
                onDragDelta = { dragValue ->
                    startTime = startTime.plusMinutes(dragValue.toLong())

                    if (duration != null) {
                        val delta = Duration.between(startTime, event.startTime)
                        Log.i("打标签喽", "delta = $delta")
                        duration = event.duration!! + delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(startTime = startTime, duration = duration)
                    viewModel.updateTimeToDB(updatedEvent)
                }
            )
        }

        Text(
            text = if (showTime) event.name else "……${event.name}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                viewModel.onNameTextClicked(event)
            }.let { modifier ->
                if (selectedEventIdsMap[event.id] == true) {
                    modifier
                        .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                } else modifier
            }
        )

        if (showTime) {
            DraggableText(
                text = endTime?.let { formatLocalDateTime(it) } ?: "...",
                modifier = Modifier.padding(start = 5.dp),
                onDragDelta = { dragValue ->
                    // 还没有结束时间的时候禁止拖动
                    if (endTime != null) {
                        endTime = endTime!!.plusMinutes(dragValue.toLong())
                        val delta = Duration.between(endTime, event.endTime)
                        duration = event.duration!! - delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(endTime = endTime, duration = duration)
                    viewModel.updateTimeToDB(updatedEvent)
                }
            )
        }

        Text(
            text = duration?.let { formatDurationInText(it) } ?: "...",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}