package com.guanpj.me.codereview

import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project

class GitOutputFilter(private val project: Project) : Filter {
    private val popupNotifier = PopupNotifier()
    
    private fun parseConsoleOutput(line: String): ReviewInfo? {
        // remote:   http://xxx [平台][CR或者PR:ID][概述][NEW]
        val regex = """remote:\s+?(http://[^\s]+)\s+?\[([^\]]+)\]\[(CR|PR):(\d+)\]\[([^\]]+)\]\[NEW\]""".toRegex()
        val matchResult = regex.find(line) ?: return null
        
        return ReviewInfo(
            platform = matchResult.groupValues[2],
            type = matchResult.groupValues[3],
            id = matchResult.groupValues[4],
            summary = matchResult.groupValues[5],
            link = matchResult.groupValues[1]
        )
    }

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        if (line.isNotEmpty()) {
            parseConsoleOutput(line)?.let { reviewInfo ->
                popupNotifier.showPopup(project, reviewInfo)
            }
        }
        return null
    }
}