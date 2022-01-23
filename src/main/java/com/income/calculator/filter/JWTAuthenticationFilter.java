package com.income.calculator.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.income.calculator.jwt.JWTHandler;

/**
 * Gets the HTTP Authorization header from the request checks for the JSon Web
 * Token i.e the Bearer string, validates the token using the JJWT library, If
 * the token is valid, method is invoked otherwise a 401 Unauthorized is sent to
 * the client
 *
 */

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Inject
	private JWTHandler jwtHandler;

	@Inject
	private Logger logger;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		// Get the HTTP Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		logger.info("#### authorizationHeader : " + authorizationHeader);

		// Check if the HTTP Authorization header is present and formatted correctly
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			logger.info("#### invalid authorizationHeader : " + authorizationHeader);
			throw new NotAuthorizedException("Authorization header must be provided");
		}

		// Extract the token from the HTTP Authorization header
		String token = authorizationHeader.substring("Bearer".length()).trim();

		try {

			jwtHandler.validateToken(token);

		} catch (Exception e) {
			logger.severe("#### invalid token : " + token);
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}

	}
}
