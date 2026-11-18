package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeezLayoutMode
import com.example.data.KeyboardSettings
import com.example.ui.CompanionViewModel
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.NotoSansEthiopicFamily
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: KeyboardSettings,
    totalLearnedWords: Int,
    viewModel: CompanionViewModel,
) {
    var geezDropdownExpanded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Personal Dictionary?") },
            text = { Text("This deletes all $totalLearnedWords learned words stored locally on your device. Built-in dictionaries remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLearnedDictionary()
                        showClearDialog = false
                    }
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Look and Behavior (መልክ እና ባህሪ)
            item {
                SettingsSectionCard(
                    titleEnglish = "Look and Behavior",
                    titleAmharic = "መልክ እና ባህሪ",
                    icon = Icons.Filled.Palette,
                ) {
                    BilingualToggleRow(
                        titleEnglish = "Font Weight: Bold - ፊደል",
                        subtitleAmharic = "የፊደል ውፍረት (ደማቅ / መደበኛ)",
                        checked = settings.boldFidelFont,
                        tag = "setting_bold_fidel",
                        onCheckedChange = viewModel::setBoldFidelFont,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Keyboard Height",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "የኪቦርድ ቁመት",
                                    fontFamily = NotoSansEthiopicFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = "${settings.keyboardHeightDp.roundToInt()} dp",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Slider(
                            value = settings.keyboardHeightDp,
                            onValueChange = viewModel::setKeyboardHeight,
                            valueRange = 220f..340f,
                            modifier = Modifier.testTag("setting_height_slider"),
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    BilingualToggleRow(
                        titleEnglish = "Show Sub-Keys",
                        subtitleAmharic = "ንዑስ ቁልፎችን አሳይ",
                        checked = settings.showSubKeys,
                        tag = "setting_sub_keys",
                        onCheckedChange = viewModel::setShowSubKeys,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Show Number Row",
                        subtitleAmharic = "የቁጥር ረድፍ አሳይ",
                        checked = settings.showNumberRow,
                        tag = "setting_number_row",
                        onCheckedChange = viewModel::setShowNumberRow,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Show Key Preview",
                        subtitleAmharic = "የቁልፍ ቅድመ-እይታ",
                        checked = settings.showKeyPreview,
                        tag = "setting_key_preview",
                        onCheckedChange = viewModel::setShowKeyPreview,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Show Suggestions at Bottom",
                        subtitleAmharic = "የቃላት ጥቆማዎችን ከታች አሳይ",
                        checked = settings.showSuggestionsAtBottom,
                        tag = "setting_suggestions_bottom",
                        onCheckedChange = viewModel::setShowSuggestionsAtBottom,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Low-Latency Mode (Fast Typing)",
                        subtitleAmharic = "ፈጣን አፈጻጸም (ለቀርፋፋ ስልኮች ያለ መዘግየት)",
                        checked = settings.lowLatencyMode,
                        tag = "setting_low_latency",
                        onCheckedChange = viewModel::setLowLatencyMode,
                    )
                }
            }

            // 2. Sound & Vibration (ድምጽ እና ንዝረት)
            item {
                SettingsSectionCard(
                    titleEnglish = "Sound & Vibration",
                    titleAmharic = "ድምጽ እና ንዝረት",
                    icon = Icons.Filled.RecordVoiceOver,
                ) {
                    BilingualToggleRow(
                        titleEnglish = "Sound on Key Press",
                        subtitleAmharic = "በቁልፍ ንክኪ ድምጽ",
                        checked = settings.soundOnKeyPress,
                        tag = "setting_sound",
                        onCheckedChange = viewModel::setSoundOnKeyPress,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Vibrate on Key Press",
                        subtitleAmharic = "በቁልፍ ንክኪ ንዝረት",
                        checked = settings.vibrateOnKeyPress,
                        tag = "setting_vibrate",
                        onCheckedChange = viewModel::setVibrateOnKeyPress,
                    )
                    if (settings.vibrateOnKeyPress) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = "Vibration Strength",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = "የንዝረት ጥንካሬ (0 = device default)",
                                        fontFamily = NotoSansEthiopicFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = if (settings.vibrationStrength == 0) "Default" else "${settings.vibrationStrength}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Slider(
                                value = settings.vibrationStrength.toFloat(),
                                onValueChange = { viewModel.setVibrationStrength(it.roundToInt()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.testTag("setting_vibration_slider"),
                            )
                        }
                    }
                }
            }

            // 3. Word Prediction (የቃላት ትንበያ)
            item {
                SettingsSectionCard(
                    titleEnglish = "Word Prediction",
                    titleAmharic = "የቃላት ትንበያ",
                    icon = Icons.Filled.Tune,
                ) {
                    BilingualToggleRow(
                        titleEnglish = "Show Suggestions",
                        subtitleAmharic = "የቃላት ጥቆማ አሳይ",
                        checked = settings.showSuggestions,
                        tag = "setting_show_suggestions",
                        onCheckedChange = viewModel::setShowSuggestions,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Double Space as Full Stop",
                        subtitleAmharic = "ድርብ ክፍተት እንደ አራት ነጥብ (። / .)",
                        checked = settings.doubleSpaceFullStop,
                        tag = "setting_double_space",
                        onCheckedChange = viewModel::setDoubleSpaceFullStop,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Auto Capitalisation (Latin only)",
                        subtitleAmharic = "ራስ-ሰር ካፒታል (ላቲን ብቻ)",
                        checked = settings.autoCapitalization,
                        tag = "setting_auto_caps",
                        onCheckedChange = viewModel::setAutoCapitalization,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Swap Space and Punctuation",
                        subtitleAmharic = "ክፍተት እና ሥርዓተ ነጥብ ቀያይር",
                        checked = settings.swapSpaceAndPunctuation,
                        tag = "setting_swap_space",
                        onCheckedChange = viewModel::setSwapSpaceAndPunctuation,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Auto Pick Suggestion on SPACE",
                        subtitleAmharic = "በክፍተት የተጠቆመውን ቃል ምረጥ",
                        checked = settings.autoPickOnSpace,
                        tag = "setting_autopick_space",
                        onCheckedChange = viewModel::setAutoPickOnSpace,
                    )
                    BilingualToggleRow(
                        titleEnglish = "Auto Space on Handwriting",
                        subtitleAmharic = "በእጅ ጽሑፍ ራስ-ሰር ክፍተት",
                        checked = settings.autoSpaceOnHandwriting,
                        tag = "setting_autospace_handwriting",
                        onCheckedChange = viewModel::setAutoSpaceOnHandwriting,
                    )
                }
            }

            // 4. Ge'ez Layout (ግዕዝ)
            item {
                SettingsSectionCard(
                    titleEnglish = "Ge'ez Layout",
                    titleAmharic = "ግዕዝ",
                    icon = Icons.Filled.Translate,
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { geezDropdownExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                .testTag("setting_geez_layout_dropdown"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = settings.geezLayoutMode.displayName,
                                    fontFamily = NotoSansEthiopicFamily,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = settings.geezLayoutMode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Ge'ez Layout")
                        }

                        DropdownMenu(
                            expanded = geezDropdownExpanded,
                            onDismissRequest = { geezDropdownExpanded = false },
                        ) {
                            GeezLayoutMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = mode.displayName,
                                                fontFamily = NotoSansEthiopicFamily,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                text = mode.description,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (mode == settings.geezLayoutMode) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setGeezLayoutMode(mode)
                                        geezDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // 5. Privacy (ግላዊነት)
            item {
                SettingsSectionCard(
                    titleEnglish = "Privacy",
                    titleAmharic = "ግላዊነት",
                    icon = Icons.Filled.Lock,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldAccent.copy(alpha = 0.12f))
                            .border(1.dp, EmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "የእርስዎን መረጃዎች አይሰበስብም",
                                fontFamily = NotoSansEthiopicFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent,
                            )
                            Text(
                                text = "FynLegacy operates 100% offline. No INTERNET permission is declared. All keystrokes, learned personal words, and clipboard snippets stay strictly on-device in encrypted app SQLite storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Learned words: $totalLearnedWords",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { showClearDialog = true },
                            enabled = totalLearnedWords > 0,
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clear Learned Words")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    titleEnglish: String,
    titleAmharic: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = titleEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "• $titleAmharic",
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun BilingualToggleRow(
    titleEnglish: String,
    subtitleAmharic: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = titleEnglish,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitleAmharic,
                fontFamily = NotoSansEthiopicFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
        )
    }
}
