package com.example.ime.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardSettings
import com.example.data.KeyboardThemePreset
import com.example.data.SettingsRepository
import com.example.data.db.ClipboardEntry
import com.example.engine.EmojiCatalog
import com.example.engine.EthiopianDate
import com.example.engine.InkPoint
import com.example.engine.InkStroke
import com.example.engine.OfflineStrokeRecognizer
import com.example.ime.KeyboardController
import com.example.ime.KeyboardPalette
import com.example.ime.KeyboardPanelMode
import com.example.ui.theme.NotoSansEthiopicFamily
import com.example.ui.theme.PlusJakartaSansFamily
import kotlinx.coroutines.launch

@Composable
fun HandwritingPadPanel(
    controller: KeyboardController,
    settings: KeyboardSettings,
    palette: KeyboardPalette,
    height: Dp,
    settingsRepository: SettingsRepository,
) {
    val recognizer = remember { OfflineStrokeRecognizer() }
    val completedStrokes = remember { mutableStateListOf<InkStroke>() }
    var activePoints by remember { mutableStateOf<List<InkPoint>>(emptyList()) }
    var candidates by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Candidate recognition chips + Auto-space toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (candidates.isEmpty()) {
                    item {
                        Text(
                            text = "Draw ${settings.activeLanguage.nativeName} character below",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = palette.keyText.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        )
                    }
                } else {
                    items(candidates) { charCandidate ->
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accentSoft)
                                .border(1.dp, palette.accent, RoundedCornerShape(8.dp))
                                .clickable {
                                    controller.onDirectTextInsert(
                                        text = charCandidate,
                                        appendSpaceIfHandwriting = true,
                                    )
                                    completedStrokes.clear()
                                    candidates = emptyList()
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = charCandidate,
                                fontFamily = NotoSansEthiopicFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accent,
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.actionKeyBackground)
                    .clickable {
                        scope.launch {
                            settingsRepository.updateAutoSpaceOnHandwriting(!settings.autoSpaceOnHandwriting)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("handwriting_auto_space_toggle"),
            ) {
                Text(
                    text = "Auto Space",
                    fontSize = 11.sp,
                    color = palette.keyText,
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (settings.autoSpaceOnHandwriting) palette.accent else palette.divider),
                    contentAlignment = Alignment.Center,
                ) {
                    if (settings.autoSpaceOnHandwriting) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = palette.enterKeyText,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
            }
        }

        // Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.keyBackground)
                .border(1.dp, palette.divider, RoundedCornerShape(12.dp)),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(settings.activeLanguage) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                activePoints = listOf(InkPoint(offset.x, offset.y))
                            },
                            onDrag = { change, _ ->
                                activePoints = activePoints + InkPoint(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                if (activePoints.size >= 2) {
                                    completedStrokes.add(InkStroke(activePoints))
                                    candidates = recognizer.recognize(
                                        completedStrokes.toList(),
                                        settings.activeLanguage,
                                    )
                                }
                                activePoints = emptyList()
                            },
                        )
                    }
                    .testTag("handwriting_canvas"),
            ) {
                fun drawStroke(points: List<InkPoint>) {
                    if (points.size < 2) return
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = palette.accent,
                        style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }

                completedStrokes.forEach { drawStroke(it.points) }
                drawStroke(activePoints)
            }

            // Clear & Backspace actions inside pad corner
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.actionKeyBackground)
                        .clickable {
                            completedStrokes.clear()
                            activePoints = emptyList()
                            candidates = emptyList()
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = palette.keyText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Clear", fontSize = 11.sp, color = palette.keyText)
                    }
                }
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.actionKeyBackground)
                        .clickable { controller.onBackspaceTap() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = palette.keyText,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ClipboardAndSnippetsPanel(
    controller: KeyboardController,
    palette: KeyboardPalette,
    entries: List<ClipboardEntry>,
    pinnedOnly: Boolean,
    height: Dp,
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newSnippetText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("CBE") }
    val snippetTags = remember { listOf("CBE", "Telebirr", "Email", "Address", "Note") }
    val displayed = if (pinnedOnly) entries.filter { it.isPinned } else entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (pinnedOnly) "Pinned Snippets (CBE / Telebirr / Email)" else "Offline Clipboard Vault",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = palette.keyText,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.accentSoft)
                        .clickable { showAddForm = !showAddForm }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_snippet_button"),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = palette.accent, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Save Snippet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.accent)
                    }
                }
                if (!pinnedOnly && entries.any { !it.isPinned }) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.actionKeyBackground)
                            .clickable { controller.clearUnpinnedClipboard() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("Clear Recent", fontSize = 11.sp, color = palette.keyText)
                    }
                }
            }
        }

        if (showAddForm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyBackground)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(snippetTags) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTag == tag) palette.accent else palette.actionKeyBackground)
                                .clickable { selectedTag = tag }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTag == tag) palette.enterKeyText else palette.keyText,
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OutlinedTextField(
                        value = newSnippetText,
                        onValueChange = { newSnippetText = it },
                        placeholder = { Text("Enter $selectedTag snippet...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.keyText,
                            unfocusedTextColor = palette.keyText,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.divider,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.enterKeyBackground)
                            .clickable {
                                if (newSnippetText.isNotBlank()) {
                                    controller.saveClipboardSnippet(
                                        text = newSnippetText,
                                        isPinned = true,
                                        label = selectedTag,
                                    )
                                    newSnippetText = ""
                                    showAddForm = false
                                }
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Pin", fontWeight = FontWeight.Bold, color = palette.enterKeyText)
                    }
                }
            }
        }

        if (displayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.keyBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (pinnedOnly) {
                        "No pinned snippets yet. Tap \"Save Snippet\" to store CBE, Telebirr, Email, or Address offline."
                    } else {
                        "Copied items stay strictly on-device in SQLite. Copy text or pin a snippet above."
                    },
                    fontSize = 12.sp,
                    color = palette.keyText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(displayed, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(palette.keyBackground)
                            .clickable {
                                controller.onDirectTextInsert(item.text)
                                controller.setPanelMode(KeyboardPanelMode.KEYBOARD)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.label != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(palette.accentSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accent,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = item.text,
                            fontFamily = NotoSansEthiopicFamily,
                            fontSize = 13.sp,
                            color = palette.keyText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin",
                            tint = if (item.isPinned) palette.accent else palette.keyText.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { controller.togglePinSnippet(item) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = palette.keyText.copy(alpha = 0.55f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { controller.deleteClipboardEntry(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EthiopianCalendarInsertPanel(
    controller: KeyboardController,
    palette: KeyboardPalette,
    height: Dp,
) {
    val etDate = remember { controller.todayEthiopianDate() }
    val options = remember(etDate) {
        listOf(
            "Ethiopic (Standard)" to etDate.formatEthiopic(),
            "Ge'ez Numerals (ግዕዝ)" to etDate.formatEthiopicWithGeezNumerals(),
            "Qubee / English E.C." to etDate.formatLatin(),
            "Gregorian" to EthiopianDate.formatGregorian(),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Ethiopian Calendar Quick-Insert • የኢትዮጵያ ዘመን አቆጣጠር",
                fontFamily = NotoSansEthiopicFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.keyText,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(options) { (label, formatted) ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.keyBackground)
                        .border(1.dp, palette.divider, RoundedCornerShape(10.dp))
                        .clickable { controller.insertEthiopianDate(formatted) }
                        .padding(10.dp)
                        .testTag("calendar_insert_$label"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = palette.subKeyText,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = formatted,
                        fontFamily = NotoSansEthiopicFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.keyText,
                    )
                }
            }
        }
    }
}

@Composable
fun BilingualEmojiPanel(
    controller: KeyboardController,
    palette: KeyboardPalette,
    height: Dp,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { EmojiCatalog.search(query) }
    val quickTags = remember { listOf("ቡና", "buna", "ፍቅር", "jaalala", "ኢትዮጵያ", "ሰላም") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "Search (e.g. ቡና, buna, love, ሰላም)...",
                        fontFamily = NotoSansEthiopicFamily,
                        fontSize = 12.sp,
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = palette.keyText,
                    unfocusedTextColor = palette.keyText,
                    focusedBorderColor = palette.accent,
                    unfocusedBorderColor = palette.divider,
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("emoji_search_input"),
            )
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.actionKeyBackground)
                    .clickable { controller.onBackspaceTap() }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = palette.keyText)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(quickTags) { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (query == tag) palette.accent else palette.actionKeyBackground)
                        .clickable { query = if (query == tag) "" else tag }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = tag,
                        fontFamily = NotoSansEthiopicFamily,
                        fontSize = 12.sp,
                        color = if (query == tag) palette.enterKeyText else palette.keyText,
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(44.dp),
            contentPadding = PaddingValues(2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(results) { item ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground)
                        .clickable { controller.onDirectTextInsert(item.emoji) }
                        .testTag("emoji_${item.emoji}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = item.emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun ThemeQuickPickerPanel(
    controller: KeyboardController,
    currentPreset: KeyboardThemePreset,
    palette: KeyboardPalette,
    height: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Keyboard Theme • የኪቦርድ ቀለም",
            fontFamily = NotoSansEthiopicFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = palette.keyText,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(KeyboardThemePreset.entries) { preset ->
                val selected = preset == currentPreset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) palette.accentSoft else palette.keyBackground)
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) palette.accent else palette.divider,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable {
                            controller.selectTheme(preset)
                            controller.setPanelMode(KeyboardPanelMode.KEYBOARD)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.keyText,
                        )
                        Text(
                            text = preset.subtitleAmharic,
                            fontFamily = NotoSansEthiopicFamily,
                            fontSize = 11.sp,
                            color = palette.subKeyText,
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = palette.accent,
                        )
                    }
                }
            }
        }
    }
}
