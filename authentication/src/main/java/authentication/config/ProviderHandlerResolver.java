package authentication.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProviderHandlerResolver {
    private final List<OAuth2ProviderHandler> handlers;

    public OAuth2ProviderHandler resolve(String registrationId) {
        return handlers.stream()
                .filter(handler -> handler.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + registrationId));
    }
}
