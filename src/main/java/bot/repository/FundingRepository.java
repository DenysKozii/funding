package bot.repository;

import bot.entity.Funding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundingRepository extends MongoRepository<Funding, String> {

    Optional<Funding> findFirstByOrderByIdDesc();

}
