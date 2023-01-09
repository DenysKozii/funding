package bot.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum LeverageLevel {
    LEVERAGE_15(15, 300),
    LEVERAGE_10(10, 2500),
    LEVERAGE_8(8, 12500),
    LEVERAGE_5(5, 50000),
    LEVERAGE_2(2, 500000);

    Integer value;
    Integer limit;

    public static Integer getLeverage(double accountBalance){
        if (accountBalance < LEVERAGE_15.getLimit()) {
            return LEVERAGE_15.value;
        } else if (accountBalance < LEVERAGE_10.getLimit()) {
            return LEVERAGE_10.value;
        } else if (accountBalance < LEVERAGE_8.getLimit()) {
            return LEVERAGE_8.value;
        } else if (accountBalance < LEVERAGE_5.getLimit()) {
            return LEVERAGE_5.value;
        }
        return LEVERAGE_2.value;
    }
}
