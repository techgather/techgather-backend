package domain.repository;

import domain.entity.ProviderRefreshToken;
import domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRefreshTokenRepository extends JpaRepository<ProviderRefreshToken, String>  {
    Optional<ProviderRefreshToken> findByRefreshToken(RefreshToken token);
}
