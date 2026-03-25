package api.aop

import api.util.TokenUtils
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class RoleAspect(
    private val tokenUtils: TokenUtils
) {

    @Before("@within(api.annotation.Role) || @annotation(api.annotation.Role)")
    fun checkRole(joinPoint: JoinPoint) {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            ?: throw IllegalStateException("HTTP request not available")

        val authHeader = request.getHeader("Authorization")
            ?: throw AccessDeniedException("Authorization header is missing")

        val tokenClaims = try {
            tokenUtils.parseAccessToken(authHeader)
        } catch (e: Exception) {
            throw AccessDeniedException("Invalid token: ${e.message}")
        }

        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val roleAnnotation = method.getAnnotation(api.annotation.Role::class.java)
            ?: method.declaringClass.getAnnotation(api.annotation.Role::class.java)
            ?: throw IllegalStateException("@Role annotation not found")

        val requiredRoles = roleAnnotation.codes

        val userRoles = tokenClaims.cognitoGroups.ifEmpty { listOf("USER") }
        val hasRequiredRole = requiredRoles.any { required -> userRoles.contains(required) }

        if (!hasRequiredRole) {
            throw AccessDeniedException(
                "권한이 없습니다. 필요한 역할: ${requiredRoles.joinToString(", ")}, 현재 역할: ${userRoles.joinToString(", ")}"
            )
        }
    }
}
