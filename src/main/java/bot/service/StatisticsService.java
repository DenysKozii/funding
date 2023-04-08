package bot.service;

import bot.dto.FundingDto;
import bot.dto.StatisticsDto;
import bot.dto.TradeDto;

import java.util.List;

public interface StatisticsService {

    List<StatisticsDto> getStatistics();

    List<TradeDto> getTrades();

    List<FundingDto> getFundings();

}
