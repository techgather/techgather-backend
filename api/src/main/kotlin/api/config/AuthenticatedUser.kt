package api.config

import domain.constants.Role

data class AuthenticatedUser(
    val userId: Long,
    val sub: String,
    val role: Role
)
