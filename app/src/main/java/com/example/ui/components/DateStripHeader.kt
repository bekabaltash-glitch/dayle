package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateStripHeader(
    selectedDateStr: String,
    onDateSelected: (String) -> Unit,
    onTodayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate = try {
        LocalDate.parse(selectedDateStr)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val today = LocalDate.now()
    val isTodaySelected = selectedDate.isEqual(today)

    // Generate range of 30 days (-14 to +15 around today)
    val dates = remember(today) {
        (-14L..15L).map { today.plusDays(it) }
    }

    val listState = rememberLazyListState()

    // Scroll to selected date
    LaunchedEffect(selectedDateStr) {
        val index = dates.indexOfFirst { it.isEqual(selectedDate) }
        if (index != -1) {
            listState.animateScrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    val monthFormatted = formatMonthKazakh(selectedDate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Top row with Month Year & Today button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = monthFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                onClick = onTodayClicked,
                shape = CircleShape,
                color = if (isTodaySelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("today_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Бүгін",
                        tint = if (isTodaySelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Бүгін",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTodaySelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Horizontal Date Strip
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = dates, key = { date -> date.toString() }) { date ->
                val isSelected = date.isEqual(selectedDate)
                val isCurrentToday = date.isEqual(today)
                val dayName = formatDayNameKazakh(date)
                val dayNum = date.dayOfMonth.toString()

                val cardBg = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isCurrentToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isCurrentToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val subTextColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    isCurrentToday -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(
                            width = if (isCurrentToday && !isSelected) 1.dp else 0.dp,
                            color = if (isCurrentToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onDateSelected(date.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("date_item_${date}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor
                        )
                        Text(
                            text = dayNum,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        if (isCurrentToday) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        } else {
                            Box(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun formatDayNameKazakh(date: LocalDate): String {
    return when (date.dayOfWeek.value) {
        1 -> "Дүй"
        2 -> "Сей"
        3 -> "Сәр"
        4 -> "Бей"
        5 -> "Жұм"
        6 -> "Сен"
        7 -> "Жек"
        else -> ""
    }
}

private fun formatMonthKazakh(date: LocalDate): String {
    val monthKazakh = when (date.monthValue) {
        1 -> "Қаңтар"
        2 -> "Ақпан"
        3 -> "Наурыз"
        4 -> "Сәуір"
        5 -> "Мамыр"
        6 -> "Маусым"
        7 -> "Шілде"
        8 -> "Тамыз"
        9 -> "Қыркүйек"
        10 -> "Қазан"
        11 -> "Қараша"
        12 -> "Желтоқсан"
        else -> ""
    }
    return "$monthKazakh ${date.year}"
}
