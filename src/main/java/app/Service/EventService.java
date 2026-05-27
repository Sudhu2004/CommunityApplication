package app.Service;

import app.DTO.Event.*;
import app.Database.*;
import app.DTO.Event.EventMapper;
import app.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventAttendanceRepository attendanceRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private CommunityMembershipRepository communityMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    /**
     * Create a new event
     */
    @Transactional
    public EventDTO createEvent(String userCode, CreateEventRequest request) {
        User creator = userService.getUserByShortCode(userCode);

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCreatedBy(creator);
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setLocation(request.getLocation());
        event.setAttendanceEnabled(request.getAttendanceEnabled());

        // Set community if provided
        if (request.getCommunityCode() != null) {
            UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, request.getCommunityCode());
            Community community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new RuntimeException("Community not found"));
            
            // Authorization for community-wide events (Notices)
            if (request.getGroupCode() == null) {
                if (!isCommunityAdminOrOwner(creator.getId(), community.getId())) {
                    throw new RuntimeException("Only community owners or admins can create community-wide notices");
                }
                event.setIsNotice(true);
            }
            event.setCommunity(community);
        }

        // Set group if provided
        if (request.getGroupCode() != null) {
            UUID groupId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.GROUP, request.getGroupCode());
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            // Verify creator is a member of the group
            if (!groupMembershipRepository.existsByUserIdAndGroupIdAndStatus(creator.getId(), group.getId(), MembershipStatus.ACCEPTED)) {
                throw new RuntimeException("You must be an active member of the group to create events");
            }

            event.setGroup(group);
            if (event.getCommunity() == null) {
                event.setCommunity(group.getCommunity());
            }
        }

        if (event.getCommunity() == null) {
            throw new RuntimeException("Event must belong to a community");
        }

        Event savedEvent = eventRepository.save(event);
        globalShortCodeService.generateAndReserve(DatabaseType.EVENTS, savedEvent.getId());

        // Initialize attendance automatically if enabled
        if (savedEvent.getAttendanceEnabled()) {
            initializeAttendanceForEvent(savedEvent);
        }

        return eventMapper.toDTO(savedEvent);
    }

    /**
     * Get event by Code
     */
    public EventDTO getEventByCode(String eventCode) {
        Event event = getEventEntityByCode(eventCode);
        return eventMapper.toDTO(event);
    }

    /**
     * Get events by community
     */
    public List<EventDTO> getEventsByCommunity(String communityCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        List<Event> events = eventRepository.findByCommunityIdOrderByDateDesc(communityId);
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get events by group
     */
    public List<EventDTO> getEventsByGroup(String groupCode) {
        UUID groupId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.GROUP, groupCode);
        List<Event> events = eventRepository.findByGroupIdOrderByDateDesc(groupId);
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming events by community
     */
    public List<EventDTO> getUpcomingEventsByCommunity(String communityCode) {
        UUID communityId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.COMMUNITY, communityCode);
        List<Event> events = eventRepository.findUpcomingEventsByCommunityId(communityId, LocalDate.now());
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming events by group
     */
    public List<EventDTO> getUpcomingEventsByGroup(String groupCode) {
        UUID groupId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.GROUP, groupCode);
        List<Event> events = eventRepository.findUpcomingEventsByGroupId(groupId, LocalDate.now());
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update event
     */
    @Transactional
    public EventDTO updateEvent(String eventCode, String userCode, UpdateEventRequest request) {
        Event event = getEventEntityByCode(eventCode);
        User user = userService.getUserByShortCode(userCode);

        // Check authorization
        if (!canManageEvent(user.getId(), event.getId())) {
            throw new RuntimeException("Unauthorized to update this event");
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getEventTime() != null) event.setEventTime(request.getEventTime());
        if (request.getLocation() != null) event.setLocation(request.getLocation());

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toDTO(updatedEvent);
    }

    /**
     * Delete event
     */
    @Transactional
    public void deleteEvent(String eventCode, String userCode) {
        Event event = getEventEntityByCode(eventCode);
        User user = userService.getUserByShortCode(userCode);

        boolean isCommOwner = isCommunityOwner(user.getId(), event.getCommunity().getId());
        boolean isCommAdmin = isCommunityAdmin(user.getId(), event.getCommunity().getId());
        boolean isCreator = event.getCreatedBy().getId().equals(user.getId());

        // Community Owner can delete any event in community
        // Community Admin can delete group events where they are admin
        // Creator can delete
        if (isCommOwner || isCreator || (isCommAdmin && canManageEvent(user.getId(), event.getId()))) {
            eventRepository.delete(event);
        } else {
            throw new RuntimeException("Unauthorized to delete this event");
        }
    }

    /**
     * Toggle attendance for event
     */
    @Transactional
    public EventDTO toggleAttendance(String eventCode, String userCode, boolean enabled) {
        Event event = getEventEntityByCode(eventCode);
        User user = userService.getUserByShortCode(userCode);

        if (!canManageEvent(user.getId(), event.getId())) {
            throw new RuntimeException("Unauthorized to modify attendance");
        }

        event.setAttendanceEnabled(enabled);
        Event updatedEvent = eventRepository.save(event);

        if (enabled && event.getAttendances().isEmpty()) {
            initializeAttendanceForEvent(event);
        }

        return eventMapper.toDTO(updatedEvent);
    }

    /**
     * Mark attendance
     */
    @Transactional
    public EventAttendanceDTO markAttendance(String eventCode, String markerCode, MarkAttendanceRequest request) {
        Event event = getEventEntityByCode(eventCode);
        User marker = userService.getUserByShortCode(markerCode);

        if (!event.getAttendanceEnabled()) {
            throw new RuntimeException("Attendance is not enabled");
        }

        // Check if marker has permission
        if (!canManageEvent(marker.getId(), event.getId())) {
            throw new RuntimeException("Only admins can mark attendance");
        }

        User user = userService.getUserByShortCode(request.getUserCode());
        Group group = null;

        if (request.getType() == DatabaseType.GROUP) {
            if (request.getGroupCode() == null || request.getGroupCode().isEmpty()) {
                throw new RuntimeException("Group code is required for group-based attendance");
            }
            UUID groupId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.GROUP, request.getGroupCode());
            group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
        }

        final Group finalGroup = group;
        EventAttendance attendance = attendanceRepository
                .findByEventAndUserAndGroup(event, user, finalGroup)
                .orElseGet(() -> {
                    EventAttendance newAttendance = new EventAttendance();
                    newAttendance.setEvent(event);
                    newAttendance.setUser(user);
                    newAttendance.setGroup(finalGroup);
                    return newAttendance;
                });

        attendance.setStatus(request.getStatus());
        attendance.setMarkedBy(marker);
        attendance.setMarkedAt(LocalDateTime.now());

        EventAttendance savedAttendance = attendanceRepository.save(attendance);
        return eventMapper.toAttendanceDTO(savedAttendance);
    }

    /**
     * Get event attendance
     */
    public List<EventAttendanceDTO> getEventAttendance(String eventCode) {
        Event event = getEventEntityByCode(eventCode);
        return attendanceRepository.findByEvent(event).stream()
                .map(eventMapper::toAttendanceDTO).collect(Collectors.toList());
    }

    /**
     * Get event attendance statistics
     */
    public AttendanceStatsDTO getAttendanceStats(String eventCode) {
        Event event = getEventEntityByCode(eventCode);
        int total = attendanceRepository.findByEvent(event).size();
        int present = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT);
        int absent = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.ABSENT);
        int pending = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PENDING);
        return new AttendanceStatsDTO(total, present, absent, pending);
    }

    /**
     * Initialize attendance records for all members (Group or Community)
     */
    @Transactional
    public void initializeAttendanceForEvent(Event event) {
        if (event.getGroup() != null) {
            // Group event
            List<GroupMembership> memberships = groupMembershipRepository.findByGroupIdAndStatus(event.getGroup().getId(), MembershipStatus.ACCEPTED);
            for (GroupMembership membership : memberships) {
                createAttendanceIfNotExist(event, membership.getUser(), event.getGroup());
            }
        } else {
            // Community-wide event (Notice)
            List<CommunityMembership> memberships = communityMembershipRepository.findByCommunityIdAndStatus(event.getCommunity().getId(), MembershipStatus.ACCEPTED);
            for (CommunityMembership membership : memberships) {
                createAttendanceIfNotExist(event, membership.getUser(), null);
            }
        }
    }

    private void createAttendanceIfNotExist(Event event, User user, Group group) {
        if (!attendanceRepository.existsByEventIdAndUserIdAndGroupId(event.getId(), user.getId(), group != null ? group.getId() : null)) {
            EventAttendance attendance = new EventAttendance();
            attendance.setEvent(event);
            attendance.setUser(user);
            attendance.setGroup(group);
            attendance.setStatus(AttendanceStatus.PENDING);
            attendanceRepository.save(attendance);
        }
    }

    // Helper methods
    private Event getEventEntityByCode(String eventCode) {
        UUID eventId = globalShortCodeService.getUUIDfromShortCode(DatabaseType.EVENTS, eventCode);
        return eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private boolean canManageEvent(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        
        // Community Owner can manage everything
        if (isCommunityOwner(userId, event.getCommunity().getId())) return true;
        
        // Creator can manage
        if (event.getCreatedBy().getId().equals(userId)) return true;

        if (event.getGroup() != null) {
            // Group Admin/Owner can manage
            return groupMembershipRepository.findByUserIdAndGroupIdAndStatus(userId, event.getGroup().getId(), MembershipStatus.ACCEPTED)
                    .map(m -> m.getRole() == MemberRole.ADMIN || m.getRole() == MemberRole.OWNER)
                    .orElse(false);
        } else {
            // Community Admin can manage community events
            return isCommunityAdmin(userId, event.getCommunity().getId());
        }
    }

    private boolean isCommunityOwner(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER).orElse(false);
    }

    private boolean isCommunityAdmin(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.ADMIN).orElse(false);
    }

    private boolean isCommunityAdminOrOwner(UUID userId, UUID communityId) {
        return communityMembershipRepository.findByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.ACCEPTED)
                .map(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.ADMIN).orElse(false);
    }

    public List<EventAttendanceDTO> getEventAttendanceByGroup(String eventCode, String groupCode) {
        List<EventAttendanceDTO> eventAttendanceDTOS = getEventAttendance(eventCode);

        return eventAttendanceDTOS.stream()
                .filter(dto -> dto.getGroupCode() != null && dto.getGroupCode().equals(groupCode))
                .toList(); // or collect(Collectors.toList()) if using Java < 16
    }
}
