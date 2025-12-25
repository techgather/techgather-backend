package authentication.oauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CognitoClientHelper {

    private final ClientRegistrationRepository repo;

    public ClientRegistration cognito() {
        return repo.findByRegistrationId("cognito");
    }

    public String getClientId() {
        return cognito().getClientId();
    }

    public String getClientSecret() {
        return cognito().getClientSecret();
    }

    public String getDomainUri() {
        return cognito().getProviderDetails().getTokenUri();
    }
}

