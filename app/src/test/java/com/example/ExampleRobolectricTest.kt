package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FynLegacy", appName)
    }

    @Test
    fun `companion app renders home tribute switches tabs and opens test sheet`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_list")
            .performScrollToNode(hasText("In Tribute to Yohannes Ejigu"))
        composeTestRule.onNodeWithText("In Tribute to Yohannes Ejigu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_test_keyboard").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Look and Behavior").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_themes").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Classic FynGeez Dark Grey").assertExists()

        composeTestRule.onNodeWithTag("nav_tab_languages").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("languages_list")
            .performScrollToNode(hasText("Afaan Oromoo"))
        composeTestRule.onNodeWithText("Afaan Oromoo").assertExists()

        composeTestRule.onNodeWithTag("fab_test_keyboard").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("test_keyboard_input").assertExists()
    }
}
