package bot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document("credentials")
public class Credentials {

    @Id
    String id;

    String secret;

    String key;

    String name;

    Double percentage;

}
