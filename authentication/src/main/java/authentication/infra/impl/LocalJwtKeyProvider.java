package authentication.infra.impl;

import application.exception.TechGatherException;
import authentication.infra.JwtKeyProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static authentication.exception.AuthErrorCode.LOCAL_JWT_KEY_GENERATION_FAILED;

@Component
@Profile("local")
public class LocalJwtKeyProvider implements JwtKeyProvider {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public LocalJwtKeyProvider() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (Exception e) {
            throw TechGatherException.of(LOCAL_JWT_KEY_GENERATION_FAILED);
        }
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
