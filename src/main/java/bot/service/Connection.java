package bot.service;

import bot.binance.SyncRequestClient;
import bot.dto.CredentialsDto;

import java.util.List;

public interface Connection {

    String addCredentials(CredentialsDto credentials);

    List<SyncRequestClient> getClientFutures();

}
