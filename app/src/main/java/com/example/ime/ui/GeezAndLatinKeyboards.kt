package com.example.ime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.North
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardSettings
import com.example.engine.FidelKeySpec
import com.example.engine.GeezFidelData
import com.example.ime.KeyboardController
import com.example.ime.KeyboardPalette
import com.example.ime.KeyboardPanelMode
import com.example.ime.ShiftState
import com.example.ui.theme.NotoSansEthiopicFamily
import com.example.ui.theme.PlusJakartaSansFamily

@Composable
fun FloatingFidelOrderBar(
    activeSpec: FidelKeySpec?,
    palette: KeyboardPalette,
    boldFidel: Boolean,
    lowLatencyMode: Boolean = true,
    onSelectOrder: (String) -> Unit,
) {
    val barContent: @Composable (FidelKeySpec) -> Unit = { spec ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .then(if (lowLatencyMode) Modifier else Modifier.shadow(4.dp, RoundedCornerShape(12.dp)))
                .clip(RoundedCornerShape(12.dp))
                .background(palette.popupBackground)
                .border(1.dp, palette.accent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            spec.orders.forEachIndexed { idx, fidelChar ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (idx == 0) palette.accentSoft else palette.keyBackground.copy(alpha = 0.65f)
                        )
                        .clickable { onSelectOrder(fidelChar) }
                        .testTag("fidel_order_${fidelChar}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = fidelChar,
                        fontFamily = NotoSansEthiopicFamily,
                        fontWeight = if (boldFidel) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 18.sp,
                        color = if (idx == 0) palette.accent else palette.popupText,
                    )
                }
            }
        }
    }

    if (lowLatencyMode) {
        if (activeSpec != null) {
            barContent(activeSpec)
        }
    } else {
        AnimatedVisibility(
            visible = activeSpec != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            if (activeSpec != null) {
                barContent(activeSpec)
            }
        }
    }
}

@Composable
fun EthiopicPunctuationStrip(
    palette: KeyboardPalette,
    isGeezNumeralsActive: Boolean,
    onPunctuationTap: (String) -> Unit,
    onToggleGeezNumerals: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GeezFidelData.ethiopicPunctuation.forEach { symbol ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(palette.actionKeyBackground)
                    .clickable { onPunctuationTap(symbol) }
                    .testTag("punct_$symbol"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = symbol,
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.keyText,
                )
            }
        }
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (isGeezNumeralsActive) palette.accent else palette.accentSoft)
                .clickable { onToggleGeezNumerals() }
                .testTag("geez_numerals_toggle"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "፩፪",
                fontFamily = NotoSansEthiopicFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGeezNumeralsActive) palette.enterKeyText else palette.accent,
            )
        }
    }
}

