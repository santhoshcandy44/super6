package com.super6.pot.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

import com.super6.pot.ui.chat.LinkPreviewData
import com.super6.pot.ui.chat.fetchLinkPreview
import com.super6.pot.ui.theme.customColorScheme
import com.super6.pot.utils.openUrlInCustomTab
import java.io.IOException
import java.text.CharacterIterator
import java.text.NumberFormat
import java.text.StringCharacterIterator
import java.util.Currency
import java.util.Locale
import kotlin.math.max


fun isUriExist(context: Context, uri: Uri): Boolean {

    return try {
        context.contentResolver.openInputStream(uri)?.close()
        true
    } catch (e: Exception) {
        false
    }
}


fun humanReadableBytesSize(bytes: Long): String {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
    if (absB < 1024) {
        return "${bytes}B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= java.lang.Long.signum(bytes).toLong()
    return String.format(
        Locale("en", "IN"),
        "%.2f%cB",
        value / 1024.0,
        ci.current()
    ) // Explicit Locale used
}


/**
 * Get file path from URI.
 */
fun getPathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val cursor: Cursor? =
        context.contentResolver.query(uri, arrayOf(MediaStore.Video.Media.DATA), null, null, null)

    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        if (it.moveToFirst()) {
            // If the cursor is not empty, get the file path from the column
            filePath = it.getString(columnIndex)
        }
    }

    // Return the file path or null if not found
    return filePath
}


fun MediaMetadataRetriever.getMiddleVideoThumbnail(
    context: Context,
    duration: Long,
    uri: Uri
): Bitmap? {


    setDataSource(getPathFromUri(context, uri))

    // Fall back to middle of video
    // Note: METADATA_KEY_DURATION unit is in ms, not us.
    val thumbnailTimeUs: Long = duration * 1000 / 2

    val thumbnail = getFrameAtTime(thumbnailTimeUs)

    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnail(context: Context, uri: Uri, frameAt: Long = 0): Bitmap? {


    val openFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: throw IOException("Failed to open file descriptor")

    setDataSource(openFileDescriptor.fileDescriptor)
    val thumbnail = getFrameAtTime(frameAt)
    openFileDescriptor.close()
    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnailFromPath(path: String): Bitmap? {
    setDataSource(path)
    val thumbnail = getFrameAtTime(0)
    release()
    return thumbnail
}





fun enterFullScreenMode(activity: Activity) {
    val windowInsetsController = WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For Android 11 and above, use the new API for immersive full screen
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


    } else {
        // For lower versions, use the legacy method
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}

fun exitFullScreenMode(activity: Activity) {
    val windowInsetsController = WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    } else {
        // Show the system bars for lower versions
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

    }
}


fun isValidEmail(email: String): Boolean {
    // Implement email validation logic
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


class PlaceholderTransformation(val placeholder: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return PlaceholderFilter(text, placeholder)
    }
}

fun PlaceholderFilter(text: AnnotatedString, placeholder: String): TransformedText {

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return 0
        }

        override fun transformedToOriginal(offset: Int): Int {
            return 0
        }
    }

    return TransformedText(AnnotatedString(placeholder), numberOffsetTranslator)
}


fun formatCurrency(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode) {
        "INR" -> Locale("en", "IN")  // India Locale for INR
        "USD" -> Locale("en", "US")  // US Locale for USD
        else -> Locale.getDefault()  // Default locale if currency code is unknown
    }

    val currency = Currency.getInstance(currencyCode)

    // Get an instance of NumberFormat for the appropriate currency
    val format = NumberFormat.getCurrencyInstance(locale)
    format.currency = currency  // Set the currency type

    return format.format(amount)
}


fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
    // Ensure the bitmap is square
    val size = Math.min(bitmap.width, bitmap.height)
    val radius = size / 2f

    // Create a new bitmap with a size that can fit the rounded image
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    // Prepare the paint with a shader to render the image as a circle
    val paint = Paint()
    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    paint.shader = shader
    paint.isAntiAlias = true

    // Draw a circle on the canvas with the bitmap shader
    canvas.drawCircle(radius, radius, radius, paint)

    return output
}


