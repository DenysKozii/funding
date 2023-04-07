package bot.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundingDto {

    String date;

    String symbol;

    Double value;

    Boolean skip;

}
