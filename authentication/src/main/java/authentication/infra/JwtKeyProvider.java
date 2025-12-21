package authentication.infra;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface JwtKeyProvider {
    PublicKey getPublicKey();
    PrivateKey getPrivateKey();
}
