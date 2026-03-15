package app.Repository;

import app.Database.*;
import app.Repository.CommunityRepository;
import app.Repository.GroupMembershipRepository;
import app.Repository.GroupRepository;
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
@DisplayName("GroupRepository Tests")
class GroupRepositoryTest {

    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMembershipRepository groupMembershipRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User owner;
    private User anotherUser;
    private Community community;
    private Community otherCommunity;
    private Group group1;
    private Group group2;
    private Group group3;

    @BeforeEach
    void setUp() {
        groupMembershipRepository.deleteAll();
        groupRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        owner       = saveUser("owner@test.com", "Owner");
        anotherUser = saveUser("other@test.com", "Other");

        community      = saveCommunity("Main Community", owner);
        otherCommunity = saveCommunity("Other Community", anotherUser);

        group1 = saveGroup("Java Devs",   "Java group",   community, owner);
        group2 = saveGroup("Spring Boot", "Spring group", community, owner);
        group3 = saveGroup("React Team",  "React group",  otherCommunity, anotherUser);

        // anotherUser is a member of group1
        GroupMembership gm = new GroupMembership();
        gm.setGroup(group1);
        gm.setUser(anotherUser);
        gm.setRole(MemberRole.MEMBER);
        groupMembershipRepository.save(gm);
    }

    // ===================== findByCommunity =====================

    @Nested
    @DisplayName("findByCommunity()")
    class FindByCommunity {

        @Test
        @DisplayName("Should return all groups in a community")
        void find_byCommunity_returnsGroups() {
            List<Group> result = groupRepository.findByCommunity(community);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty list when community has no groups")
        void find_byCommunity_whenNoGroups_returnsEmpty() {
            Community empty = saveCommunity("Empty", owner);
            assertTrue(groupRepository.findByCommunity(empty).isEmpty());
        }
    }

    // ===================== findByCommunityId =====================

    @Nested
    @DisplayName("findByCommunityId()")
    class FindByCommunityId {

        @Test
        @DisplayName("Should return groups by community ID")
        void find_byCommunityId_returnsGroups() {
            List<Group> result = groupRepository.findByCommunityId(community.getId());
            assertEquals(2, result.size());
        }
    }

    // ===================== findByCreatedBy =====================

    @Nested
    @DisplayName("findByCreatedBy()")
    class FindByCreatedBy {

        @Test
        @DisplayName("Should return all groups created by a user across all communities")
        void find_byCreatedBy_returnsAllGroups() {
            List<Group> result = groupRepository.findByCreatedBy(owner);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty when user has not created any groups")
        void find_byCreatedBy_whenNone_returnsEmpty() {
            User nobody = saveUser("nobody@test.com", "Nobody");
            assertTrue(groupRepository.findByCreatedBy(nobody).isEmpty());
        }
    }

    // ===================== findByCommunityAndCreatedBy =====================

    @Nested
    @DisplayName("findByCommunityAndCreatedBy()")
    class FindByCommunityAndCreatedBy {

