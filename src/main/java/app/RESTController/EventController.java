package app.RESTController;

import app.DTO.Event.*;
import app.Service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader("userCode") String userCode) {
        EventDTO event = eventService.createEvent(userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    /**
     * GET /api/events/{eventCode}
     * Get an event by ID
     */
    @GetMapping("/{eventCode}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable String eventCode) {
        EventDTO event = eventService.getEventByCode(eventCode);
        return ResponseEntity.ok(event);
    }

    /**
     * GET /api/events/community/{communityCode}
     * Get all events in a community
     */
    @GetMapping("/community/{communityCode}")
    public ResponseEntity<List<EventDTO>> getEventsByCommunity(
            @PathVariable String communityCode) {
        List<EventDTO> events = eventService.getEventsByCommunity(communityCode);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/community/{communityCode}/upcoming
     * Get upcoming events in a community
     */
    @GetMapping("/community/{communityCode}/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEventsByCommunity(
            @PathVariable String communityCode) {
        List<EventDTO> events = eventService.getUpcomingEventsByCommunity(communityCode);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/group/{groupCode}
     * Get all events in a group
     */
    @GetMapping("/group/{groupCode}")
    public ResponseEntity<List<EventDTO>> getEventsByGroup(
            @PathVariable String groupCode) {
        List<EventDTO> events = eventService.getEventsByGroup(groupCode);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/events/group/{groupCode}/upcoming
     * Get upcoming events in a group
     */
    @GetMapping("/group/{groupCode}/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEventsByGroup(
            @PathVariable String groupCode) {
        List<EventDTO> events = eventService.getUpcomingEventsByGroup(groupCode);
        return ResponseEntity.ok(events);
    }

    /**
     * PUT /api/events/{eventCode}
     * Update an event
     */
    @PutMapping("/{eventCode}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable String eventCode,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader("userCode") String userCode) {
        EventDTO event = eventService.updateEvent(eventCode, userCode, request);
        return ResponseEntity.ok(event);
    }

    /**
     * DELETE /api/events/{eventCode}
     * Delete an event
     */
    @DeleteMapping("/{eventCode}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String eventCode,
            @RequestHeader("userCode") String userCode) {
        eventService.deleteEvent(eventCode, userCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/events/{eventCode}/attendance
     * Toggle attendance for an event
     */
    @PatchMapping("/{eventCode}/attendance")
    public ResponseEntity<EventDTO> toggleAttendance(
            @PathVariable String eventCode,
            @RequestParam("enabled") boolean enabled,
            @RequestHeader("userCode") String userCode) {
        EventDTO event = eventService.toggleAttendance(eventCode, userCode, enabled);
        return ResponseEntity.ok(event);
    }

// ========== ATTENDANCE ENDPOINTS ==========

    /**
     * POST /api/events/{eventCode}/attendance
     * Mark attendance for a user
     */
    @PostMapping("/{eventCode}/attendance")
    public ResponseEntity<EventAttendanceDTO> markAttendance(
            @PathVariable String eventCode,
            @Valid @RequestBody MarkAttendanceRequest request,
            @RequestHeader("userCode") String userCode) {
        EventAttendanceDTO attendance = eventService.markAttendance(eventCode, userCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    /**
     * GET /api/events/{eventCode}/attendance
     * Get all attendance records for an event
     */
    @GetMapping("/{eventCode}/attendance")
    public ResponseEntity<List<EventAttendanceDTO>> getEventAttendance(
            @PathVariable String eventCode) {
        List<EventAttendanceDTO> attendance = eventService.getEventAttendance(eventCode);
        return ResponseEntity.ok(attendance);
    }

    /**
     * GET /api/events/{eventCode}/attendance/group/{groupCode}
     * Get attendance records for an event by group
     */
    @GetMapping("/{eventCode}/attendance/group/{groupCode}")
    public ResponseEntity<List<EventAttendanceDTO>> getEventAttendanceByGroup(
            @PathVariable String eventCode,
            @PathVariable String groupCode) {
        List<EventAttendanceDTO> attendance = eventService.getEventAttendanceByGroup(eventCode, groupCode);
        return ResponseEntity.ok(attendance);
    }

    /**
     * GET /api/events/{eventCode}/attendance/stats
     * Get attendance statistics for an event
     */
    @GetMapping("/{eventCode}/attendance/stats")
    public ResponseEntity<AttendanceStatsDTO> getAttendanceStats(
            @PathVariable String eventCode) {
        AttendanceStatsDTO stats = eventService.getAttendanceStats(eventCode);
        return ResponseEntity.ok(stats);
    }
}
