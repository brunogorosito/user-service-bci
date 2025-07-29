package com.bci.userservice.entity

import com.bci.userservice.dto.PhoneDto
import spock.lang.Specification
import java.time.LocalDateTime

class UserEntityTest extends Specification {

    def "should create user with constructor"() {
        given: "user data"
        def phones = [new Phone(123456789L, 1, "57", null)]

        when: "creating user with constructor"
        def user = new User("John Doe", "john@example.com", "hashedPass", phones)

        then: "all fields should be set correctly"
        user.name == "John Doe"
        user.email == "john@example.com"
        user.password == "hashedPass"
        user.phones == phones
        user.isActive == true // Constructor sets this to true
        user.created != null // Constructor generates timestamp
        user.lastLogin != null // Constructor generates timestamp
        user.id != null // Constructor generates UUID
        user.id.length() == 36 // UUID string length
        user.token == null // Not set by constructor
    }

    def "should create empty user"() {
        when: "creating empty user with default constructor"
        def user = new User()

        then: "should have values set by constructor"
        user.name == null
        user.email == null
        user.password == null
        user.phones == null
        user.isActive == true // Constructor sets this
        user.created != null // Constructor generates this
        user.lastLogin != null // Constructor generates this
        user.id != null // Constructor generates UUID
        user.id.length() == 36 // UUID string format
        user.token == null
    }

    def "should set and get all fields"() {
        given: "a user instance"
        def user = new User()
        def now = LocalDateTime.now()
        def phones = [new Phone()]

        when: "setting all fields"
        user.id = "test-id"
        user.name = "Test Name"
        user.email = "test@test.com"
        user.password = "testpass"
        user.phones = phones
        user.isActive = false
        user.created = now
        user.lastLogin = now
        user.token = "test-token"

        then: "all fields should be retrievable"
        user.id == "test-id"
        user.name == "Test Name"
        user.email == "test@test.com"
        user.password == "testpass"
        user.phones == phones
        user.isActive == false
        user.created == now
        user.lastLogin == now
        user.token == "test-token"
    }

    def "should handle null phones list"() {
        when: "creating user with null phones"
        def user = new User("John", "john@test.com", "pass", null)

        then: "phones should be null"
        user.phones == null
    }

    def "should handle empty phones list"() {
        when: "creating user with empty phones"
        def user = new User("John", "john@test.com", "pass", [])

        then: "phones should be empty"
        user.phones.isEmpty()
    }

    def "should allow setting isActive to different values"() {
        given: "a user instance"
        def user = new User()

        when: "setting isActive to false"
        user.isActive = false

        then: "should return false"
        user.isActive == false

        when: "setting isActive to true"
        user.isActive = true

        then: "should return true"
        user.isActive == true
    }

    def "should handle long text fields"() {
        given: "long text values"
        def longName = "A" * 100
        def longEmail = "user@" + "domain" * 20 + ".com"
        def longPassword = "password" * 20

        when: "creating user with long values"
        def user = new User(longName, longEmail, longPassword, [])

        then: "should handle long values"
        user.name == longName
        user.email == longEmail
        user.password == longPassword
    }

    def "should maintain timestamps"() {
        given: "a user"
        def user = new User()
        def originalCreated = user.created
        def originalLastLogin = user.lastLogin

        when: "updating lastLogin"
        def newLastLogin = LocalDateTime.now().plusHours(1)
        user.lastLogin = newLastLogin

        then: "created should remain unchanged but lastLogin should update"
        user.created == originalCreated
        user.lastLogin == newLastLogin
        user.lastLogin != originalLastLogin
    }

    def "should handle special characters in fields"() {
        given: "values with special characters"
        def specialName = "José María O'Connor"
        def specialEmail = "test+user@domain-name.co.uk"

        when: "creating user with special characters"
        def user = new User(specialName, specialEmail, "pass123", [])

        then: "should handle special characters correctly"
        user.name == specialName
        user.email == specialEmail
    }

    def "should handle phone relationships"() {
        given: "a user and phones"
        def user = new User("Test", "test@test.com", "pass", [])
        def phone1 = new Phone(123456789L, 1, "57", user)
        def phone2 = new Phone(987654321L, 2, "34", user)

        when: "adding phones to user"
        user.phones = [phone1, phone2]

        then: "user should have both phones"
        user.phones.size() == 2
        user.phones.contains(phone1)
        user.phones.contains(phone2)

        and: "phones should reference the user"
        phone1.user == user
        phone2.user == user
    }

    def "should allow null values for optional fields"() {
        given: "a user"
        def user = new User()

        when: "setting optional fields to null"
        user.name = null
        user.token = null
        user.phones = null

        then: "should handle null values"
        user.name == null
        user.token == null
        user.phones == null
    }

    def "should have unique IDs for different users"() {
        when: "creating multiple users"
        def user1 = new User()
        def user2 = new User()

        then: "should have different UUIDs"
        user1.id != user2.id
        user1.id != null
        user2.id != null
        user1.id.length() == 36 // UUID string length
        user2.id.length() == 36 // UUID string length

        and: "IDs should be valid UUID format"
        user1.id.matches(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)
        user2.id.matches(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)
    }

    def "should preserve data integrity"() {
        given: "user data"
        def email = "test@domain.com"
        def password = "hashedPassword123"
        def name = "Test User"

        when: "creating user with data"
        def user = new User(name, email, password, [])

        then: "data should be preserved exactly"
        user.name == name
        user.email == email
        user.password == password
        user.isActive == true
    }

    def "should verify constructor chain behavior"() {
        given: "timestamp before creation"
        def beforeCreation = LocalDateTime.now()

        when: "creating user with parameterized constructor"
        def user = new User("Test", "test@test.com", "pass", [])

        and: "timestamp after creation"
        def afterCreation = LocalDateTime.now()

        then: "constructor should have called default constructor"
        user.created.isAfter(beforeCreation) || user.created.isEqual(beforeCreation)
        user.created.isBefore(afterCreation) || user.created.isEqual(afterCreation)
        user.lastLogin.isAfter(beforeCreation) || user.lastLogin.isEqual(beforeCreation)
        user.lastLogin.isBefore(afterCreation) || user.lastLogin.isEqual(afterCreation)
        user.id != null
        user.isActive == true
    }
}