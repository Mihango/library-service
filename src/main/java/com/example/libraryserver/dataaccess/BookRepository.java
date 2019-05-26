package com.example.libraryserver.dataaccess;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface BookRepository extends ReactiveMongoRepository<Book, UUID> {}
