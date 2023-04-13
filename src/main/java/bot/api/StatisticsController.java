package bot.api;

import bot.service.StatisticsService;
import bot.service.TradingService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping
public class StatisticsController {

    StatisticsService statisticsService;
    TradingService tradingService;

    @GetMapping("statistics")
    String getStatistics(Model model) {
        model.addAttribute("statistics", statisticsService.getStatistics());
        return "index";
    }

    @GetMapping("trades")
    String getTrades(Model model) {
        model.addAttribute("trades", statisticsService.getTrades());
        return "trades";
    }

    @GetMapping
    String getFundings(Model model) {
        tradingService.reconnectSocket();
        model.addAttribute("fundings", statisticsService.getFundings());
        List<String> funding = tradingService.getFunding();
        model.addAttribute("symbol", funding.get(0));
        model.addAttribute("rate",Double.parseDouble(funding.get(1)) * 100);
        return "fundings";
    }

}
