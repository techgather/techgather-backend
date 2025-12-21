package authentication.infra;

import authentication.infra.dto.DbSecretProperties;

public interface DbSecretLoader {
    DbSecretProperties load();
}
