package bot.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StatisticsDto {

    String name;

    Double totalProfit;

    Integer tradesAmount;

    Integer tradesPositive;

    Integer tradesNegative;

}
