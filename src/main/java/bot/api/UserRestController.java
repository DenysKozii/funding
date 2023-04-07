package bot.api;

import bot.dto.CredentialsDto;
import bot.dto.FundingDto;
import bot.dto.TradeDto;
import bot.service.Connection;
import bot.service.Trading;
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

    Connection connection;
    Trading trading;

    @GetMapping
    public String alive() {
        return "alive";
    }

    @PostMapping("credentials")
    public String addCredentials(@RequestBody CredentialsDto credentialsDto) {
        return connection.addCredentials(credentialsDto);
    }

    @GetMapping("reconnect")
    public void reconnect() {
        trading.reconnectSocket();
    }

    @GetMapping("trades")
    public List<TradeDto> getTrades() {
        return trading.getTrades();
    }

    @GetMapping("fundings")
    public List<FundingDto> getFundingss() {
        return trading.getFundings();
    }

}
