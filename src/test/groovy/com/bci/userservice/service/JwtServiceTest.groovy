package com.bci.userservice.service

import com.bci.userservice.config.JwtConfig
import spock.lang.Specification
import spock.lang.Subject

class JwtServiceTest extends Specification {

    def jwtConfig = Mock(JwtConfig);

    @Subject
    JwtService jwtService = new JwtService();

    def setup() {
        jwtService.jwtConfig = jwtConfig;
        jwtConfig.getSecret() >> "myVerySecretKeyForJWTTokenGenerationThatIsLongEnough";
        jwtConfig.getExpiration() >> 86400000L; // 24 hours
    }

    def "should generate valid JWT token"() {
        given: "a user email"
        def email = "test@domain.com";

        when: "generating a token"
        def token = jwtService.generateToken(email);

        then: "token should not be null or empty"
        token != null;
        token.length() > 0;

        and: "token should be valid"
        jwtService.isTokenValid(token);

        and: "should be able to extract email from token"
        def extractedEmail = jwtService.getEmailFromToken(token);
        extractedEmail.isPresent();
        extractedEmail.get() == email;
    }

    def "should return empty optional for invalid token"() {
        given: "an invalid token"
        def invalidToken = "invalid.jwt.token";

        when: "extracting email from invalid token"
        def result = jwtService.getEmailFromToken(invalidToken);

        then: "should return empty optional"
        result.isEmpty();
    }

    def "should return false for invalid token validation"() {
        given: "an invalid token"
        def invalidToken = "invalid.jwt.token";

        when: "validating invalid token"
        def isValid = jwtService.isTokenValid(invalidToken);

        then: "should return false"
        !isValid;
    }

    def "should handle null token gracefully"() {
        when: "validating null token"
        def isValid = jwtService.isTokenValid(null);

        then: "should return false"
        !isValid;

        when: "extracting email from null token"
        def result = jwtService.getEmailFromToken(null);

        then: "should return empty optional"
        result.isEmpty();
    }

    def "should handle empty token gracefully"() {
        when: "validating empty token"
        def isValid = jwtService.isTokenValid("");

        then: "should return false"
        !isValid;

        when: "extracting email from empty token"
        def result = jwtService.getEmailFromToken("");

        then: "should return empty optional"
        result.isEmpty();
    }

    def "should handle malformed JWT token"() {
        given: "a malformed token"
        def malformedToken = "header.payload" // Missing signature

        when: "validating malformed token"
        def isValid = jwtService.isTokenValid(malformedToken)

        then: "should return false"
        !isValid

        when: "extracting email from malformed token"
        def result = jwtService.getEmailFromToken(malformedToken)

        then: "should return empty optional"
        result.isEmpty()
    }

    def "should generate different tokens for different emails"() {
        given: "two different emails"
        def email1 = "user1@domain.com"
        def email2 = "user2@domain.com"

        when: "generating tokens for both emails"
        def token1 = jwtService.generateToken(email1)
        def token2 = jwtService.generateToken(email2)

        then: "tokens should be different"
        token1 != token2

        and: "each token should contain correct email"
        jwtService.getEmailFromToken(token1).get() == email1
        jwtService.getEmailFromToken(token2).get() == email2
    }

    def "should handle whitespace in token"() {
        given: "a token with whitespace"
        def tokenWithSpaces = "  eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalid  "

        when: "validating token with spaces"
        def isValid = jwtService.isTokenValid(tokenWithSpaces)

        then: "should handle gracefully"
        !isValid
    }

    def "should handle token with invalid signature"() {
        given: "a token with invalid signature"
        def tokenWithInvalidSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGRvbWFpbi5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.invalidSignature"

        when: "validating token with invalid signature"
        def isValid = jwtService.isTokenValid(tokenWithInvalidSignature)

        then: "should return false"
        !isValid

        when: "extracting email from token with invalid signature"
        def result = jwtService.getEmailFromToken(tokenWithInvalidSignature)

        then: "should return empty optional"
        result.isEmpty()
    }

    def "should handle token with different secret"() {
        given: "a service with different secret"
        def differentJwtService = new JwtService()
        def differentConfig = Mock(JwtConfig)
        differentConfig.getSecret() >> "differentSecretKeyThatIsLongEnoughForHMAC"
        differentConfig.getExpiration() >> 86400000L
        differentJwtService.jwtConfig = differentConfig

        and: "a token generated with original service"
        def email = "test@domain.com"
        def token = jwtService.generateToken(email)

        when: "validating with service using different secret"
        def isValid = differentJwtService.isTokenValid(token)

        then: "should return false"
        !isValid

        when: "extracting email with different secret"
        def result = differentJwtService.getEmailFromToken(token)

        then: "should return empty optional"
        result.isEmpty()
    }

    def "should generate token with correct claims"() {
        given: "a user email"
        def email = "test@domain.com"

        when: "generating a token"
        def token = jwtService.generateToken(email)

        and: "extracting email from token"
        def extractedEmail = jwtService.getEmailFromToken(token)

        then: "should extract correct email"
        extractedEmail.isPresent()
        extractedEmail.get() == email
    }

