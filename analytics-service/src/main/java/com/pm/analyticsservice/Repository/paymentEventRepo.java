package com.pm.analyticsservice.Repository;

import com.pm.analyticsservice.model.PaymentEventRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface paymentEventRepo extends MongoRepository<PaymentEventRecord, String> {

    boolean existsByPaymentId(String paymentId);

    // Spring Data derives the query from the method name:
    // SELECT COUNT(*) WHERE status = :status → db.payment_events.countDocuments({status: value})
    long countByStatus(String status);
}
