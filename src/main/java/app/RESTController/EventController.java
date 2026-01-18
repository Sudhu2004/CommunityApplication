package app.RESTController;

import app.DTO.Event.*;
import app.Service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader("User-Id") UUID userId) {
        EventDTO event = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    /**
     * GET /api/events/{eventId}
     * Get an event by ID
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable UUID eventId) {
        EventDTO event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * GET /api/events/community/{communityId}
     * Get all events in a community
     */
    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<EventDTO>> getEventsByCommunity(
            @PathVariable UUID communityId) {
        List<EventDTO> events = eventService.getEventsByCommunity(communityId);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/community/{communityId}/upcoming
     * Get upcoming events in a community
     */
    @GetMapping("/community/{communityId}/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEventsByCommunity(
            @PathVariable UUID communityId) {
        List<EventDTO> events = eventService.getUpcomingEventsByCommunity(communityId);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/group/{groupId}
     * Get all events in a group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EventDTO>> getEventsByGroup(
            @PathVariable UUID groupId) {
        List<EventDTO> events = eventService.getEventsByGroup(groupId);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/group/{groupId}/upcoming
     * Get upcoming events in a group
     */
    @GetMapping("/group/{groupId}/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEventsByGroup(
            @PathVariable UUID groupId) {
        List<EventDTO> events = eventService.getUpcomingEventsByGroup(groupId);
        return ResponseEntity.ok(events);
    }

    /**
     * PUT /api/events/{eventId}
     * Update an event
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader("User-Id") UUID userId) {
        EventDTO event = eventService.updateEvent(eventId, userId, request);
        return ResponseEntity.ok(event);
    }

    /**
     * DELETE /api/events/{eventId}
     * Delete an event
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID eventId,
            @RequestHeader("User-Id") UUID userId) {
        eventService.deleteEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/events/{eventId}/attendance
     * Toggle attendance for an event
     */
    @PatchMapping("/{eventId}/attendance")
    public ResponseEntity<EventDTO> toggleAttendance(
            @PathVariable UUID eventId,
            @RequestParam("enabled") boolean enabled,
            @RequestHeader("User-Id") UUID userId) {
        EventDTO event = eventService.toggleAttendance(eventId, userId, enabled);
        return ResponseEntity.ok(event);
    }

// ========== ATTENDANCE ENDPOINTS ==========

    /**
     * POST /api/events/{eventId}/attendance
     * Mark attendance for a user
     */
    @PostMapping("/{eventId}/attendance")
    public ResponseEntity<EventAttendanceDTO> markAttendance(
            @PathVariable UUID eventId,
            @Valid @RequestBody MarkAttendanceRequest request,
            @RequestHeader("User-Id") UUID userId) {
        EventAttendanceDTO attendance = eventService.markAttendance(eventId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    /**
     * GET /api/events/{eventId}/attendance
     * Get all attendance records for an event
     */
    @GetMapping("/{eventId}/attendance")
    public ResponseEntity<List<EventAttendanceDTO>> getEventAttendance(
            @PathVariable UUID eventId) {
        List<EventAttendanceDTO> attendance = eventService.getEventAttendance(eventId);
        return ResponseEntity.ok(attendance);
    }

    /**
     * GET /api/events/{eventId}/attendance/group/{groupId}
     * Get attendance records for an event by group
     */
    @GetMapping("/{eventId}/attendance/group/{groupId}")
    public ResponseEntity<List<EventAttendanceDTO>> getEventAttendanceByGroup(
            @PathVariable UUID eventId,
            @PathVariable UUID groupId) {
        List<EventAttendanceDTO> attendance = eventService.getEventAttendanceByGroup(eventId, groupId);
        return ResponseEntity.ok(attendance);
    }

    /**
     * GET /api/events/{eventId}/attendance/stats
     * Get attendance statistics for an event
     */
    @GetMapping("/{eventId}/attendance/stats")
    public ResponseEntity<AttendanceStatsDTO> getAttendanceStats(
            @PathVariable UUID eventId) {
        AttendanceStatsDTO stats = eventService.getAttendanceStats(eventId);
        return ResponseEntity.ok(stats);
    }
}
