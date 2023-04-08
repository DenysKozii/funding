package bot.service;

import bot.binance.SyncRequestClient;
import bot.dto.CredentialsDto;

import java.util.List;

public interface ConnectionService {

    String addCredentials(CredentialsDto credentials);

    List<SyncRequestClient> getClients();

}
