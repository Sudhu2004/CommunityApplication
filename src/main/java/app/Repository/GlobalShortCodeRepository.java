package app.Repository;

import app.Database.DatabaseType;
import app.Database.GlobalShortCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GlobalShortCodeRepository extends JpaRepository<GlobalShortCode, UUID> {

    // Basic lookups
    Optional<GlobalShortCode> findByCode(String code);

    boolean existsByCode(String code);

    void deleteByCode(String code);

    // Context-based queries
    List<GlobalShortCode> findByType(DatabaseType type);

    Optional<GlobalShortCode> findByTypeAndReferenceId(DatabaseType type, UUID referenceId);

    Optional<GlobalShortCode> findByCodeAndType(String code, DatabaseType type);

    void deleteByType(DatabaseType type);
}