@Composable
fun OptionalNumberRow(
    palette: KeyboardPalette,
    isEthiopic: Boolean,
    onNumberTap: (String) -> Unit,
) {
    val digits = remember { listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        digits.forEach { d ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(palette.keyBackground.copy(alpha = 0.85f))
                    .clickable { onNumberTap(d) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = d,
                    fontFamily = if (isEthiopic) PlusJakartaSansFamily else PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.keyText,
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GeezSimpleKeyboardView(
    controller: KeyboardController,
    settings: KeyboardSettings,
    palette: KeyboardPalette,
    activeFidelKey: FidelKeySpec?,
    geezAltLayer: Boolean,
    keyRowHeight: Dp,
) {
    val isTigrigna = settings.activeLanguage == KeyboardLanguage.TIGRIGNA
    val row1 = remember(isTigrigna, geezAltLayer) {
        when {
            geezAltLayer -> GeezFidelData.extraAltRow1
            isTigrigna -> GeezFidelData.simpleRow1Tigrigna
            else -> GeezFidelData.simpleRow1Amharic
        }
    }
    val row2 = GeezFidelData.simpleRow2
    val row3 = GeezFidelData.simpleRow3

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Row 1: 11 base Fidel keys
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            row1.forEach { spec ->
                FidelBaseKeyCap(
                    spec = spec,
                    isSelected = activeFidelKey?.baseChar == spec.baseChar,
                    showSubKeys = settings.showSubKeys,
                    showPreview = settings.showKeyPreview,
                    boldFidel = settings.boldFidelFont,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = { controller.onGeezBaseKeyTap(spec) },
                    onLongPressSub = { sub ->
                        GeezFidelData.fidelOrdersMap[sub]?.let { orders ->
                            controller.onGeezBaseKeyTap(FidelKeySpec(sub, orders))
                        } ?: controller.onGeezOrderSelected(sub)
                    },
                )
            }
        }

        // Row 2: 11 base Fidel keys
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            row2.forEach { spec ->
                FidelBaseKeyCap(
                    spec = spec,
                    isSelected = activeFidelKey?.baseChar == spec.baseChar,
                    showSubKeys = settings.showSubKeys,
                    showPreview = settings.showKeyPreview,
                    boldFidel = settings.boldFidelFont,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = { controller.onGeezBaseKeyTap(spec) },
                    onLongPressSub = { sub ->
                        GeezFidelData.fidelOrdersMap[sub]?.let { orders ->
                            controller.onGeezBaseKeyTap(FidelKeySpec(sub, orders))
                        } ?: controller.onGeezOrderSelected(sub)
                    },
                )
            }
        }

        // Row 3: Alt Fidel Shift + 8 base Fidel keys + Smart Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1.35f)
                    .height(keyRowHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (geezAltLayer) palette.accent else palette.actionKeyBackground)
                    .clickable { controller.toggleGeezAltLayer() }
                    .testTag("geez_alt_shift_key"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (geezAltLayer) "ሀለ" else "ዀዐ",
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (geezAltLayer) palette.enterKeyText else palette.accent,
                )
            }

            row3.forEach { spec ->
                FidelBaseKeyCap(
                    spec = spec,
                    isSelected = activeFidelKey?.baseChar == spec.baseChar,
                    showSubKeys = settings.showSubKeys,
                    showPreview = settings.showKeyPreview,
                    boldFidel = settings.boldFidelFont,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = { controller.onGeezBaseKeyTap(spec) },
                    onLongPressSub = { sub ->
                        GeezFidelData.fidelOrdersMap[sub]?.let { orders ->
                            controller.onGeezBaseKeyTap(FidelKeySpec(sub, orders))
                        } ?: controller.onGeezOrderSelected(sub)
                    },
                )
            }

            SmartBackspaceKey(
                palette = palette,
                height = keyRowHeight,
                modifier = Modifier.weight(1.45f),
                onTap = { controller.onBackspaceTap() },
                onSwipeDeleteWord = { controller.onSmartDeleteWord() },
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FidelBaseKeyCap(
    spec: FidelKeySpec,
    isSelected: Boolean,
    showSubKeys: Boolean,
    showPreview: Boolean,
    boldFidel: Boolean,
    palette: KeyboardPalette,
    height: Dp,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onLongPressSub: (String) -> Unit,
) {
    val trackPress = showPreview && !isSelected
    val interactionSource = if (trackPress) remember { MutableInteractionSource() } else null
    val isPressed = if (interactionSource != null) {
        interactionSource.collectIsPressedAsState().value
    } else {
        false
    }
    val bgColor = when {
        isSelected -> palette.accentSoft
        isPressed -> palette.keyPressedBackground
        else -> palette.keyBackground
    }
    val borderColor = if (isSelected) palette.accent else Color.Transparent

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .then(
                if (interactionSource != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTap,
                        onLongClick = {
                            val alt = spec.subKeyChar
                            if (alt != null) onLongPressSub(alt) else onTap()
                        },
                    )
                } else {
                    Modifier.combinedClickable(
                        onClick = onTap,
                        onLongClick = {
                            val alt = spec.subKeyChar
                            if (alt != null) onLongPressSub(alt) else onTap()
                        },
                    )
                }
            )
            .testTag("key_${spec.baseChar}"),
        contentAlignment = Alignment.Center,
    ) {
        if (showSubKeys && spec.subKeyChar != null) {
            Text(
                text = spec.subKeyChar,
                fontFamily = NotoSansEthiopicFamily,
                fontSize = 9.sp,
                color = palette.subKeyText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp),
            )
        }
        Text(
            text = spec.baseChar,
            fontFamily = NotoSansEthiopicFamily,
            fontWeight = if (boldFidel) FontWeight.Bold else FontWeight.Normal,
            fontSize = 18.sp,
            color = if (isSelected) palette.accent else palette.keyText,
        )
    }
}

