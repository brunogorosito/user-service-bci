package com.bci.userservice.controller

import com.bci.userservice.dto.PhoneDto
import com.bci.userservice.dto.UserResponse
import com.bci.userservice.dto.UserSignUpRequest
import com.bci.userservice.service.UserService
import com.bci.userservice.exception.UserAlreadyExistsException
import com.bci.userservice.exception.InvalidTokenException
import com.bci.userservice.exception.UserNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.ArgumentMatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.LocalDateTime

import static org.hamcrest.Matchers.anyOf
import static org.hamcrest.Matchers.is
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController)
class UserControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @MockBean
    UserService userService

    @Autowired
    ObjectMapper objectMapper

    def "should create user successfully"() {
        given: "a valid user signup request"
        def request = createValidSignUpRequest()
        def expectedResponse = createUserResponse()

        and: "service returns successful response"
        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return created status with user data"
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.id').value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath('$.name').value("Juan Rodriguez"))
                .andExpect(jsonPath('$.email').value("juan@rodriguez.org"))
                .andExpect(jsonPath('$.token').value("eyJhbGciOiJIUzI1NiJ9..."))
                .andExpect(jsonPath('$.isActive').value(true))
    }

    def "should return bad request for invalid email format"() {
        given: "a request with invalid email"
        def request = createValidSignUpRequest()
        request.email = "invalid-email"

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return bad request"
        result.andExpect(status().isBadRequest())
    }

    def "should return bad request for invalid password format"() {
        given: "a request with invalid password"
        def request = createValidSignUpRequest()
        request.password = "invalid"

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return bad request"
        result.andExpect(status().isBadRequest())
    }

    def "should return bad request for blank email"() {
        given: "a request with blank email"
        def request = createValidSignUpRequest()
        request.email = ""

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return bad request"
        result.andExpect(status().isBadRequest())
    }

    def "should return bad request for blank password"() {
        given: "a request with blank password"
        def request = createValidSignUpRequest()
        request.password = ""

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return bad request"
        result.andExpect(status().isBadRequest())
    }

    def "should login successfully with Bearer token"() {
        given: "a valid token and expected response"
        def token = "eyJhbGciOiJIUzI1NiJ9..."
        def expectedResponse = createUserResponse()

        and: "service returns successful login"
        when(userService.login(anyString())).thenReturn(expectedResponse)

        when: "getting login endpoint with Bearer token"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "Bearer " + token))

        then: "should return ok status with user data"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.id').value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath('$.token').value("eyJhbGciOiJIUzI1NiJ9..."))
                .andExpect(jsonPath('$.isActive').value(true))
    }

    def "should login successfully with token without Bearer prefix"() {
        given: "a token without Bearer prefix"
        def token = "eyJhbGciOiJIUzI1NiJ9..."
        def expectedResponse = createUserResponse()

        and: "service handles token correctly"
        when(userService.login(anyString())).thenReturn(expectedResponse)

        when: "getting login endpoint with raw token"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", token))

        then: "should return ok status"
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    def "should return internal server error when authorization header is missing"() {
        when: "getting login endpoint without authorization header"
        def result = mockMvc.perform(get("/users/login"))

        then: "should return internal server error due to missing required header"
        result.andExpect(status().isInternalServerError())
    }

    def "should handle empty request body for sign-up"() {
        when: "posting empty json to sign-up"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))

        then: "should return bad request"
        result.andExpect(status().isBadRequest())
    }

    def "should handle malformed json request"() {
        when: "posting malformed json"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))

        then: "should return internal server error"
        result.andExpect(status().isInternalServerError())
    }

    def "should handle null request body"() {
        when: "posting without body"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON))

        then: "should return internal server error"
        result.andExpect(status().isInternalServerError())
    }

    // NUEVOS TESTS PARA MEJORAR COVERAGE:

    def "should handle service exceptions during sign-up"() {
        given: "a valid user signup request"
        def request = createValidSignUpRequest()

        and: "service throws UserAlreadyExistsException"
        when(userService.signUp(any(UserSignUpRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Usuario ya existe"))

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return conflict status"
        result.andExpect(status().isConflict())
    }

    def "should handle InvalidTokenException during login"() {
        given: "a token and service exception"
        def token = "eyJhbGciOiJIUzI1NiJ9..."

        and: "service throws InvalidTokenException"
        when(userService.login(anyString()))
                .thenThrow(new InvalidTokenException("Token inválido"))

        when: "getting login endpoint"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "Bearer " + token))

        then: "should return unauthorized status"
        result.andExpect(status().isUnauthorized())
    }

    def "should handle UserNotFoundException during login"() {
        given: "a token and service exception"
        def token = "eyJhbGciOiJIUzI1NiJ9..."

        and: "service throws UserNotFoundException"
        when(userService.login(anyString()))
                .thenThrow(new UserNotFoundException("Usuario no encontrado"))

        when: "getting login endpoint"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "Bearer " + token))

        then: "should return not found status"
        result.andExpect(status().isNotFound())
    }

    def "should handle request with valid phone data"() {
        given: "a request with phone data"
        def request = createValidSignUpRequest()
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    def "should handle request with null name"() {
        given: "a request with null name"
        def request = createValidSignUpRequest()
        request.name = null
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully since name is optional"
        result.andExpect(status().isCreated())
    }

    def "should handle unsupported media type"() {
        given: "a valid request"
        def request = createValidSignUpRequest()

        when: "posting with wrong content type"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))

        then: "should return internal server error (handled by global exception handler)"
        result.andExpect(status().isInternalServerError())
    }

    def "should handle authorization header with lowercase bearer"() {
        given: "authorization header with lowercase bearer"
        def token = "eyJhbGciOiJIUzI1NiJ9..."
        def expectedResponse = createUserResponse()
        when(userService.login(anyString())).thenReturn(expectedResponse)

        when: "using lowercase bearer"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "bearer " + token))

        then: "should process successfully"
        result.andExpect(status().isOk())
    }

    def "should handle authorization header with only token"() {
        given: "authorization header with only token"
        def token = "eyJhbGciOiJIUzI1NiJ9..."
        def expectedResponse = createUserResponse()
        when(userService.login(anyString())).thenReturn(expectedResponse)

        when: "using only token without prefix"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", token))

        then: "should process successfully"
        result.andExpect(status().isOk())
    }

    def "should handle request with empty phones list"() {
        given: "a request with empty phones"
        def request = createValidSignUpRequest()
        request.phones = []
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    def "should handle request with null phones"() {
        given: "a request with null phones"
        def request = createValidSignUpRequest()
        request.phones = null
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    def "should handle multiple phones in request"() {
        given: "a request with multiple phones"
        def request = createValidSignUpRequest()
        def phone2 = new PhoneDto()
        phone2.number = 12345678L
        phone2.citycode = 2
        phone2.contrycode = "34"
        request.phones = [request.phones[0], phone2]
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    def "should handle long email addresses"() {
        given: "a request with long email"
        def request = createValidSignUpRequest()
        request.email = "verylongusername@verylongdomainname.com"
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    def "should handle special characters in name"() {
        given: "a request with special characters in name"
        def request = createValidSignUpRequest()
        request.name = "José María O'Connor"
        def expectedResponse = createUserResponse()

        when(userService.signUp(any(UserSignUpRequest.class))).thenReturn(expectedResponse)

        when: "posting to sign-up endpoint"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "should process successfully"
        result.andExpect(status().isCreated())
    }

    // Helper methods
    private UserSignUpRequest createValidSignUpRequest() {
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = "juan@rodriguez.org"
        request.password = "Password12" // 1 mayúscula, 2 números, 8-12 caracteres

        def phone = new PhoneDto()
        phone.number = 87654321L
        phone.citycode = 1
        phone.contrycode = "57"

        request.phones = [phone]
        return request
    }

    private UserResponse createUserResponse() {
        def response = new UserResponse()
        response.id = "123e4567-e89b-12d3-a456-426614174000"
        response.name = "Juan Rodriguez"
        response.email = "juan@rodriguez.org"
        response.token = "eyJhbGciOiJIUzI1NiJ9..."
        response.isActive = true
        response.created = LocalDateTime.now()
        response.lastLogin = LocalDateTime.now()

        def phone = new PhoneDto()
        phone.number = 87654321L
        phone.citycode = 1
        phone.contrycode = "57"

        response.phones = [phone]
        return response
    }
}