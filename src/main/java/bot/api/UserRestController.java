package bot.api;

import bot.dto.CredentialsDto;
import bot.service.ConnectionService;
import bot.service.TradingService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/v1")
public class UserRestController {

    ConnectionService connectionService;
    TradingService tradingService;

    @PostMapping("register")
    String register(@RequestBody CredentialsDto credentialsDto) {
        return connectionService.register(credentialsDto);
    }

    @PatchMapping("percentage")
    boolean setPercentage(@RequestParam String name, @RequestParam Double percentage) {
        return connectionService.setPercentage(name, percentage);
    }

    @GetMapping("reconnect")
    void reconnect() {
        tradingService.reconnectSocket();
    }

}
