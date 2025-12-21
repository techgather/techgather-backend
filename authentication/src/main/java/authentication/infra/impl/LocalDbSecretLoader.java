package authentication.infra.impl;

import authentication.infra.DbSecretLoader;
import authentication.infra.dto.DbSecretProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalDbSecretLoader implements DbSecretLoader {
    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Value("${db.url}")
    private String url;

    @Override
    public DbSecretProperties load() {
        return new DbSecretProperties(username, password, url);
    }
}
