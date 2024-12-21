package com.edunexusgraphql.controller;

import com.edunexusgraphql.model.User;
import com.edunexusgraphql.service.dummy.DummyUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final DummyUserService userService;

    @QueryMapping
    public User getUser(@Argument Long userId) {
        return userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @MutationMapping
    public User createUser(
            @Argument String name,
            @Argument String email,
            @Argument String password
    ) {
        return userService.createUser(name, email, password);
    }

    @MutationMapping
    public User updateUser(
            @Argument Long userId,
            @Argument String name,
            @Argument String email
    ) {
        return userService.updateUser(userId, name, email);
    }

}