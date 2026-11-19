package com.kaylakautai.inkle.ui.user

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The stamp card through a configuration change (rotation, dark mode, font size...): if it
 * was turned over to the goal and reward form, it stays there, with what was typed.
 */
@RunWith(AndroidJUnit4::class)
class StampCardRestorationTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    /** The card, by what tapping it does: "Edit goal and reward" while on the front. */
    private fun hasClickLabel(label: String) = SemanticsMatcher("tap to $label") {
        it.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

    private fun showCard(restoration: StateRestorationTester) {
        restoration.setContent {
            AppTheme {
                StampAndRewardCard(
                    books = emptyList(),
                    goal = 6,
                    reward = null,
                    onSave = { _, _ -> }
                )
            }
        }
    }

    @Test
    fun turnedOver_staysOnTheBackWithItsEdits() {
        val restoration = StateRestorationTester(rule)
        showCard(restoration)

        rule.onNode(hasClickLabel(str(R.string.stamp_card_action_edit))).performClick()
        rule.onNodeWithText(str(R.string.stamp_card_goal_label)).assertExists()
        rule.onNodeWithContentDescription(str(R.string.cont_desc_increase_goal)).performClick()
        rule.onNodeWithText("7").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        rule.onNodeWithText(str(R.string.stamp_card_goal_label)).assertExists()
        rule.onNodeWithText("7").assertExists()
    }

    @Test
    fun front_staysOnTheFront() {
        val restoration = StateRestorationTester(rule)
        showCard(restoration)

        restoration.emulateSavedInstanceStateRestore()

        rule.onNode(hasClickLabel(str(R.string.stamp_card_action_edit))).assertExists()
        rule.onNodeWithText(str(R.string.stamp_card_goal_label)).assertDoesNotExist()
    }
}
