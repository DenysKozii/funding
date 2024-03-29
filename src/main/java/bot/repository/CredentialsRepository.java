package bot.repository;

import bot.entity.Credentials;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialsRepository extends MongoRepository<Credentials, String> {

    boolean existsByName(String name);

    Optional<Credentials> findByName(String name);

}
