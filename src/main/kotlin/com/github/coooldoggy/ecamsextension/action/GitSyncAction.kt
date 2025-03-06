package com.github.coooldoggy.ecamsextension.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.github.coooldoggy.ecamsextension.services.EcamsSyncService
import javax.swing.JOptionPane

class GitSyncAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val service = project.service<EcamsSyncService>()

        val isLoggedIn = service.loginToEcams()
        if (!isLoggedIn) {
            JOptionPane.showMessageDialog(
                null,
                "로그인 상태가 아닙니다.",
                "ECAMS Git Sync",
                JOptionPane.INFORMATION_MESSAGE
            )
            return
        }

        val changedFiles = service.getChangedFiles()
        if (changedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "변경된 파일이 없습니다.",
                "ECAMS Git Sync",
                JOptionPane.INFORMATION_MESSAGE
            )
            return
        }

        val syncSuccess = service.syncFilesToEcams(changedFiles)
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
