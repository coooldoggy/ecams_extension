package com.github.coooldoggy.ecamsextension.settings

import com.intellij.openapi.options.Configurable
import javax.swing.*

class EcamsSettingsConfigurable : Configurable {
    private var panel: JPanel? = null
    private var serverIpField: JTextField? = null
    private var portField: JTextField? = null
    private var usernameField: JTextField? = null
    private var passwordField: JPasswordField? = null

    override fun createComponent(): JComponent {
        panel = JPanel()
        val layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel!!.layout = layout

        serverIpField = JTextField(EcamsSettingsState.instance.serverIp, 20)
        portField = JTextField(EcamsSettingsState.instance.serverPort.toString(), 5)
        usernameField = JTextField(EcamsSettingsState.instance.username, 20)
        passwordField = JPasswordField(EcamsSettingsState.instance.password, 20)

        panel!!.add(JLabel("ECAMS 서버 IP:"))
        panel!!.add(serverIpField)
        panel!!.add(JLabel("ECAMS 서버 포트:"))
        panel!!.add(portField)
        panel!!.add(JLabel("사용자 ID:"))
        panel!!.add(usernameField)
        panel!!.add(JLabel("비밀번호:"))
        panel!!.add(passwordField)

        return panel!!
    }

    override fun isModified(): Boolean {
        return serverIpField!!.text != EcamsSettingsState.instance.serverIp ||
                portField!!.text != EcamsSettingsState.instance.serverPort.toString() ||
                usernameField!!.text != EcamsSettingsState.instance.username ||
                String(passwordField!!.password) != EcamsSettingsState.instance.password
    }

    override fun apply() {
        EcamsSettingsState.instance.serverIp = serverIpField!!.text
        EcamsSettingsState.instance.serverPort = portField!!.text.toInt()
        EcamsSettingsState.instance.username = usernameField!!.text
        EcamsSettingsState.instance.password = String(passwordField!!.password)
    }

    override fun getDisplayName(): String {
        return "ECAMS 서버 설정"
    }
}
