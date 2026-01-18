package app.DTO.Event;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateEventRequest {

    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private LocalDate eventDate;
    private LocalTime eventTime;

    @Size(max = 500, message = "Location must be less than 500 characters")
    private String location;

    // Constructors
    public UpdateEventRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
