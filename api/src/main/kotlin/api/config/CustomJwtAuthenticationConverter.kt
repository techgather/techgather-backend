package api.config

import application.exception.CommonClientErrorCode
import application.exception.UnAuthorizedException
import domain.constants.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    companion object {
        private const val CLAIM_USER_ID = "user_id"
        private const val CLAIM_ROLE = "role"
        private const val CLAIM_SUB = "sub"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userId = getUserId(jwt)
        val role = getRole(jwt)
        val sub = jwt.getClaimAsString(CLAIM_SUB)

        val authorities = listOf(SimpleGrantedAuthority(role.getAuthority()))

        return UsernamePasswordAuthenticationToken(
            AuthenticatedUser(userId, sub, role),
            jwt,
            authorities
        )
    }

    private fun getUserId(jwt: Jwt): Long {
        val userIdValue = jwt.getClaim<Any>(CLAIM_USER_ID)
            ?: throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null)

        return when (userIdValue) {
            is Number -> userIdValue.toLong()
            is String -> userIdValue.toLongOrNull()
                ?: throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null)
            else -> throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null)
        }
    }

    private fun getRole(jwt: Jwt): Role {
        val roleStr = jwt.getClaimAsString(CLAIM_ROLE)
            ?: throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null)

        // "ROLE_USER" -> "USER" 변환
        val roleName = roleStr.removePrefix("ROLE_")
        return try {
            Role.valueOf(roleName)
        } catch (e: IllegalArgumentException) {
            throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, e)
        }
    }
}