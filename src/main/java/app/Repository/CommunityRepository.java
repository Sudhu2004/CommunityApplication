package app.Repository;

import app.Database.Community;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {

    // Find communities created by a specific user
    List<Community> findByCreatedBy(User user);

    // Find all communities ordered by creation date (newest first)
    List<Community> findAllByOrderByCreatedAtDesc();

    // Search communities by name (partial match, case-insensitive)
    @Query("SELECT c FROM Community c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Community> searchByName(@Param("name") String name);

    // Get communities where a specific user is a member
    @Query("SELECT c FROM Community c JOIN c.memberships m WHERE m.user.id = :userId")
    List<Community> findCommunitiesByUserId(@Param("userId") UUID userId);

    // Count total members in a community
    @Query("SELECT COUNT(m) FROM CommunityMembership m WHERE m.community.id = :communityId")
    Long countMembersByCommunityId(@Param("communityId") UUID communityId);
}