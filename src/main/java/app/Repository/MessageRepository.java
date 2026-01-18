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

    @Query("SELECT m FROM Message m WHERE m.event.id = :eventId AND m.sender.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findByEventIdAndSenderIdOrderByCreatedAtDesc(
            @Param("eventId") UUID eventId,
            @Param("userId") UUID userId
    );

    @Query("SELECT COUNT(m) FROM Message m WHERE m.event.id = :eventId")
    Long countByEventId(@Param("eventId") UUID eventId);

    void deleteByEventId(UUID eventId);
}
