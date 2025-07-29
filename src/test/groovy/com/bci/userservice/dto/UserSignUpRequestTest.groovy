package com.bci.userservice.dto

import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import java.time.LocalDateTime

class UserSignUpRequestTest extends Specification {

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
    Validator validator = factory.validator

    def "should create valid user signup request"() {
        given: "a valid user signup request"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = "juan@rodriguez.org"
        request.password = "Password12"

        def phone = new PhoneDto()
        phone.number = 87654321L
        phone.citycode = 1
        phone.contrycode = "57"
        request.phones = [phone]

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have no violations"
        violations.isEmpty()

        and: "all fields should be correctly set"
        request.name == "Juan Rodriguez"
        request.email == "juan@rodriguez.org"
        request.password == "Password12"
        request.phones.size() == 1
        request.phones[0].number == 87654321L
    }

    @Unroll
    def "should fail validation when email is '#emailValue' - #testCase"() {
        given: "a request with invalid email"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = emailValue
        request.password = "Password12"

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have violations"
        !violations.isEmpty()
        violations.any { it.message == expectedMessage }

        where:
        emailValue          | testCase              | expectedMessage
        ""                  | "blank email"         | "Email es requerido"
        null                | "null email"          | "Email es requerido"
        "invalid-email"     | "invalid format"      | "Formato de email inválido"
        "test@"             | "incomplete domain"   | "Formato de email inválido"
        "@domain.com"       | "missing user"        | "Formato de email inválido"
        "test@domain"       | "missing TLD"         | "Formato de email inválido"
        "test.domain.com"   | "missing @"           | "Formato de email inválido"
    }

    @Unroll
    def "should validate email format correctly for '#emailValue'"() {
        given: "a request with email"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = emailValue
        request.password = "Password12"

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have expected validation result"
        violations.findAll { it.propertyPath.toString() == "email" }.isEmpty() == isValid

        where:
        emailValue                  | isValid
        "test@domain.com"          | true
        "user123@example.org"      | true
        "valid@test.co"            | true
        "a@b.co"                   | true
        "test123@domain123.com"    | true
    }

    @Unroll
    def "should fail validation when password is '#passwordValue' - #testCase"() {
        given: "a request with invalid password"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = "juan@rodriguez.org"
        request.password = passwordValue

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have violations"
        !violations.isEmpty()
        violations.any { it.message == expectedMessage }

        where:
        passwordValue    | testCase              | expectedMessage
        ""               | "blank password"      | "Password es requerido"
        null             | "null password"       | "Password es requerido"
        "invalid"        | "invalid format"      | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "password12"     | "no uppercase"        | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "Password1"      | "only one number"     | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "PASSWORD12"     | "no lowercase"        | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "Pass12"         | "too short"           | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "PasswordTooLong12" | "too long"         | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
        "Password@12"    | "special chars"       | "Password debe tener una mayúscula, dos números, solo letras y números, entre 8-12 caracteres"
    }

    @Unroll
    def "should validate password format correctly for '#passwordValue'"() {
        given: "a request with password"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = "juan@rodriguez.org"
        request.password = passwordValue

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have no password violations"
        violations.findAll { it.propertyPath.toString() == "password" }.isEmpty()

        where:
        passwordValue << [
                "Password12", "MyPass34", "Test1234", "Strong78",
                "Abc12345", "Hello123", "World567", "Testing99", "Sample88"
        ]
    }

    def "should allow null values for optional fields"() {
        given: "a request with only required fields"
        def request = new UserSignUpRequest()
        request.name = null  // name es opcional
        request.email = "juan@rodriguez.org"
        request.password = "Password12"
        request.phones = null  // phones es opcional

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have no violations"
        violations.isEmpty()
        request.name == null
        request.phones == null
    }

    def "should handle empty phones list"() {
        given: "a request with empty phones list"
        def request = new UserSignUpRequest()
        request.name = "Juan Rodriguez"
        request.email = "juan@rodriguez.org"
        request.password = "Password12"
        request.phones = []

        when: "validating the request"
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request)

        then: "should have no violations"
        violations.isEmpty()
        request.phones.isEmpty()
    }
}

class UserResponseTest extends Specification {

