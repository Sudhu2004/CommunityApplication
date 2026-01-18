package app.RESTController;

import app.DTO.Message.CreateMessageRequest;
import app.DTO.Message.MessageDTO;
import app.Service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        try {
            MessageDTO message = messageService.createMessage(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable UUID messageId) {
        try {
            MessageDTO message = messageService.getMessageById(messageId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Page<MessageDTO>> getMessagesByEventId(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageDTO> messages = messageService.getMessagesByEventId(eventId, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventId}/all")
    public ResponseEntity<List<MessageDTO>> getAllMessagesByEventId(@PathVariable UUID eventId) {
        List<MessageDTO> messages = messageService.getAllMessagesByEventId(eventId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByEventIdAndUserId(
            @PathVariable UUID eventId,
            @PathVariable UUID userId) {
        List<MessageDTO> messages = messageService.getMessagesByEventIdAndSenderId(eventId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> getMessageCount(@PathVariable UUID eventId) {
        Long count = messageService.getMessageCountByEventId(eventId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestHeader("X-User-Id") UUID userId) {
        try {
            messageService.deleteMessage(messageId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
