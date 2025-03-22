package cryss.dev.rabbit_basic.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;


@Data
public class ExcelCreatedEvent implements Serializable {

    private String id;
    private String path;

    @JsonSerialize(as = LocalDateTime.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING ,pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now ();

    public ExcelCreatedEvent() {
    }

    public ExcelCreatedEvent(String id) {
        this.id = id;
    }

}
