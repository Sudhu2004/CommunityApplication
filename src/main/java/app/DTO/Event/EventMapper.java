package app.DTO.Event;

import app.Database.DatabaseType;
import app.Database.Event;
import app.Database.EventAttendance;
import app.DTO.User.UserMapper;
import app.Service.GlobalShortCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final UserMapper userMapper;
    private final GlobalShortCodeService globalShortCodeService;

    @Autowired
    public EventMapper(UserMapper userMapper, GlobalShortCodeService globalShortCodeService) {
        this.userMapper = userMapper;
        this.globalShortCodeService = globalShortCodeService;
    }

    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventDTO dto = new EventDTO();
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());

        if (event.getCommunity() != null) {
            dto.setCommunityCode(globalShortCodeService.getShortCode(DatabaseType.COMMUNITY, event.getCommunity().getId()));
            dto.setCommunityName(event.getCommunity().getName());
        }

        if (event.getGroup() != null) {
            dto.setGroupCode(globalShortCodeService.getShortCode(DatabaseType.GROUP, event.getGroup().getId()));
            dto.setGroupName(event.getGroup().getName());
        }

        dto.setCreatedBy(userMapper.toDTO(event.getCreatedBy()));
        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setLocation(event.getLocation());
        dto.setAttendanceEnabled(event.getAttendanceEnabled());
        dto.setIsNotice(event.getIsNotice());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        // Set counts
        dto.setMessageCount(event.getMessages() != null ? event.getMessages().size() : 0);
        dto.setAttendanceCount(event.getAttendances() != null ? event.getAttendances().size() : 0);

        dto.setEventCode(globalShortCodeService.getShortCode(DatabaseType.EVENTS, event.getId()));

        return dto;
    }

    public EventAttendanceDTO toAttendanceDTO(EventAttendance attendance) {
        if (attendance == null) {
            return null;
        }

        EventAttendanceDTO dto = new EventAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEventCode(globalShortCodeService.getShortCode(DatabaseType.EVENTS, attendance.getEvent().getId()));
        dto.setEventTitle(attendance.getEvent().getTitle());
        dto.setUser(userMapper.toDTO(attendance.getUser()));

        if (attendance.getGroup() != null) {
            dto.setGroupCode(globalShortCodeService.getShortCode(DatabaseType.GROUP, attendance.getGroup().getId()));
            dto.setGroupName(attendance.getGroup().getName());
        }

        dto.setStatus(attendance.getStatus());

        if (attendance.getMarkedBy() != null) {
            dto.setMarkedBy(userMapper.toDTO(attendance.getMarkedBy()));
        }

        dto.setMarkedAt(attendance.getMarkedAt());
        dto.setCreatedAt(attendance.getCreatedAt());

        return dto;
    }
}
