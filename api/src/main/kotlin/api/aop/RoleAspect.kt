package api.aop

import application.constants.Role
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class RoleAspect {

    companion object {
        val ROLE_PREFIX = "ROLE_"
    }

    @Before("@within(api.annotation.Role) || @annotation(api.annotation.Role)")
    fun checkRole(role: Role) {
        val code = role.code
        val authentication = SecurityContextHolder.getContext().authentication

        val currentRole = authentication.authorities
            .asSequence()
            .mapNotNull { it?.authority }
            .map { it.removePrefix(ROLE_PREFIX) }
            .toSet()

        if (code !in currentRole) {
            throw AccessDeniedException("권한이 없습니다.")
        }
    }
}