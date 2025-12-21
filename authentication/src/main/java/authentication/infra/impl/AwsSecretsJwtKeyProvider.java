package authentication.infra.impl;

import authentication.infra.JwtKeyProvider;
import authentication.infra.dto.JwtKeySecret;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class AwsSecretsJwtKeyProvider implements JwtKeyProvider {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.secrets.jwt-key-name}")
    private String secretName;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        GetSecretValueResponse response =
                secretsManagerClient.getSecretValue(
                        GetSecretValueRequest.builder()
                                .secretId(secretName)
                                .build()
                );

        JwtKeySecret secret =
                objectMapper.readValue(response.secretString(), JwtKeySecret.class);

        this.privateKey = parsePrivateKey(secret.getPrivateKey());
        this.publicKey = parsePublicKey(secret.getPublicKey());
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(content);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    @Override
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }
}
