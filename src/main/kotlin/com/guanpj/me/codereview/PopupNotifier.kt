package com.guanpj.me.codereview

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import java.awt.datatransfer.StringSelection

class PopupNotifier {
    companion object {
        private const val REVIEWER_NAME = "@张沛峰"
        private const val BUG_TRACKING_TEMPLATE = "http://dlc.coding.byd.com/p/%s/bug-tracking/issues/%s/detail"
        private const val REQUIREMENTS_TEMPLATE = "http://dlc.coding.byd.com/p/%s/requirements/issues/%s/detail"
        private const val NOTIFICATION_GROUP_ID = "Git Console Monitor"
        
        // 创建通知组
        private val notificationGroup by lazy {
            NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
        }
    }

    private fun generateNotificationContent(info: ReviewInfo): String {
        val testLink = when (info.type) {
            "PR" -> String.format(BUG_TRACKING_TEMPLATE, info.platform, info.id)
            "CR" -> String.format(REQUIREMENTS_TEMPLATE, info.platform, info.id)
            else -> ""
        }

        return """
            |$REVIEWER_NAME 帮忙review代码 ，谢谢。
            |提交内容：${info.type}
            |提交链接：
            |${info.link}
            |已自测：
            |$testLink
        """.trimMargin()
    }

    fun showPopup(project: Project, reviewInfo: ReviewInfo) {
        val notificationContent = generateNotificationContent(reviewInfo)
        
        notificationGroup.createNotification(notificationContent, NotificationType.INFORMATION)
            .addAction(object : NotificationAction("Copy") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    CopyPasteManager.getInstance().setContents(StringSelection(notificationContent))
                }
            })
            .addAction(object : NotificationAction("Copy and close") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    CopyPasteManager.getInstance().setContents(StringSelection(notificationContent))
                    notification.expire()
                }
            })
            .notify(project)
    }
}