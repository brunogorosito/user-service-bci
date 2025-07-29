// JwtServiceTest.groovy
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
}