fun Modifier.scrollbar(
    state: ScrollState,
    direction: Orientation,
    indicatorThickness: Dp = 8.dp,
    indicatorColor: Color = Color.LightGray,
    alpha: Float = if (state.isScrollInProgress) 0.8f else 0f,
    alphaAnimationSpec: AnimationSpec<Float> = tween(
        delayMillis = if (state.isScrollInProgress) 0 else 1500,
        durationMillis = if (state.isScrollInProgress) 150 else 500
    ),
    padding: PaddingValues = PaddingValues(all = 0.dp),
): Modifier = composed {
    val scrollbarAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = alphaAnimationSpec, label = ""
    )

    drawWithContent {
        drawContent()

        val showScrollBar = state.isScrollInProgress || scrollbarAlpha > 0.0f

        // Draw scrollbar only if currently scrolling or if scroll animation is ongoing.
        if (showScrollBar) {
            val (topPadding, bottomPadding, startPadding, endPadding) = listOf(
                padding.calculateTopPadding().toPx(), padding.calculateBottomPadding().toPx(),
                padding.calculateStartPadding(layoutDirection).toPx(),
                padding.calculateEndPadding(layoutDirection).toPx()
            )
            val contentOffset = state.value
            val viewPortLength = if (direction == Orientation.Vertical)
                size.height else size.width
            val viewPortCrossAxisLength = if (direction == Orientation.Vertical)
                size.width else size.height
            val contentLength =
                max(viewPortLength + state.maxValue, 0.001f)  // To prevent divide by zero error
            val indicatorLength = ((viewPortLength / contentLength) * viewPortLength) - (
                    if (direction == Orientation.Vertical) topPadding + bottomPadding
                    else startPadding + endPadding
                    )
            val indicatorThicknessPx = indicatorThickness.toPx()

            val scrollOffsetViewPort = viewPortLength * contentOffset / contentLength

            val scrollbarSizeWithoutInsets = if (direction == Orientation.Vertical)
                Size(indicatorThicknessPx, indicatorLength)
            else Size(indicatorLength, indicatorThicknessPx)

            val scrollbarPositionWithoutInsets = if (direction == Orientation.Vertical)
                Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr)
                        viewPortCrossAxisLength - indicatorThicknessPx - endPadding
                    else startPadding,
                    y = scrollOffsetViewPort + topPadding
                )
            else
                Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr)
                        scrollOffsetViewPort + startPadding
                    else viewPortLength - scrollOffsetViewPort - indicatorLength - endPadding,
                    y = viewPortCrossAxisLength - indicatorThicknessPx - bottomPadding
                )

            drawRoundRect(
                color = indicatorColor,
                cornerRadius = CornerRadius(
                    x = indicatorThicknessPx / 2, y = indicatorThicknessPx / 2
                ),
                topLeft = scrollbarPositionWithoutInsets,
                size = scrollbarSizeWithoutInsets,
                alpha = scrollbarAlpha
            )
        }
    }
}

data class ScrollBarConfig(
    val indicatorThickness: Dp = 8.dp,
    val indicatorColor: Color = Color.LightGray,
    val alpha: Float? = null,
    val alphaAnimationSpec: AnimationSpec<Float>? = null,
    val padding: PaddingValues = PaddingValues(all = 0.dp),
)

fun Modifier.verticalScrollWithScrollbar(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
    scrollbarConfig: ScrollBarConfig = ScrollBarConfig(),
) = this
    .scrollbar(
        state, Orientation.Vertical,
        indicatorThickness = scrollbarConfig.indicatorThickness,
        indicatorColor = scrollbarConfig.indicatorColor,
        alpha = scrollbarConfig.alpha ?: if (state.isScrollInProgress) 0.8f else 0f,
        alphaAnimationSpec = scrollbarConfig.alphaAnimationSpec ?: tween(
            delayMillis = if (state.isScrollInProgress) 0 else 500,
            durationMillis = if (state.isScrollInProgress) 100 else 300
        ),
        padding = scrollbarConfig.padding
    )
//    .verticalScroll(state, enabled, flingBehavior, reverseScrolling)


fun Modifier.horizontalScrollWithScrollbar(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
    scrollbarConfig: ScrollBarConfig = ScrollBarConfig(),
) = this
    .scrollbar(
        state, Orientation.Horizontal,
        indicatorThickness = scrollbarConfig.indicatorThickness,
        indicatorColor = scrollbarConfig.indicatorColor,
        alpha = scrollbarConfig.alpha ?: if (state.isScrollInProgress) 0.8f else 0f,
        alphaAnimationSpec = scrollbarConfig.alphaAnimationSpec ?: tween(
            delayMillis = if (state.isScrollInProgress) 0 else 1500,
            durationMillis = if (state.isScrollInProgress) 150 else 500
        ),
        padding = scrollbarConfig.padding
    )
//    .horizontalScroll(state, enabled, flingBehavior, reverseScrolling)


fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}


// Global function to execute actions based on lifecycle state
fun dropUnlessResumedV2(lifecycleOwner: LifecycleOwner, block: () -> Unit) {
    // Execute the action only if the lifecycle is RESUMED
    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        block()
    }
}


@Composable
fun NavigatorSubmitButton(
    isLoading: Boolean,
    onNextButtonClicked: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    debounceInterval: Long = 500L,// Debounce interval in milliseconds
) {

    val lifecycleOwner = LocalLifecycleOwner.current


    // State to track the last click time (for debouncing)
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }


    Button(
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            // Check both local clicked state and debounce interval
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval && !isLoading) {
                // Update the last click time
                lastClickTimeMillis = currentTimeMillis

                dropUnlessResumedV2(lifecycleOwner) {
                    onNextButtonClicked()
                }

            }
        },
        modifier = modifier,
        enabled = !isLoading, // Disable button if loading
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        content()
    }
}


