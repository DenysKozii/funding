package bot.service;

import bot.dto.CredentialsDto;
import com.binance.client.SyncRequestClient;

import java.util.List;

public interface Connection {

    String addCredentials(CredentialsDto credentials);

    List<SyncRequestClient> getClientFutures();

}
