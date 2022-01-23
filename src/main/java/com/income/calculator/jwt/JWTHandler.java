package com.income.calculator.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.income.calculator.filter.AuthenticatedUser;
import com.income.calculator.util.KeyGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JWTHandler {

	@Inject
	private Logger logger;

	@Inject
	private KeyGenerator keyGenerator;
 
	@Inject
	@AuthenticatedUser
	Event<String> userAuthenticatedEvent; 
	
	public String issueToken(@NotNull String login, @Context UriInfo uriInfo) {
        Key key = keyGenerator.generateKey();
        String jwtToken = Jwts.builder()
                .setSubject(login)
                .setAudience(uriInfo.getBaseUri().toString())
                .setIssuer(uriInfo.getAbsolutePath().toString())
                .setIssuedAt(new Date())
                .setExpiration(toDate(LocalDateTime.now().plusMinutes(15L)))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        
        logger.info("#### generating token for a key : " + jwtToken + " - " + key);
        return jwtToken;

    }
	
	public void validateToken(String token) throws Exception {
		// Check if it was issued by the server and if it's not expired
		// Throw an Exception if the token is invalid

		Claims claims = extractClaimsFromToken(token); 
		String username = (String) claims.get("sub"); 
		userAuthenticatedEvent.fire(username);
	} 

	private Claims extractClaimsFromToken(String token) throws RuntimeException {
		Claims claims;
		try {
			Key key = keyGenerator.generateKey();
			claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException
				| IllegalArgumentException e) {
			throw new RuntimeException("token:" + token + " is invalid");
		}
		return claims;
	}

	private Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
}
