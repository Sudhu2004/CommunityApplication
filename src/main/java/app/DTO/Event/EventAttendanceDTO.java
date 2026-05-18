package app.DTO.Event;

import app.DTO.User.UserDTO;
import app.Database.AttendanceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventAttendanceDTO {
    private UUID id;
    private String eventCode;
    private String eventTitle;
    private UserDTO user;
    private String groupCode;
    private String groupName;
    private AttendanceStatus status;
    private UserDTO markedBy;
    private LocalDateTime markedAt;
    private LocalDateTime createdAt;

    // Constructors
    public EventAttendanceDTO() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public UserDTO getMarkedBy() { return markedBy; }
    public void setMarkedBy(UserDTO markedBy) { this.markedBy = markedBy; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
