package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GeezLayoutMode
import com.example.data.OneHandedMode
import com.example.data.SettingsRepository
import com.example.ime.KeyboardController
import com.example.ime.KeyboardPanelMode
import com.example.ime.resolveKeyboardPalette
import com.example.ui.theme.NotoSansEthiopicFamily

@Composable
fun KeyboardRootView(
    controller: KeyboardController,
    onCollapseKeyboard: () -> Unit,
    onOpenSettings: () -> Unit,
    applyNavigationBarInsets: Boolean = true,
) {
    val context = LocalContext.current
    val settingsRepository = SettingsRepository.getInstance(context)
    val settings by controller.settings.collectAsStateWithLifecycle()
    val panelMode by controller.panelMode.collectAsStateWithLifecycle()
    val activeFidelKey by controller.activeFidelKey.collectAsStateWithLifecycle()
    val shiftState by controller.shiftState.collectAsStateWithLifecycle()
    val geezAltLayer by controller.geezAltLayer.collectAsStateWithLifecycle()
    val suggestions by controller.suggestions.collectAsStateWithLifecycle()
    val clipboardEntries by controller.clipboardEntries.collectAsStateWithLifecycle()

    val palette = resolveKeyboardPalette(settings.themePreset)
    val totalBoardHeight = settings.keyboardHeightDp.dp
    val rowCount = if (settings.showNumberRow) 5 else 4
    val keyRowHeight = ((totalBoardHeight - 62.dp) / rowCount).coerceIn(36.dp, 56.dp)
    val utilityPanelHeight = (totalBoardHeight - 40.dp).coerceIn(190.dp, 280.dp)

    val containerModifier = Modifier
        .fillMaxWidth()
        .background(palette.boardBackground)
        .then(if (applyNavigationBarInsets) Modifier.navigationBarsPadding() else Modifier)

    Box(modifier = containerModifier) {
        val boardContent: @Composable (Modifier) -> Unit = { widthMod ->
            Column(
                modifier = widthMod.padding(bottom = 4.dp),
            ) {
                // Ethiopian Heritage Micro-Stripe (1.5dp subtle green-amber-red bar at very top)
                Row(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    Box(Modifier.weight(1f).height(2.dp).background(palette.ethiopianGreen))
                    Box(Modifier.weight(1f).height(2.dp).background(palette.ethiopianYellow))
                    Box(Modifier.weight(1f).height(2.dp).background(palette.ethiopianRed))
                }

                // Top Utility Toolbar
                KeyboardUtilityToolbar(
                    palette = palette,
                    panelMode = panelMode,
                    oneHandedMode = settings.oneHandedMode,
                    onCollapse = onCollapseKeyboard,
                    onOpenSettings = onOpenSettings,
                    onTogglePanel = { controller.setPanelMode(it) },
                    onCycleOneHanded = { controller.cycleOneHandedMode() },
                )

                // Top Suggestions Strip (unless configured at bottom)
                if (settings.showSuggestions && !settings.showSuggestionsAtBottom && panelMode == KeyboardPanelMode.KEYBOARD) {
                    SuggestionStripRow(
                        suggestions = suggestions,
                        isEthiopic = settings.activeLanguage.isEthiopic,
                        palette = palette,
                        onSelect = { controller.applySuggestion(it) },
                    )
                }

                // Floating 7-order Fidel selector
                if (panelMode == KeyboardPanelMode.KEYBOARD && settings.activeLanguage.isEthiopic) {
                    FloatingFidelOrderBar(
                        activeSpec = activeFidelKey,
                        palette = palette,
                        boldFidel = settings.boldFidelFont,
                        lowLatencyMode = settings.lowLatencyMode,
                        onSelectOrder = { controller.onGeezOrderSelected(it) },
                    )
                }

                when (panelMode) {
                    KeyboardPanelMode.HANDWRITING -> HandwritingPadPanel(
                        controller = controller,
                        settings = settings,
                        palette = palette,
                        height = utilityPanelHeight,
                        settingsRepository = settingsRepository,
                    )

                    KeyboardPanelMode.CLIPBOARD -> ClipboardAndSnippetsPanel(
                        controller = controller,
                        palette = palette,
                        entries = clipboardEntries,
                        pinnedOnly = false,
                        height = utilityPanelHeight,
                    )

                    KeyboardPanelMode.SNIPPETS -> ClipboardAndSnippetsPanel(
                        controller = controller,
                        palette = palette,
                        entries = clipboardEntries,
                        pinnedOnly = true,
                        height = utilityPanelHeight,
                    )

                    KeyboardPanelMode.CALENDAR -> EthiopianCalendarInsertPanel(
                        controller = controller,
                        palette = palette,
                        height = utilityPanelHeight,
                    )

                    KeyboardPanelMode.EMOJI -> BilingualEmojiPanel(
                        controller = controller,
                        palette = palette,
                        height = utilityPanelHeight,
                    )

                    KeyboardPanelMode.THEME_PICKER -> ThemeQuickPickerPanel(
                        controller = controller,
                        currentPreset = settings.themePreset,
                        palette = palette,
                        height = utilityPanelHeight,
                    )

                    KeyboardPanelMode.SYMBOLS,
                    KeyboardPanelMode.GEEZ_NUMERALS -> {
                        if (settings.activeLanguage.isEthiopic) {
                            EthiopicPunctuationStrip(
                                palette = palette,
                                isGeezNumeralsActive = panelMode == KeyboardPanelMode.GEEZ_NUMERALS,
                                onPunctuationTap = { controller.onPunctuationTap(it) },
                                onToggleGeezNumerals = {
                                    controller.setPanelMode(
                                        if (panelMode == KeyboardPanelMode.GEEZ_NUMERALS) {
                                            KeyboardPanelMode.KEYBOARD
                                        } else {
                                            KeyboardPanelMode.GEEZ_NUMERALS
                                        }
                                    )
                                },
                            )
                        }
                        SymbolsAndNumeralsKeyboardView(
                            controller = controller,
                            palette = palette,
                            isGeezNumerals = panelMode == KeyboardPanelMode.GEEZ_NUMERALS,
                            keyRowHeight = keyRowHeight,
                        )
                        BottomActionRow(
                            controller = controller,
                            settings = settings,
                            palette = palette,
                            panelMode = panelMode,
                            height = keyRowHeight,
                        )
                    }

                    KeyboardPanelMode.KEYBOARD -> {
                        if (settings.showNumberRow) {
                            OptionalNumberRow(
                                palette = palette,
                                isEthiopic = settings.activeLanguage.isEthiopic,
                                onNumberTap = { controller.onDirectTextInsert(it) },
                            )
                        }

                        if (settings.activeLanguage.isEthiopic) {
                            EthiopicPunctuationStrip(
                                palette = palette,
                                isGeezNumeralsActive = false,
                                onPunctuationTap = { controller.onPunctuationTap(it) },
                                onToggleGeezNumerals = { controller.setPanelMode(KeyboardPanelMode.GEEZ_NUMERALS) },
                            )
                            if (settings.geezLayoutMode == GeezLayoutMode.PHONETIC) {
                                LatinQwertyKeyboardView(
                                    controller = controller,
                                    settings = settings,
                                    palette = palette,
                                    shiftState = shiftState,
                                    keyRowHeight = keyRowHeight,
                                )
                            } else {
                                GeezSimpleKeyboardView(
                                    controller = controller,
                                    settings = settings,
                                    palette = palette,
                                    activeFidelKey = activeFidelKey,
                                    geezAltLayer = geezAltLayer,
                                    keyRowHeight = keyRowHeight,
                                )
                            }
                        } else {
                            LatinQwertyKeyboardView(
                                controller = controller,
                                settings = settings,
                                palette = palette,
                                shiftState = shiftState,
                                keyRowHeight = keyRowHeight,
                            )
                        }

                        // Optional Bottom Suggestions above Spacebar (FynGeez classic option!)
                        if (settings.showSuggestions && settings.showSuggestionsAtBottom) {
                            SuggestionStripRow(
                                suggestions = suggestions,
                                isEthiopic = settings.activeLanguage.isEthiopic,
                                palette = palette,
                                onSelect = { controller.applySuggestion(it) },
                            )
                        }

                        BottomActionRow(
                            controller = controller,
                            settings = settings,
                            palette = palette,
                            panelMode = panelMode,
                            height = keyRowHeight,
                        )
                    }
                }
            }
        }

        // One-Handed Mode Dock Container
        when (settings.oneHandedMode) {
            OneHandedMode.NONE -> boardContent(Modifier.fillMaxWidth())
            OneHandedMode.LEFT -> Row(modifier = Modifier.fillMaxWidth()) {
                boardContent(Modifier.weight(0.84f))
                OneHandedSideRail(
                    palette = palette,
                    modifier = Modifier.weight(0.16f),
                    onResetFull = { controller.setOneHandedMode(OneHandedMode.NONE) },
                    onFlipSide = { controller.setOneHandedMode(OneHandedMode.RIGHT) },
                )
            }
            OneHandedMode.RIGHT -> Row(modifier = Modifier.fillMaxWidth()) {
                OneHandedSideRail(
                    palette = palette,
                    modifier = Modifier.weight(0.16f),
                    onResetFull = { controller.setOneHandedMode(OneHandedMode.NONE) },
                    onFlipSide = { controller.setOneHandedMode(OneHandedMode.LEFT) },
                )
                boardContent(Modifier.weight(0.84f))
            }
        }
    }
}

