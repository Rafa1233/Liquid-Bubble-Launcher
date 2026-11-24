package com.liquidbubble.launcher.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidbubble.launcher.ui.theme.LocalLiquidPalette

@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search apps or packages...",
    focusRequester: FocusRequester? = null
) {
    val palette = LocalLiquidPalette.current
    val focusManager = LocalFocusManager.current

    LiquidBubbleSurface(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .semantics { contentDescription = "Search installed applications" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).align(Alignment.Center)
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(palette.accentPrimary),
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(placeholder, color = palette.textSecondary.copy(alpha = 0.65f), fontSize = 15.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .weight(1f)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
