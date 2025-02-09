package com.guanpj.me.codereview

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import java.awt.datatransfer.StringSelection
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.InputStreamReader

class PopupNotifier {
    companion object {
        private const val REVIEWER_NAME = "@袁伟"
        private const val BUG_TRACKING_TEMPLATE = "http://dlc.coding.byd.com/p/%s/bug-tracking/issues/%s/detail"
        private const val REQUIREMENTS_TEMPLATE = "http://dlc.coding.byd.com/p/%s/requirements/issues/%s/detail"
        private const val NOTIFICATION_GROUP_ID = "git.console.monitor"

        // 标题特征模式
        private const val TITLE_PATTERN = """<div class="titleView-[^"]*"[^>]*>[^<]+</div>"""

        private val notificationGroup = NotificationGroup(NOTIFICATION_GROUP_ID, NotificationDisplayType.BALLOON)
    }

    private fun isUrlContentValid(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = TimeUnit.SECONDS.toMillis(5).toInt()
                readTimeout = TimeUnit.SECONDS.toMillis(5).toInt()
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return false
            }

            val content = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
            connection.disconnect()

            val titleMatch = Regex(TITLE_PATTERN, RegexOption.IGNORE_CASE).find(content)
            titleMatch != null && titleMatch.value.replace(Regex("<[^>]+>"), "").trim().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun generateNotificationContent(info: ReviewInfo): String {
        val testLink = when (info.type) {
            "PR" -> String.format(BUG_TRACKING_TEMPLATE, info.platform, info.id)
            "CR" -> String.format(REQUIREMENTS_TEMPLATE, info.platform, info.id)
            else -> ""
        }

        /*val testLinkWithStatus = if (testLink.isNotEmpty()) {
            val isValid = isUrlContentValid(testLink)
            if (!isValid) "$testLink [无效链接]" else testLink
        } else testLink*/

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

        val notification = notificationGroup.createNotification(
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