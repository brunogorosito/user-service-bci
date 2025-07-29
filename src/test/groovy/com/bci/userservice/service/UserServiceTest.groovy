package com.bci.userservice.service

import com.bci.userservice.dto.PhoneDto
import com.bci.userservice.dto.UserSignUpRequest
import com.bci.userservice.dto.UserResponse
import com.bci.userservice.entity.User
import com.bci.userservice.entity.Phone
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
        def savedUser = createCompleteUser("Test User", "test@domain.com", "encodedPassword")
        savedUser.id = "test-uuid";
        savedUser.token = "jwt-token";
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
        def existingUser = createCompleteUser("Existing", "existing@domain.com", "pass")
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
        def existingUser = createCompleteUser("Test User", userEmail, "encodedPassword")
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

    // NUEVOS TESTS PARA MEJORAR COVERAGE:

    def "should update lastLogin timestamp on successful login"() {
        given: "a valid token and existing user"
        def token = "valid-token"
        def email = "test@domain.com"
        def originalLastLogin = LocalDateTime.now().minusHours(2)

        def existingUser = createCompleteUser("Test User", email, "hashedPassword")
        existingUser.id = "test-uuid"
        existingUser.lastLogin = originalLastLogin
        existingUser.isActive = true

        jwtService.isTokenValid(token) >> true
        jwtService.getEmailFromToken(token) >> Optional.of(email)
        userRepository.findByEmail(email) >> Optional.of(existingUser)
        jwtService.generateToken(email) >> "new-token"

        // Mock más simple para save()
        userRepository.save(_ as User) >> { User user ->
            user.lastLogin = LocalDateTime.now()
            return user
        }

        when: "logging in"
        def result = userService.login(token)

        then: "should return result with updated login time"
        result != null
        result.email == email
        result.lastLogin.isAfter(originalLastLogin)
    }

    def "should preserve user data during login"() {
        given: "existing user with complete data"
        def token = "valid-token"
        def email = "test@domain.com"
        def created = LocalDateTime.now().minusDays(1)

        def existingUser = createCompleteUser("Original Name", email, "hashedPass")
        existingUser.id = "original-id"
        existingUser.created = created
        existingUser.isActive = true

        jwtService.isTokenValid(token) >> true
        jwtService.getEmailFromToken(token) >> Optional.of(email)
        userRepository.findByEmail(email) >> Optional.of(existingUser)
        jwtService.generateToken(email) >> "new-token"
        userRepository.save(_ as User) >> existingUser

        when: "logging in"
        def result = userService.login(token)

        then: "original data should be preserved"
        result.name == "Original Name"
        result.id == "original-id"
        result.created == created
        result.isActive == true
        result.email == email
    }

    def "should handle user with null phones during login"() {
        given: "user with null phones"
        def token = "valid-token"
        def email = "test@domain.com"

        def existingUser = createCompleteUser("Test User", email, "pass")
        existingUser.phones = null // Explícitamente null

        jwtService.isTokenValid(token) >> true
        jwtService.getEmailFromToken(token) >> Optional.of(email)
        userRepository.findByEmail(email) >> Optional.of(existingUser)
        jwtService.generateToken(email) >> "new-token"
        userRepository.save(_ as User) >> existingUser

        when: "logging in"
        def result = userService.login(token)

        then: "should handle null phones gracefully"
        result != null
        // El servicio debería manejar phones null correctamente
        noExceptionThrown()
    }

    def "should handle user with empty phones list during login"() {
        given: "user with empty phones"
        def token = "valid-token"
        def email = "test@domain.com"

        def existingUser = createCompleteUser("Test User", email, "pass")
        existingUser.phones = [] // Lista vacía de Phone entities

        jwtService.isTokenValid(token) >> true
        jwtService.getEmailFromToken(token) >> Optional.of(email)
        userRepository.findByEmail(email) >> Optional.of(existingUser)
        jwtService.generateToken(email) >> "new-token"
        userRepository.save(_ as User) >> existingUser

        when: "logging in"
        def result = userService.login(token)

        then: "should handle empty phones gracefully"
        result != null
        result.phones != null
        result.phones.isEmpty()
    }

    def "should create user with phones successfully"() {
        given: "a request with phone data"
        def request = new UserSignUpRequest()
        request.name = "Test User"
        request.email = "test@domain.com"
        request.password = "Password123"

        def phoneDto = new PhoneDto()
        phoneDto.number = 87654321L
        phoneDto.citycode = 1
        phoneDto.contrycode = "57"
        request.phones = [phoneDto]

        and: "user does not exist"
        userRepository.findByEmail("test@domain.com") >> Optional.empty()

        and: "services work correctly"
        passwordEncoder.encode("Password123") >> "encodedPassword"
        jwtService.generateToken("test@domain.com") >> "jwt-token"

        and: "user is saved with phone entities"
        def savedUser = createCompleteUser("Test User", "test@domain.com", "encodedPassword")
        // Crear Phone entity usando tu clase
        def phoneEntity = new Phone(87654321L, 1, "57", savedUser)
        savedUser.phones = [phoneEntity]
        userRepository.save(_ as User) >> savedUser

        when: "signing up"
        def result = userService.signUp(request)

        then: "user should be created with phones"
        result != null
        result.phones != null
        result.phones.size() == 1
        result.phones[0].number == 87654321L
    }

    def "should handle signup with null phones in request"() {
        given: "a request without phones"
        def request = new UserSignUpRequest()
        request.name = "Test User"
        request.email = "test@domain.com"
        request.password = "Password123"
        request.phones = null

        and: "user does not exist"
        userRepository.findByEmail("test@domain.com") >> Optional.empty()

        and: "services work correctly"
        passwordEncoder.encode("Password123") >> "encodedPassword"
        jwtService.generateToken("test@domain.com") >> "jwt-token"

        and: "user is saved without phones"
        def savedUser = createCompleteUser("Test User", "test@domain.com", "encodedPassword")
        userRepository.save(_ as User) >> savedUser

        when: "signing up"
        def result = userService.signUp(request)

        then: "user should be created successfully"
        result != null
        noExceptionThrown()
    }

    def "should handle signup with empty phones list in request"() {
        given: "a request with empty phones list"
        def request = new UserSignUpRequest()
        request.name = "Test User"
        request.email = "test@domain.com"
        request.password = "Password123"
        request.phones = []

        and: "user does not exist"
        userRepository.findByEmail("test@domain.com") >> Optional.empty()

        and: "services work correctly"
        passwordEncoder.encode("Password123") >> "encodedPassword"
        jwtService.generateToken("test@domain.com") >> "jwt-token"

        and: "user is saved without phones"
        def savedUser = createCompleteUser("Test User", "test@domain.com", "encodedPassword")
        userRepository.save(_ as User) >> savedUser

        when: "signing up"
        def result = userService.signUp(request)

        then: "user should be created successfully"
        result != null
        noExceptionThrown()
    }

    // Helper method simplificado
    private User createCompleteUser(String name, String email, String password) {
        def user = new User()
        user.name = name
        user.email = email
        user.password = password
        user.isActive = true
        user.created = LocalDateTime.now()
        user.lastLogin = LocalDateTime.now()
        user.phones = [] // Lista vacía por defecto
        return user
    }
}