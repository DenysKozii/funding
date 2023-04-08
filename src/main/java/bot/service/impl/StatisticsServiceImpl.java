package bot.service.impl;

import bot.dto.StatisticsDto;
import bot.service.StatisticsService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public List<StatisticsDto> getStatistics() {
        return null;
    }
}
