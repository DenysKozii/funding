package bot.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ProfitLevel {
    REJECT(0.0, 0.004),
    LOW(0.0009, 0.01),
    MEDIUM(0.0017, 0.015),
    HIGH(0.0025, 1.0);

    Double profit;
    Double funding;

    public static ProfitLevel getProfitLevel(double funding) {
        if (funding < REJECT.funding) {
          return REJECT;
        } else if (funding < LOW.funding) {
            return LOW;
        } else if (funding < MEDIUM.funding) {
            return MEDIUM;
        }
        return HIGH;
    }

}
