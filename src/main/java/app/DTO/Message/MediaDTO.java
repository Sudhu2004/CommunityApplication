package app.DTO.Message;
import app.Database.MediaType;
import java.util.UUID;

public class MediaDTO {
    private UUID id;
    private MediaType mediaType;
    private String url;
    private Long sizeInBytes;
    private Integer width;
    private Integer height;
    private Integer durationInSeconds;

    public MediaDTO() {
    }

    public MediaDTO(UUID id, MediaType mediaType, String url, Long sizeInBytes,
                    Integer width, Integer height, Integer durationInSeconds) {
        this.id = id;
        this.mediaType = mediaType;
        this.url = url;
        this.sizeInBytes = sizeInBytes;
        this.width = width;
        this.height = height;
        this.durationInSeconds = durationInSeconds;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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
