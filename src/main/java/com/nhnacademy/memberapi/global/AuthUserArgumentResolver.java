package com.nhnacademy.memberapi.global;

import com.nhnacademy.memberapi.domain.auth.dto.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @AuthUser 어노테이션이 붙어있고, 타입이 CustomUserDetails인 경우만 처리
        return parameter.hasParameterAnnotation(AuthUser.class)
                && CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // HttpServletRequest(혹은 MockMvc의 principal)에서 인증 객체를 가져옴
        Principal principal = webRequest.getUserPrincipal();

        // Principal이 없거나 Authentication 타입이 아니면 null 반환 (혹은 예외 발생)
        if (principal == null) {
            return null; // or throw new UnauthorizedException();
        }

        // UsernamePasswordAuthenticationToken 등에서 실제 UserDetails 꺼내기
        if (principal instanceof Authentication authentication) {
            Object principalObj = authentication.getPrincipal();
            if (principalObj instanceof CustomUserDetails) {
                return principalObj;
            }
        }

        return null;
    }
}