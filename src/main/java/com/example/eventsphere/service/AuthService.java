package com.example.eventsphere.service;

import com.example.eventsphere.entity.User;
import com.example.eventsphere.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> login(String nom, String mdp) {
        Optional<User> user = userRepository.findByNom(nom);
        if (user.isPresent() && user.get().getMdp().equals(mdp)) {
            return user;
        }
        return Optional.empty();
    }
}