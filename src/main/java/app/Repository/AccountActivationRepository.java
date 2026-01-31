package app.Repository;

import app.Database.AccountActivation;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountActivationRepository extends JpaRepository<AccountActivation, UUID> {
    Optional<AccountActivation> findByUserAndActivationCodeAndActivatedAtIsNull(User user, String activationCode);
    Optional<AccountActivation> findTopByUserOrderByCreatedAtDesc(User user);
}