@Composable
fun LatinQwertyKeyboardView(
    controller: KeyboardController,
    settings: KeyboardSettings,
    palette: KeyboardPalette,
    shiftState: ShiftState,
    keyRowHeight: Dp,
) {
    val isOromo = settings.activeLanguage == KeyboardLanguage.AFAAN_OROMO
    val row1 = remember { listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p") }
    val row2 = remember(isOromo) {
        if (isOromo) {
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "'")
        } else {
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        }
    }
    val row3 = remember { listOf("z", "x", "c", "v", "b", "n", "m") }

    val subHintMap = remember {
        mapOf(
            "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
            "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
            "a" to "@", "s" to "#", "d" to "$", "f" to "%", "g" to "&",
            "h" to "-", "j" to "+", "k" to "(", "l" to ")",
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            row1.forEach { key ->
                val display = if (shiftState != ShiftState.OFF) key.uppercase() else key
                LatinKeyCap(
                    label = display,
                    subHint = if (settings.showSubKeys) subHintMap[key] else null,
                    showPreview = settings.showKeyPreview,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = { controller.onLatinKeyTap(key) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isOromo) 3.dp else 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            row2.forEach { key ->
                val isHudhaa = key == "'"
                val display = if (!isHudhaa && shiftState != ShiftState.OFF) key.uppercase() else key
                LatinKeyCap(
                    label = display,
                    subHint = if (isHudhaa) "hudhaa" else if (settings.showSubKeys) subHintMap[key] else null,
                    isHighlighted = isHudhaa,
                    showPreview = settings.showKeyPreview,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        if (isHudhaa) controller.onDirectTextInsert("'") else controller.onLatinKeyTap(key)
                    },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1.35f)
                    .height(keyRowHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (shiftState != ShiftState.OFF) palette.accent else palette.actionKeyBackground
                    )
                    .clickable { controller.toggleShift() }
                    .testTag("shift_key"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (shiftState == ShiftState.LOCKED) {
                        Icons.Filled.KeyboardCapslock
                    } else {
                        Icons.Filled.North
                    },
                    contentDescription = "Shift",
                    tint = if (shiftState != ShiftState.OFF) palette.enterKeyText else palette.keyText,
                    modifier = Modifier.size(19.dp),
                )
            }

            row3.forEach { key ->
                val display = if (shiftState != ShiftState.OFF) key.uppercase() else key
                LatinKeyCap(
                    label = display,
                    subHint = null,
                    showPreview = settings.showKeyPreview,
                    palette = palette,
                    height = keyRowHeight,
                    modifier = Modifier.weight(1f),
                    onTap = { controller.onLatinKeyTap(key) },
                )
            }

            SmartBackspaceKey(
                palette = palette,
                height = keyRowHeight,
                modifier = Modifier.weight(1.4f),
                onTap = { controller.onBackspaceTap() },
                onSwipeDeleteWord = { controller.onSmartDeleteWord() },
            )
        }
    }
}

@Composable
private fun LatinKeyCap(
    label: String,
    subHint: String?,
    isHighlighted: Boolean = false,
    showPreview: Boolean,
    palette: KeyboardPalette,
    height: Dp,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val interactionSource = if (showPreview) remember { MutableInteractionSource() } else null
    val isPressed = if (interactionSource != null) {
        interactionSource.collectIsPressedAsState().value
    } else {
        false
    }
    val bg = when {
        isPressed -> palette.keyPressedBackground
        isHighlighted -> palette.accentSoft
        else -> palette.keyBackground
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(
                if (isHighlighted) {
                    Modifier.border(1.dp, palette.accent.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .then(
                if (interactionSource != null) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onTap)
                } else {
                    Modifier.clickable(onClick = onTap)
                }
            )
            .testTag("key_$label"),
        contentAlignment = Alignment.Center,
    ) {
        if (subHint != null) {
            Text(
                text = subHint,
                fontFamily = PlusJakartaSansFamily,
                fontSize = if (isHighlighted) 7.sp else 9.sp,
                color = palette.subKeyText,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp),
            )
        }
        Text(
            text = label,
            fontFamily = PlusJakartaSansFamily,
            fontSize = if (isHighlighted) 22.sp else 18.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) palette.accent else palette.keyText,
        )
    }
}

@Composable
fun SymbolsAndNumeralsKeyboardView(
    controller: KeyboardController,
    palette: KeyboardPalette,
    isGeezNumerals: Boolean,
    keyRowHeight: Dp,
) {
    val row1 = if (isGeezNumerals) {
        GeezFidelData.geezNumeralsRow1
    } else {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    }
    val row2 = if (isGeezNumerals) {
        GeezFidelData.geezNumeralsRow2
    } else {
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
    }
    val row3 = if (isGeezNumerals) {
        listOf("፡", "።", "፣", "፤", "፥", "፦", "፧", "?")
    } else {
        listOf("*", "\"", "'", ":", ";", "!", "?", "_")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(row1, row2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { sym ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(keyRowHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.keyBackground)
                            .clickable { controller.onDirectTextInsert(sym) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = sym,
                            fontFamily = if (isGeezNumerals) NotoSansEthiopicFamily else PlusJakartaSansFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.keyText,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(keyRowHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.actionKeyBackground)
                    .clickable {
                        controller.setPanelMode(
                            if (isGeezNumerals) KeyboardPanelMode.SYMBOLS else KeyboardPanelMode.GEEZ_NUMERALS
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isGeezNumerals) "!#1" else "፩፪፫",
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.accent,
                )
            }

            row3.forEach { sym ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(keyRowHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground)
                        .clickable { controller.onDirectTextInsert(sym) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sym,
                        fontFamily = NotoSansEthiopicFamily,
                        fontSize = 17.sp,
                        color = palette.keyText,
                    )
                }
            }

            SmartBackspaceKey(
                palette = palette,
                height = keyRowHeight,
                modifier = Modifier.weight(1.3f),
                onTap = { controller.onBackspaceTap() },
                onSwipeDeleteWord = { controller.onSmartDeleteWord() },
            )
        }
    }
}

