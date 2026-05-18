package app.DTO.Event;

import app.DTO.User.UserDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventDTO {
    private String title;
    private String description;
    private String communityCode;
    private String communityName;
    private String groupCode;
    private String groupName;
    private UserDTO createdBy;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String location;
    private Boolean attendanceEnabled;
    private Boolean isNotice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int messageCount;
    private int attendanceCount;
    private String eventCode;

    // Constructors
    public EventDTO() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCommunityCode() { return communityCode; }
    public void setCommunityCode(String communityCode) { this.communityCode = communityCode; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public UserDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserDTO createdBy) { this.createdBy = createdBy; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getAttendanceEnabled() { return attendanceEnabled; }
    public void setAttendanceEnabled(Boolean attendanceEnabled) { this.attendanceEnabled = attendanceEnabled; }

    public Boolean getIsNotice() { return isNotice; }
    public void setIsNotice(Boolean isNotice) { this.isNotice = isNotice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public int getAttendanceCount() { return attendanceCount; }
    public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }

    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
}
