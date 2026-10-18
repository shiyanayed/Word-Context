package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

object MarkdownTextHelper {

    /**
     * Cleans raw AI markdown text:
     * 1. Eliminates literal '***' lines / horizontal rules.
     * 2. Formats '***text***' as bold + italic without the ugly asterisks.
     * 3. Formats '**text**' as bold without the asterisks.
     * 4. Formats '*text*' as italic without the asterisks.
     * 5. Strips any stray or orphaned '***' and '**'.
     */
    fun cleanAndFormatMarkdown(raw: String): AnnotatedString {
        if (raw.isBlank()) return AnnotatedString("")

        // Step 1: Remove markdown divider lines like '***' or '---' on their own line
        val cleanDividers = raw.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            if (trimmed == "***" || trimmed == "---" || trimmed == "___") {
                ""
            } else {
                line
            }
        }

        // Step 2: Use regex parsing to build AnnotatedString without asterisks
        return buildAnnotatedString {
            // Regex matches:
            // Group 1: ***bold italic***
            // Group 2: **bold**
            // Group 3: *italic*
            // Group 4: stray *** or **
            val pattern = Regex("""\*\*\*([^*\n]+?)\*\*\*|\*\*([^*\n]+?)\*\*|\*([^*\n]+?)\*|\*{2,3}""")
            var currentIndex = 0

            val matches = pattern.findAll(cleanDividers)
            for (match in matches) {
                // Append text before match
                if (match.range.first > currentIndex) {
                    append(cleanDividers.substring(currentIndex, match.range.first))
                }

                when {
                    // Match ***bold italic***
                    match.groups[1] != null -> {
                        val text = match.groups[1]!!.value
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                        append(text)
                        pop()
                    }
                    // Match **bold**
                    match.groups[2] != null -> {
                        val text = match.groups[2]!!.value
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(text)
                        pop()
                    }
                    // Match *italic*
                    match.groups[3] != null -> {
                        val text = match.groups[3]!!.value
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text)
                        pop()
                    }
                    // Stray *** or **: simply omit them!
                    else -> {
                        // Do not append the asterisks
                    }
                }
                currentIndex = match.range.last + 1
            }

            // Append any remaining text
            if (currentIndex < cleanDividers.length) {
                append(cleanDividers.substring(currentIndex))
            }
        }
    }
}

@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {
    val annotated = remember(text) { MarkdownTextHelper.cleanAndFormatMarkdown(text) }
    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        textAlign = textAlign,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = style
    )
}
