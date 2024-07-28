@file:OptIn(ExperimentalFoundationApi::class, FlowPreview::class)

package com.pramod.dailyword.framework.ui.settings.custom_time_notification

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.util.CalenderUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue


@Preview
@Composable
fun PreviewNotificationTimePicker() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        NotificationTimePicker(null) {}
    }
}

private fun isSameDay(
    userSelectedTimeInMillis: Long, startTimeInMillis: Long
): Boolean {
    return userSelectedTimeInMillis in startTimeInMillis until getLocalCalendar().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}


@Composable
fun NotificationTimePicker(
    notificationTriggerTime: NotificationPrefManager.NotificationTriggerTime?,
    onSetNotificationTime: (NotificationPrefManager.NotificationTriggerTime?) -> Unit
) {

    val context = LocalContext.current

    val color = colorResource(id = R.color.notification_timer_background)

    val localTimeAsPer5PMIst = remember {
        getLocalCalendar(17, 0).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            //if not same day then reduce one day
            if (getLocalCalendar().timeInMillis < timeInMillis) {
                add(Calendar.DATE, -1)
            }
        }
    }

    val defaultHourSelection = remember {
        //if user have not select hour then show the default server triggering time
        notificationTriggerTime?.let {
            getLocalCalendar().apply { timeInMillis = it.timeInMillis }.get(Calendar.HOUR_OF_DAY)
        } ?: localTimeAsPer5PMIst.get(Calendar.HOUR_OF_DAY)
    }

    val defaultMinSelection = remember {
        //if user have not select min then show the default server triggering time
        notificationTriggerTime?.let {
            getLocalCalendar().apply { timeInMillis = it.timeInMillis }.get(Calendar.MINUTE)
        } ?: localTimeAsPer5PMIst.get(Calendar.MINUTE)
    }

    var userSelectionTimeInMillis by remember(localTimeAsPer5PMIst, notificationTriggerTime) {
        if (notificationTriggerTime != null) {
            val notificationTriggerTimeInMillis = getLocalCalendar().apply {
                timeInMillis = notificationTriggerTime.timeInMillis
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                //if not same day then reduce one day
                if (getLocalCalendar().timeInMillis < timeInMillis) {
                    add(Calendar.DATE, -1)
                }
            }.timeInMillis
            mutableLongStateOf(notificationTriggerTimeInMillis)
        } else {
            mutableLongStateOf(localTimeAsPer5PMIst.timeInMillis)
        }
    }

    val startEndHourList = remember(localTimeAsPer5PMIst) { (0..23).toList() }

    val startEndMinuteList = remember(localTimeAsPer5PMIst) { (0..59).toList() }

    val notificationTriggerTimeText = remember(userSelectionTimeInMillis) {

        val calendar = getLocalCalendar().apply {
            timeInMillis = userSelectionTimeInMillis
            if (userSelectionTimeInMillis < getLocalCalendar().timeInMillis) {
                add(Calendar.DATE, 1)
            }
        }

        val time = CalenderUtil.convertCalenderToString(
            calender = calendar, dateFormat = CalenderUtil.TIME_FORMAT
        )

        String.format(
            context.resources.getString(R.string.dialog_notification_triggering_text_able),
            time,
            if (userSelectionTimeInMillis in localTimeAsPer5PMIst.timeInMillis..getLocalCalendar().apply {
                    timeInMillis = localTimeAsPer5PMIst.timeInMillis
                    add(Calendar.DATE, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            ) "same day" else "next day"
        )
    }

    AppCompatTheme {

        Column(
            modifier = Modifier.navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.dialog_notification_time_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.textColor_highEmphasis),
                    fontFamily = FontFamily(Font(R.font.lato_black))
                )
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .background(color, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    color, color.copy(0.5f), Color.Transparent
                                )
                            ), shape = RectangleShape
                        )
                        .align(Alignment.TopCenter)
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    VerticalWheelSpinner(modifier = Modifier.width(60.dp),
                        items = startEndHourList,
                        defaultSelected = defaultHourSelection,
                        onMovement = { isUpward, movement ->
                            userSelectionTimeInMillis = getLocalCalendar().apply {
                                timeInMillis = userSelectionTimeInMillis
                                add(Calendar.HOUR_OF_DAY, if (isUpward) movement else -(movement))
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        })

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = ":", style = MaterialTheme.typography.headlineMedium.copy(
                            color = colorResource(
                                id = R.color.textColor_mediumEmphasis
                            )
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    VerticalWheelSpinner(modifier = Modifier.width(60.dp),
                        items = startEndMinuteList,
                        defaultSelected = defaultMinSelection,
                        onMovement = { isUpward, movement ->
                            userSelectionTimeInMillis = getLocalCalendar().apply {
                                timeInMillis = userSelectionTimeInMillis
                                add(Calendar.MINUTE, if (isUpward) movement else -(movement))
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        })

                    Spacer(modifier = Modifier.weight(1f))

                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent, color.copy(0.5f), color
                                )
                            ), shape = RectangleShape
                        )
                        .align(Alignment.BottomCenter)
                )

            }


            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(color, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(16.dp),
                text = notificationTriggerTimeText,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.textColor_mediumEmphasis)
            )

            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = R.string.dialog_notification_time_note),
                color = colorResource(id = R.color.textColor_mediumEmphasis),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(35.dp),
                    onClick = {
                        onSetNotificationTime(null)
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.colorAccent),
                        disabledContentColor = colorResource(id = R.color.colorAccent).copy(0.5f),
                    ),
                    border = BorderStroke(
                        1.dp,
                        androidx.compose.material.MaterialTheme.colors.onSurface.copy(0.1f)
                    ),
                ) {
                    Text(text = stringResource(id = R.string.dialog_notification_time_negative_btn))
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorAccent),
                        disabledContainerColor = colorResource(id = R.color.colorAccent).copy(0.5f),
                    ),
                    modifier = Modifier
                        .weight(0.5f)
                        .height(35.dp),
                    shape = RoundedCornerShape(4.dp),
                    onClick = {
                        onSetNotificationTime(
                            NotificationPrefManager.NotificationTriggerTime(
                                isNextDay = userSelectionTimeInMillis !in localTimeAsPer5PMIst.timeInMillis..getLocalCalendar().apply {
                                    timeInMillis = localTimeAsPer5PMIst.timeInMillis
                                    add(Calendar.DATE, 1)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis, timeInMillis = getLocalCalendar().apply {
                                    timeInMillis = userSelectionTimeInMillis
                                    if (getLocalCalendar().timeInMillis > userSelectionTimeInMillis) {
                                        add(Calendar.DATE, 1)
                                    }
                                }.timeInMillis
                            )
                        )
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_notification_time_positive_btn),
                        color = colorResource(id = R.color.button_textColor_highEmphasis)
                    )
                }
            }

        }

    }

}

