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
            @RequestHeader("userCode") String userCode) {
        try {
            MessageDTO message = messageService.createMessage(request, userCode);
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

    @GetMapping("/event/{eventCode}")
    public ResponseEntity<Page<MessageDTO>> getMessagesByEventCode(
            @PathVariable String eventCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageDTO> messages = messageService.getMessagesByEventCode(eventCode, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventCode}/all")
    public ResponseEntity<List<MessageDTO>> getAllMessagesByEventCode(@PathVariable String eventCode) {
        List<MessageDTO> messages = messageService.getAllMessagesByEventCode(eventCode);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventCode}/user/{userCode}")
    public ResponseEntity<List<MessageDTO>> getMessagesByEventCodeAndUserCode(
            @PathVariable String eventCode,
            @PathVariable String userCode) {
        List<MessageDTO> messages = messageService.getMessagesByEventCodeAndUserCode(eventCode, userCode);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventCode}/count")
    public ResponseEntity<Long> getMessageCount(@PathVariable String eventCode) {
        Long count = messageService.getMessageCountByEventCode(eventCode);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/community/{communityCode}")
    public ResponseEntity<Page<MessageDTO>> getMessagesByCommunityCode(
            @PathVariable String communityCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageDTO> messages = messageService.getMessagesByCommunityCode(communityCode, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/community/{communityCode}/all")
    public ResponseEntity<List<MessageDTO>> getAllMessagesByCommunityCode(@PathVariable String communityCode) {
        List<MessageDTO> messages = messageService.getAllMessagesByCommunityCode(communityCode);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/community/{communityCode}/count")
    public ResponseEntity<Long> getCommunityMessageCount(@PathVariable String communityCode) {
        Long count = messageService.getMessageCountByCommunityCode(communityCode);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestHeader("userCode") String userCode) {
        try {
            messageService.deleteMessage(messageId, userCode);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
