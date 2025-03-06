package com.github.coooldoggy.ecamsextension.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class MyApplicationActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        thisLogger().warn("Lets make something more easier than before...")
        thisLogger().info("ECAMS IntelliJ 플러그인이 활성화되었습니다!")
    }
}
