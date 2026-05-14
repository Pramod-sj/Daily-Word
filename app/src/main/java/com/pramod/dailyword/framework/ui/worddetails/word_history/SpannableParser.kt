package com.pramod.dailyword.framework.ui.worddetails.word_history

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Parses a string containing custom BBCode-like tags into an [AnnotatedString] for use in Jetpack Compose.
 *
 * Supported tags:
 * - `[i]text[/i]`: Italicizes text.
 * - `[b]text[/b]`: Bolds text.
 * - `[u]text[/u]`: Underlines text.
 * - `[s]text[/s]`: Strikes through text.
 * - `[size=20]text[/size]`: Sets font size in sp.
 * - `[color=#FFFFFF,#000000]text[/color]`: Sets text color. Accepts a comma-separated pair for light and dark modes.
 * - `[link url="https://example.com"]text[/link]`: Creates a clickable link.
 *
 * @param linkColor The color to be applied to the text within `[link]` tags.
 * @param isSystemInDarkTheme Determines which color to pick from the `[color]` tag's parameter list.
 * @param onLinkClick A callback invoked when a clickable link is tapped, providing the URL string.
 * @return An [AnnotatedString] with the styles and annotations applied.
 */
suspend fun String.parseToCustomAnnotatedString(
    linkColor: Color,
    isSystemInDarkTheme: Boolean = false, // 1. Added Theme State Parameter
    onLinkClick: (String) -> Unit
): AnnotatedString = withContext(Dispatchers.Default) {
    return@withContext buildAnnotatedString {
        val regex = Regex("""\[(/)?(i|b|u|s|link|size|color)(?:\s+url="([^"]+)")?(?:=([^\]]+))?]""")
        var cursor = 0
        var stackDepth = 0
        val rawString = this@parseToCustomAnnotatedString.trim()

        regex.findAll(rawString).forEach { match ->
            if (match.range.first > cursor) {
                append(rawString.substring(cursor, match.range.first))
            }

            val isClosing = match.groupValues[1] == "/"
            val tag = match.groupValues[2]
            val url = match.groupValues[3]
            val valueString = match.groupValues[4]

            if (isClosing) {
                if (stackDepth > 0) {
                    pop()
                    stackDepth--
                }
            } else {
                var validTag = true

                when (tag) {
                    "i" -> pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    "b" -> pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    "u" -> pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    "s" -> pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))

                    "size" -> {
                        val fontSize = valueString.toIntOrNull()?.sp ?: TextUnit.Unspecified
                        pushStyle(SpanStyle(fontSize = fontSize))
                    }

                    "color" -> {
                        // 2. Split the values and pick based on the theme
                        val colors = valueString.split(",")
                        val lightHex = colors.getOrNull(0)?.trim()
                        val darkHex = colors.getOrNull(1)?.trim()
                            ?: lightHex // Fallback to light if dark is missing

                        val activeHex = if (isSystemInDarkTheme) darkHex else lightHex

                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(activeHex))
                        } catch (_: Exception) {
                            Color.Unspecified
                        }
                        pushStyle(SpanStyle(color = parsedColor))
                    }

                    "link" -> pushLink(
                        LinkAnnotation.Clickable(
                            tag = url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                )
                            ),
                            linkInteractionListener = {
                                onLinkClick(url)
                            }
                        )
                    )

                    else -> validTag = false
                }

                if (validTag) stackDepth++
            }
            cursor = match.range.last + 1
        }

        if (cursor < rawString.length) {
            append(rawString.substring(cursor))
        }
    }
}