package app.DTO.Event;

import app.Database.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class MarkAttendanceRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Group ID is required")
    private UUID groupId;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    // Constructors
    public MarkAttendanceRequest() {}

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
