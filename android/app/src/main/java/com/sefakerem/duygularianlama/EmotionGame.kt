package com.sefakerem.duygularianlama

class EmotionGame {

    private val emotions = listOf("Kızgın", "Sakin", "Mutlu", "Üzgün")
    private var emotionIndex = 0
    private var attemptCount = 0
    private val maxAttempts = 3
    var totalScore = 0
        private set

    val currentEmotion: String
        get() = emotions[emotionIndex]
    
    fun evaluateResponse(response: Map<String, Float>): Int {
        val score = (response[currentEmotion] ?: 0f) * 10
        totalScore += score.toInt()
        return score.toInt()
    }

    fun moveToNextEmotion(): Boolean {
        attemptCount++
        return if (attemptCount >= maxAttempts) {
            attemptCount = 0
            emotionIndex++
            emotionIndex < emotions.size
        } else {
            true
        }
    }
}
