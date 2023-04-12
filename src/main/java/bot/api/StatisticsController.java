package bot.api;

import bot.service.StatisticsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping
public class StatisticsController {

    StatisticsService statisticsService;

    @GetMapping
    String getStatistics(Model model) {
        model.addAttribute("statistics", statisticsService.getStatistics());
        return "index";
    }

    @GetMapping("trades")
    String getTrades(Model model) {
        model.addAttribute("trades", statisticsService.getTrades());
        return "trades";
    }

    @GetMapping("fundings")
    String getFundings(Model model) {
        model.addAttribute("fundings", statisticsService.getFundings());
        return "fundings";
    }

}
