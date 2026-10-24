package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Strategy implements OAuth2UserInfoStrategy {

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public String extractEmail(OAuth2User oauthUser) {
        return oauthUser.getAttribute("email");
    }

    @Override
    public String extractProviderId(OAuth2User oauthUser) {
        return oauthUser.getAttribute("sub");
    }
}