package com.income.calculator.filter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class JWTAuthorizationFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;
	
	@Context
	private SecurityContext securityContext;
	
	@Inject
	@AuthenticatedUser
    private User currentUser;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		// Get the resource class which matches with the requested URL
		// Extract the UserRoles declared by it
		Class<?> resourceClass = resourceInfo.getResourceClass();
		List<UserRole> classRoles = extractRoles(resourceClass);

		// Get the resource method which matches with the requested URL
		// Extract the UserRoles declared by it
		Method resourceMethod = resourceInfo.getResourceMethod();
		List<UserRole> methodRoles = extractRoles(resourceMethod);

		try {

			// Check if the user is allowed to execute the method
			// The method annotations override the class annotations
			if (methodRoles.isEmpty()) {
				checkPermissions(classRoles);
			} else {
				checkPermissions(methodRoles);
			}

		} catch (Exception e) {
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
		}
	}

	// Extract the UserRoles from the annotated element
	private List<UserRole> extractRoles(AnnotatedElement annotatedElement) {
		if (annotatedElement == null) {
			return new ArrayList<UserRole>();
		} else {
			Secured secured = annotatedElement.getAnnotation(Secured.class);
			if (secured == null) {
				return new ArrayList<UserRole>();
			} else {
				UserRole[] allowedUserRoles = secured.permission();
				return Arrays.asList(allowedUserRoles);
			}
		}
	}

	private void checkPermissions(List<UserRole> allowedUserRoles) throws Exception {
		// Check if the user contains one of the allowed UserRoles
		// Throw an Exception if the user has not permission to execute the method
		// get user info from the securityContext directly
 
		if (!allowedUserRoles.contains(currentUser.getUserRole())) {
//			logger.info("\n\n#### ORG FAILED USER ROLES : " + currentUser.toString());
			throw new ForbiddenException("roles: " + currentUser.getUserRole() + " is not allowed");
		}
	}

}
