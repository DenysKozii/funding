package bot.service.impl;

import bot.dto.CredentialsDto;
import bot.entity.Credentials;
import bot.repository.CredentialsRepository;
import bot.service.ConnectionService;
import bot.binance.RequestOptions;
import bot.binance.SyncRequestClient;
import bot.binance.BinanceApiException;
import bot.binance.BinanceApiInternalFactory;
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
public class ConnectionServiceImpl implements ConnectionService {

    CredentialsRepository credentialsRepository;

    List<SyncRequestClient> clients = new ArrayList<>();

    @Autowired
    public ConnectionServiceImpl(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        credentialsRepository.findAll().forEach(credentials -> clients.add(BinanceApiInternalFactory
                .getInstance()
                .createSyncRequestClient(credentials.getKey(), credentials.getSecret(), new RequestOptions())));
        clients.forEach(client -> log.info("connected to an account with futures balance = {}",
                client.getAccountInformation().getAvailableBalance()));
    }

    @Override
    public String addCredentials(CredentialsDto credentialsDto) {
        if (!credentialsRepository.existsById(credentialsDto.getKey())) {
            String key = credentialsDto.getKey();
            String secret = credentialsDto.getSecret();
            String name = credentialsDto.getName();
            try {
                SyncRequestClient client = BinanceApiInternalFactory.getInstance()
                        .createSyncRequestClient(key, secret, new RequestOptions());
                log.info("connected to an account with futures balance = {}",
                        client.getAccountInformation().getAvailableBalance());
                clients.add(client);
            } catch (BinanceApiException binanceApiException) {
                log.error(binanceApiException.getMessage());
                return binanceApiException.getMessage();
            }
            Credentials credentials = Credentials.builder()
                    .key(key)
                    .secret(secret)
                    .name(name)
                    .build();
            credentialsRepository.save(credentials);
            return "connected";
        }
        return "connection already exists";
    }
}
