package com.example.springbootbasiclogin.controller;

import com.example.springbootbasiclogin.entity.Users;
import com.example.springbootbasiclogin.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
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
        return authService.logout(auth.getUsername());
    }
}
