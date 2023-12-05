package com.example.springbootbasiclogin.service;

import com.example.springbootbasiclogin.entity.Roles;
import com.example.springbootbasiclogin.entity.Users;
import com.example.springbootbasiclogin.entity.VerificationToken;
import com.example.springbootbasiclogin.repo.RoleRepository;
import com.example.springbootbasiclogin.repo.UserRepository;
import com.example.springbootbasiclogin.repo.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService implements ReactiveUserDetailsService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, VerificationTokenRepository verificationTokenRepository, BCryptPasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    //find the User + Roles using the username when require by basic auth
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .flatMap(user ->
                roleRepository
                    .findByUserId(user.getId())
                    .map(Roles::getRole)
                    .collectList()
                    .map(roles -> User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(roles.toArray(new String[0]))
                        .build()
                    )
            );
    }

    public Mono<Users> loginUser(String username, String password) {
        return userRepository.findByUsername(username)
            .filter(user -> {
                //Get the password without {bcrypt} at front
                String passwordEdited =  user.getPassword().substring(8);
                return passwordEncoder.matches(password, passwordEdited);
            })
            .flatMap(user -> {
                //updated "active" status to true
                user.setActive(true);
                return userRepository.save(user);
            });
    }

    public Mono<Users> registerUser(String username, String password, String role, String email) {
        //create new user
        Users user= new Users();
        user.setUsername(username);
        user.setPassword("{bcrypt}"+ passwordEncoder.encode(password));
        user.setEmail(email);

        return userRepository.save(user) //save the user to db
            .flatMap(savedUser -> {
                //create new role
                Roles roles = new Roles();
                roles.setUserId(savedUser.getId());
                roles.setRole(role);
                //create new verificationToken
                Mono<VerificationToken> savedToken = generateToken(user);

                return roleRepository.save(roles)  //save the role to db
                    .then(savedToken
                        .doOnSuccess(tokenSaved ->
                            sendEmail(savedUser.getEmail(), tokenSaved.getToken(),
                            "Email Verification",
                                "Thank you for registering. " +
                                    "Please click on the link to verify your email: " +
                                    "http://localhost:8080/verify-email/" ))
                        .thenReturn(savedUser));
            });
    }

    public Mono<String> verifyEmail(String verificationToken) {
        return verificationTokenRepository.findByToken(verificationToken)
            .flatMap(token -> {
                if (isTokenExpired(token.getCreationTime())) {
                    return Mono.just("Verification token has expired.");
                }

                return userRepository.findById(token.getUserId())
                    .flatMap(user -> {
                        user.setVerified(true); // Set the verified flag to true
                        return userRepository.save(user);
                    })
                    .thenReturn("Email verified successfully.");
            });
    }

    public Mono<String> forgetPassword(String email){
        return userRepository.findByEmail(email)
            .flatMap(user ->{
                Mono<VerificationToken> savedToken = generateToken(user);
                return savedToken
                    .doOnSuccess(tokenSaved ->
                        sendEmail(user.getEmail(), tokenSaved.getToken(),
                        "Forget Email",
                        "Forget Email? " +
                                "Please click on the link to change your password: " +
                                "http://localhost:8080/reset-password/" ))
                    .thenReturn("Do check your email for resetting password")
                    .defaultIfEmpty("Error! cannot save Verification Token");
            })
            .defaultIfEmpty("Email is not registered");
    }

    public Mono<String> resetPassword(String verificationToken, String password) {
        return verificationTokenRepository.findByToken(verificationToken)
            .flatMap(token -> {
                if (isTokenExpired(token.getCreationTime())) {
                    return Mono.just("Verification token has expired.");
                }

                return userRepository.findById(token.getUserId())
                    .flatMap(user -> {
                        // Updated the password
                        user.setPassword("{bcrypt}"+ passwordEncoder.encode(password));
                        return userRepository.save(user);
                    })
                    .thenReturn("Password Reset successfully.");
            });
    }

    private Mono<VerificationToken> generateToken(Users savedUser) {
        return verificationTokenRepository.findByUserId(savedUser.getId())
            .flatMap(existingToken -> {
                existingToken.setToken(UUID.randomUUID().toString());
                existingToken.setCreationTime(LocalDateTime.now());
                return verificationTokenRepository.save(existingToken);
            })
            .switchIfEmpty(Mono.defer(() -> {
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setToken(UUID.randomUUID().toString());
                verificationToken.setUserId(savedUser.getId());
                verificationToken.setCreationTime(LocalDateTime.now());
                return verificationTokenRepository.save(verificationToken);
            }));
    }

    private void sendEmail(String to, String verificationToken, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(text + verificationToken);
        message.setTo(to);

        mailSender.send(message);
    }

    private boolean isTokenExpired(LocalDateTime creationTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(creationTime, now);
        return duration.toMinutes() > 5; // Token expires after 5 minutes
    }

    //Delete the Roles + VerificationToken + User using id
    public Mono<String> deleteUsersCascade(int userId){
        return roleRepository.deleteByUserId(userId)
            .then(verificationTokenRepository.deleteByUserId(userId))
            .then(userRepository.deleteById(userId)
            .thenReturn("Deleted user id - " + userId)
            .defaultIfEmpty("Error Deleted cause by Cascade")
        );
    }
}
