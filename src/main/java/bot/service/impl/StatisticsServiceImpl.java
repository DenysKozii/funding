package bot.service.impl;

import bot.dto.FundingDto;
import bot.dto.StatisticsDto;
import bot.dto.TradeDto;
import bot.entity.Trade;
import bot.mapper.FundingMapper;
import bot.mapper.TradeMapper;
import bot.repository.FundingRepository;
import bot.repository.TradeRepository;
import bot.service.StatisticsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticsServiceImpl implements StatisticsService {

    TradeRepository tradeRepository;
    FundingRepository fundingRepository;

    @Override
    public List<StatisticsDto> getStatistics() {
        HashMap<String, StatisticsDto> statisticsMap = new HashMap<>();
        for (Trade trade : tradeRepository.findAll()) {
            String name = trade.getName();
            StatisticsDto previousStatistics = statisticsMap.get(name);
            StatisticsDto statistics = StatisticsDto.builder()
                    .name(name)
                    .tradesAmount(1)
                    .tradesPositive(trade.getProfit() >= 0 ? 1 : 0)
                    .tradesNegative(trade.getProfit() < 0 ? 1 : 0)
                    .totalProfit(trade.getProfit())
                    .build();

            if (previousStatistics != null) {
                statistics.setTradesAmount(previousStatistics.getTradesAmount() + 1);
                statistics.setTradesPositive(trade.getProfit() >= 0 ?
                        previousStatistics.getTradesPositive() + 1 :
                        previousStatistics.getTradesPositive());
                statistics.setTradesNegative(trade.getProfit() < 0 ?
                        previousStatistics.getTradesNegative() + 1 :
                        previousStatistics.getTradesNegative());
                statistics.setTotalProfit(previousStatistics.getTotalProfit() + statistics.getTotalProfit());
            }
            statisticsMap.put(name, statistics);
        }
        return new ArrayList<>(statisticsMap.values());
    }

    @Override
    public List<TradeDto> getTrades() {
        return tradeRepository.findAll().stream()
                .map(TradeMapper.INSTANCE::mapToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        l -> {
                            Collections.reverse(l);
                            return l;
                        }));
    }

    @Override
    public List<FundingDto> getFundings() {
        return fundingRepository.findAll().stream()
                .map(FundingMapper.INSTANCE::mapToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        l -> {
                            Collections.reverse(l);
                            return l;
                        }));
    }

}
