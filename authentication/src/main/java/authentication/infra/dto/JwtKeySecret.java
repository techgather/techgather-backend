package authentication.infra.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtKeySecret {
    private String privateKey;
    private String publicKey;
}