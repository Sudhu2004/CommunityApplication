package app.Repository;

import app.Database.Activity;
import app.Database.DatabaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByType(DatabaseType type);

    List<Activity> findByTypeAndReferenceId(DatabaseType type, UUID referenceId);

    List<Activity> findByReferenceId(UUID referenceId);
}