package com.example.library.server.business;

import com.example.library.server.dataaccess.Book;
import com.example.library.server.dataaccess.BookRepository;
import com.example.library.server.dataaccess.User;
import com.example.library.server.dataaccess.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final ModelMapper modelMapper;

    @Autowired
    public BookService(
            BookRepository bookRepository,
            UserRepository userRepository,
            IdGenerator idGenerator,
            ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
        this.modelMapper = modelMapper;
    }

    public Mono<Void> create(Mono<BookResource> bookResource) {
        return bookRepository.insert(bookResource.map(this::convert)).then();
    }

    public Mono<BookResource> findById(UUID uuid) {
        return bookRepository.findById(uuid).map(this::convert);
    }

    public Mono<Void> borrowById(UUID uuid, UUID userId) {

        if (uuid == null || userId == null) {
            return Mono.empty();
        }

        return bookRepository
                .findById(uuid)
                .log()
                .flatMap(
                        b ->
                                userRepository
                                        .findById(userId)
                                        .flatMap(
                                                u -> {
                                                    b.doBorrow(u);
                                                    return bookRepository.save(b).then();
                                                })
                                        .switchIfEmpty(Mono.empty()))
                .switchIfEmpty(Mono.empty());
    }

    public Mono<Void> returnById(UUID uuid, UUID userId) {

        if (uuid == null || userId == null) {
            return Mono.empty();
        }

        return bookRepository
                .findById(uuid)
                .log()
                .flatMap(
                        b ->
                                userRepository
                                        .findById(userId)
                                        .flatMap(
                                                u -> {
                                                    b.doReturn(u);
                                                    return bookRepository.save(b).then();
                                                })
                                        .switchIfEmpty(Mono.empty()))
                .switchIfEmpty(Mono.empty());
    }

    public Flux<BookResource> findAll() {
        return bookRepository.findAll().map(this::convert);
    }

    public Mono<Void> deleteById(UUID uuid) {
        return bookRepository.deleteById(uuid).then();
    }

    private Book convert(BookResource br) {
        UserResource borrowedBy = br.getBorrowedBy();
        Book book = modelMapper.map(br, Book.class);
        if (borrowedBy != null) {
            book.doBorrow(modelMapper.map(borrowedBy, User.class));
        }
        if (book.getId() == null) {
            book.setId(idGenerator.generateId());
        }
        return book;
    }

    private BookResource convert(Book b) {
        User borrowedBy = b.getBorrowedBy();
        BookResource bookResource = modelMapper.map(b, BookResource.class);
        if (borrowedBy != null) {
            bookResource.setBorrowedBy(modelMapper.map(borrowedBy, UserResource.class));
        }
        return bookResource;
    }
}
