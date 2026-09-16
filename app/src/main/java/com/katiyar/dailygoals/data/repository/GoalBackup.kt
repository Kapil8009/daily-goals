package com.katiyar.dailygoals.data.repository

import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.model.Priority
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object GoalBackup {
    private const val FORMAT_VERSION = 1

    fun encode(goals: List<GoalEntity>): String = JSONObject().apply {
        put("formatVersion", FORMAT_VERSION)
        put("exportedAt", System.currentTimeMillis())
        put("goals", JSONArray().apply { goals.forEach { put(it.toJson()) } })
    }.toString(2)

    fun decode(json: String): List<GoalEntity> {
        val root = JSONObject(json)
        require(root.optInt("formatVersion", -1) == FORMAT_VERSION) { "Unsupported backup format" }
        val values = root.getJSONArray("goals")
        return buildList {
            for (index in 0 until values.length()) add(values.getJSONObject(index).toGoal())
        }
    }

    private fun GoalEntity.toJson() = JSONObject().apply {
        put("title", title)
        put("description", description ?: JSONObject.NULL)
        put("date", date.toString())
        put("priority", priority.name)
        put("reminderTime", reminderTime?.toString() ?: JSONObject.NULL)
        put("isCompleted", isCompleted)
        put("completedAt", completedAt ?: JSONObject.NULL)
        put("isCarriedForward", isCarriedForward)
        put("seriesId", seriesId)
        put("isCancelled", isCancelled)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toGoal(): GoalEntity = GoalEntity(
        title = getString("title").trim().also {
            require(it.isNotEmpty() && it.length <= 120) { "Backup contains an invalid title" }
        },
        description = optString("description").takeIf { it.isNotBlank() && it != "null" },
        date = LocalDate.parse(getString("date")),
        priority = runCatching { Priority.valueOf(getString("priority")) }.getOrDefault(Priority.MEDIUM),
        reminderTime = optString("reminderTime").takeIf { it.isNotBlank() && it != "null" }?.let(LocalTime::parse),
        isCompleted = optBoolean("isCompleted"),
        completedAt = optLong("completedAt").takeIf { has("completedAt") && !isNull("completedAt") },
        isCarriedForward = optBoolean("isCarriedForward"),
        seriesId = optString("seriesId").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
        isCancelled = optBoolean("isCancelled"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        updatedAt = optLong("updatedAt", System.currentTimeMillis()),
    )
}
