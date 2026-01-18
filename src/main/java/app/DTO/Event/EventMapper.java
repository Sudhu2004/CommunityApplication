package app.DTO.Event;

import app.Database.Event;
import app.Database.EventAttendance;
import app.DTO.User.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final UserMapper userMapper;

    @Autowired
    public EventMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());

        if (event.getCommunity() != null) {
            dto.setCommunityId(event.getCommunity().getId());
            dto.setCommunityName(event.getCommunity().getName());
        }

        if (event.getGroup() != null) {
            dto.setGroupId(event.getGroup().getId());
            dto.setGroupName(event.getGroup().getName());
        }

        dto.setCreatedBy(userMapper.toDTO(event.getCreatedBy()));
        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setLocation(event.getLocation());
        dto.setAttendanceEnabled(event.getAttendanceEnabled());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        // Set counts
        dto.setMessageCount(event.getMessages() != null ? event.getMessages().size() : 0);
        dto.setAttendanceCount(event.getAttendances() != null ? event.getAttendances().size() : 0);

        return dto;
    }

    public EventAttendanceDTO toAttendanceDTO(EventAttendance attendance) {
        if (attendance == null) {
            return null;
        }

        EventAttendanceDTO dto = new EventAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEventId(attendance.getEvent().getId());
        dto.setEventTitle(attendance.getEvent().getTitle());
        dto.setUser(userMapper.toDTO(attendance.getUser()));
        dto.setGroupId(attendance.getGroup().getId());
        dto.setGroupName(attendance.getGroup().getName());
        dto.setStatus(attendance.getStatus());

        if (attendance.getMarkedBy() != null) {
            dto.setMarkedBy(userMapper.toDTO(attendance.getMarkedBy()));
        }

        dto.setMarkedAt(attendance.getMarkedAt());
        dto.setCreatedAt(attendance.getCreatedAt());

        return dto;
    }
}
