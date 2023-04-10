package bot.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TradeDto {

    String date;

    String name;

    String symbol;

    Double balanceBefore;

    Double balanceAfter;

    Double profit;

    Double fundingRate;

}
