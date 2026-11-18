package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.core.app.ApplicationProvider
import com.example.ime.InAppTextFieldBridge
import com.example.ime.KeyboardController
import com.example.ime.ui.KeyboardRootView
import com.example.ui.theme.FynLegacyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardRootViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `keyboard root view types geez 7 order fidel and ethiopian calendar`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val textState = mutableStateOf(TextFieldValue(""))
        val controller = KeyboardController(
            context = context,
            scope = scope,
            bridge = InAppTextFieldBridge(textState),
        )

        composeTestRule.setContent {
            FynLegacyTheme {
                KeyboardRootView(
                    controller = controller,
                    onCollapseKeyboard = {},
                    onOpenSettings = {},
                    applyNavigationBarInsets = false,
                )
            }
        }
        composeTestRule.waitForIdle()

        // Tap base key 'ሰ', then select 4th order 'ሳ' from floating 7-order bar
        composeTestRule.onNodeWithTag("key_ሰ").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fidel_order_ሳ").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ሳ", textState.value.text)

        // Open Ethiopian Calendar Quick-Insert panel and insert Ethiopic date
        composeTestRule.onNodeWithTag("toolbar_calendar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_insert_Ethiopic (Standard)").performClick()
        composeTestRule.waitForIdle()
        assertTrue(textState.value.text.length > 3)

        scope.cancel()
    }
}
