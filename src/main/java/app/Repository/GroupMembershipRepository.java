package app.Repository;

import app.Database.Group;
import app.Database.GroupMembership;
import app.Database.MemberRole;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    // Check if user is member of a group
    Optional<GroupMembership> findByUserAndGroup(User user, Group group);

    // Alternative with IDs
    Optional<GroupMembership> findByUserIdAndGroupId(UUID userId, UUID groupId);

    // Check if membership exists
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);

    // Get all members of a group
    List<GroupMembership> findByGroup(Group group);

    // Get all members by group ID
    List<GroupMembership> findByGroupId(UUID groupId);

    // Get all groups a user is part of
    List<GroupMembership> findByUser(User user);

    // Get all groups by user ID
    List<GroupMembership> findByUserId(UUID userId);

    // Find members by role
    List<GroupMembership> findByGroupAndRole(Group group, MemberRole role);

    // Find members by group ID and role
    List<GroupMembership> findByGroupIdAndRole(UUID groupId, MemberRole role);

    // Get owners of a group
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.id = :groupId AND gm.role = 'OWNER'")
    List<GroupMembership> findOwnersByGroupId(@Param("groupId") UUID groupId);

    // Get all members of groups in a community
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.community.id = :communityId")
    List<GroupMembership> findMembersByCommunityId(@Param("communityId") UUID communityId);

    // Get users who are members of specific group in a community
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.id = :groupId AND gm.group.community.id = :communityId")
    List<GroupMembership> findByGroupIdAndCommunityId(@Param("groupId") UUID groupId, @Param("communityId") UUID communityId);

    // Delete membership
    void deleteByUserIdAndGroupId(UUID userId, UUID groupId);

    // Count members in a group
    long countByGroupId(UUID groupId);
}
