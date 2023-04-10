package bot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document("fundings")
public class Funding {

    @Id
    String id;

    String date;

    String symbol;

    Double rate;

    Double stretch;

    Boolean skip;

}
