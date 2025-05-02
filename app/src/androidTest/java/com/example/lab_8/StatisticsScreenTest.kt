package com.example.lab_8

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab_8.repository.TransactionRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAddTransaction() {
        composeTestRule.onNodeWithContentDescription("Транзакции", useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText("Описание").performTextInput("Test transaction")
        composeTestRule.onNodeWithText("Сумма").performTextInput("100")
        composeTestRule.onNodeWithText("Добавить").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Test transaction").assertExists()
        composeTestRule.onNodeWithText("+100.00 BYN").assertExists()
    }

    @Test
    fun testSwitchToStatisticsScreen() {
        composeTestRule.onNodeWithContentDescription("Статистика", useUnmergedTree = true).performClick()

        composeTestRule.waitUntil(
            condition = {
                composeTestRule.onAllNodes(hasText("Общая статистика (в USD)")).fetchSemanticsNodes().isNotEmpty()
            },
            timeoutMillis = 5_000
        )

        composeTestRule.onNodeWithText("Общая статистика (в USD)").assertExists()
    }

    @Test
    fun testDeleteTransaction() {
        composeTestRule.onNodeWithContentDescription("Транзакции", useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText("Описание").performTextInput("To delete")
        composeTestRule.onNodeWithText("Сумма").performTextInput("50")
        composeTestRule.onNodeWithText("Добавить").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Удалить", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("Удалить").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("To delete").assertDoesNotExist()
    }

    @Test
    fun testThemeSwitching() {
        composeTestRule.onNodeWithContentDescription("Тёмная тема", useUnmergedTree = true).performClick()

        composeTestRule.waitUntil(
            condition = {
                composeTestRule.onAllNodes(hasContentDescription("Светлая тема")).fetchSemanticsNodes().isNotEmpty()
            },
            timeoutMillis = 3_000
        )

        composeTestRule.onNodeWithContentDescription("Светлая тема", useUnmergedTree = true).assertExists()
    }
}
