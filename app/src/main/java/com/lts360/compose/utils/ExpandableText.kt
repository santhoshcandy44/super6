package com.lts360.compose.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import com.lts360.components.utils.openUrlInCustomTab
import com.lts360.compose.ui.theme.customColorScheme

@Composable
fun ExpandableText(
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    textModifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontStyle: FontStyle? = null,
    collapsedMaxLine: Int = 5,
    showMoreText: String = "... Show More",
    showMoreStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.W500),
    showLessText: String = " Show Less",
    showLessStyle: SpanStyle = showMoreStyle,
    textAlign: TextAlign? = null,
    linkColor: Color = MaterialTheme.customColorScheme.chatTextLinkColor
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var clickable by rememberSaveable { mutableStateOf(false) }
    var lastCharIndex by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current

    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints {

        val maxWidth = this.maxWidth
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx().toInt() }

        val annotatedString = buildAnnotatedString {

            val urlPattern = Regex(
                "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
                setOf(RegexOption.IGNORE_CASE)
            )

            var currentIndex = 0


            urlPattern.findAll(text).forEach { match ->
                if (match.range.first > currentIndex) {
                    append(text.substring(currentIndex, match.range.first))
                }

                pushStringAnnotation(tag = "URL", annotation = match.value)

                withLink(
                    LinkAnnotation.Url(
                        url = match.value,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            ))) {
                        openUrlInCustomTab(
                            context,
                            match.value.trim()
                        )

                    }
                ) {
                    append(match.value)
                }

                pop()

                currentIndex = match.range.last + 1
            }

            // Append the remaining part of the message if any text is left
            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }

        val result = remember { textMeasurer.measure(text = annotatedString,
            style = style.copy(fontSize = fontSize), maxLines = collapsedMaxLine,
            constraints = Constraints(maxWidth = (maxWidthPx)))
        }

        if (!isExpanded && result.hasVisualOverflow) {
            clickable = true
            lastCharIndex = result.getLineEnd(collapsedMaxLine - 1)
        }

        val finalizedText = buildAnnotatedString {
            if (clickable) {
                if (isExpanded) {
                    append(annotatedString)

                    withLink(
                        LinkAnnotation.Clickable(tag = showLessText, styles = TextLinkStyles(style = showLessStyle)) {

                            if (clickable) {
                                isExpanded = !isExpanded
                            }
                        }
                    ) {
                        append(showLessText)
                    }

                } else {


                    // Display truncated text and "Show More" button when collapsed.
                    val adjustText = annotatedString.substring(startIndex = 0, endIndex = lastCharIndex)
                        .dropLast(showMoreText.length)
                        .dropLastWhile { Character.isWhitespace(it) || it == '.' }

                    append(adjustText)

                    withLink(
                        LinkAnnotation.Clickable(
                            tag = showMoreText,
                            styles = TextLinkStyles(
                                style = showMoreStyle
                            )
                        ) {
                            if (clickable) {
                                isExpanded = !isExpanded
                            }
                        }
                    ) {
                        append(showMoreText)
                    }

                }
            } else {
                append(annotatedString)
            }
        }

        Text(
            modifier = textModifier,
            text = finalizedText,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
            fontStyle = fontStyle,
            style = style,
            textAlign = textAlign,
            fontSize = fontSize
        )
    }

}