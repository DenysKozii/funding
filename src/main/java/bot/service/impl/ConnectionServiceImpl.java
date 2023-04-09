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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionServiceImpl implements ConnectionService {

    CredentialsRepository credentialsRepository;
    Double percentageMax;

    List<SyncRequestClient> clients = new ArrayList<>();

    @Autowired
    public ConnectionServiceImpl(CredentialsRepository credentialsRepository,
                                 @Value("${trade.percentage}") Double percentageMax) {
        this.credentialsRepository = credentialsRepository;
        this.percentageMax = percentageMax;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        credentialsRepository.findAll().forEach(credentials -> clients.add(BinanceApiInternalFactory
                .getInstance()
                .createSyncRequestClient(
                        credentials.getKey(),
                        credentials.getSecret(),
                        credentials.getName(),
                        credentials.getPercentage(),
                        new RequestOptions())));
        clients.forEach(client -> log.info("Connected to an account {} with futures balance = {} USD",
                client.getName(),
                client.getAccountInformation().getAvailableBalance()));
    }

    @Override
    public String register(CredentialsDto credentialsDto) {
        if (!credentialsRepository.existsByName(credentialsDto.getName())) {
            String key = credentialsDto.getKey();
            String secret = credentialsDto.getSecret();
            String name = credentialsDto.getName();
            Double percentage = Math.max(Math.min(credentialsDto.getPercentage(), percentageMax), 0);
            try {
                SyncRequestClient client = BinanceApiInternalFactory.getInstance()
                        .createSyncRequestClient(key, secret, name, percentage, new RequestOptions());
                log.info("Connected to an account {} with futures balance = {} USD",
                        name,
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
                    .percentage(percentage)
                    .build();
            credentialsRepository.save(credentials);
            return String.format("Connection with name %s successfully created!", credentialsDto.getName());

        }
        return String.format("Connection with name %s already exists!", credentialsDto.getName());
    }

    @Override
    public boolean setPercentage(String name, Double percentageToUpdate) {
        Double percentage = Math.max(Math.min(percentageToUpdate, percentageMax), 0);
        Credentials credentials = credentialsRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(String.format("Credentials for user %s not found!", name)));
        credentials.setPercentage(percentage);
        credentialsRepository.save(credentials);
        clients.stream()
                .filter(client -> client.getName().equals(name))
                .forEach(client -> client.setPercentage(percentage));
        return true;
    }

}
