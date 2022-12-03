package bot.api;

import bot.dto.CredentialsDto;
import bot.service.Connection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/v1")
public class UserRestController {

    Connection connection;

    @GetMapping
    public String alive() {
        return "alive";
    }

    @PostMapping("credentials")
    public String credentials(@RequestBody CredentialsDto credentialsDto) {
        connection.writeCredentials(credentialsDto);
        connection.connect();
        return "credentials injected";
    }

}
