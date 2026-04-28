package app.Database;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_short_codes")
public class GlobalShortCode {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    // Optional: track usage context
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType type; // e.g. "COMMUNITY", "EVENT", etc.

    private UUID referenceId; // ID of the entity using it

    private LocalDateTime createdAt = LocalDateTime.now();

    public GlobalShortCode() {}

    public GlobalShortCode(String code, DatabaseType type, UUID referenceId) {
        this.code = code;
        this.type = type;
        this.referenceId = referenceId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
