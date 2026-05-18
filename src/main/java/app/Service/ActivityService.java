package app.Service;

import app.Database.Activity;
import app.Database.DatabaseType;
import app.Repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // Record a new activity — call this anywhere instead of sending a system message
    public void record(DatabaseType type, UUID referenceId, String message) {
        Activity activity = new Activity();
        activity.setType(type);
        activity.setReferenceId(referenceId);
        activity.setMessage(message);

        activityRepository.save(activity);
    }

    public List<Activity> getActivitiesByType(DatabaseType type) {
        return activityRepository.findByType(type);
    }

    public List<Activity> getActivitiesByTypeAndReferenceId(DatabaseType type, UUID referenceId) {
        return activityRepository.findByTypeAndReferenceId(type, referenceId);
    }

    public List<Activity> getActivitiesByReferenceId(UUID referenceId) {
        return activityRepository.findByReferenceId(referenceId);
    }
}