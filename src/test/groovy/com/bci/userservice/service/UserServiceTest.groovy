package com.bci.userservice.service

import com.bci.userservice.dto.UserSignUpRequest
import com.bci.userservice.dto.UserResponse
import com.bci.userservice.entity.User
import com.bci.userservice.exception.UserAlreadyExistsException
import com.bci.userservice.exception.InvalidTokenException
import com.bci.userservice.exception.UserNotFoundException
import com.bci.userservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class UserServiceTest extends Specification {

    def userRepository = Mock(UserRepository);
    def jwtService = Mock(JwtService);
    def passwordEncoder = Mock(PasswordEncoder);

    @Subject
    UserService userService = new UserService();

    def setup() {
        userService.userRepository = userRepository;
        userService.jwtService = jwtService;
        userService.passwordEncoder = passwordEncoder;
    }

    def "should create user successfully"() {
        given: "a valid user signup request"
        def request = new UserSignUpRequest();
        request.name = "Test User";
        request.email = "test@domain.com";
        request.password = "a2asfGfdfdf4";

        and: "user does not exist"
        userRepository.findByEmail("test@domain.com") >> Optional.empty();

        and: "password encoding and token generation work"
        passwordEncoder.encode("a2asfGfdfdf4") >> "encodedPassword";
        jwtService.generateToken("test@domain.com") >> "jwt-token";

        and: "user is saved successfully"
        def savedUser = new User("Test User", "test@domain.com", "encodedPassword", []);
        savedUser.id = "test-uuid";
        savedUser.token = "jwt-token";
        savedUser.created = LocalDateTime.now();
        savedUser.lastLogin = LocalDateTime.now();
        savedUser.isActive = true;
        userRepository.save(_ as User) >> savedUser;

        when: "signing up the user"
        def result = userService.signUp(request);

        then: "user is created successfully"
        result != null;
        result.email == "test@domain.com";
        result.name == "Test User";
        result.token == "jwt-token";
        result.isActive == true;
        result.id == "test-uuid";
    }

    def "should throw exception when user already exists"() {
        given: "a signup request for existing user"
        def request = new UserSignUpRequest();
        request.email = "existing@domain.com";
        request.password = "a2asfGfdfdf4";

        and: "user already exists in database"
        def existingUser = new User();
        userRepository.findByEmail("existing@domain.com") >> Optional.of(existingUser);

        when: "trying to sign up existing user"
        userService.signUp(request);

        then: "should throw UserAlreadyExistsException"
        thrown(UserAlreadyExistsException);

        and: "should not interact with other services"
        0 * passwordEncoder.encode(_);
        0 * jwtService.generateToken(_);
        0 * userRepository.save(_);
    }

    def "should login user successfully with valid token"() {
        given: "a valid JWT token"
        def token = "valid-jwt-token";
        def userEmail = "test@domain.com";

        and: "token is valid and contains email"
        jwtService.isTokenValid(token) >> true;
        jwtService.getEmailFromToken(token) >> Optional.of(userEmail);

        and: "user exists in database"
        def existingUser = new User("Test User", userEmail, "encodedPassword", []);
        existingUser.id = "test-uuid";
        existingUser.created = LocalDateTime.now().minusDays(1);
        existingUser.lastLogin = LocalDateTime.now().minusHours(1);
        existingUser.isActive = true;
        userRepository.findByEmail(userEmail) >> Optional.of(existingUser);

        and: "new token is generated"
        def newToken = "new-jwt-token";
        jwtService.generateToken(userEmail) >> newToken;

        and: "user is updated successfully"
        def updatedUser = existingUser;
        updatedUser.token = newToken;
        updatedUser.lastLogin = LocalDateTime.now();
        userRepository.save(_ as User) >> updatedUser;

        when: "logging in with token"
        def result = userService.login(token);

        then: "should return updated user information"
        result != null;
        result.email == userEmail;
        result.name == "Test User";
        result.token == newToken;
        result.isActive == true;
        result.id == "test-uuid";
    }

    def "should throw exception when token is invalid"() {
        given: "an invalid JWT token"
        def invalidToken = "invalid-jwt-token";

        and: "token validation fails"
        jwtService.isTokenValid(invalidToken) >> false;

        when: "trying to login with invalid token"
        userService.login(invalidToken);

        then: "should throw InvalidTokenException"
        thrown(InvalidTokenException);

        and: "should not interact with other services"
        0 * jwtService.getEmailFromToken(_);
        0 * userRepository.findByEmail(_);
    }

    def "should throw exception when token is valid but user not found"() {
        given: "a valid token with non-existent user email"
        def token = "valid-jwt-token";
        def userEmail = "nonexistent@domain.com";

        and: "token is valid but user doesn't exist"
        jwtService.isTokenValid(token) >> true;
        jwtService.getEmailFromToken(token) >> Optional.of(userEmail);
        userRepository.findByEmail(userEmail) >> Optional.empty();

        when: "trying to login"
        userService.login(token);

        then: "should throw UserNotFoundException"
        thrown(UserNotFoundException);

        and: "should not generate new token or save user"
        0 * jwtService.generateToken(_);
        0 * userRepository.save(_);
    }

    def "should throw exception when token does not contain email"() {
        given: "a token without email information"
        def token = "token-without-email";

        and: "token is valid but email extraction fails"
        jwtService.isTokenValid(token) >> true;
        jwtService.getEmailFromToken(token) >> Optional.empty();

        when: "trying to login"
        userService.login(token);

        then: "should throw InvalidTokenException"
        thrown(InvalidTokenException);

        and: "should not interact with repository"
        0 * userRepository.findByEmail(_);
        0 * userRepository.save(_);
    }
}