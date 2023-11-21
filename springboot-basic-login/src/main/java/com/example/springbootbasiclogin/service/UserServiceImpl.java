package com.example.springbootbasiclogin.service;

import com.example.springbootbasiclogin.dao.UserRepository;
import com.example.springbootbasiclogin.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository theUserRepository) {
        userRepository = theUserRepository;
    }

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> findById(int theId) {
        return userRepository.findById(theId);
    }

    @Override
    public Mono<User> save(User theUser) {
        return userRepository.save(theUser);
    }

    @Override
    public Mono<User> update(int id, User theUser) {
        return userRepository.findById(id).map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(optionalUser -> {
                    if(optionalUser.isPresent()){
                        theUser.setId(id);
                        return userRepository.save(theUser);
                    }
                    //else empty
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> deleteById(int theId) {
        return userRepository.deleteById(theId);
    }
}



