package tech.metavm.message.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.message.MessageManager;
import tech.metavm.message.rest.dto.MessageDTO;
import tech.metavm.message.rest.dto.MessageQuery;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageManager messageManager;

    public MessageController(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageManager.getUnreadCount());
    }

    @PostMapping("/{id}/mark-read")
    public Result<Void> read(@PathVariable("id") String id) {
        messageManager.read(id);
        return Result.voidSuccess();
    }

    @PostMapping("/query")
    public Result<Page<MessageDTO>> query(@RequestBody MessageQuery query) {
        return Result.success(messageManager.query(query));
    }

}
