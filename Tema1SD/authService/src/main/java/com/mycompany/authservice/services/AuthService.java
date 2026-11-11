package com.mycompany.authservice.services;


import com.mycompany.authservice.model.UserCredential;
import com.mycompany.authservice.repository.UserCredentialRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserCredentialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul " + username + " nu a fost găsit."));
    }

    public UserCredential saveUser(UserCredential userCredential) {

        String encodedPassword = passwordEncoder.encode(userCredential.getPassword());
        userCredential.setPassword(encodedPassword);

        if (userCredential.getId() == null) {
            userCredential.setId(repository.findAll().stream()
                    .mapToInt(UserCredential::getId)
                    .max()
                    .orElse(0) + 1);
        }

        return repository.save(userCredential);
    }



    public List<UserCredential> findAllUsers() {
        return repository.findAll();
    }

    public Optional<UserCredential> findUserById(Integer id) {
        return repository.findById(id);
    }

    public UserCredential updateUser(Integer id, UserCredential userDetails) throws UsernameNotFoundException {
        UserCredential existingUser = repository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul cu ID-ul " + id + " nu a fost găsit."));

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        existingUser.setUsername(userDetails.getUsername());
        existingUser.setRole(userDetails.getRole());

        return repository.save(existingUser);
    }

    public void deleteUser(Integer id) {
        repository.deleteById(id);
    }
}