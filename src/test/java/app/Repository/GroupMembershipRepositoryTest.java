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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GroupMembershipRepository Tests")
class GroupMembershipRepositoryTest {

    @Autowired private GroupMembershipRepository groupMembershipRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User owner;
    private User adminUser;
    private User memberUser;
    private Community community;
    private Group group1;
    private Group group2;
    private GroupMembership ownerMembership;
    private GroupMembership adminMembership;
    private GroupMembership memberMembership;

    @BeforeEach
    void setUp() {
        groupMembershipRepository.deleteAll();
        groupRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        owner      = saveUser("owner@test.com", "Owner");
        adminUser  = saveUser("admin@test.com", "Admin");
        memberUser = saveUser("member@test.com", "Member");

        community = saveCommunity("Test Community", owner);

        group1 = saveGroup("Group Alpha", community, owner);
        group2 = saveGroup("Group Beta",  community, owner);

        ownerMembership  = saveMembership(owner,      group1, MemberRole.OWNER);
        adminMembership  = saveMembership(adminUser,  group1, MemberRole.ADMIN);
        memberMembership = saveMembership(memberUser, group1, MemberRole.MEMBER);
        // owner is also in group2
        saveMembership(owner, group2, MemberRole.OWNER);
    }

    // ===================== findByUserAndGroup =====================

    @Nested
    @DisplayName("findByUserAndGroup()")
    class FindByUserAndGroup {

        @Test
        @DisplayName("Should return membership when user is in group")
        void find_whenExists_returnsMembership() {
            Optional<GroupMembership> result =
                    groupMembershipRepository.findByUserAndGroup(owner, group1);

            assertTrue(result.isPresent());
            assertEquals(MemberRole.OWNER, result.get().getRole());
        }

        @Test
        @DisplayName("Should return empty when user is not in group")
        void find_whenNotMember_returnsEmpty() {
            Optional<GroupMembership> result =
                    groupMembershipRepository.findByUserAndGroup(memberUser, group2);

            assertFalse(result.isPresent());
        }
    }

    // ===================== findByUserIdAndGroupId =====================

    @Nested
    @DisplayName("findByUserIdAndGroupId()")
    class FindByUserIdAndGroupId {

        @Test
        @DisplayName("Should return membership by IDs")
        void find_byIds_returnsMembership() {
            Optional<GroupMembership> result = groupMembershipRepository
                    .findByUserIdAndGroupId(adminUser.getId(), group1.getId());

            assertTrue(result.isPresent());
            assertEquals(MemberRole.ADMIN, result.get().getRole());
        }

        @Test
        @DisplayName("Should return empty when IDs don't match any membership")
        void find_byIds_whenNotMember_returnsEmpty() {
            Optional<GroupMembership> result = groupMembershipRepository
                    .findByUserIdAndGroupId(memberUser.getId(), group2.getId());

            assertFalse(result.isPresent());
        }
    }

    // ===================== existsByUserIdAndGroupId =====================

    @Nested
    @DisplayName("existsByUserIdAndGroupId()")
    class ExistsByUserIdAndGroupId {

        @Test
        @DisplayName("Should return true when membership exists")
        void exists_whenMembership_returnsTrue() {
            assertTrue(groupMembershipRepository.existsByUserIdAndGroupId(
                    memberUser.getId(), group1.getId()));
        }

        @Test
        @DisplayName("Should return false when membership does not exist")
        void exists_whenNoMembership_returnsFalse() {
            assertFalse(groupMembershipRepository.existsByUserIdAndGroupId(
                    memberUser.getId(), group2.getId()));
        }
    }

    // ===================== findByGroup =====================

    @Nested
    @DisplayName("findByGroup()")
    class FindByGroup {

        @Test
        @DisplayName("Should return all members of a group")
        void find_byGroup_returnsAllMembers() {
            List<GroupMembership> result = groupMembershipRepository.findByGroup(group1);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should return only owner for group2")
        void find_byGroup_withOneOwner_returnsOne() {
            List<GroupMembership> result = groupMembershipRepository.findByGroup(group2);
            assertEquals(1, result.size());
            assertEquals(MemberRole.OWNER, result.get(0).getRole());
        }
    }

    // ===================== findByGroupId =====================

    @Nested
    @DisplayName("findByGroupId()")
    class FindByGroupId {

        @Test
        @DisplayName("Should return memberships by group ID")
        void find_byGroupId_returnsMembers() {
            List<GroupMembership> result =
                    groupMembershipRepository.findByGroupId(group1.getId());
            assertEquals(3, result.size());
        }
    }

    // ===================== findByUser =====================

    @Nested
    @DisplayName("findByUser()")
    class FindByUser {

        @Test
        @DisplayName("Should return all group memberships for a user")
        void find_byUser_returnsMemberships() {
            List<GroupMembership> result = groupMembershipRepository.findByUser(owner);
            assertEquals(2, result.size()); // owner is in group1 + group2
        }

