package fr.eseo.ld.ts.cinelog.model

import java.util.Date

data class Review(
    val userId: String = "",
    var username: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val posterPath: String? = null,
    val timestamp: Date = Date(),
    val movieId: String = "",
)