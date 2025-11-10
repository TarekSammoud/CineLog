package fr.eseo.ld.ts.cinelog.model

data class Review(
    val userId: String = "",
    val username: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)