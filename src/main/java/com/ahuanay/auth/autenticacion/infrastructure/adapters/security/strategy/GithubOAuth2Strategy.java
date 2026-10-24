package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GithubOAuth2Strategy implements OAuth2UserInfoStrategy {

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GITHUB;
    }

    @Override
    public String extractEmail(OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        if (email == null) {
            email = oauthUser.getAttribute("login") + "@github.local";
        }
        return email;
    }

    @Override
    public String extractProviderId(OAuth2User oauthUser) {
        return oauthUser.getName();
    }
}