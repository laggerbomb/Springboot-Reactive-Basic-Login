package com.example.springbootbasiclogin.controller;

import com.example.springbootbasiclogin.entity.User;
import com.example.springbootbasiclogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService theUserService) {
        userService = theUserService;
    }

    // expose "/users" and return a list of users
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public Flux<User> findAll() {
        return userService.findAll();
    }

    // add mapping for GET /users/{userId}
    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<User> getUser(@PathVariable int userId) {
        return userService.findById(userId);
    }

    // add mapping for POST /users - add new user
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> addUser(@RequestBody User theUser) {
        // also just in case they pass an id in JSON ... set id to 0
        // this is to force a save of new item ... instead of update
        theUser.setId(0);

        return userService.save(theUser);
    }

    // add mapping for PUT /users - update existing user
    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<User> updateUser(@PathVariable ("userId") int id, @RequestBody User theUser) {
        return userService.update(id, theUser);
    }

    // add mapping for DELETE /users/{userId} - delete user
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> deleteUser(@PathVariable int userId) {
        return userService.findById(userId)
            .map(user -> {
                // Deleting user in a non-blocking way
                userService.deleteById(userId).subscribe();
                return "Deleted user id - " + userId;
            })
            .defaultIfEmpty("Failed to delete. User id not found - " + userId);
    }
}
