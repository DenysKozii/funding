package bot.service;

import bot.dto.CredentialsDto;
import com.binance.client.SyncRequestClient;

public interface Connection {

    void connect();

    void readCredentials();

    void writeCredentials(CredentialsDto credentials);

    SyncRequestClient getClientFutures();

}
