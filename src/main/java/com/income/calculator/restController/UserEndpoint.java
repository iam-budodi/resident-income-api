package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.income.calculator.filter.Secured;
import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;
import com.income.calculator.service.UserService;

@Named
@SessionScoped
@SuppressWarnings("serial")
@Path("/users")
public class UserEndpoint implements Serializable {

	@Inject
	private BeanManager beanManager;

	@Inject
	transient private UserService userService;

	@POST
	@Consumes(APPLICATION_JSON)
	public Response registerNewUser(User user, @Context UriInfo uriInfo) {
		return userService.registerNewUser(user, uriInfo);
	}

	@POST
	@Path("/login")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response authenticateUser(@FormParam("login") String login, @FormParam("password") String password,
			@Context UriInfo uriInfo) {
		return userService.authenticateUser(login, password, uriInfo);

	}
	
	@POST
	@Path("reset")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response resetUserPassword(@FormParam("login") String login, @FormParam("oldPassword") String oldPassword,
			@FormParam("newPassword") String newPassword, @FormParam("confirmNewPassword") String confirmNewPassword) {
		return userService.resetUserPassword(login, oldPassword, newPassword, confirmNewPassword);

	}

	@POST
	@Path("/logout")
	public Response logoutUser() {
		AlterableContext ctx = (AlterableContext) beanManager.getContext(SessionScoped.class);
		Bean<?> myBean = beanManager.getBeans(UserEndpoint.class).iterator().next();
		ctx.destroy(myBean);

		return Response.status(Response.Status.OK).build();
	}
	
	@GET
	@Path("/{id: \\d+}")
	@Secured(permission = { UserRole.ADMIN, UserRole.USER })
	@Produces(APPLICATION_JSON)
	public Response findUserById(@PathParam("id") @Min(1) Long userId) {
		return userService.findUserById(userId);
	}

	@GET
	@Secured(permission = UserRole.ADMIN)
	@Produces(APPLICATION_JSON)
	public Response findAllUsers() {
		return userService.findAllUsers();
	}

	@GET
	@Path("/user")
	@Secured(permission = UserRole.ADMIN)
	@Produces(APPLICATION_JSON)
	public Response findUsersByKeyword(@QueryParam("name") String name) {
		return userService.findUsersByKeyword(name);
	}

	@GET
	@Path("/count")
	@Secured(permission = UserRole.ADMIN)
	@Produces(TEXT_PLAIN)
	public Response countUsers() {
		return userService.countUsers();
	}

	@PUT
	@Path("/{id: \\d+}")
	@Secured(permission = { UserRole.ADMIN, UserRole.USER })
	@Consumes(APPLICATION_JSON)
	public Response updateUserProfile(@PathParam("id") @Min(1) Long userId, User user) {
		return userService.updateUserProfile(userId, user);
	}

	@DELETE
	@Path("/{id: \\d+}")
	@Secured(permission = UserRole.ADMIN) 
	public Response deleteUser(@PathParam("id") @Min(1) Long userId) {
		return userService.deleteUser(userId);
	}

}
