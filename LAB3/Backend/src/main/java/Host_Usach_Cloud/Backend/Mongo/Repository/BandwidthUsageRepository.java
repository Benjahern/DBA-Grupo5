package Host_Usach_Cloud.Backend.Mongo.Repository;

import Host_Usach_Cloud.Backend.Mongo.Entity.BandwidthUsageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BandwidthUsageRepository extends MongoRepository<BandwidthUsageDocument, String> {

    List<BandwidthUsageDocument> findByUserId(Long userId);

    List<BandwidthUsageDocument> findByBillingPeriod(String billingPeriod);

    List<BandwidthUsageDocument> findByUserIdAndBillingPeriod(Long userId, String billingPeriod);

    long countByBillingPeriod(String billingPeriod);
}
