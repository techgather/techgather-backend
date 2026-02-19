package api.config

import application.exception.CommonClientErrorCode
import application.exception.UnAuthorizedException
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    companion object {
        private const val CLAIM_SUB = "sub"
        private const val CLAIM_COGNITO_GROUPS = "cognito:groups"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val sub = getSub(jwt)
        val authorities = getAuthorities(jwt)

        return UsernamePasswordAuthenticationToken(
            AuthenticatedUser(sub),
            jwt,
            authorities
        )
    }

    private fun getSub(jwt: Jwt): String {
        return jwt.getClaimAsString(CLAIM_SUB)
            ?.takeIf { it.isNotBlank() }
            ?: throw UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null)
    }

    private fun getAuthorities(jwt: Jwt): List<SimpleGrantedAuthority> {
        val groups = jwt.getClaimAsStringList(CLAIM_COGNITO_GROUPS) ?: emptyList()
        if (groups.isEmpty()) {
            return listOf(SimpleGrantedAuthority("ROLE_USER"))
        }

        return groups
            .filter { it.isNotBlank() }
            .map { SimpleGrantedAuthority("ROLE_$it") }
            .ifEmpty { listOf(SimpleGrantedAuthority("ROLE_USER")) }
    }
}
