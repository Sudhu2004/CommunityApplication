package app.Repository;

import app.Database.AccountActivation;
import app.Database.User;
import app.Repository.AccountActivationRepository;
import app.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AccountActivationRepository Tests")
class AccountActivationRepositoryTest {

    @Autowired
    private AccountActivationRepository activationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private AccountActivation activeCode;
    private AccountActivation usedCode;
    private AccountActivation expiredCode;

    @BeforeEach
    void setUp() {
        activationRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setPassword("hashedpass");
        user.setActive(false);
        user = userRepository.save(user);

        // A fresh, unused activation code
        activeCode = new AccountActivation();
        activeCode.setActivationCode("123456");
        activeCode.setUser(user);
        activeCode.setCreatedAt(LocalDateTime.now());
        activeCode.setExpiresAt(LocalDateTime.now().plusHours(24));
        activeCode = activationRepository.save(activeCode);

        // An already-used activation code (activatedAt is set)
        usedCode = new AccountActivation();
        usedCode.setActivationCode("999999");
        usedCode.setUser(user);
        usedCode.setCreatedAt(LocalDateTime.now().minusHours(2));
        usedCode.setExpiresAt(LocalDateTime.now().plusHours(22));
        usedCode.setActivatedAt(LocalDateTime.now().minusHours(1));
        usedCode = activationRepository.save(usedCode);

        // An expired, unused code
        expiredCode = new AccountActivation();
        expiredCode.setActivationCode("777777");
        expiredCode.setUser(user);
        expiredCode.setCreatedAt(LocalDateTime.now().minusDays(2));
        expiredCode.setExpiresAt(LocalDateTime.now().minusDays(1));
        expiredCode = activationRepository.save(expiredCode);
    }

    // ===================== findByUserAndActivationCodeAndActivatedAtIsNull =====================

    @Nested
    @DisplayName("findByUserAndActivationCodeAndActivatedAtIsNull()")
    class FindByUserAndActivationCodeAndActivatedAtIsNull {

        @Test
        @DisplayName("Should return activation when code is correct and not yet used")
        void find_withCorrectUnusedCode_returnsActivation() {
            Optional<AccountActivation> result = activationRepository
                    .findByUserAndActivationCodeAndActivatedAtIsNull(user, "123456");

            assertTrue(result.isPresent());
            assertEquals("123456", result.get().getActivationCode());
            assertNull(result.get().getActivatedAt());
        }

        @Test
        @DisplayName("Should return empty when code matches but is already used (activatedAt is set)")
        void find_withUsedCode_returnsEmpty() {
            Optional<AccountActivation> result = activationRepository
                    .findByUserAndActivationCodeAndActivatedAtIsNull(user, "999999");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty when code does not match")
        void find_withWrongCode_returnsEmpty() {
            Optional<AccountActivation> result = activationRepository
                    .findByUserAndActivationCodeAndActivatedAtIsNull(user, "000000");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty when user does not match")
        void find_withWrongUser_returnsEmpty() {
            User otherUser = new User();
            otherUser.setEmail("other@example.com");
            otherUser.setName("Other");
            otherUser.setPassword("pass");
            otherUser.setActive(false);
            otherUser = userRepository.save(otherUser);

            Optional<AccountActivation> result = activationRepository
                    .findByUserAndActivationCodeAndActivatedAtIsNull(otherUser, "123456");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return expired code if it was never used (activatedAt is null)")
        void find_withExpiredButUnusedCode_returnsActivation() {
            // The query only filters on activatedAt IS NULL — expiry logic is in the service
            Optional<AccountActivation> result = activationRepository
                    .findByUserAndActivationCodeAndActivatedAtIsNull(user, "777777");

            assertTrue(result.isPresent());
        }
    }

    // ===================== findTopByUserOrderByCreatedAtDesc =====================

    @Nested
    @DisplayName("findTopByUserOrderByCreatedAtDesc()")
    class FindTopByUserOrderByCreatedAtDesc {

        @Test
        @DisplayName("Should return the most recently created activation for the user")
        void findTop_returnsLatestActivation() {
            // activeCode was saved last with the most recent createdAt
            Optional<AccountActivation> result = activationRepository
                    .findTopByUserOrderByCreatedAtDesc(user);

            assertTrue(result.isPresent());
            // The latest one should be activeCode (createdAt = now)
            assertEquals("123456", result.get().getActivationCode());
        }

        @Test
        @DisplayName("Should return empty when user has no activations")
        void findTop_whenNoActivations_returnsEmpty() {
            User newUser = new User();
            newUser.setEmail("new@example.com");
            newUser.setName("New User");
            newUser.setPassword("pass");
            newUser.setActive(false);
            newUser = userRepository.save(newUser);

            Optional<AccountActivation> result = activationRepository
                    .findTopByUserOrderByCreatedAtDesc(newUser);

            assertFalse(result.isPresent());
        }
    }

    // ===================== entity state helpers =====================

    @Nested
    @DisplayName("Entity helper methods")
    class EntityHelpers {

        @Test
        @DisplayName("isExpired() returns true when expiresAt is in the past")
        void isExpired_whenPast_returnsTrue() {
            assertTrue(expiredCode.isExpired());
        }

        @Test
        @DisplayName("isExpired() returns false when expiresAt is in the future")
        void isExpired_whenFuture_returnsFalse() {
            assertFalse(activeCode.isExpired());
        }

        @Test
        @DisplayName("isActivated() returns true when activatedAt is set")
        void isActivated_whenActivatedAtSet_returnsTrue() {
            assertTrue(usedCode.isActivated());
        }

        @Test
        @DisplayName("isActivated() returns false when activatedAt is null")
        void isActivated_whenActivatedAtNull_returnsFalse() {
            assertFalse(activeCode.isActivated());
        }
    }

    // ===================== basic CRUD =====================

    @Nested
    @DisplayName("Basic CRUD")
    class BasicCrud {

        @Test
        @DisplayName("Should persist and find activation by ID")
        void save_persistsActivation() {
            AccountActivation activation = new AccountActivation();
            activation.setActivationCode("555555");
            activation.setUser(user);

            AccountActivation saved = activationRepository.save(activation);

            assertNotNull(saved.getId());
            assertTrue(activationRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should delete activation by ID")
        void delete_removesActivation() {
            activationRepository.deleteById(activeCode.getId());
            assertFalse(activationRepository.findById(activeCode.getId()).isPresent());
        }
    }
}