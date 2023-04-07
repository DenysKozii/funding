package bot.dto;

import bot.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TradeDto {

    String date;

    User user;

    Double balanceBefore;

    Double balanceAfter;

    Double profit;

}
