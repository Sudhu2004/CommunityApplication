package app.DTO.Event;

import app.Database.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class MarkAttendanceRequest {

    @NotNull(message = "User code is required")
    private String userCode;

    @NotNull(message = "Group code is required")
    private String groupCode;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    // Constructors
    public MarkAttendanceRequest() {}

    // Getters and Setters
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