@Composable
fun VerticalWheelSpinner(
    modifier: Modifier = Modifier,
    items: List<Int>,
    defaultSelected: Int?,
    onMovement: (isUpward: Boolean, totalMovement: Int) -> Unit = { _, _ -> },
) {

    val coroutineScope = rememberCoroutineScope()

    val view = LocalView.current

    val height = 140.dp

    val cellSize = height / 3

    val numbers = listOf(-1) + items + listOf(-1)

    val pagerState =
        rememberPagerState(items.indexOf(defaultSelected).takeIf { it != -1 } ?: 0) { numbers.size }

    var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }

    VerticalPager(
        modifier = modifier.height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = pagerState,
        pageSize = PageSize.Fixed(cellSize),
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(10),
            snapAnimationSpec = tween(),
        )

    ) { page ->

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellSize), contentAlignment = Alignment.Center
        ) {

            val content = numbers[page]

            if (content != -1) {

                Text(modifier = Modifier
                    .graphicsLayer {
                        val pageOffset =
                            (((pagerState.currentPage + 1) - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        // We animate the alpha, between 50% and 100%
                        alpha = lerp(
                            start = 0.4f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        val scale = lerp(
                            start = 1f, stop = 1.5f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page - 1)
                        }
                    },

                    text = String.format(
                        Locale.Builder().setLocale(Locale.getDefault()).build(), "%02d", content
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(id = R.color.textColor_highEmphasis)
                )
            }
        }

    }

    // Collect the pager state in a snapshot flow
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.filter { it != previousPage }  // Only process changes
            .collect { newPage ->
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                val jump = abs(newPage - previousPage)
                onMovement(previousPage < newPage, jump)
                previousPage = newPage
            }
    }
}