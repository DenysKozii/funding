package bot.service.impl;

import bot.dto.CredentialsDto;
import bot.entity.Credentials;
import bot.repository.CredentialsRepository;
import bot.service.Connection;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.impl.BinanceApiInternalFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionImpl implements Connection {

    CredentialsRepository credentialsRepository;

    List<SyncRequestClient> clientFutures = new ArrayList<>();

    @Autowired
    public ConnectionImpl(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        credentialsRepository.findAll().forEach(credentials -> clientFutures.add(BinanceApiInternalFactory
                .getInstance()
                .createSyncRequestClient(credentials.getKey(), credentials.getSecret(), new RequestOptions())));
        clientFutures.forEach(client -> log.info("connected to an account with balance = {}",
                client.getAccountInformation().getAvailableBalance()));
    }

    @Override
    public String addCredentials(CredentialsDto credentials) {
        if (!credentialsRepository.existsById(credentials.getKey())) {
            String key = credentials.getKey();
            String secret = credentials.getSecret();
            try {
                SyncRequestClient client = BinanceApiInternalFactory.getInstance()
                        .createSyncRequestClient(key, secret, new RequestOptions());
                log.info("connected to an account with balance = {}", client.getAccountInformation().getAvailableBalance());
                clientFutures.add(client);
            } catch (BinanceApiException binanceApiException) {
                log.error(binanceApiException.getMessage());
                return binanceApiException.getMessage();
            }
            credentialsRepository.save(new Credentials(key, secret, credentials.getName()));
            return "connected";
        }
        return "connection already exists";
    }
}
