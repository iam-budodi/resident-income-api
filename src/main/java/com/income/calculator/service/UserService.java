package com.income.calculator.service;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.income.calculator.filter.AuthenticatedUser;
import com.income.calculator.jwt.JWTHandler;
import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;
import com.income.calculator.repository.UserRepository;
import com.income.calculator.util.PasswordDigest;
import com.income.calculator.util.UserInputValidator;

public class UserService {

	@Inject
	private JWTHandler jwtHandler;

	@Inject
	@AuthenticatedUser
	private User currentUser;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserInputValidator validator;

	public Response authenticateUser(@NotNull String login, @NotNull String password, @Context UriInfo uriInfo) {

		try {

			User user = userRepository.findUserByLoginAndPassword(login, password);

			if (user == null) {
				throw new SecurityException("Invalid username or password");
			}

			// Issue a token for the user
			String token = jwtHandler.issueToken(login, uriInfo);

			// Return the token on the response
			return Response.ok().header(AUTHORIZATION, "Bearer " + token).entity("Welcome back " + user.getFullName())
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password").build();
		}
	}

	public Response registerNewUser(@NotNull User user, @Context UriInfo uriInfo) {
		if (userRepository.findUserByLogin(user.getLogin()).size() > 0) {
			return Response.status(Response.Status.CONFLICT)
					.entity("Username " + user.getLogin() + " already exists, choose a different username").build();
		}

		if (!validator.isValid(user.getPassword())) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					"Password must contain at least 8 characters, one digit, one lowercase and uppercase and one special character")
					.build();
		}

		try {
			user = userRepository.createUser(user);
		} catch (Exception rollbackException) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		URI createdUri = uriInfo.getAbsolutePathBuilder().path(user.getUserId().toString()).build();
		return Response.created(createdUri).build();
	}

	public Response findAllUsers() {
		List<User> allUsers = userRepository.findAllUsers();

		if (allUsers.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("No user found").build();

		allUsers.stream().forEach(user -> user.setPassword(null)); // added
		return Response.ok(allUsers).build();
	}

	public Response findUsersByKeyword(@NotNull String keyword) {
		List<User> allMatchUsers = userRepository.findUsersByKeyword(keyword);

		if (allMatchUsers.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("No user found").build();

		allMatchUsers.stream().forEach(user -> user.setPassword(null)); // add
		return Response.ok(allMatchUsers).build();

	}

	public Response findUserById(@NotNull Long userId) {

		User user = userRepository.findUserById(userId);

		if (user == null)
			return Response.status(Response.Status.NOT_FOUND).entity("User don't exist").build();

		if ((currentUser.getUserId() != user.getUserId()) && (currentUser.getUserRole() != UserRole.ADMIN)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		user.setPassword(null); // added
		return Response.ok(user).build();
	}

	public Response countUsers() {
		Long nbOfUsers = userRepository.countAllUsers();

		if (nbOfUsers == 0)
			return Response.status(Response.Status.NO_CONTENT).build();

		return Response.ok(nbOfUsers).build();
	}

	public Response resetUserPassword(@NotNull String login, @NotNull String oldPassword, @NotNull String newPassword,
			@NotNull String confirmNewPassword) {

		User user;

		if (!newPassword.equals(confirmNewPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("New Password mismatch").build();
		} 

		if (!validator.isValid(newPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					"Password must contain at least 8 characters, one digit, one owercase and uppercase and one special character")
					.build();
		}

		try {

			user = userRepository.findUserByLoginAndPassword(login, oldPassword);
			String passPhrase = PasswordDigest.digestPassword(newPassword);
			if (user.getPassword().equals(passPhrase)) {
				return Response.status(Response.Status.BAD_REQUEST).entity("New Password mismatch").build();
				//				throw new SecurityException("Choose a different password");
			}

//			user.setPassword(newPassword);
			user.setPassword(passPhrase);
			userRepository.updateUser(user);

		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
		}

		catch (NoResultException nre) {
			user = null;
		}

		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("User doesn't exists.").build();
		}

		return Response.status(Response.Status.NO_CONTENT).build();
	}
	
	public Response updateUserProfile(@NotNull Long userId, @NotNull User user) {
		if (user == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		if (userId == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		if (!userId.equals(user.getUserId()))
			return Response.status(Response.Status.CONFLICT).entity(user).build(); 
		
		if (userRepository.findUserById(userId) == null)
			return Response.status(Response.Status.NOT_FOUND).build(); 
		
		try {
			userRepository.updateUser(user);
		}  catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
		}
		
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	public Response deleteUser(@NotNull Long userId) {
		User user = userRepository.findUserById(userId);

		if (user == null)
			return Response.status(Response.Status.NOT_FOUND).entity("User don't exist").build();

		userRepository.deleteUser(userId);

		return Response.status(Response.Status.NO_CONTENT).entity(user.getFirstName() + " is successful removed")
				.build();
	}
}
