package bot.dto;

import com.binance.client.model.enums.OrderSide;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogDto {

    String date;

    String name;

    String symbol;

    Double rate;

    Double price;

    Double responsePrice;

    OrderStatus orderStatus;

    OrderSide orderSide;

    Double changePercents;

    Double accountBalance;

}
