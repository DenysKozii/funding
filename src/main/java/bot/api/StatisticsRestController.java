package bot.api;

import bot.dto.StatisticsDto;
import bot.service.StatisticsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/v1/statistics")
public class StatisticsRestController {

    StatisticsService statisticsService;

    @GetMapping
    List<StatisticsDto> getStatistics() {
        return statisticsService.getStatistics();
    }

}
