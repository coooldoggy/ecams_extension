package com.github.coooldoggy.ecamsextension.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(name = "EcamsSettings", storages = [Storage("EcamsSettings.xml")])
class EcamsSettingsState : PersistentStateComponent<EcamsSettingsState> {
    var serverIp: String = "127.0.0.1"
    var serverPort: Int = 9090
    var username: String = "admin"
    var password: String = "password"

    override fun getState(): EcamsSettingsState = this

    override fun loadState(state: EcamsSettingsState) {
        this.serverIp = state.serverIp
        this.serverPort = state.serverPort
        this.username = state.username
        this.password = state.password
    }

    companion object {
        val instance: EcamsSettingsState
            get() = ApplicationManager.getApplication().getService(EcamsSettingsState::class.java)
    }
}
