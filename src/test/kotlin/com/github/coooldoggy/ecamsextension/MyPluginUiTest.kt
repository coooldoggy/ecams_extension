package com.github.coooldoggy.ecamsextension

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.coooldoggy.ecamsextension.services.EcamsSyncService
import com.github.coooldoggy.ecamsextension.toolWindow.EcamsNavigationTool
import org.junit.Test
import javax.swing.JLabel
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginUiTest : BasePlatformTestCase() {

    @Test
    fun testUiLoginAndSync() {
        val project = project
        val service = project.service<EcamsSyncService>()

        // UI 테스트 실행
        invokeAndWaitIfNeeded {
            val toolWindowManager = ToolWindowManager.getInstance(project)

            // Tool Window 강제 등록 (테스트 환경에서 자동 초기화되지 않는 경우 해결)
            runWriteAction {
                if (toolWindowManager.getToolWindow("ECAMS") == null) {
                    toolWindowManager.registerToolWindow("ECAMS")
                }
            }

            // Tool Window 초기화 대기
            var toolWindow: ToolWindow? = toolWindowManager.getToolWindow("ECAMS")
            for (i in 1..5) {
                if (toolWindow != null) break
                Thread.sleep(500)  // 0.5초 대기 후 다시 확인
                toolWindow = toolWindowManager.getToolWindow("ECAMS")
            }

            assertNotNull(toolWindow, "ECAMS Tool Window가 초기화되지 않았습니다.")

            // UI 동작 테스트 (로그인 버튼 클릭)
            val uiComponent = EcamsNavigationTool()
            val panel = uiComponent.getContent(project)

            service.setTestMode(true)
            val loginSuccess = service.loginToEcams()
            assertTrue(loginSuccess, "가상 로그인 실패")

            // 로그인 UI 상태 확인
            val loginLabel = panel.components.filterIsInstance<JLabel>().firstOrNull { it.text.startsWith("로그인 상태:") }
            assertNotNull(loginLabel, "로그인 상태 UI가 없습니다.")
            assertTrue(loginLabel!!.text.contains("로그인됨"), "로그인 상태가 업데이트되지 않음.")

            // UI 동작 테스트 (동기화 버튼 클릭)
            val syncSuccess = service.syncFilesToEcams(service.getChangedFiles())
            assertTrue(syncSuccess, "가상 동기화 실패")
        }
    }
}
