package com.pramod.dailyword.framework.ui.worddetails.word_history

import androidx.annotation.DimenRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.worddetails.WordHistoryEntryUiModel
import com.pramod.dailyword.framework.ui.worddetails.WordHistoryUiModel


@Composable
fun WordHistorySection(
    wordHistory: WordHistoryUiModel,
    wordColor: Color,
    modifier: Modifier = Modifier
) {
    val hasContent = !wordHistory.originStory.isNullOrEmpty()
            || !wordHistory.bornIn.isNullOrEmpty()
            || !wordHistory.throughTheAges.isNullOrEmpty()

    if (!hasContent) return

    val dividerColor = colorResource(R.color.textColor_highEmphasis).copy(alpha = 0.05f)
    val labelColor = wordColor

    AppCompatTheme {
        Card(
            elevation = 0.dp,
            shape = RectangleShape
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.margin_large))
            ) {
                Text(
                    text = "Word History",
                    style = TextStyle(
                        fontSize = textSize(R.dimen.text_sub_title),
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colorResource(R.color.textColor_highEmphasis)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

                val rows = buildList {
                    wordHistory.originStory?.let { add("Etymology" to it) }
                    wordHistory.bornIn?.let { add("First Known Use" to it) }
                    wordHistory.throughTheAges?.let {
                        add(
                            "Time Traveler" to listOf(
                                WordHistoryEntryUiModel(text = it)
                            )
                        )
                    }
                }

                rows.forEach { (label, entries) ->
                    WordHistoryRow(label = label, labelColor = labelColor) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            entries.forEach { entry -> SubEntry(entry) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubEntry(entry: WordHistoryEntryUiModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        entry.partOfSpeech?.let {
            PartOfSpeechBadge(label = it)
        }
        entry.text?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontSize = dimensionResource(R.dimen.text_sub_title).value.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = dimensionResource(R.dimen.text_sub_title).value.sp * 1.6f
                ),
                color = colorResource(R.color.textColor_mediumEmphasis)
            )
        }
    }
}

@Composable
private fun PartOfSpeechBadge(label: AnnotatedString) {
    Box(
        modifier = Modifier
            .background(
                color = colorResource(R.color.textColor_highEmphasis).copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.5.dp,
                color = colorResource(R.color.textColor_highEmphasis).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = dimensionResource(R.dimen.text_tiny).value.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.1.sp
            ),
            color = colorResource(R.color.textColor_mediumEmphasis)
        )
    }
}

@Composable
private fun WordHistoryRow(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = labelColor.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.5.dp,
                color = labelColor.copy(alpha = 0.09f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = dimensionResource(R.dimen.text_tiny).value.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            ),
            color = labelColor,
        )
        content()
    }

    Spacer(modifier = Modifier.size(16.dp))
}

@Composable
private fun textSize(@DimenRes id: Int): TextUnit {
    val density = LocalDensity.current
    val size = dimensionResource(id)
    return remember(size, density) { with(density) { size.toSp() } }
}