@Composable
fun NavigatorCard(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    debounceInterval: Long = 500L,// Debounce interval in milliseconds
    shape: Shape = RectangleShape,
    onCardClicked: () -> Unit,
    content: @Composable () -> Unit,
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    // State to track the last click time (for debouncing)
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }


    Card(
        shape = shape,
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            // Check both local clicked state and debounce interval
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval && !isLoading) {
                // Update the last click time
                lastClickTimeMillis = currentTimeMillis
                // Execute the action only if the button is not in loading state
                dropUnlessResumedV2(lifecycleOwner) {
                    onCardClicked()
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }

}


@Composable
fun NavigatorOutlinedCard(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    debounceInterval: Long = 500L,// Debounce interval in milliseconds
    shape: Shape = RectangleShape,
    onCardClicked: () -> Unit,
    content: @Composable () -> Unit,
) {


    // State to track the last click time (for debouncing)
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current


    Card(
        shape = shape,
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            // Check both local clicked state and debounce interval
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval && !isLoading) {
                // Update the last click time
                lastClickTimeMillis = currentTimeMillis

                // Execute the action only if the button is not in loading state
                dropUnlessResumedV2(lifecycleOwner) {
                    onCardClicked()
                }

            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }

}


@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableText(
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
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
    // State variables to track the expanded state, clickable state, and last character index.
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var clickable by rememberSaveable { mutableStateOf(false) }
    var lastCharIndex by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current


    // Box composable containing the Text composable.
    Box(
        modifier = Modifier
            .then(modifier)
    ) {


        // Detecting links using regex
        val annotatedString = buildAnnotatedString {

            val urlPattern = Regex(
                "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
                setOf(RegexOption.IGNORE_CASE) // Enable case insensitive matching
            )

            var currentIndex = 0


            // Find all links in the message
            urlPattern.findAll(text).forEach { match ->
                // Add text before the link
                if (match.range.first > currentIndex) {
                    append(text.substring(currentIndex, match.range.first))
                }

                // Add the link itself as a clickable text
                pushStringAnnotation(tag = "URL", annotation = match.value)


                withLink(
                    LinkAnnotation.Url(
                        url = match.value,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )

                    ) {
                        openUrlInCustomTab(
                            context,
                            match.value.trim()
                        )

                    }
                ) {
                    append(match.value) // Link style (blue)
                }



                pop()

                currentIndex = match.range.last + 1
            }

            // Append the remaining part of the message if any text is left
            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }


        // Text composable with buildAnnotatedString to handle "Show More" and "Show Less" buttons.
        Text(
            modifier = textModifier,
            text = buildAnnotatedString {
                if (clickable) {
                    if (isExpanded) {
                        // Display the full text and "Show Less" button when expanded.
                        append(annotatedString)

                        withLink(
                            LinkAnnotation.Clickable(
                                tag = showLessText,
                                styles = TextLinkStyles(
                                    style = showLessStyle
                                )
                            ) {

                                if (clickable) {
                                    isExpanded = !isExpanded
                                }
                            }
                        ) {
                            append(showLessText)
                        }


                    } else {

                        // Display truncated text and "Show More" button when collapsed.
                        val adjustText =
                            annotatedString.substring(startIndex = 0, endIndex = lastCharIndex)
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
                    // Display the full text when not clickable.
                    append(annotatedString)
                }
            },
            // Set max lines based on the expanded state.
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
            fontStyle = fontStyle,
            // Callback to determine visual overflow and enable click ability.
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    clickable = true
                    lastCharIndex = textLayoutResult.getLineEnd(collapsedMaxLine - 1)
                }
            },
            style = style,
            textAlign = textAlign,
            fontSize = fontSize
        )
    }

}


@Composable
fun ChatMessageLinkPreview(text: String) {


    val context = LocalContext.current

    // State for the link preview
    var linkPreview by remember { mutableStateOf<LinkPreviewData?>(null) }


    // Extract the first URL from the text.
    val firstLink = Regex(
        "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
        setOf(RegexOption.IGNORE_CASE) // Enable case insensitive matching
    ).find(text)?.value

    // Fetch link preview for the first link, if available.
    LaunchedEffect(firstLink) {
        if (firstLink != null) {
            linkPreview = fetchLinkPreview(firstLink)
        }
    }





    Column(modifier = Modifier.fillMaxWidth(0.7f)) {


        // Show link preview if available.
        if (linkPreview != null) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = linkPreview?.title.orEmpty(),
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = linkPreview?.description.orEmpty(),
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )

            // Load and display image if available
            linkPreview?.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Link Preview Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.aspectRatio(5f / 4f)
                )
            }

        }


    }

}

@Composable
fun ChatMessageLinkPreviewHeader(linkPreviewData: LinkPreviewData?) {


    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Load and display image if available
        linkPreviewData?.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Link Preview Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8.dp radius
            )
        }


        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = linkPreviewData?.title.orEmpty(),
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = linkPreviewData?.description.orEmpty(),
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )
        }


    }

}


@Composable
fun ChatMessageLinkPreviewHeaderLoading() {


    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Box(
            modifier = Modifier.size(80.dp)
        )

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = "",
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = "",
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )
        }

    }

}