        @Test
        @DisplayName("Should return groups in community created by specific user")
        void find_byCommunityAndCreator_returnsGroups() {
            List<Group> result = groupRepository.findByCommunityAndCreatedBy(community, owner);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty when user has no groups in that community")
        void find_byCommunityAndCreator_whenNone_returnsEmpty() {
            List<Group> result = groupRepository.findByCommunityAndCreatedBy(community, anotherUser);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByCommunityIdAndCreatedById =====================

    @Nested
    @DisplayName("findByCommunityIdAndCreatedById()")
    class FindByCommunityIdAndCreatedById {

        @Test
        @DisplayName("Should return groups by community and creator IDs")
        void find_byIds_returnsGroups() {
            List<Group> result = groupRepository
                    .findByCommunityIdAndCreatedById(community.getId(), owner.getId());
            assertEquals(2, result.size());
        }
    }

    // ===================== searchByNameInCommunity =====================

    @Nested
    @DisplayName("searchByNameInCommunity()")
    class SearchByNameInCommunity {

        @Test
        @DisplayName("Should return groups matching partial name in community")
        void search_partialMatch_returnsGroups() {
            List<Group> result = groupRepository
                    .searchByNameInCommunity(community.getId(), "java");
            assertEquals(1, result.size());
            assertEquals("Java Devs", result.get(0).getName());
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void search_caseInsensitive_returnsGroups() {
            List<Group> result = groupRepository
                    .searchByNameInCommunity(community.getId(), "SPRING");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should not return groups from other communities")
        void search_doesNotCrossCommunityboundary() {
            List<Group> result = groupRepository
                    .searchByNameInCommunity(community.getId(), "react");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty when no match")
        void search_noMatch_returnsEmpty() {
            List<Group> result = groupRepository
                    .searchByNameInCommunity(community.getId(), "zzznotagroup");
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findGroupsByUserId =====================

    @Nested
    @DisplayName("findGroupsByUserId()")
    class FindGroupsByUserId {

        @Test
        @DisplayName("Should return groups where user is a member")
        void find_byUserId_returnsMemberGroups() {
            List<Group> result = groupRepository.findGroupsByUserId(anotherUser.getId());
            assertEquals(1, result.size());
            assertEquals("Java Devs", result.get(0).getName());
        }

        @Test
        @DisplayName("Should return empty when user has no group memberships")
        void find_byUserId_whenNoMemberships_returnsEmpty() {
            User loner = saveUser("loner@test.com", "Loner");
            assertTrue(groupRepository.findGroupsByUserId(loner.getId()).isEmpty());
        }
    }

    // ===================== findGroupsByCommunityIdAndUserId =====================

    @Nested
    @DisplayName("findGroupsByCommunityIdAndUserId()")
    class FindGroupsByCommunityIdAndUserId {

        @Test
        @DisplayName("Should return groups in specific community where user is a member")
        void find_byCommunityAndUser_returnsGroups() {
            List<Group> result = groupRepository
                    .findGroupsByCommunityIdAndUserId(community.getId(), anotherUser.getId());
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty when user is not a member of any group in that community")
        void find_byCommunityAndUser_whenNoMatch_returnsEmpty() {
            List<Group> result = groupRepository
                    .findGroupsByCommunityIdAndUserId(otherCommunity.getId(), owner.getId());
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByCommunityIdOrderByCreatedAtDesc =====================

    @Nested
    @DisplayName("findByCommunityIdOrderByCreatedAtDesc()")
    class FindByCommunityIdOrderByCreatedAtDesc {

        @Test
        @DisplayName("Should return groups ordered by creation date descending")
        void find_orderedByDate_returnsNewestFirst() {
            List<Group> result = groupRepository
                    .findByCommunityIdOrderByCreatedAtDesc(community.getId());

            assertEquals(2, result.size());
            // group2 was created after group1, so it should appear first
            assertEquals("Spring Boot", result.get(0).getName());
        }
    }

    // ===================== countByCommunityId =====================

    @Nested
    @DisplayName("countByCommunityId()")
    class CountByCommunityId {

        @Test
        @DisplayName("Should return correct group count for community")
        void count_returnsCorrectCount() {
            assertEquals(2L, groupRepository.countByCommunityId(community.getId()));
        }

        @Test
        @DisplayName("Should return 0 for community with no groups")
        void count_whenNoGroups_returnsZero() {
            Community empty = saveCommunity("Empty", owner);
            assertEquals(0L, groupRepository.countByCommunityId(empty.getId()));
        }
    }

    // ===================== helpers =====================

    private User saveUser(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setPassword("pass");
        u.setActive(true);
        return userRepository.save(u);
    }

    private Community saveCommunity(String name, User creator) {
        Community c = new Community();
        c.setName(name);
        c.setCreatedBy(creator);
        return communityRepository.save(c);
    }

    private Group saveGroup(String name, String desc, Community community, User creator) {
        Group g = new Group();
        g.setName(name);
        g.setDescription(desc);
        g.setCommunity(community);
        g.setCreatedBy(creator);
        return groupRepository.save(g);
    }
}
