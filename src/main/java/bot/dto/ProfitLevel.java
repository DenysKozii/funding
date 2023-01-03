package bot.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfitLevel {
    LOW(0.001, 0.01),
    MEDIUM(0.002, 0.015),
    HIGH(0.003, 1.0);

    private final Double profit;
    private final Double funding;

}
