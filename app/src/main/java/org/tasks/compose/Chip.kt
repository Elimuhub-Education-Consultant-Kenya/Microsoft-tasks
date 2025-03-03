package org.tasks.compose

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import org.tasks.themes.CustomIcons

@Composable
fun Chip(
    @DrawableRes icon: Int?,
    name: String?,
    theme: Int,
    showText: Boolean,
    showIcon: Boolean,
    onClick: () -> Unit,
    colorProvider: (Int) -> Int,
) {
    Chip(
        color = Color(colorProvider(theme)),
        text = if (showText) name else null,
        icon = if (showIcon && icon != null) icon else null,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Chip(
    text: String? = null,
    icon: Int? = null,
    color: Color,
    onClick: () -> Unit = {},
) {
    CompositionLocalProvider(
        LocalMinimumTouchTargetEnforcement provides false
    ) {
        Chip(
            onClick = onClick,
            border = BorderStroke(1.dp, color = color),
            leadingIcon = {
                if (text != null) {
                    ChipIcon(iconRes = icon)
                }
            },
            modifier = Modifier.defaultMinSize(minHeight = 26.dp),
            colors = ChipDefaults.chipColors(
                backgroundColor = color.copy(alpha = .1f),
                contentColor = MaterialTheme.colors.onSurface
            ),
        ) {
            if (text == null) {
                ChipIcon(iconRes = icon)
            }
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ChipIcon(iconRes: Int?) {
    iconRes?.let {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TasksChipIconAndTextPreview() {
    MdcTheme {
        Chip(
            text = "Home",
            icon = CustomIcons.getIconResId(CustomIcons.LABEL),
            color = Color.Red,
        )
    }
}

@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TasksChipIconPreview() {
    MdcTheme {
        Chip(
            text = null,
            icon = CustomIcons.getIconResId(CustomIcons.LABEL),
            color = Color.Red,
        )
    }
}

@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TasksChipTextPreview() {
    MdcTheme {
        Chip(
            text = "Home",
            icon = null,
            color = Color.Red,
        )
    }
}