package cryss.dev.rabbit_basic.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class ExcelCreatedEvent {

    @Setter
    private String id;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now ();

    public ExcelCreatedEvent() {
    }

    public ExcelCreatedEvent(String id) {
        this.id = id;
    }

}
