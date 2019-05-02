package com.progra3.v1.controller;

import com.progra3.v1.model.User;
import com.progra3.v1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final int DELAY_PER_ITEM_MS = 1;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Mono<User> addUser(@Valid @RequestBody User user) {
        return userRepository.save(user);
    }

    @RequestMapping(value = "/userAll", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> getUser() {
        Flux<User> flux = userRepository.findAll().delayElements(Duration.ofSeconds(5));
        flux.subscribe();
        //return userRepository.findAll().delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
        return flux;
    }

    @RequestMapping(value = "/findUser/id={id}", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<User>> getUserId(@PathVariable(value = "id") String userId) {
        return userRepository.findById(userId).map(savedUser -> ResponseEntity.ok(savedUser))
                .defaultIfEmpty(ResponseEntity.notFound().build()).delayElement(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }

    @RequestMapping(value = "/updateUser/id={id}", method = RequestMethod.PUT, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<User>> updateUser(@PathVariable(value = "id") String userId
            , @Valid @RequestBody User user) {
        return userRepository.findById(userId).flatMap(existingUser -> {
            existingUser.setName(user.getName());
            existingUser.setRol(user.getRol());
            return userRepository.save(existingUser);
        }).map(updatedUser -> new ResponseEntity<>(updatedUser, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/deleteUser/id={id}", method = RequestMethod.DELETE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable(value = "id") String userID) {
        return userRepository.findById(userID).flatMap(existingUser ->
                userRepository.delete(existingUser))
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
