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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardThemePreset
import com.example.ime.resolveKeyboardPalette
import com.example.ui.theme.NotoSansEthiopicFamily

@Composable
fun ThemesScreen(
    selectedPreset: KeyboardThemePreset,
    onSelectPreset: (KeyboardThemePreset) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Keyboard Themes • የኪቦርድ ቀለሞች",
                        fontFamily = NotoSansEthiopicFamily,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Choose a live theme preset for the FynLegacy keyboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(KeyboardThemePreset.entries) { preset ->
                val isSelected = preset == selectedPreset
                val previewPalette = resolveKeyboardPalette(preset)

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectPreset(preset) }
                        .testTag("theme_card_${preset.id}"),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = preset.subtitleAmharic,
                                    fontFamily = NotoSansEthiopicFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Active Theme",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }

                        // Live Miniature Keyboard Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(previewPalette.boardBackground)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            val sampleRow = listOf("ሀ", "ለ", "ሐ", "መ", "ሠ", "ረ", "ሰ", "ሸ", "ቀ")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                sampleRow.forEach { ch ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(previewPalette.keyBackground),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = ch,
                                            fontFamily = NotoSansEthiopicFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = previewPalette.keyText,
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(previewPalette.actionKeyBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("AB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = previewPalette.accent)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(4f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(previewPalette.keyBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "< አማርኛ >",
                                        fontFamily = NotoSansEthiopicFamily,
                                        fontSize = 10.sp,
                                        color = previewPalette.keyText.copy(alpha = 0.8f),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(previewPalette.enterKeyBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("↵", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = previewPalette.enterKeyText)
                                }
                            }
                        }

                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
