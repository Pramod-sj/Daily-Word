package com.pramod.dailyword.games.common.game_rules.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pramod.dailyword.games.common.game_rules.model.GameRule
import kotlinx.coroutines.launch

@Preview
@Composable
fun GameInstructionsDialogPreview() {

    // 1. Create the mock JSON data exactly as the CodeIgniter API would send it
    val mockRules = listOf(
        GameRule(
            iconName = "TOUCH_APP",
            title = "Tap to Type",
            description = "Tap any square to select a word. Tap again to switch between Across and Down."
        ),
        GameRule(
            iconName = "LIGHTBULB",
            title = "Use Hints",
            description = "Stuck on a word? Use the hint button to reveal a single letter."
        ),
        GameRule(
            iconName = "TROPHY",
            title = "Achieve Mastery",
            description = "Complete the grid perfectly to lock these words into your active memory."
        )
    )

    // 2. Wrap it in your app's theme so the Surface and Primary colors render perfectly
    GameInstructionsBottomSheet(
        rules = mockRules,
        onDismiss = { /* Do nothing in preview */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameInstructionsBottomSheet(
    rules: List<GameRule>,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // This state controls the swipe behavior
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Forces it to open fully so they see all rules
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow, // Premium subtle background
        dragHandle = { BottomSheetDefaults.DragHandle() }, // The little grey pill at the top
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How to Play",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The scrollable list of rules
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(rules) { rule ->
                    DynamicInstructionRow(rule)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // The Call to Action
            Button(
                onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Got it, let's play!",
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DynamicInstructionRow(rule: GameRule) {

    // ✅ Translate the backend string to a native Compose Icon safely
    val iconVector: ImageVector = when (rule.iconName.uppercase()) {
        "TOUCH_APP" -> Icons.Outlined.TouchApp
        "LIGHTBULB" -> Icons.Outlined.Lightbulb
        "TROPHY" -> Icons.Outlined.EmojiEvents
        "CHECK" -> Icons.Outlined.FactCheck
        else -> Icons.Outlined.HelpOutline // Safe fallback if API sends a bad string
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rule.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
        }
    }
}