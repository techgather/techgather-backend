package api.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class TokenUtils(
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val CLAIM_SUB = "sub"
        private const val CLAIM_COGNITO_GROUPS = "cognito:groups"
    }

    fun parseAccessToken(token: String): TokenClaims {
        val cleanToken = token.removePrefix("Bearer ").trim()

        val parts = cleanToken.split(".")
        require(parts.size == 3) { "Invalid JWT token format" }

        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        val claims = objectMapper.readValue(payload, object : TypeReference<Map<String, Any>>() {})

        return TokenClaims(
            sub = claims[CLAIM_SUB]?.toString() ?: throw IllegalArgumentException("Missing sub claim"),
            cognitoGroups = parseCognitoGroups(claims),
            allClaims = claims
        )
    }

    private fun parseCognitoGroups(claims: Map<String, Any>): List<String> {
        return when (val groups = claims[CLAIM_COGNITO_GROUPS]) {
            is List<*> -> groups.mapNotNull { it?.toString() }
            else -> emptyList()
        }
    }
}
