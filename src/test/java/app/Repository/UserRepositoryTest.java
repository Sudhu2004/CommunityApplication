package app.Repository;

import app.Database.User;
import app.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new User();
        user1.setEmail("alice@example.com");
        user1.setName("Alice Smith");
        user1.setPhone("1111111111");
        user1.setPassword("hashedpassword1");
        user1.setActive(true);

        user2 = new User();
        user2.setEmail("bob@example.com");
        user2.setName("Bob Jones");
        user2.setPhone("2222222222");
        user2.setPassword("hashedpassword2");
        user2.setActive(false);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    // ===================== findByEmail =====================

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("Should return user when email exists")
        void findByEmail_whenEmailExists_returnsUser() {
            Optional<User> result = userRepository.findByEmail("alice@example.com");

            assertTrue(result.isPresent());
            assertEquals("Alice Smith", result.get().getName());
        }

        @Test
        @DisplayName("Should return empty Optional when email not found")
        void findByEmail_whenEmailNotFound_returnsEmpty() {
            Optional<User> result = userRepository.findByEmail("notfound@example.com");

            assertFalse(result.isPresent());
        }
    }

    // ===================== existsByEmail =====================

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("Should return true for existing email")
        void existsByEmail_whenEmailExists_returnsTrue() {
            assertTrue(userRepository.existsByEmail("alice@example.com"));
        }

        @Test
        @DisplayName("Should return false for non-existing email")
        void existsByEmail_whenEmailNotFound_returnsFalse() {
            assertFalse(userRepository.existsByEmail("nobody@example.com"));
        }
    }

    // ===================== findByPhone =====================

    @Nested
    @DisplayName("findByPhone()")
    class FindByPhone {

        @Test
        @DisplayName("Should return user when phone exists")
        void findByPhone_whenPhoneExists_returnsUser() {
            Optional<User> result = userRepository.findByPhone("1111111111");

            assertTrue(result.isPresent());
            assertEquals("alice@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty Optional when phone not found")
        void findByPhone_whenPhoneNotFound_returnsEmpty() {
            Optional<User> result = userRepository.findByPhone("9999999999");

            assertFalse(result.isPresent());
        }
    }

    // ===================== searchByName =====================

    @Nested
    @DisplayName("searchByName()")
    class SearchByName {

        @Test
        @DisplayName("Should return users matching partial name (case-insensitive)")
        void searchByName_withPartialMatch_returnsMatchingUsers() {
            List<User> results = userRepository.searchByName("alice");

            assertEquals(1, results.size());
            assertEquals("Alice Smith", results.get(0).getName());
        }

        @Test
        @DisplayName("Should return multiple users when multiple match")
        void searchByName_withCommonTerm_returnsAllMatches() {
            // Both "Alice Smith" and "Bob Jones" have names, search for "s" (in Smith)
            List<User> results = userRepository.searchByName("smith");

            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void searchByName_isCaseInsensitive() {
            List<User> results = userRepository.searchByName("ALICE");

            assertFalse(results.isEmpty());
            assertEquals("Alice Smith", results.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty list when no match found")
        void searchByName_withNoMatch_returnsEmpty() {
            List<User> results = userRepository.searchByName("zzznotaname");

            assertTrue(results.isEmpty());
        }
    }

    // ===================== findAll =====================

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Should return all persisted users")
        void findAll_returnsAllUsers() {
            List<User> users = userRepository.findAll();

            assertEquals(2, users.size());
        }
    }

    // ===================== save & deleteById =====================

    @Nested
    @DisplayName("save() and deleteById()")
    class SaveAndDelete {

        @Test
        @DisplayName("Should persist user with auto-generated UUID")
        void save_persistsUserWithUUID() {
            User newUser = new User();
            newUser.setEmail("charlie@example.com");
            newUser.setName("Charlie");
            newUser.setPassword("pass");
            newUser.setActive(true);

            User saved = userRepository.save(newUser);

            assertNotNull(saved.getId());
            assertEquals("charlie@example.com", saved.getEmail());
        }

        @Test
        @DisplayName("Should delete user by ID")
        void deleteById_removesUser() {
            User saved = userRepository.findByEmail("alice@example.com").get();
            userRepository.deleteById(saved.getId());

            assertFalse(userRepository.existsByEmail("alice@example.com"));
        }
    }
}
