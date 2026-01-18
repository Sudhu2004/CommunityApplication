package app.Repository;

import app.Database.Media;
import app.Database.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    @Query("SELECT m FROM Media m WHERE m.message.id = :messageId")
    List<Media> findByMessageId(@Param("messageId") UUID messageId);

    @Query("SELECT m FROM Media m WHERE m.message.event.id = :eventId")
    List<Media> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT m FROM Media m WHERE m.message.event.id = :eventId AND m.mediaType = :mediaType")
    List<Media> findByEventIdAndMediaType(
            @Param("eventId") UUID eventId,
            @Param("mediaType") MediaType mediaType
    );

    void deleteByMessageId(UUID messageId);
}