@Composable
fun SmartBackspaceKey(
    palette: KeyboardPalette,
    height: Dp,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onSwipeDeleteWord: () -> Unit,
) {
    var dragAccumulated by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.actionKeyBackground)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulated += dragAmount
                        if (dragAccumulated <= -36f) {
                            onSwipeDeleteWord()
                            dragAccumulated = 0f
                        }
                    },
                )
            }
            .clickable(onClick = onTap)
            .testTag("backspace_key"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace (swipe left to delete word)",
            tint = palette.keyText,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
fun BottomActionRow(
    controller: KeyboardController,
    settings: KeyboardSettings,
    palette: KeyboardPalette,
    panelMode: KeyboardPanelMode,
    height: Dp,
) {
    val lang = settings.activeLanguage
    val punctuationSymbol = if (lang.isEthiopic) "።" else "."
    val secondaryPunctuation = if (lang.isEthiopic) "፣" else ","
    var spaceDragX by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Symbols / ABC toggle
        Box(
            modifier = Modifier
                .weight(1.25f)
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.actionKeyBackground)
                .clickable {
                    val target = if (panelMode == KeyboardPanelMode.SYMBOLS || panelMode == KeyboardPanelMode.GEEZ_NUMERALS) {
                        KeyboardPanelMode.KEYBOARD
                    } else {
                        KeyboardPanelMode.SYMBOLS
                    }
                    controller.setPanelMode(target)
                }
                .testTag("symbols_toggle_key"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (panelMode == KeyboardPanelMode.SYMBOLS || panelMode == KeyboardPanelMode.GEEZ_NUMERALS) {
                    if (lang.isEthiopic) "ሀለ" else "ABC"
                } else {
                    "?123"
                },
                fontFamily = NotoSansEthiopicFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.keyText,
            )
        }

        // 1-tap Globe / Language Switch key (ሀለ/AB)
        Box(
            modifier = Modifier
                .weight(1.15f)
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.actionKeyBackground)
                .clickable { controller.switchLanguage(forward = true) }
                .testTag("globe_language_key"),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = "Switch Language",
                    tint = palette.accent,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (lang.isEthiopic) "AB" else "ሀለ",
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.keyText,
                )
            }
        }

        @Composable
        fun PunctKey(symbol: String) {
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.actionKeyBackground)
                    .clickable { controller.onPunctuationTap(symbol) }
                    .testTag("bottom_punct_$symbol"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = symbol,
                    fontFamily = NotoSansEthiopicFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.keyText,
                )
            }
        }

        @Composable
        fun SpaceBarKey() {
            Box(
                modifier = Modifier
                    .weight(3.8f)
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.keyBackground)
                    .pointerInput(lang) {
                        detectHorizontalDragGestures(
                            onDragStart = { spaceDragX = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                spaceDragX += dragAmount
                                // Slide > 24px moves cursor smoothly like a trackpad
                                if (spaceDragX >= 24f) {
                                    controller.onCursorMove(1)
                                    spaceDragX = 0f
                                } else if (spaceDragX <= -24f) {
                                    controller.onCursorMove(-1)
                                    spaceDragX = 0f
                                }
                            },
                        )
                    }
                    .clickable { controller.onSpaceTap() }
                    .testTag("spacebar_key"),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 6.dp),
                ) {
                    Text(
                        text = "< ",
                        fontSize = 11.sp,
                        color = palette.subKeyText,
                        modifier = Modifier.clickable { controller.switchLanguage(forward = false) },
                    )
                    Text(
                        text = lang.nativeName,
                        fontFamily = if (lang.isEthiopic) NotoSansEthiopicFamily else PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.keyText.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = " >",
                        fontSize = 11.sp,
                        color = palette.subKeyText,
                        modifier = Modifier.clickable { controller.switchLanguage(forward = true) },
                    )
                }
            }
        }

        if (settings.swapSpaceAndPunctuation) {
            PunctKey(punctuationSymbol)
            SpaceBarKey()
            PunctKey(secondaryPunctuation)
        } else {
            PunctKey(secondaryPunctuation)
            SpaceBarKey()
            PunctKey(punctuationSymbol)
        }

        // Enter / Send Key
        Box(
            modifier = Modifier
                .weight(1.45f)
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.enterKeyBackground)
                .clickable { controller.onEnterTap() }
                .testTag("enter_key"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                contentDescription = "Enter",
                tint = palette.enterKeyText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
