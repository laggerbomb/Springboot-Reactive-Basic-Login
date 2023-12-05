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

import java.util.Map;

@RestController
public class UserController {

    private UserService userService;
    private AuthService authService;

    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /* Auth and Autz */
    @PutMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> loginUser(@RequestBody Users theUser) {
        return authService.loginUser(theUser.getUsername(), theUser.getPassword())
                .map(user -> "Login successful for user: " + user.getUsername())
                .defaultIfEmpty("Wrong password or username");
    }

    @PostMapping("/register")
    public Mono<Users> registerUser(@RequestBody Map<String, String> payload){
        String username = payload.get("username");
        String password = payload.get("password");
        String role = payload.get("role");
        String email = payload.get("email");
        return authService.registerUser(username, password, role, email);
    }

    @GetMapping("/verify-email/{verificationToken}")
    public Mono<String> verifyEmail(@PathVariable String verificationToken) {
        return authService.verifyEmail(verificationToken);
    }

    @PostMapping("/fp")
    public Mono<String> forgetPassword(@RequestBody Users users){
        return authService.forgetPassword(users.getEmail());
    }

    @PostMapping("/reset-password/{verificationToken}")
    public Mono<String> resetPassword(@PathVariable String verificationToken,
                                      @RequestBody Users users) {
        return authService.resetPassword(verificationToken, users.getPassword());
    }

    @GetMapping("/logout")
    public Mono<String> logout( @AuthenticationPrincipal UserDetails auth) {
        return userService.logout(auth.getUsername());
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

    // add mapping for POST /users - add new user
    @PreAuthorize("authenticated")
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Users> addUser(@RequestBody Users theUser) {
        // also just in case they pass an id in JSON ... set id to 0
        // this is to force a save of new item ... instead of update
        theUser.setId(0);
        return userService.save(theUser);
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
