package bot.service;

import bot.binance.SyncRequestClient;
import bot.dto.CredentialsDto;

import java.util.List;

public interface ConnectionService {

    String register(CredentialsDto credentials);

    List<SyncRequestClient> getClients();

    boolean setPercentage(String name, Double percentage);

}
