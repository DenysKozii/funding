package bot.service.impl;

import bot.dto.StatisticsDto;
import bot.entity.Trade;
import bot.repository.TradeRepository;
import bot.service.StatisticsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticsServiceImpl implements StatisticsService {

    TradeRepository tradeRepository;

    @Override
    public List<StatisticsDto> getStatistics() {
        ArrayList<StatisticsDto> statistics = new ArrayList<>();
        Double profitSum = tradeRepository.findAll().stream()
                .map(Trade::getProfit)
                .mapToDouble(Double::doubleValue)
                .sum();
        statistics.add(new StatisticsDto("Denys", profitSum));
        return statistics;
    }
}
