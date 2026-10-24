package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoStrategy {
    AuthProvider getProvider();

    String extractEmail(OAuth2User oauthUser);

    String extractProviderId(OAuth2User oauthUser);
}