package com.guanpj.me.codereview

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import java.awt.datatransfer.StringSelection

data class ReviewInfo(
    val platform: String,
    val type: String,
    val id: String,
    val summary: String,
    val link: String
)

class PopupNotifier {
    companion object {
        private const val REVIEWER_NAME = "@袁伟"
        private const val BUG_TRACKING_TEMPLATE = "http://dlc.coding.byd.com/p/%s/bug-tracking/issues/%s/detail"
        private const val REQUIREMENTS_TEMPLATE = "http://dlc.coding.byd.com/p/%s/requirements/issues/%s/detail"
        private const val NOTIFICATION_GROUP_ID = "git.console.monitor"
    }

    private val notificationGroup by lazy {
        try {
            // 尝试使用新版 API
            NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
        } catch (e: Throwable) {
            // 回退到旧版 API
            NotificationGroup.balloonGroup(NOTIFICATION_GROUP_ID)
        }
    }

    private fun generateNotificationContent(info: ReviewInfo): String {
        val testLink = when (info.type) {
            "PR" -> String.format(BUG_TRACKING_TEMPLATE, info.platform.toLowerCase(), info.id)
            "CR" -> String.format(REQUIREMENTS_TEMPLATE, info.platform.toLowerCase(), info.id)
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

        // 兼容新版和旧版API
        val notification = notificationGroup?.createNotification(
            notificationContent,
            NotificationType.INFORMATION
        ) ?: Notification(
            NOTIFICATION_GROUP_ID,
            notificationContent,
            NotificationType.INFORMATION
        )

        notification.addAction(object : NotificationAction("Copy") {
            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                CopyPasteManager.getInstance().setContents(StringSelection(notificationContent))
            }
        }).addAction(object : NotificationAction("Copy and close") {
            override fun actionPerformed(p0: AnActionEvent, p1: Notification) {
                CopyPasteManager.getInstance().setContents(StringSelection(notificationContent))
                notification.expire()
            }
        })
        
        Notifications.Bus.notify(notification, project)
    }
}