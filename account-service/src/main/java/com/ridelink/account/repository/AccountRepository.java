package com.ridelink.account.repository;

import com.ridelink.account.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {

}