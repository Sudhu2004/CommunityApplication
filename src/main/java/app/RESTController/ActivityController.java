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

    // GET /api/activities?type=USER
    @GetMapping
    public ResponseEntity<List<Activity>> getByType(
            @RequestParam DatabaseType type) {
        return ResponseEntity.ok(activityService.getActivitiesByType(type));
    }

    // GET /api/activities/{referenceId}?type=GROUP
    @GetMapping("/{referenceId}")
    public ResponseEntity<List<Activity>> getByTypeAndReferenceId(
            @PathVariable UUID referenceId,
            @RequestParam DatabaseType type) {
        return ResponseEntity.ok(activityService.getActivitiesByTypeAndReferenceId(type, referenceId));
    }

    // GET /api/activities/{referenceId}/all
    @GetMapping("/{referenceId}/all")
    public ResponseEntity<List<Activity>> getAllByReferenceId(
            @PathVariable UUID referenceId) {
        return ResponseEntity.ok(activityService.getActivitiesByReferenceId(referenceId));
    }
}