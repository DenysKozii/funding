package bot.service.impl;

import bot.dto.CredentialsDto;
import bot.service.Connection;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.impl.BinanceApiInternalFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionImpl implements Connection {

    String credentialsFile;
    List<SyncRequestClient> clientFutures = new ArrayList<>();
    Map<String, String> credentialsMap = new HashMap<>();

    @Autowired
    public ConnectionImpl(@Value("${credentials.file}") String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        readCredentials();
        credentialsMap.forEach((key, value) -> clientFutures.add(BinanceApiInternalFactory
                .getInstance()
                .createSyncRequestClient(key, value, new RequestOptions())));
        clientFutures.forEach(client -> log.info("connected to an account with balance = {}",
                client.getAccountInformation().getAvailableBalance()));
    }

    @SneakyThrows
    @Override
    public void readCredentials() {
        File file = new File(credentialsFile);
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] credentialsLine = line.split(";");
                credentialsMap.putIfAbsent(credentialsLine[0], credentialsLine[1]);
            }
        }
    }

    @SneakyThrows
    @Override
    public String addCredentials(CredentialsDto credentials) {
        if (!credentialsMap.containsKey(credentials.getKey())) {
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(credentialsFile, true));
            writer.write(key + ";" + secret);
            writer.newLine();
            writer.close();
            credentialsMap.put(key, secret);
            return "connected";
        }
        return "connection already exists";
    }
}
