package com.bci.userservice.service;

import com.bci.userservice.dto.PhoneDto;
import com.bci.userservice.dto.UserResponse;
import com.bci.userservice.dto.UserSignUpRequest;
import com.bci.userservice.entity.Phone;
import com.bci.userservice.entity.User;
import com.bci.userservice.exception.InvalidTokenException;
import com.bci.userservice.exception.UserAlreadyExistsException;
import com.bci.userservice.exception.UserNotFoundException;
import com.bci.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse signUp(UserSignUpRequest request) {
        // Verificar si el usuario ya existe
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("El usuario ya existe");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Agregar teléfonos si existen
        if (request.getPhones() != null && !request.getPhones().isEmpty()) {
            List<Phone> phones = request.getPhones().stream()
                    .map(phoneDto -> new Phone(phoneDto.getNumber(), phoneDto.getCitycode(),
                            phoneDto.getContrycode(), user))
                    .collect(Collectors.toList());
            user.setPhones(phones);
        }

        // Generar token JWT
        String token = jwtService.generateToken(user.getEmail());
        user.setToken(token);

        // Guardar usuario
        User savedUser = userRepository.save(user);

        return convertToUserResponse(savedUser);
    }

    public UserResponse login(String token) {
        // Validar token
        if (!jwtService.isTokenValid(token)) {
            throw new InvalidTokenException("Token inválido");
        }

        // Extraer email del token
        Optional<String> emailOpt = jwtService.getEmailFromToken(token);
        if (!emailOpt.isPresent()) {
            throw new InvalidTokenException("Token inválido");
        }

        // Buscar usuario
        User user = userRepository.findByEmail(emailOpt.get())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Actualizar último login y generar nuevo token
        user.setLastLogin(LocalDateTime.now());
        String newToken = jwtService.generateToken(user.getEmail());
        user.setToken(newToken);

        User updatedUser = userRepository.save(user);

        return convertToUserResponse(updatedUser);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setCreated(user.getCreated());
        response.setLastLogin(user.getLastLogin());
        response.setToken(user.getToken());
        response.setIsActive(user.getIsActive());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPassword(user.getPassword());

        if (user.getPhones() != null) {
            List<PhoneDto> phoneDtos = user.getPhones().stream()
                    .map(phone -> new PhoneDto(phone.getNumber(), phone.getCitycode(), phone.getContrycode()))
                    .collect(Collectors.toList());
            response.setPhones(phoneDtos);
        }

        return response;
    }
}