        @Test
        @DisplayName("Should return empty when user has no memberships")
        void find_byUser_whenNone_returnsEmpty() {
            User loner = saveUser("loner@test.com", "Loner");
            assertTrue(groupMembershipRepository.findByUser(loner).isEmpty());
        }
    }

    // ===================== findByUserId =====================

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("Should return memberships by user ID")
        void find_byUserId_returnsMemberships() {
            List<GroupMembership> result =
                    groupMembershipRepository.findByUserId(memberUser.getId());
            assertEquals(1, result.size());
        }
    }

    // ===================== findByGroupAndRole =====================

    @Nested
    @DisplayName("findByGroupAndRole()")
    class FindByGroupAndRole {

        @Test
        @DisplayName("Should return only ADMIN members")
        void find_byGroupAndRole_returnsAdmins() {
            List<GroupMembership> result =
                    groupMembershipRepository.findByGroupAndRole(group1, MemberRole.ADMIN);
            assertEquals(1, result.size());
            assertEquals(MemberRole.ADMIN, result.get(0).getRole());
        }

        @Test
        @DisplayName("Should return empty when no members have that role")
        void find_byGroupAndRole_whenNoMatch_returnsEmpty() {
            List<GroupMembership> result =
                    groupMembershipRepository.findByGroupAndRole(group2, MemberRole.MEMBER);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByGroupIdAndRole =====================

    @Nested
    @DisplayName("findByGroupIdAndRole()")
    class FindByGroupIdAndRole {

        @Test
        @DisplayName("Should return MEMBER role by group ID")
        void find_byGroupIdAndRole_returnsMembers() {
            List<GroupMembership> result = groupMembershipRepository
                    .findByGroupIdAndRole(group1.getId(), MemberRole.MEMBER);
            assertEquals(1, result.size());
        }
    }

    // ===================== findOwnersByGroupId =====================

    @Nested
    @DisplayName("findOwnersByGroupId()")
    class FindOwnersByGroupId {

        @Test
        @DisplayName("Should return owners via custom JPQL query")
        void findOwners_returnsOwners() {
            List<GroupMembership> result =
                    groupMembershipRepository.findOwnersByGroupId(group1.getId());
            assertEquals(1, result.size());
            assertEquals(owner.getEmail(), result.get(0).getUser().getEmail());
        }
    }

    // ===================== findMembersByCommunityId =====================

    @Nested
    @DisplayName("findMembersByCommunityId()")
    class FindMembersByCommunityId {

        @Test
        @DisplayName("Should return all group memberships within a community")
        void find_byCommunityId_returnsAllGroupMemberships() {
            List<GroupMembership> result =
                    groupMembershipRepository.findMembersByCommunityId(community.getId());
            // group1 has 3 members, group2 has 1
            assertEquals(4, result.size());
        }
    }

    // ===================== findByGroupIdAndCommunityId =====================

    @Nested
    @DisplayName("findByGroupIdAndCommunityId()")
    class FindByGroupIdAndCommunityId {

        @Test
        @DisplayName("Should return memberships in a specific group within a community")
        void find_byGroupAndCommunity_returnsMembers() {
            List<GroupMembership> result = groupMembershipRepository
                    .findByGroupIdAndCommunityId(group1.getId(), community.getId());
            assertEquals(3, result.size());
        }
    }

    // ===================== deleteByUserIdAndGroupId =====================

    @Nested
    @DisplayName("deleteByUserIdAndGroupId()")
    class DeleteByUserIdAndGroupId {

        @Test
        @DisplayName("Should remove membership by user and group IDs")
        void delete_removesMembership() {
            groupMembershipRepository.deleteByUserIdAndGroupId(
                    memberUser.getId(), group1.getId());

            assertFalse(groupMembershipRepository.existsByUserIdAndGroupId(
                    memberUser.getId(), group1.getId()));
        }
    }

    // ===================== countByGroupId =====================

    @Nested
    @DisplayName("countByGroupId()")
    class CountByGroupId {

        @Test
        @DisplayName("Should return correct member count for group")
        void count_returnsCorrectCount() {
            assertEquals(3L, groupMembershipRepository.countByGroupId(group1.getId()));
        }

        @Test
        @DisplayName("Should return 1 for group with only owner")
        void count_oneOwner_returns1() {
            assertEquals(1L, groupMembershipRepository.countByGroupId(group2.getId()));
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

    private Group saveGroup(String name, Community community, User creator) {
        Group g = new Group();
        g.setName(name);
        g.setCommunity(community);
        g.setCreatedBy(creator);
        return groupRepository.save(g);
    }

    private GroupMembership saveMembership(User user, Group group, MemberRole role) {
        GroupMembership m = new GroupMembership();
        m.setUser(user);
        m.setGroup(group);
        m.setRole(role);
        return groupMembershipRepository.save(m);
    }
}
