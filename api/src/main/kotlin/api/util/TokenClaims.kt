package api.util

import domain.constants.Role

data class TokenClaims(
    val sub: String,
    val userId: Long,
    val role: Role,
    val cognitoGroups: List<String>,
    val allClaims: Map<String, Any>
)