package com.mimo.badmintonscore.model

import android.content.Context
import android.content.SharedPreferences

class MatchPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("badminton_match_prefs", Context.MODE_PRIVATE)

    var leftTeamName: String
        get() = prefs.getString(KEY_LEFT_NAME, "蓝方") ?: "蓝方"
        set(value) = prefs.edit().putString(KEY_LEFT_NAME, value.ifBlank { "蓝方" }).apply()

    var rightTeamName: String
        get() = prefs.getString(KEY_RIGHT_NAME, "红方") ?: "红方"
        set(value) = prefs.edit().putString(KEY_RIGHT_NAME, value.ifBlank { "红方" }).apply()

    var matchType: MatchType
        get() {
            val name = prefs.getString(KEY_MATCH_TYPE, MatchType.SINGLES.name)
            return try {
                MatchType.valueOf(name ?: MatchType.SINGLES.name)
            } catch (e: Exception) {
                MatchType.SINGLES
            }
        }
        set(value) = prefs.edit().putString(KEY_MATCH_TYPE, value.name).apply()

    var targetScore: Int
        get() = prefs.getInt(KEY_TARGET_SCORE, 21)
        set(value) = prefs.edit().putInt(KEY_TARGET_SCORE, value).apply()

    var enableIntervalTimer: Boolean
        get() = prefs.getBoolean(KEY_INTERVAL_TIMER, true)
        set(value) = prefs.edit().putBoolean(KEY_INTERVAL_TIMER, value).apply()

    var isServeAssistantEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVE_ASSISTANT, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVE_ASSISTANT, value).apply()

    var isServeDirectionReversed: Boolean
        get() = prefs.getBoolean(KEY_SERVE_REVERSED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVE_REVERSED, value).apply()

    companion object {
        private const val KEY_LEFT_NAME = "pref_left_team_name"
        private const val KEY_RIGHT_NAME = "pref_right_team_name"
        private const val KEY_MATCH_TYPE = "pref_match_type"
        private const val KEY_TARGET_SCORE = "pref_target_score"
        private const val KEY_INTERVAL_TIMER = "pref_interval_timer"
        private const val KEY_SERVE_ASSISTANT = "pref_serve_assistant"
        private const val KEY_SERVE_REVERSED = "pref_serve_reversed"
    }
}
