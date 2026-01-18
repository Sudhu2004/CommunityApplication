package app.Repository;

import app.Database.Community;
import app.Database.Group;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    // Find all groups in a community
    List<Group> findByCommunity(Community community);

    // Find all groups by community ID
    List<Group> findByCommunityId(UUID communityId);

    // Find groups created by a user
    List<Group> findByCreatedBy(User user);

    // Find groups created by a user in a specific community
    List<Group> findByCommunityAndCreatedBy(Community community, User createdBy);

    // Find groups by community ID and creator ID
    List<Group> findByCommunityIdAndCreatedById(UUID communityId, UUID createdById);

    // Search groups by name within a community
    @Query("SELECT g FROM Group g WHERE g.community.id = :communityId AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Group> searchByNameInCommunity(@Param("communityId") UUID communityId, @Param("name") String name);

    // Get groups where a specific user is a member
    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE m.user.id = :userId")
    List<Group> findGroupsByUserId(@Param("userId") UUID userId);

    // Get groups in a community where user is a member
    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE g.community.id = :communityId AND m.user.id = :userId")
    List<Group> findGroupsByCommunityIdAndUserId(@Param("communityId") UUID communityId, @Param("userId") UUID userId);

    @Query("SELECT g FROM Group g WHERE g.community.id = :communityId ORDER BY g.createdAt DESC")
    List<Group> findByCommunityIdOrderByCreatedAtDesc(@Param("communityId") UUID communityId);

    // Count groups in a community
    long countByCommunityId(UUID communityId);
}
