package Host_Usach_Cloud.Backend.Mongo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("alerts")
public class AlertDocument {

    @Id
    private String id;

    private Long userId;

    private String alertType;

    private String message;

    private LocalDateTime timestamp;

    private boolean read;
}
