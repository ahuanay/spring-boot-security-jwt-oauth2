package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuth2StrategyRegistry {

    private final Map<AuthProvider, OAuth2UserInfoStrategy> strategyMap;

    public OAuth2StrategyRegistry(List<OAuth2UserInfoStrategy> strategies) {
        strategyMap = new EnumMap<>(AuthProvider.class);
        for (OAuth2UserInfoStrategy strategy : strategies) {
            strategyMap.put(strategy.getProvider(), strategy);
        }
    }

    public OAuth2UserInfoStrategy getStrategy(AuthProvider provider) {
        OAuth2UserInfoStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new UsuarioInvalidoException("No existe una estrategia de OAuth2 para el proveedor: " + provider);
        }
        return strategy;
    }
}