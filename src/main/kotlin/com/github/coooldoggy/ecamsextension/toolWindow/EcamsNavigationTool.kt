package com.github.coooldoggy.ecamsextension.toolWindow

import com.github.coooldoggy.ecamsextension.services.EcamsSyncService
import com.github.coooldoggy.ecamsextension.settings.EcamsSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.Component
import java.awt.Font
import java.awt.Image
import javax.swing.*

class EcamsNavigationTool : ToolWindowFactory {

    class EcamsToolWindow(toolWindow: ToolWindow) {

        private val serverIpLabel = JLabel("ECAMS 서버 IP: ${EcamsSettingsState.instance.serverIp}")
        private val portLabel = JLabel("ECAMS 서버 포트: ${EcamsSettingsState.instance.serverPort}")
        private val usernameLabel = JLabel("사용자 ID: ${EcamsSettingsState.instance.username}")
        private val loginStatusLabel = JLabel("로그인 상태: 로그아웃됨")

        private val headerIcon = createScaledIcon("/icons/code.png", 20, 20)
        private val serverIcon = createScaledIcon("/icons/server.png", 20, 20)
        private val userIcon = createScaledIcon("/icons/user.png", 20, 20)

        fun getContent(project: Project): JPanel {
            val service = project.service<EcamsSyncService>()
            val loginSuccess = service.loginToEcams()
            val isLoginSession = service.isLoggedIn()
            val settings = EcamsSettingsState.instance

            val mainPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                alignmentX = Component.CENTER_ALIGNMENT
                alignmentY = Component.CENTER_ALIGNMENT
            }

            val headerLabel = JLabel("Ecams Status").apply {
                icon = headerIcon
            }
            mainPanel.add(Box.createVerticalStrut(20))
            mainPanel.add(headerLabel)
            mainPanel.add(Box.createVerticalStrut(10))

            serverIpLabel.icon = serverIcon
            portLabel.icon = serverIcon
            usernameLabel.icon = userIcon

            serverIpLabel.font = serverIpLabel.font.deriveFont(Font.PLAIN, 13f)
            portLabel.font = portLabel.font.deriveFont(Font.PLAIN, 13f)
            usernameLabel.font = usernameLabel.font.deriveFont(Font.PLAIN, 13f)
            loginStatusLabel.font = loginStatusLabel.font.deriveFont(Font.BOLD, 13f)

            val infoPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(serverIpLabel)
                add(Box.createVerticalStrut(5))
                add(portLabel)
                if (isLoginSession) {
                    add(Box.createVerticalStrut(5))
                    add(usernameLabel)
                }
                add(Box.createVerticalStrut(5))
                add(loginStatusLabel)
                add(Box.createVerticalStrut(5))
            }
            mainPanel.add(infoPanel)
            mainPanel.add(Box.createVerticalStrut(10))

            val buttonPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                alignmentX = Component.CENTER_ALIGNMENT
                alignmentY = Component.CENTER_ALIGNMENT
            }

            val loginButton = if (!isLoginSession) {
                JButton("로그인").apply {
                    font = font.deriveFont(Font.PLAIN, 12f)
                    addActionListener {
                        val username =
                            JOptionPane.showInputDialog("사용자 ID 입력:", settings.username) ?: return@addActionListener
                        val password =
                            JOptionPane.showInputDialog("비밀번호 입력:", settings.password) ?: return@addActionListener

                        settings.username = username
                        settings.password = password

                        if (loginSuccess) {
                            loginStatusLabel.text = "로그인 상태: 로그인됨 (${settings.username})"
                        } else {
                            JOptionPane.showMessageDialog(
                                null,
                                "로그인 실패",
                                "ECAMS 로그인",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            } else {
                JButton("Hi").apply {
                    loginStatusLabel.text = "로그인 상태: 로그인됨 (${settings.username})"
                }
            }

            val syncButton = JButton("동기화").apply {
                font = font.deriveFont(Font.PLAIN, 12f)
                addActionListener {
                    val service = project.service<EcamsSyncService>()
                    val syncSuccess = service.syncFilesToEcams(service.getChangedFiles())

                    if (syncSuccess) {
                        JOptionPane.showMessageDialog(
                            null,
                            "ECAMS 동기화 완료",
                            "ECAMS Git Sync",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } else {
                        JOptionPane.showMessageDialog(
                            null,
                            "ECAMS 동기화 실패",
                            "ECAMS Git Sync",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }

            buttonPanel.add(Box.createHorizontalStrut(10))
            buttonPanel.add(syncButton)
            buttonPanel.add(loginButton)

            mainPanel.add(buttonPanel)

            return mainPanel
        }

        private fun createScaledIcon(path: String, width: Int, height: Int): ImageIcon {
            val icon = ImageIcon(javaClass.getResource(path))
            val scaledImage = icon.image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            return ImageIcon(scaledImage)
        }
    }

    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = EcamsToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(project), null, false)
        toolWindow.contentManager.addContent(content)
    }
}
