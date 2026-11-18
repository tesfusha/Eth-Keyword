package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ime.InAppTextFieldBridge
import com.example.ime.KeyboardController
import com.example.ime.ui.KeyboardRootView
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguagesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.NotoSansEthiopicFamily

enum class CompanionTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
    THEMES("themes", R.string.nav_themes, Icons.Filled.Palette, Icons.Outlined.Palette),
    LANGUAGES("languages", R.string.nav_languages, Icons.Filled.Language, Icons.Outlined.Language),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FynLegacyApp(
    initialTab: CompanionTab = CompanionTab.HOME,
    viewModel: CompanionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val totalLearnedWords by viewModel.totalLearnedWords.collectAsStateWithLifecycle()
    val learnedCounts by viewModel.learnedCountsByLanguage.collectAsStateWithLifecycle()
    val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(initialTab) }
    var showTestSheet by remember { mutableStateOf(false) }

    // Pre-initialize sandbox state & controller once so opening Test Keyboard is instantaneous
    val testTextState = remember { mutableStateOf(TextFieldValue("")) }
    var useInAppSandbox by remember { mutableStateOf(true) }
    val sandboxScope = rememberCoroutineScope()
    val sandboxController = remember(context) {
        KeyboardController(
            context = context,
            scope = sandboxScope,
            bridge = InAppTextFieldBridge(testTextState),
        )
    }

    // Refresh IME system activation status whenever user returns from System Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshImeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Back gesture returns to Home tab from secondary tabs
    BackHandler(enabled = currentTab != CompanionTab.HOME) {
        currentTab = CompanionTab.HOME
    }

    val openSystemImeSettings: () -> Unit = {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val openInputMethodPicker: () -> Unit = {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = settings.activeLanguage.nativeName,
                                fontFamily = NotoSansEthiopicFamily,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        CompanionTab.entries.forEach { tab ->
                            val selected = tab == currentTab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes),
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showTestSheet = true },
                    icon = { Icon(Icons.Filled.Keyboard, contentDescription = null) },
                    text = { Text(stringResource(R.string.fab_test_keyboard)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_test_keyboard"),
                )
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavigationRail) {
                    NavigationRail {
                        CompanionTab.entries.forEach { tab ->
                            val selected = tab == currentTab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes),
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag("rail_tab_${tab.route}"),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        CompanionTab.HOME -> HomeScreen(
                            imeStatus = imeStatus,
                            totalLearnedWords = totalLearnedWords,
                            onOpenSystemImeSettings = openSystemImeSettings,
                            onOpenInputMethodPicker = openInputMethodPicker,
                            onOpenTestKeyboard = { showTestSheet = true },
                        )

                        CompanionTab.SETTINGS -> SettingsScreen(
                            settings = settings,
                            totalLearnedWords = totalLearnedWords,
                            viewModel = viewModel,
                        )

                        CompanionTab.THEMES -> ThemesScreen(
                            selectedPreset = settings.themePreset,
                            onSelectPreset = viewModel::setThemePreset,
                        )

                        CompanionTab.LANGUAGES -> LanguagesScreen(
                            settings = settings,
                            learnedCounts = learnedCounts,
                            onToggleLanguage = viewModel::setLanguageEnabled,
                            onSetActiveLanguage = viewModel::setActiveLanguage,
                        )
                    }
                }
            }
        }

        if (showTestSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { showTestSheet = false },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Test Keyboard • ኪቦርድ ሞክር",
                            fontFamily = NotoSansEthiopicFamily,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (testTextState.value.text.isNotEmpty()) {
                                TextButton(onClick = { testTextState.value = TextFieldValue("") }) {
                                    Text("Clear")
                                }
                            }
                            OutlinedButton(onClick = openInputMethodPicker) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("System IME")
                            }
                        }
                    }

                    // Instant mode + 1-tap language selector pills
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = useInAppSandbox,
                                onClick = { useInAppSandbox = true },
                                label = { Text("Built-in Full Keyboard") },
                            )
                        }
                        item {
                            FilterChip(
                                selected = !useInAppSandbox,
                                onClick = { useInAppSandbox = false },
                                label = { Text("System IME") },
                            )
                        }
                        items(settings.enabledLanguages()) { lang ->
                            FilterChip(
                                selected = settings.activeLanguage == lang,
                                onClick = { viewModel.setActiveLanguage(lang) },
                                label = {
                                    Text(
                                        text = lang.nativeName,
                                        fontFamily = NotoSansEthiopicFamily,
                                    )
                                },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = testTextState.value,
                        onValueChange = { testTextState.value = it },
                        readOnly = useInAppSandbox,
                        placeholder = {
                            Text(
                                text = "Tap keys, try 7-order Fidel, Hudhaa ('), Handwriting, or 📅...",
                                fontFamily = NotoSansEthiopicFamily,
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("test_keyboard_input"),
                    )

                    if (useInAppSandbox) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        ) {
                            KeyboardRootView(
                                controller = sandboxController,
                                onCollapseKeyboard = { showTestSheet = false },
                                onOpenSettings = {
                                    showTestSheet = false
                                    currentTab = CompanionTab.SETTINGS
                                },
                                applyNavigationBarInsets = false,
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!imeStatus.isEnabledInSystem) {
                                Text(
                                    text = "1. Enable FynLegacy in Android System Settings first, then switch keyboards.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                OutlinedButton(
                                    onClick = openSystemImeSettings,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Open Android Keyboard Settings")
                                }
                            } else if (!imeStatus.isSelectedAsDefault) {
                                Text(
                                    text = "FynLegacy is enabled! Tap below to switch the active keyboard, then tap the text box above.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                OutlinedButton(
                                    onClick = openInputMethodPicker,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Select FynLegacy as Active Keyboard")
                                }
                            } else {
                                Text(
                                    text = "FynLegacy System Keyboard is active! Tap the text field above to type with the system IME.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
