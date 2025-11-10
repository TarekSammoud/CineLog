package fr.eseo.ld.ts.cinelog.data

data class User(
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val pseudo: String = "",
    val photoUrl: String? = null
)

