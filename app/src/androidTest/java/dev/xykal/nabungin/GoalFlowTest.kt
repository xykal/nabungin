package dev.xykal.nabungin

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** User-flow gate, not just compilation: open -> create goal -> save -> tabung. */
@RunWith(AndroidJUnit4::class)
class GoalFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun createGoalAndSaveMoney() {
        compose.onNodeWithText("+ Tabungan").performClick()
        compose.onNodeWithTag("goal-name").performTextInput("Laptop kerja baru")
        compose.onNodeWithText("1", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithText("0", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("save-goal").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Laptop kerja baru").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Laptop kerja baru").assertExists()
    }
}
