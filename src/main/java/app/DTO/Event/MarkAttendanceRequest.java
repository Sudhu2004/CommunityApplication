package app.DTO.Event;

import app.Database.AttendanceStatus;
import app.Database.DatabaseType;
import jakarta.validation.constraints.NotNull;

public class MarkAttendanceRequest {

    @NotNull(message = "User code is required")
    private String userCode;

    private String groupCode;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    @NotNull(message = "Type is required")
    private DatabaseType type;

    // Constructors
    public MarkAttendanceRequest() {}

    // Getters and Setters
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public DatabaseType getType() { return type; }
    public void setType(DatabaseType type) { this.type = type; }
}
