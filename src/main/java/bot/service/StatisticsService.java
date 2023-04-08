package bot.service;

import bot.dto.StatisticsDto;

import java.util.List;

public interface StatisticsService {

    List<StatisticsDto> getStatistics();

}
