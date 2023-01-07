package bot.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ProfitLevel {
    LOW(0.001, 0.01),
    MEDIUM(0.002, 0.015),
    HIGH(0.003, 1.0);

    Double profit;
    Double funding;

}
