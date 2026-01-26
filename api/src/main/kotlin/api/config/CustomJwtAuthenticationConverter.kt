package api.config

import application.exception.CommonClientErrorCode
import application.exception.UnAuthorizedException
import domain.constants.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
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

        val authorities = listOf(SimpleGrantedAuthority(role.authority))

        return UsernamePasswordAuthenticationToken(
            AuthenticatedUser(userId, sub, role),
            jwt,
            authorities
        )
    }

    private fun getUserId(jwt: Jwt): Long {
        val userIdStr = jwt.getClaimAsString(CLAIM_USER_ID)
        return try {
            userIdStr.toLong()
        } catch (e: NumberFormatException) {
            throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, e)
        }
    }

    private fun getRole(jwt: Jwt): Role {
        val role = jwt.getClaimAsString(CLAIM_ROLE)
        return try {
            Role.fromAuthority(role)
        } catch (e: IllegalArgumentException) {
            throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, e)
        }
    }
}