package app.Repository;

import app.Database.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.event.id = :eventId ORDER BY m.createdAt DESC")
    Page<Message> findByEventIdOrderByCreatedAtDesc(@Param("eventId") UUID eventId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.event.id = :eventId ORDER BY m.createdAt ASC")
    List<Message> findByEventIdOrderByCreatedAtAsc(@Param("eventId") UUID eventId);

    @Query("SELECT m FROM Message m WHERE m.community.id = :communityId ORDER BY m.createdAt DESC")
    Page<Message> findByCommunityIdOrderByCreatedAtDesc(@Param("communityId") UUID communityId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.community.id = :communityId ORDER BY m.createdAt ASC")
    List<Message> findByCommunityIdOrderByCreatedAtAsc(@Param("communityId") UUID communityId);

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.createdAt DESC")
    Page<Message> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") UUID groupId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.createdAt ASC")
    List<Message> findByGroupIdOrderByCreatedAtAsc(@Param("groupId") UUID groupId);

    @Query("SELECT m FROM Message m WHERE m.event.id = :eventId AND m.sender.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findByEventIdAndSenderIdOrderByCreatedAtDesc(
            @Param("eventId") UUID eventId,
            @Param("userId") UUID userId
    );

    @Query("SELECT COUNT(m) FROM Message m WHERE m.event.id = :eventId")
    Long countByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.community.id = :communityId")
    Long countByCommunityId(@Param("communityId") UUID communityId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.group.id = :groupId")
    Long countByGroupId(@Param("groupId") UUID groupId);

    void deleteByEventId(UUID eventId);
}
