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

import java.util.ArrayList;
import java.util.List;
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
        ArrayList<StatisticsDto> statistics = new ArrayList<>();
        Double profitSum = tradeRepository.findAll().stream()
                .map(Trade::getProfit)
                .mapToDouble(Double::doubleValue)
                .sum();
        statistics.add(new StatisticsDto("Denys", profitSum));
        return statistics;
    }

    @Override
    public List<TradeDto> getTrades() {
        return tradeRepository.findAll().stream()
                .map(TradeMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundingDto> getFundings() {
        return fundingRepository.findAll().stream()
                .map(FundingMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
    }
}
