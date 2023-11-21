package com.example.springbootbasiclogin.service;

import com.example.springbootbasiclogin.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {

    Flux<User> findAll();

    Mono<User> findById(int theId);

    Mono<User> save(User theUser);

    Mono<User> update(int id, User theUser);

    Mono<Void> deleteById(int theId);
}
