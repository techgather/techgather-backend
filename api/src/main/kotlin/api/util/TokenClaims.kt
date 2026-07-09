package api.util

data class TokenClaims(
    val sub: String,
    val cognitoGroups: List<String>,
    val allClaims: Map<String, Any>
)
