package app.Repository;

import app.Database.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CommunityMembershipRepository Tests")
class CommunityMembershipRepositoryTest {

    @Autowired private CommunityMembershipRepository membershipRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User owner;
    private User admin;
    private User member;
    private Community community1;
    private Community community2;
    private CommunityMembership ownerMembership;
    private CommunityMembership adminMembership;
    private CommunityMembership memberMembership;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        owner = saveUser("owner@test.com", "Owner");
        admin = saveUser("admin@test.com", "Admin");
        member = saveUser("member@test.com", "Member");

        community1 = saveCommunity("Community Alpha", owner);
        community2 = saveCommunity("Community Beta", owner);

        ownerMembership  = saveMembership(owner,  community1, MemberRole.OWNER);
        adminMembership  = saveMembership(admin,  community1, MemberRole.ADMIN);
        memberMembership = saveMembership(member, community1, MemberRole.MEMBER);
        // owner is also in community2
        saveMembership(owner, community2, MemberRole.OWNER);
    }

    // ===================== findByUserAndCommunity =====================

    @Nested
    @DisplayName("findByUserAndCommunity()")
    class FindByUserAndCommunity {

        @Test
        @DisplayName("Should return membership when user and community match")
        void find_whenExists_returnsMembership() {
            Optional<CommunityMembership> result =
                    membershipRepository.findByUserAndCommunity(owner, community1);

            assertTrue(result.isPresent());
            assertEquals(MemberRole.OWNER, result.get().getRole());
        }

        @Test
        @DisplayName("Should return empty when user is not a member of that community")
        void find_whenNotMember_returnsEmpty() {
            Optional<CommunityMembership> result =
                    membershipRepository.findByUserAndCommunity(admin, community2);

            assertFalse(result.isPresent());
        }
    }

    // ===================== findByUserIdAndCommunityId =====================

    @Nested
    @DisplayName("findByUserIdAndCommunityId()")
    class FindByUserIdAndCommunityId {

        @Test
        @DisplayName("Should return membership by IDs when it exists")
        void find_byIds_returnsMembership() {
            Optional<CommunityMembership> result =
                    membershipRepository.findByUserIdAndCommunityId(admin.getId(), community1.getId());

            assertTrue(result.isPresent());
            assertEquals(MemberRole.ADMIN, result.get().getRole());
        }

        @Test
        @DisplayName("Should return empty when IDs don't match")
        void find_byIds_whenNotMember_returnsEmpty() {
            Optional<CommunityMembership> result =
                    membershipRepository.findByUserIdAndCommunityId(member.getId(), community2.getId());

            assertFalse(result.isPresent());
        }
    }

    // ===================== existsByUserIdAndCommunityId =====================

    @Nested
    @DisplayName("existsByUserIdAndCommunityId()")
    class ExistsByUserIdAndCommunityId {

        @Test
        @DisplayName("Should return true when membership exists")
        void exists_whenMembership_returnsTrue() {
            assertTrue(membershipRepository.existsByUserIdAndCommunityId(
                    member.getId(), community1.getId()));
        }

        @Test
        @DisplayName("Should return false when membership does not exist")
        void exists_whenNoMembership_returnsFalse() {
            assertFalse(membershipRepository.existsByUserIdAndCommunityId(
                    member.getId(), community2.getId()));
        }
    }

    // ===================== findByCommunity =====================

    @Nested
    @DisplayName("findByCommunity()")
    class FindByCommunity {

        @Test
        @DisplayName("Should return all members of a community")
        void find_byCommunity_returnsAllMembers() {
            List<CommunityMembership> result = membershipRepository.findByCommunity(community1);

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should return only owner when community has one member")
        void find_byCommunity_whenOneMember_returnsOne() {
            List<CommunityMembership> result = membershipRepository.findByCommunity(community2);

            assertEquals(1, result.size());
            assertEquals(MemberRole.OWNER, result.get(0).getRole());
        }
    }

    // ===================== findByCommunityId =====================

    @Nested
    @DisplayName("findByCommunityId()")
    class FindByCommunityId {

        @Test
        @DisplayName("Should return all memberships for a community ID")
        void find_byCommunityId_returnsMembers() {
            List<CommunityMembership> result =
                    membershipRepository.findByCommunityId(community1.getId());

            assertEquals(3, result.size());
        }
    }

    // ===================== findByUser / findByUserId =====================

    @Nested
    @DisplayName("findByUser() and findByUserId()")
    class FindByUser {

        @Test
        @DisplayName("Should return all community memberships for a user")
        void findByUser_returnsAllMembershipsForUser() {
            List<CommunityMembership> result = membershipRepository.findByUser(owner);
            assertEquals(2, result.size()); // owner is in community1 and community2
        }

        @Test
        @DisplayName("Should return empty list when user has no memberships")
        void findByUser_whenNoMemberships_returnsEmpty() {
            User loner = saveUser("loner@test.com", "Loner");
            List<CommunityMembership> result = membershipRepository.findByUser(loner);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return memberships by user ID")
        void findByUserId_returnsMemberships() {
            List<CommunityMembership> result =
                    membershipRepository.findByUserId(member.getId());
            assertEquals(1, result.size());
        }
    }

    // ===================== findByCommunityAndRole =====================

    @Nested
    @DisplayName("findByCommunityAndRole()")
    class FindByCommunityAndRole {

        @Test
        @DisplayName("Should return only ADMIN members")
        void find_byRole_returnsCorrectRole() {
            List<CommunityMembership> admins =
                    membershipRepository.findByCommunityAndRole(community1, MemberRole.ADMIN);

            assertEquals(1, admins.size());
            assertEquals(MemberRole.ADMIN, admins.get(0).getRole());
        }

        @Test
        @DisplayName("Should return empty when no members have that role")
        void find_byRole_whenNoMatch_returnsEmpty() {
            List<CommunityMembership> result =
                    membershipRepository.findByCommunityAndRole(community2, MemberRole.MEMBER);

            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByCommunityIdAndRole =====================

    @Nested
    @DisplayName("findByCommunityIdAndRole()")
    class FindByCommunityIdAndRole {

        @Test
        @DisplayName("Should return members with OWNER role by community ID")
        void find_byCommunityIdAndRole_returnsOwners() {
            List<CommunityMembership> result = membershipRepository
                    .findByCommunityIdAndRole(community1.getId(), MemberRole.OWNER);

            assertEquals(1, result.size());
            assertEquals(MemberRole.OWNER, result.get(0).getRole());
        }
    }

    // ===================== findOwnersByCommunityId =====================

    @Nested
    @DisplayName("findOwnersByCommunityId()")
    class FindOwnersByCommunityId {

        @Test
        @DisplayName("Should return owner memberships using custom JPQL query")
        void findOwners_returnsOwners() {
            List<CommunityMembership> result =
                    membershipRepository.findOwnersByCommunityId(community1.getId());

            assertEquals(1, result.size());
            assertEquals(owner.getEmail(), result.get(0).getUser().getEmail());
        }
    }

    // ===================== deleteByUserIdAndCommunityId =====================

    @Nested
    @DisplayName("deleteByUserIdAndCommunityId()")
    class DeleteByUserIdAndCommunityId {

        @Test
        @DisplayName("Should remove membership by user and community IDs")
        void delete_removesMembership() {
            membershipRepository.deleteByUserIdAndCommunityId(
                    member.getId(), community1.getId());

            assertFalse(membershipRepository.existsByUserIdAndCommunityId(
                    member.getId(), community1.getId()));
        }
    }

    // ===================== countByCommunityId =====================

    @Nested
    @DisplayName("countByCommunityId()")
    class CountByCommunityId {

        @Test
        @DisplayName("Should return 3 members for community1")
        void count_returnsCorrectMemberCount() {
            long count = membershipRepository.countByCommunityId(community1.getId());
            assertEquals(3L, count);
        }

        @Test
        @DisplayName("Should return 1 for community2 (only owner)")
        void count_forCommunityWithOneOwner_returns1() {
            long count = membershipRepository.countByCommunityId(community2.getId());
            assertEquals(1L, count);
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
        c.setDescription("desc");
        c.setCreatedBy(creator);
        return communityRepository.save(c);
    }

    private CommunityMembership saveMembership(User user, Community community, MemberRole role) {
        CommunityMembership m = new CommunityMembership();
        m.setUser(user);
        m.setCommunity(community);
        m.setRole(role);
        return membershipRepository.save(m);
    }
}
