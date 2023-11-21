package com.example.springbootbasiclogin.dao;

import com.example.springbootbasiclogin.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    // that's it ... no need to write any code LOL!

}
