package com.bci.userservice.controller


import com.bci.userservice.exception.UserAlreadyExistsException
import com.bci.userservice.exception.InvalidTokenException
import com.bci.userservice.exception.UserNotFoundException
import com.bci.userservice.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController)
class ExceptionHandlerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @MockBean
    UserService userService

    def "should handle UserAlreadyExistsException"() {
        given: "service throws UserAlreadyExistsException"
        when(userService.signUp(any())).thenThrow(new UserAlreadyExistsException("El correo ya registrado"))

        when: "making request"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"email":"test@test.com","password":"Password12"}'))

        then: "should return conflict with error message"
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.mensaje').value("El correo ya registrado"))
    }

    def "should handle InvalidTokenException"() {
        given: "service throws InvalidTokenException"
        when(userService.login(anyString())).thenThrow(new InvalidTokenException("Token inválido"))

        when: "making request"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "Bearer invalid-token"))

        then: "should return unauthorized with error message"
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath('$.mensaje').value("Token inválido"))
    }

    def "should handle UserNotFoundException"() {
        given: "service throws UserNotFoundException"
        when(userService.login(anyString())).thenThrow(new UserNotFoundException("Usuario no encontrado"))

        when: "making request"
        def result = mockMvc.perform(get("/users/login")
                .header("Authorization", "Bearer valid-token"))

        then: "should return not found with error message"
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath('$.mensaje').value("Usuario no encontrado"))
    }

    def "should handle validation errors"() {
        when: "sending request with validation errors"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"email":"invalid-email","password":""}'))

        then: "should return bad request with validation messages"
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors').exists())
    }

    def "should handle generic exceptions"() {
        given: "service throws generic exception"
        when(userService.signUp(any())).thenThrow(new RuntimeException("Error interno"))

        when: "making request"
        def result = mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"email":"test@test.com","password":"Password12"}'))

        then: "should return internal server error"
        result.andExpect(status().isInternalServerError())
    }
}