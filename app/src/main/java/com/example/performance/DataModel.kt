package com.example.performance

data class User(
    val email: String = "",
    val pass: String = "",
    var level: Long = 1,
    var totalPoints: Long = 0,
    var current5minPoints: Long = 0,
    var coins: Long = 10,
    var fifty: Long = 5,
    var skip: Long = 5,
    var username: String = ""
)

data class Session(
    val sessionID: String = "",
    val timeStamp: Long = 0,
    val questions: List<Question> = listOf(),
    var wins: Long = 0,
    var loss: Long = 0,
    var points: Long = 0
)

data class Question (
    val questionId : String = "",
    val questionText: String = "",
    val correctAnswer: String = "",
    val userResponse: String = ""
)

data class Rankings(
    val type: RankingType,
    val userName: String = "",
    val lvl: Long = 0,
    val totalPoints: Long = 0,
    val current5minPoints: Long
)

enum class RankingType{
    LEVEL,
    FIVE_MINUTE
}

