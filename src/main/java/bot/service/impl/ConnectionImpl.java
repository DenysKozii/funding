package bot.service.impl;

import bot.dto.CredentialsDto;
import bot.service.Connection;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.impl.BinanceApiInternalFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.*;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectionImpl implements Connection {

    String apiKey;
    String apiSecret;
    String credentialsFile;
    SyncRequestClient clientFutures;


    @Autowired
    public ConnectionImpl(@Value("${credentials.file}") String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    @EventListener(ApplicationReadyEvent.class)
    @SneakyThrows
    @Override
    public void connect() {
        while (apiKey == null && apiSecret == null) {
            log.info("reading credentials");
            readCredentials();
            Thread.sleep(5 * 1000);
        }
        clientFutures = BinanceApiInternalFactory
                .getInstance()
                .createSyncRequestClient(apiKey, apiSecret, new RequestOptions());
        log.info("connected to an account with balance = {}", clientFutures.getAccountInformation().getAvailableBalance());
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
            boolean read = true;
            while ((line = br.readLine()) != null) {
                if (read) {
                    apiKey = line;
                    read = false;
                    continue;
                }
                apiSecret = line;
                return;
            }
        }
    }

    @SneakyThrows
    @Override
    public void writeCredentials(CredentialsDto credentials) {
        BufferedWriter writer = new BufferedWriter(new FileWriter(credentialsFile));
        writer.write(credentials.getKey());
        writer.newLine();
        writer.write(credentials.getSecret());
        writer.close();
    }
}
