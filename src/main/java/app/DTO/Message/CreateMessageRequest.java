package app.DTO.Message;

import app.Database.MediaType;
import app.Database.MessageType;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateMessageRequest {
    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Message type is required")
    private MessageType type;

    private String content;

    private List<MediaRequest> mediaList = new ArrayList<>();

    public CreateMessageRequest() {
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<MediaRequest> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaRequest> mediaList) {
        this.mediaList = mediaList;
    }

    public static class MediaRequest {
        @NotNull(message = "Media type is required")
        private MediaType mediaType;

        @NotNull(message = "URL is required")
        private String url;

        private Long sizeInBytes;
        private Integer width;
        private Integer height;
        private Integer durationInSeconds;

        public MediaRequest() {
        }

        // Getters and Setters
        public MediaType getMediaType() {
            return mediaType;
        }

        public void setMediaType(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Long getSizeInBytes() {
            return sizeInBytes;
        }

        public void setSizeInBytes(Long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Integer getDurationInSeconds() {
            return durationInSeconds;
        }

        public void setDurationInSeconds(Integer durationInSeconds) {
            this.durationInSeconds = durationInSeconds;
        }
    }
}