    def "should handle special characters in email"() {
        given: "an email with special characters"
        def specialEmail = "test+user@domain-name.co.uk"

        when: "generating token"
        def token = jwtService.generateToken(specialEmail)

        then: "should generate valid token"
        token != null
        token.length() > 0

        and: "should extract correct email"
        def extractedEmail = jwtService.getEmailFromToken(token)
        extractedEmail.isPresent()
        extractedEmail.get() == specialEmail
    }

    def "should validate multiple tokens are functional"() {
        given: "multiple tokens for same email"
        def email = "test@domain.com"

        when: "generating multiple tokens"
        def token1 = jwtService.generateToken(email)
        def token2 = jwtService.generateToken(email)

        then: "both should be valid tokens"
        token1 != null
        token2 != null
        jwtService.isTokenValid(token1)
        jwtService.isTokenValid(token2)

        and: "both should contain same email"
        jwtService.getEmailFromToken(token1).get() == email
        jwtService.getEmailFromToken(token2).get() == email
    }

    def "should handle expired token correctly"() {
        given: "a service with very short expiration"
        def shortExpirationService = new JwtService()
        def shortExpirationConfig = Mock(JwtConfig)
        shortExpirationConfig.getSecret() >> "myVerySecretKeyForJWTTokenGenerationThatIsLongEnough"
        shortExpirationConfig.getExpiration() >> 1L // 1 millisecond
        shortExpirationService.jwtConfig = shortExpirationConfig

        when: "generating token with short expiration"
        def token = shortExpirationService.generateToken("test@domain.com")

        and: "waiting for expiration"
        Thread.sleep(10) // Wait longer than expiration

        then: "token should be invalid due to expiration"
        !shortExpirationService.isTokenValid(token)

        and: "should not extract email from expired token"
        shortExpirationService.getEmailFromToken(token).isEmpty()
    }

    def "should handle very long email addresses"() {
        given: "a very long email address"
        def longEmail = "verylongusernamethatexceedsnormallengths@verylongdomainnamethatalsosexceedsnormallengths.com"

        when: "generating token with long email"
        def token = jwtService.generateToken(longEmail)

        then: "should generate valid token"
        token != null
        jwtService.isTokenValid(token)

        and: "should extract correct long email"
        def extractedEmail = jwtService.getEmailFromToken(token)
        extractedEmail.isPresent()
        extractedEmail.get() == longEmail
    }

    def "should handle token with tampered payload"() {
        given: "a valid token"
        def email = "test@domain.com"
        def validToken = jwtService.generateToken(email)

        and: "tampered token (changed one character in payload)"
        def tokenParts = validToken.split("\\.")
        def tamperedPayload = tokenParts[1].substring(0, tokenParts[1].length() - 1) + "X"
        def tamperedToken = tokenParts[0] + "." + tamperedPayload + "." + tokenParts[2]

        when: "validating tampered token"
        def isValid = jwtService.isTokenValid(tamperedToken)

        then: "should return false"
        !isValid

        when: "extracting email from tampered token"
        def result = jwtService.getEmailFromToken(tamperedToken)

        then: "should return empty optional"
        result.isEmpty()
    }

    def "should handle JWT with only header and payload"() {
        given: "incomplete JWT token"
        def incompleteToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0"

        when: "validating incomplete token"
        def isValid = jwtService.isTokenValid(incompleteToken)

        then: "should return false"
        !isValid

        when: "extracting email from incomplete token"
        def result = jwtService.getEmailFromToken(incompleteToken)

        then: "should return empty optional"
        result.isEmpty()
    }

    def "should handle token with too many parts"() {
        given: "token with extra parts"
        def extraPartsToken = "header.payload.signature.extra"

        when: "validating token with extra parts"
        def isValid = jwtService.isTokenValid(extraPartsToken)

        then: "should return false"
        !isValid
    }

    def "should generate tokens with proper structure"() {
        given: "a user email"
        def email = "test@domain.com"

        when: "generating a token"
        def token = jwtService.generateToken(email)

        then: "token should have proper JWT structure (3 parts separated by dots)"
        def parts = token.split("\\.")
        parts.length == 3
        parts[0].length() > 0 // header
        parts[1].length() > 0 // payload
        parts[2].length() > 0 // signature
    }

    def "should handle numeric characters in email"() {
        given: "an email with numbers"
        def numericEmail = "user123@domain456.com"

        when: "generating token"
        def token = jwtService.generateToken(numericEmail)

        then: "should generate valid token"
        token != null
        jwtService.isTokenValid(token)

        and: "should extract correct email"
        def extractedEmail = jwtService.getEmailFromToken(token)
        extractedEmail.isPresent()
        extractedEmail.get() == numericEmail
    }

    def "should handle emails with dots and underscores"() {
        given: "an email with dots and underscores"
        def complexEmail = "first.last_name@sub.domain.com"

        when: "generating token"
        def token = jwtService.generateToken(complexEmail)

        then: "should generate valid token"
        token != null
        jwtService.isTokenValid(token)

        and: "should extract correct email"
        def extractedEmail = jwtService.getEmailFromToken(token)
        extractedEmail.isPresent()
        extractedEmail.get() == complexEmail
    }
}