@Composable
private fun KeyboardUtilityToolbar(
    palette: com.example.ime.KeyboardPalette,
    panelMode: KeyboardPanelMode,
    oneHandedMode: OneHandedMode,
    onCollapse: () -> Unit,
    onOpenSettings: () -> Unit,
    onTogglePanel: (KeyboardPanelMode) -> Unit,
    onCycleOneHanded: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(palette.toolbarBackground)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ToolbarIconButton(
            icon = if (panelMode != KeyboardPanelMode.KEYBOARD) {
                Icons.AutoMirrored.Filled.KeyboardArrowLeft
            } else {
                Icons.Filled.ExpandMore
            },
            contentDescription = "Collapse or Back",
            isActive = false,
            palette = palette,
            tag = "toolbar_collapse",
            onClick = {
                if (panelMode != KeyboardPanelMode.KEYBOARD) {
                    onTogglePanel(KeyboardPanelMode.KEYBOARD)
                } else {
                    onCollapse()
                }
            },
        )

        ToolbarIconButton(
            icon = Icons.Filled.Settings,
            contentDescription = "Settings",
            isActive = false,
            palette = palette,
            tag = "toolbar_settings",
            onClick = onOpenSettings,
        )

        ToolbarIconButton(
            icon = Icons.Filled.Palette,
            contentDescription = "Themes",
            isActive = panelMode == KeyboardPanelMode.THEME_PICKER,
            palette = palette,
            tag = "toolbar_themes",
            onClick = { onTogglePanel(KeyboardPanelMode.THEME_PICKER) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.Draw,
            contentDescription = "Handwriting Pad",
            isActive = panelMode == KeyboardPanelMode.HANDWRITING,
            palette = palette,
            tag = "toolbar_handwriting",
            onClick = { onTogglePanel(KeyboardPanelMode.HANDWRITING) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.ContentPaste,
            contentDescription = "Clipboard Manager",
            isActive = panelMode == KeyboardPanelMode.CLIPBOARD,
            palette = palette,
            tag = "toolbar_clipboard",
            onClick = { onTogglePanel(KeyboardPanelMode.CLIPBOARD) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.PushPin,
            contentDescription = "Pinned Snippets",
            isActive = panelMode == KeyboardPanelMode.SNIPPETS,
            palette = palette,
            tag = "toolbar_snippets",
            onClick = { onTogglePanel(KeyboardPanelMode.SNIPPETS) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.CalendarMonth,
            contentDescription = "Ethiopian Calendar",
            isActive = panelMode == KeyboardPanelMode.CALENDAR,
            palette = palette,
            tag = "toolbar_calendar",
            onClick = { onTogglePanel(KeyboardPanelMode.CALENDAR) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.EmojiEmotions,
            contentDescription = "Emoji Panel",
            isActive = panelMode == KeyboardPanelMode.EMOJI,
            palette = palette,
            tag = "toolbar_emoji",
            onClick = { onTogglePanel(KeyboardPanelMode.EMOJI) },
        )

        ToolbarIconButton(
            icon = Icons.Filled.Smartphone,
            contentDescription = "One-Handed Mode",
            isActive = oneHandedMode != OneHandedMode.NONE,
            palette = palette,
            tag = "toolbar_one_handed",
            onClick = onCycleOneHanded,
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    palette: com.example.ime.KeyboardPalette,
    tag: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) palette.accentSoft else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) palette.accent else palette.keyText.copy(alpha = 0.85f),
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun SuggestionStripRow(
    suggestions: List<String>,
    isEthiopic: Boolean,
    palette: com.example.ime.KeyboardPalette,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(palette.toolbarBackground.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(suggestions) { word ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.keyBackground.copy(alpha = 0.75f))
                    .clickable { onSelect(word) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("suggestion_$word"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = word,
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 13.sp,
                    fontWeight = if (isEthiopic) FontWeight.SemiBold else FontWeight.Medium,
                    color = palette.keyText,
                )
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = palette.divider)
}

@Composable
private fun OneHandedSideRail(
    palette: com.example.ime.KeyboardPalette,
    modifier: Modifier = Modifier,
    onResetFull: () -> Unit,
    onFlipSide: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(vertical = 24.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.actionKeyBackground)
                .clickable(onClick = onResetFull),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Fullscreen,
                contentDescription = "Full Width",
                tint = palette.accent,
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.actionKeyBackground)
                .clickable(onClick = onFlipSide),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = "Flip Hand",
                tint = palette.keyText,
            )
        }
    }
}
