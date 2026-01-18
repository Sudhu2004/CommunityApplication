package app.DTO.Message;

import app.Database.Event;
import app.Database.Media;
import app.Database.Message;
import app.Database.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setEventId(message.getEvent() != null ? message.getEvent().getId() : null);
        dto.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
        dto.setSenderName(message.getSender() != null ? message.getSender().getName() : null);
        dto.setSenderProfilePhotoUrl(message.getSender() != null ? message.getSender().getProfilePhotoUrl() : null);
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());

        if (message.getMediaList() != null) {
            dto.setMediaList(message.getMediaList().stream()
                    .map(this::mediaToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Message toEntity(CreateMessageRequest request, Event event, User sender) {
        if (request == null) {
            return null;
        }

        Message message = new Message();
        message.setEvent(event);
        message.setSender(sender);
        message.setType(request.getType());
        message.setContent(request.getContent());

        if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
            List<Media> mediaList = request.getMediaList().stream()
                    .map(mediaRequest -> mediaRequestToEntity(mediaRequest, message))
                    .collect(Collectors.toList());
            message.setMediaList(mediaList);
        }

        return message;
    }

    public MediaDTO mediaToDTO(Media media) {
        if (media == null) {
            return null;
        }

        return new MediaDTO(
                media.getId(),
                media.getMediaType(),
                media.getUrl(),
                media.getSizeInBytes(),
                media.getWidth(),
                media.getHeight(),
                media.getDurationInSeconds()
        );
    }

    private Media mediaRequestToEntity(CreateMessageRequest.MediaRequest request, Message message) {
        if (request == null) {
            return null;
        }

        Media media = new Media();
        media.setMediaType(request.getMediaType());
        media.setUrl(request.getUrl());
        media.setSizeInBytes(request.getSizeInBytes());
        media.setWidth(request.getWidth());
        media.setHeight(request.getHeight());
        media.setDurationInSeconds(request.getDurationInSeconds());
        media.setMessage(message);

        return media;
    }

    public List<MessageDTO> toDTOList(List<Message> messages) {
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
