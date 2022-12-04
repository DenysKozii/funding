package bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication
public class ApplicationSpringBoot {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationSpringBoot.class, args);
    }

}
