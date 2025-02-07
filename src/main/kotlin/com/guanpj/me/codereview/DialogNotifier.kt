package com.guanpj.me.codereview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class DialogNotifier(private val project: Project): DialogWrapper(project) {
    override fun createCenterPanel(): JComponent? {
        TODO("Not yet implemented")
    }

}