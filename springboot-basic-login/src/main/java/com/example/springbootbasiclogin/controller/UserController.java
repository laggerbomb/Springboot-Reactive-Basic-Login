package com.example.springbootbasiclogin.controller;

import com.example.springbootbasiclogin.entity.Users;
import com.example.springbootbasiclogin.service.AuthService;
import com.example.springbootbasiclogin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    private UserService userService;
    private AuthService authService;

    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /* Managing User Profile */
    // expose "/users" and return a list of users
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Users> findAll() {
        return userService.findAll();
    }

    // add mapping for GET /users/{userId}
    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Users> getUser(@PathVariable int userId) {
        return userService.findById(userId);
    }

    // add mapping for PUT /users - update existing user
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Users> updateUser(@PathVariable ("userId") int id,
                                  @RequestBody Users theUser,
                                  @AuthenticationPrincipal UserDetails auth) {

        return userService.findByUsername(auth.getUsername())
            .filter(Users::isActive)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not active")))
            .flatMap(user ->
                Mono.defer(() ->
                    userService.isUsernameMatching(id, auth.getUsername())
                        .flatMap(isMatching -> {
                            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                                    isMatching) {
                                return userService.update(id, theUser);
                            } else {
                                return Mono.error(new AccessDeniedException("Access Denied"));
                            }
                        })
                )
            );
    }

    // add mapping for DELETE /users/{userId} - delete user
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> deleteUser(@PathVariable ("userId") int id,
                                 @AuthenticationPrincipal UserDetails auth) {

        return userService.findByUsername(auth.getUsername())
            .filter(Users::isActive)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not active")))
            .flatMap(user ->
                Mono.defer(() ->
                    userService.isUsernameMatching(id, auth.getUsername())
                        .flatMap(isMatching ->{
                            if(auth.getAuthorities().contains(
                                    new SimpleGrantedAuthority("ROLE_ADMIN")) || isMatching){
                                //delete everything related to that user
                                return authService.deleteUsersCascade(id);
                            }
                            //ask help to print the error on Postman Response
                            else {
                                return Mono.error(new AccessDeniedException("Access Denied"));
                            }
                        })
                )
            );
    }
}
