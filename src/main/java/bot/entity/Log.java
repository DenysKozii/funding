package bot.entity;

import bot.dto.OrderStatus;
import com.binance.client.model.enums.OrderSide;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document("logs")
public class Log {

    @Id
    String date;

    String symbol;

    Double rate;

    Double price;

    OrderStatus orderStatus;

    OrderSide orderSide;

    Double accountBalance;

}