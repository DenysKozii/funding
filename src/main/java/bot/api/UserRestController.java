package bot.api;

import bot.dto.CredentialsDto;
import bot.dto.LogDto;
import bot.service.Connection;
import bot.service.Trade;
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
    Trade trade;

    @GetMapping
    public String alive() {
        return "alive";
    }

    @GetMapping("logs")
    public List<LogDto> getLogs() {
        return trade.getLogs();
    }

    @PostMapping("credentials")
    public String addCredentials(@RequestBody CredentialsDto credentialsDto) {
        return connection.addCredentials(credentialsDto);
    }

}
