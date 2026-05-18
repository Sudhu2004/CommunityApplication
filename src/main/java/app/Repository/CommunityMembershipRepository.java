package app.Repository;

import app.Database.Community;
import app.Database.CommunityMembership;
import app.Database.MemberRole;
import app.Database.MembershipStatus;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, UUID> {

    // Check if user is member of a community
    Optional<CommunityMembership> findByUserAndCommunity(User user, Community community);

    Optional<CommunityMembership> findByUserIdAndCommunityId(UUID userId, UUID communityId);

    // Filtered by status
    Optional<CommunityMembership> findByUserIdAndCommunityIdAndStatus(UUID userId, UUID communityId, MembershipStatus status);

    boolean existsByUserIdAndCommunityId(UUID userId, UUID communityId);

    boolean existsByUserIdAndCommunityIdAndStatus(UUID userId, UUID communityId, MembershipStatus status);

    List<CommunityMembership> findByCommunityAndStatus(Community community, MembershipStatus status);

    List<CommunityMembership> findByCommunityIdAndStatus(UUID communityId, MembershipStatus status);

    List<CommunityMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    // Get all members of a community
    List<CommunityMembership> findByCommunity(Community community);

    // Get all members by community ID
    List<CommunityMembership> findByCommunityId(UUID communityId);

    // Get all communities a user is part of
    List<CommunityMembership> findByUser(User user);

    // Get all communities by user ID
    List<CommunityMembership> findByUserId(UUID userId);

    // Find members by role (e.g., all owners/admins)
    List<CommunityMembership> findByCommunityAndRole(Community community, MemberRole role);

    // Find members by community ID and role
    List<CommunityMembership> findByCommunityIdAndRole(UUID communityId, MemberRole role);

    // Get owners of a community
    @Query("SELECT cm FROM CommunityMembership cm WHERE cm.community.id = :communityId AND cm.role = 'OWNER'")
    List<CommunityMembership> findOwnersByCommunityId(@Param("communityId") UUID communityId);

    // Delete membership by user and community
    void deleteByUserIdAndCommunityId(UUID userId, UUID communityId);

    // Count members in a community
    long countByCommunityId(UUID communityId);
}
