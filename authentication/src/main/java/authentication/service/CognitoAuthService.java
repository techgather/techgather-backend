package authentication.service;

import authentication.config.aws.CognitoProperties;
import authentication.dto.response.OAuthTokenResponse;
import authentication.oauth.service.OAuthClientRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
public class CognitoAuthService {

    private final RestClient restClient;
    private final CognitoProperties properties;
    private final CognitoIdentityProviderClient cognitoClient;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${cognito.user-pool-id}")
    private String userPoolId;
    @Value("${spring.security.oauth2.client.provider.cognito.domain-uri}")
    private String domainUri;
    public static final String REFRESH_TOKEN = "refresh_token";

    public void addUserToDefaultGroup(String sub) {
        try {
            cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(sub)
                    .groupName("USER")
                    .build());
        } catch (Exception e) {
            // 필요 시 예외를 던지거나 로그만 남기고 진행
        }
    }

    public OAuthTokenResponse refresh(String refreshToken, String clientId) {
        ClientRegistration registration = findRegistrationByClientId(clientId);

        return restClient.post()
                .uri(registration.getProviderDetails().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(createForm(refreshToken, registration.getClientId(), registration.getClientSecret()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Failed to refresh token");
                })
                .body(OAuthTokenResponse.class);
    }

    public OAuthTokenResponse refreshWithUserClient(String refreshToken) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(OAuthClientRegistration.COGNITO);
        if (registration == null) {
            throw new IllegalStateException("Cognito client registration not found for user flow");
        }

        return restClient.post()
                .uri(registration.getProviderDetails().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(createForm(refreshToken, registration.getClientId(), registration.getClientSecret()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Failed to refresh token");
                })
                .body(OAuthTokenResponse.class);
    }

    private ClientRegistration findRegistrationByClientId(String clientId) {
        if (properties.getAdminClientId().equals(clientId)) {
            return clientRegistrationRepository.findByRegistrationId("cognito-admin");
        } else if (properties.getUserClientId().equals(clientId)) {
            return clientRegistrationRepository.findByRegistrationId("cognito");
        }
        throw new IllegalArgumentException("알 수 없는 Client ID입니다: " + clientId);
    }

    public String globalSignOut(String username, String baseUrl) {
        try {
            if (username == null || username.isBlank()) {
                return buildLogoutUrl(baseUrl);
            }

            cognitoClient.adminUserGlobalSignOut(
                    AdminUserGlobalSignOutRequest.builder()
                            .userPoolId(userPoolId)
                            .username(username)
                            .build()
            );
        } catch (UserNotFoundException e) {

        } catch (CognitoIdentityProviderException e) {
            // AWS SDK 에러
        } catch (Exception e) {

        }

        return buildLogoutUrl(baseUrl);
    }

    // 어디로 리다이렉트 시킬 지 논의 필요
    public String buildLogoutUrl(String baseUrl) {
        return domainUri + "/logout"
                + "?client_id=" + URLEncoder.encode(properties.getAdminClientId(), UTF_8)
                + "&logout_uri=" + URLEncoder.encode(baseUrl, UTF_8);
    }

    // 쿼리 파라미터 사용시
    private ClientRegistration getCognitoRegistration(String role) {
        String registrationId = role.contains("ADMIN") ? "cognito-admin" : "cognito";

        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(registrationId);

        if (registration == null) {
            throw new IllegalStateException("Cognito client registration not found for role: " + role);
        }
        return registration;
    }

    private MultiValueMap<String, String> createForm(String refreshToken, String clientId, String clientSecret) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", REFRESH_TOKEN);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        return formData;
    }

}
