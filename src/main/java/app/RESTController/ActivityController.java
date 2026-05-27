package app.RESTController;

import app.Database.Activity;
import app.Database.DatabaseType;
import app.Service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private app.Service.GlobalShortCodeService shortCodeService;

    // GET /api/activities?type=USER
    @GetMapping
    public ResponseEntity<List<Activity>> getByType(
            @RequestParam DatabaseType type) {
        return ResponseEntity.ok(activityService.getActivitiesByType(type));
    }

    // GET /api/activities/{referenceId}?type=GROUP
    @GetMapping("/{referenceId}")
    public ResponseEntity<List<Activity>> getByTypeAndReferenceId(
            @PathVariable String referenceId,
            @RequestParam DatabaseType type) {
        
        UUID uuid;
        try {
            uuid = UUID.fromString(referenceId);
        } catch (IllegalArgumentException e) {
            // Not a UUID, try to resolve as short code
            uuid = shortCodeService.getUUIDfromShortCode(type, referenceId);
        }
        
        return ResponseEntity.ok(activityService.getActivitiesByTypeAndReferenceId(type, uuid));
    }

    // GET /api/activities/{referenceId}/all
    @GetMapping("/{referenceId}/all")
    public ResponseEntity<List<Activity>> getAllByReferenceId(
            @PathVariable String referenceId) {
        
        UUID uuid;
        try {
            uuid = UUID.fromString(referenceId);
        } catch (IllegalArgumentException e) {
            // No type here, so we use the generic one
            uuid = shortCodeService.getUUIDfromShortCode(referenceId);
        }
        return ResponseEntity.ok(activityService.getActivitiesByReferenceId(uuid));
    }
}