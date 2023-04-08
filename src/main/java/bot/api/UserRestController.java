package bot.api;

import bot.dto.CredentialsDto;
import bot.dto.FundingDto;
import bot.dto.TradeDto;
import bot.service.ConnectionService;
import bot.service.TradingService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/v1")
public class UserRestController {

    ConnectionService connectionService;
    TradingService tradingService;

    @PostMapping("credentials")
    String addCredentials(@RequestBody CredentialsDto credentialsDto) {
        return connectionService.addCredentials(credentialsDto);
    }

    @GetMapping("reconnect")
    void reconnect() {
        tradingService.reconnectSocket();
    }

    @GetMapping("trades")
    List<TradeDto> getTrades() {
        return tradingService.getTrades();
    }

    @GetMapping("fundings")
    List<FundingDto> getFundingss() {
        return tradingService.getFundings();
    }

}
