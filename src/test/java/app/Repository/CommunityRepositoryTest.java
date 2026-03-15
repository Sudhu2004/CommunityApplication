package app.Repository;

import app.Database.Community;
import app.Database.CommunityMembership;
import app.Database.MemberRole;
import app.Database.User;
import app.Repository.CommunityMembershipRepository;
import app.Repository.CommunityRepository;
import app.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CommunityRepository Integration Tests")
class CommunityRepositoryTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherUser;
    private Community community1;
    private Community community2;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setName("Owner User");
        owner.setPassword("password");
        owner.setActive(true);
        owner = userRepository.save(owner);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setName("Other User");
        otherUser.setPassword("password");
        otherUser.setActive(true);
        otherUser = userRepository.save(otherUser);

        community1 = new Community();
        community1.setName("Java Developers");
        community1.setDescription("A community for Java developers");
        community1.setCreatedBy(owner);
        community1 = communityRepository.save(community1);

        community2 = new Community();
        community2.setName("Spring Boot Enthusiasts");
        community2.setDescription("Spring Boot community");
        community2.setCreatedBy(otherUser);
        community2 = communityRepository.save(community2);

        // Add owner as OWNER of community1
        CommunityMembership ownerMembership = new CommunityMembership();
        ownerMembership.setCommunity(community1);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(MemberRole.OWNER);
        membershipRepository.save(ownerMembership);

        // Add otherUser as MEMBER of community1
        CommunityMembership memberMembership = new CommunityMembership();
        memberMembership.setCommunity(community1);
        memberMembership.setUser(otherUser);
        memberMembership.setRole(MemberRole.MEMBER);
        membershipRepository.save(memberMembership);
    }

    // ===================== findByCreatedBy =====================

    @Nested
    @DisplayName("findByCreatedBy()")
    class FindByCreatedBy {

        @Test
        @DisplayName("Should return communities created by a specific user")
        void findByCreatedBy_returnsCommunitiesForOwner() {
            List<Community> result = communityRepository.findByCreatedBy(owner);

            assertEquals(1, result.size());
            assertEquals("Java Developers", result.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty list when user has no communities")
        void findByCreatedBy_whenNoCommunitiesCreated_returnsEmpty() {
            User newUser = new User();
            newUser.setEmail("newuser@example.com");
            newUser.setName("New User");
            newUser.setPassword("pass");
            newUser.setActive(true);
            newUser = userRepository.save(newUser);

            List<Community> result = communityRepository.findByCreatedBy(newUser);

            assertTrue(result.isEmpty());
        }
    }

    // ===================== findAllByOrderByCreatedAtDesc =====================

    @Nested
    @DisplayName("findAllByOrderByCreatedAtDesc()")
    class FindAllOrdered {

        @Test
        @DisplayName("Should return all communities ordered newest first")
        void findAllByOrderByCreatedAtDesc_returnsBothCommunities() {
            List<Community> result = communityRepository.findAllByOrderByCreatedAtDesc();

            assertEquals(2, result.size());
            // community2 was created after community1, so it should appear first
            assertEquals("Spring Boot Enthusiasts", result.get(0).getName());
        }
    }

    // ===================== searchByName =====================

    @Nested
    @DisplayName("searchByName()")
    class SearchByName {

        @Test
        @DisplayName("Should return communities matching partial name (case-insensitive)")
        void searchByName_partialMatch_returnsResults() {
            List<Community> result = communityRepository.searchByName("java");

            assertEquals(1, result.size());
            assertEquals("Java Developers", result.get(0).getName());
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void searchByName_caseInsensitive_returnsResults() {
            List<Community> result = communityRepository.searchByName("SPRING");

            assertEquals(1, result.size());
            assertEquals("Spring Boot Enthusiasts", result.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty list when no match")
        void searchByName_noMatch_returnsEmpty() {
            List<Community> result = communityRepository.searchByName("zzznotaname");

            assertTrue(result.isEmpty());
        }
    }

    // ===================== findCommunitiesByUserId =====================

    @Nested
    @DisplayName("findCommunitiesByUserId()")
    class FindCommunitiesByUserId {

        @Test
        @DisplayName("Should return communities where user is a member")
        void findCommunitiesByUserId_returnsMemberCommunities() {
            List<Community> result = communityRepository.findCommunitiesByUserId(otherUser.getId());

            assertEquals(1, result.size());
            assertEquals("Java Developers", result.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty list when user has no memberships")
        void findCommunitiesByUserId_whenNoMemberships_returnsEmpty() {
            User loner = new User();
            loner.setEmail("loner@example.com");
            loner.setName("Loner");
            loner.setPassword("pass");
            loner.setActive(true);
            loner = userRepository.save(loner);

            List<Community> result = communityRepository.findCommunitiesByUserId(loner.getId());

            assertTrue(result.isEmpty());
        }
    }

    // ===================== countMembersByCommunityId =====================

    @Nested
    @DisplayName("countMembersByCommunityId()")
    class CountMembers {

        @Test
        @DisplayName("Should return correct member count for a community")
        void countMembersByCommunityId_returnsCorrectCount() {
            Long count = communityRepository.countMembersByCommunityId(community1.getId());

            assertEquals(2L, count); // owner + otherUser
        }

        @Test
        @DisplayName("Should return zero for community with no members")
        void countMembersByCommunityId_whenNoMembers_returnsZero() {
            Long count = communityRepository.countMembersByCommunityId(community2.getId());

            assertEquals(0L, count);
        }
    }
}
