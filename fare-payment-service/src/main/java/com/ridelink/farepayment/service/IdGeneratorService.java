package com.ridelink.farepayment.service;

import org.bson.Document;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.stereotype.Service;

@Service
public class IdGeneratorService {

    private final MongoTemplate mongoTemplate;

    // Constructor injection
    public IdGeneratorService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Generate the next ID for a collection
    public long generateId(String sequenceName) {

        if (sequenceName == null || sequenceName.isBlank()) {
            throw new IllegalArgumentException(
                    "Sequence name cannot be empty"
            );
        }

        // Find the sequence by its unique name
        Query query = new Query(
                Criteria.where("_id").is(sequenceName)
        );

        // Atomically increase its current value by one
        Update update = new Update()
                .inc("value", 1L);

        // Create the sequence if it does not exist
        // Return the updated document
        FindAndModifyOptions options =
                FindAndModifyOptions.options()
                        .upsert(true)
                        .returnNew(true);

        Document counter = mongoTemplate.findAndModify(
                query,
                update,
                options,
                Document.class,
                "database_sequences"
        );

        if (counter == null) {
            throw new IllegalStateException(
                    "Unable to generate ID"
            );
        }

        Number nextValue = counter.get("value", Number.class);

        if (nextValue == null) {
            throw new IllegalStateException(
                    "Generated sequence value is missing"
            );
        }

        return nextValue.longValue();
    }
}