    def "should create user response with all fields"() {
        given: "user response data"
        def now = LocalDateTime.now()
        def phone = new PhoneDto(87654321L, 1, "57")

        when: "creating user response"
        def response = new UserResponse()
        response.id = "123e4567-e89b-12d3-a456-426614174000"
        response.name = "Juan Rodriguez"
        response.email = "juan@rodriguez.org"
        response.password = "Password12"
        response.token = "eyJhbGciOiJIUzI1NiJ9..."
        response.isActive = true
        response.created = now
        response.lastLogin = now
        response.phones = [phone]

        then: "all fields should be correctly set"
        response.id == "123e4567-e89b-12d3-a456-426614174000"
        response.name == "Juan Rodriguez"
        response.email == "juan@rodriguez.org"
        response.password == "Password12"
        response.token == "eyJhbGciOiJIUzI1NiJ9..."
        response.isActive == true
        response.created == now
        response.lastLogin == now
        response.phones.size() == 1
        response.phones[0].number == 87654321L
        response.phones[0].citycode == 1
        response.phones[0].contrycode == "57"
    }

    def "should create empty user response"() {
        when: "creating empty user response"
        def response = new UserResponse()

        then: "all fields should be null"
        response.id == null
        response.name == null
        response.email == null
        response.password == null
        response.token == null
        response.isActive == null
        response.created == null
        response.lastLogin == null
        response.phones == null
    }

    def "should set and get all fields individually"() {
        given: "a user response"
        def response = new UserResponse()
        def created = LocalDateTime.of(2024, 1, 1, 10, 0)
        def lastLogin = LocalDateTime.of(2024, 1, 2, 15, 30)

        when: "setting all fields"
        response.id = "test-id"
        response.name = "Test Name"
        response.email = "test@test.com"
        response.password = "testpass"
        response.token = "test-token"
        response.isActive = false
        response.created = created
        response.lastLogin = lastLogin
        response.phones = []

        then: "all fields should be correctly retrieved"
        response.id == "test-id"
        response.name == "Test Name"
        response.email == "test@test.com"
        response.password == "testpass"
        response.token == "test-token"
        response.isActive == false
        response.created == created
        response.lastLogin == lastLogin
        response.phones.isEmpty()
    }

    def "should handle null values correctly"() {
        given: "a user response"
        def response = new UserResponse()

        when: "setting all fields to null"
        response.id = null
        response.name = null
        response.email = null
        response.password = null
        response.token = null
        response.isActive = null
        response.created = null
        response.lastLogin = null
        response.phones = null

        then: "all fields should be null"
        response.id == null
        response.name == null
        response.email == null
        response.password == null
        response.token == null
        response.isActive == null
        response.created == null
        response.lastLogin == null
        response.phones == null
    }

    def "should handle boolean isActive field correctly"() {
        given: "a user response"
        def response = new UserResponse()

        when: "setting isActive to true"
        response.isActive = true

        then: "should return true"
        response.isActive == true

        when: "setting isActive to false"
        response.isActive = false

        then: "should return false"
        response.isActive == false

        when: "setting isActive to null"
        response.isActive = null

        then: "should return null"
        response.isActive == null
    }
}

class PhoneDtoTest extends Specification {

    def "should create phone dto with constructor"() {
        when: "creating phone with constructor"
        def phone = new PhoneDto(87654321L, 1, "57")

        then: "all fields should be set"
        phone.number == 87654321L
        phone.citycode == 1
        phone.contrycode == "57"
    }

    def "should create empty phone dto"() {
        when: "creating empty phone"
        def phone = new PhoneDto()

        then: "all fields should be null"
        phone.number == null
        phone.citycode == null
        phone.contrycode == null
    }

    def "should set and get all fields"() {
        given: "an empty phone dto"
        def phone = new PhoneDto()

        when: "setting all fields"
        phone.number = 12345678L
        phone.citycode = 2
        phone.contrycode = "34"

        then: "all fields should be correctly retrieved"
        phone.number == 12345678L
        phone.citycode == 2
        phone.contrycode == "34"
    }

    def "should handle null values"() {
        given: "a phone dto"
        def phone = new PhoneDto()

        when: "setting fields to null"
        phone.number = null
        phone.citycode = null
        phone.contrycode = null

        then: "all fields should be null"
        phone.number == null
        phone.citycode == null
        phone.contrycode == null
    }

    def "should handle different number types"() {
        given: "a phone dto"
        def phone = new PhoneDto()

        when: "setting different number values"
        phone.number = numberValue
        phone.citycode = citycodeValue

        then: "values should be set correctly"
        phone.number == numberValue
        phone.citycode == citycodeValue

        where:
        numberValue    | citycodeValue
        0L             | 0
        1L             | 1
        999999999L     | 999
        87654321L      | 57
    }
}