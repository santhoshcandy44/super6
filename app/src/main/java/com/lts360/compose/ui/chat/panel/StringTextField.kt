package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.utils.ScrollBarConfig
import com.lts360.compose.utils.verticalScrollWithScrollbar

@Composable
fun StringTextField(
    value: String,
    state: TextFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onChooseAttachmentClicked: () -> Unit
) {


    val scrollState = rememberScrollState()

    // This is effectively a rememberUpdatedState, but it combines the updated state (text) with
    // some state that is preserved across updates (selection).
    var valueWithSelection by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }
    valueWithSelection = valueWithSelection.copy(text = value)


    // EditText for message input
    BasicTextField(
        state = state,
//                    onValueChange = { viewModel.onMessageValueChange(it) },
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        ),

        modifier = modifier.then(
            StateSyncingModifier(
                state = state,
                value = valueWithSelection,
                onValueChanged = {
                    // Don't fire the callback if only the selection/cursor changed.
                    if (it.text != valueWithSelection.text) {
                        onValueChange(it.text)
                    }
                    valueWithSelection = it
                },
                writeSelectionFromTextFieldValue = false
            )

                .background(
                    MaterialTheme.customColorScheme.searchBarColor,
                    RoundedCornerShape(20.dp)
                )
                .heightIn(min = 40.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ),
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
        scrollState = scrollState,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorator = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScrollWithScrollbar(
                            scrollState,
                            scrollbarConfig = ScrollBarConfig()
                        ),
                    contentAlignment = Alignment.CenterStart

                ) {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                "Type message...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        it()
                    }

                }


                if (state.text.isEmpty()) {
                    IconButton(
                        onClick = onChooseAttachmentClicked, modifier =
                            Modifier
                                .size(32.dp)
                                .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Add Attachments",
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    /*         IconButton(

                                 onClick = {}, modifier = Modifier
                                     .size(32.dp)
                                     .minimumInteractiveComponentSize()
                             ) {
                                 Icon(
                                     imageVector = Icons.Filled.PhotoCamera,
                                     contentDescription = "Add Camera Photos",
                                 )
                             }*/
                }

            }

        })


}

/**
 * Synchronizes between [TextFieldState], immutable values, and value change callbacks for
 * [BasicTextField] that may take a value+callback for state instead of taking a [TextFieldState]
 * directly. Effectively a fancy `rememberUpdatedState`.
 *
 * @param writeSelectionFromTextFieldValue If true, [update] will synchronize the selection from the
 * [TextFieldValue] to the [TextFieldState]. The text will be synchronized regardless.
 */
class StateSyncingModifier(
    private val state: TextFieldState,
    private val value: TextFieldValue,
    private val onValueChanged: (TextFieldValue) -> Unit,
    private val writeSelectionFromTextFieldValue: Boolean,
) : ModifierNodeElement<StateSyncingModifierNode>() {

    override fun create(): StateSyncingModifierNode =
        StateSyncingModifierNode(state, onValueChanged, writeSelectionFromTextFieldValue)

    override fun update(node: StateSyncingModifierNode) {
        node.update(value, onValueChanged)
    }

    override fun equals(other: Any?): Boolean {
        // Always call update, without comparing the text. Update can compare more efficiently.
        return false
    }

    override fun hashCode(): Int {
        // Avoid calculating hash from values that can change on every recomposition.
        return state.hashCode()
    }

    override fun InspectorInfo.inspectableProperties() {
        // no inspector properties
    }
}

class StateSyncingModifierNode(
    private val state: TextFieldState,
    private var onValueChanged: (TextFieldValue) -> Unit,
    private val writeSelectionFromTextFieldValue: Boolean,
) : Modifier.Node(), ObserverModifierNode, FocusEventModifierNode {

    private var isFocused = false
    private var lastValueWhileFocused: TextFieldValue? = null

    override val shouldAutoInvalidate: Boolean
        get() = false

    /**
     * Synchronizes the latest [value] to the [TextFieldState] and updates our [onValueChanged]
     * callback. Should be called from [ModifierNodeElement.update].
     */
    fun update(value: TextFieldValue, onValueChanged: (TextFieldValue) -> Unit) {
        this.onValueChanged = onValueChanged

        // Don't modify the text programmatically while an edit session is in progress.
        // WARNING: While editing, the code that holds the external state is temporarily not the
        // actual source of truth. This "stealing" of control is generally an anti-pattern. We do it
        // intentionally here because text field state is very sensitive to timing, and if a state
        // update is delivered a frame late, it breaks text input. It is very easy to accidentally
        // introduce small bits of asynchrony in real-world scenarios, e.g. with Flow-based reactive
        // architectures. The benefit of avoiding that easy pitfall outweighs the weirdness in this
        // case.
        if (!isFocused) {
            updateState(value)
        } else {
            this.lastValueWhileFocused = value
        }
    }

    override fun onAttach() {
        // Don't fire the callback on first frame.
        observeTextState(fireOnValueChanged = false)
    }

    override fun onFocusEvent(focusState: FocusState) {
        if (this.isFocused && !focusState.isFocused) {
            // Lost focus, perform deferred synchronization.
            lastValueWhileFocused?.let(::updateState)
            lastValueWhileFocused = null
        }
        this.isFocused = focusState.isFocused
    }

    /** Called by the modifier system when the [TextFieldState] has changed. */
    override fun onObservedReadsChanged() {
        observeTextState()
    }

    private fun updateState(value: TextFieldValue) {
        state.edit {
            // Ideally avoid registering a state change if the text isn't actually different.
            // Take a look at `setTextIfChanged` implementation in TextFieldBuffer
            replace(0, length, value.text)

            // The BasicTextField2(String) variant can't push a selection value, so ignore it.
            if (writeSelectionFromTextFieldValue) {
                selection = value.selection
            }
        }
    }

    private fun observeTextState(fireOnValueChanged: Boolean = true) {
        lateinit var value: TextFieldValue
        observeReads {
            value = TextFieldValue(
                state.text.toString(),
                state.selection,
                state.composition
            )
        }

        // This code is outside of the observeReads lambda so we don't observe any state reads the
        // callback happens to do.
        if (fireOnValueChanged) {
            onValueChanged(value)
        }
    }
}

