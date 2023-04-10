package bot.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document("trades")
public class Trade {

    @Id
    String id;

    String date;

    String name;

    String symbol;

    Double balanceBefore;

    Double balanceAfter;

    Double profit;

    Double fundingRate;

}
