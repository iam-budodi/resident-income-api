package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.security.Key;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.income.calculator.filter.AuthenticatedUser;
import com.income.calculator.filter.AuthenticatedUserProducer;
import com.income.calculator.filter.JWTAuthenticationFilter;
import com.income.calculator.filter.JWTAuthorizationFilter;
import com.income.calculator.filter.Secured;
import com.income.calculator.jwt.JWTHandler;
import com.income.calculator.listener.EMIAnalyzerListner;
import com.income.calculator.listener.EMICalculationListener;
import com.income.calculator.model.LoanRecord;
import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;
import com.income.calculator.repository.UserRepository;
import com.income.calculator.service.UserService;
import com.income.calculator.util.KeyGenerator;
import com.income.calculator.util.LoggerProducer;
import com.income.calculator.util.PasswordDigest;
import com.income.calculator.util.PasswordValidator;
import com.income.calculator.util.SimpleKeyGenerator;
import com.income.calculator.util.UserInputValidator;

import io.jsonwebtoken.Jwts;

@RunWith(Arquillian.class)
@RunAsClient
public class UserEndpointTest {

	private static final User TEST_USER = new User("Japhet", "Sebastian", "jeff", "P@$$w0rd2o2!", "0744608510",
			"japhet.sebastian01@gmail.com", "avatar");

	private static final User TEST_ADMIN = new User("Budodi", "Sebastian", "budodi", "P@$$w0rd_2o2!", UserRole.ADMIN,
			"0754608510", "japhetseba@gmail.com", "avatar");

	private static String userId;
	private static String anotherUserId;
	private static String adminId;
	private static String authUserToken;
	private static String authAdminToken;
	private static User userFoundInDatabase;
	private Client client;
	private WebTarget authTarget;
	private Response response;

	@ArquillianResource
	private URI baseURL;

	@Deployment(testable = false)
	public static Archive<?> createDeploymentPackage() {
		// Import Maven runtime dependencies
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class).addClass(User.class).addClass(UserRole.class)
				.addClass(LoanRecord.class).addClass(EMICalculationListener.class).addClass(EMIAnalyzerListner.class)
				.addClass(UserRepository.class).addClass(UserService.class).addClass(JWTHandler.class)
				.addClass(UserEndpoint.class).addClass(PasswordDigest.class).addClass(UserInputValidator.class)
				.addClass(PasswordValidator.class).addClass(KeyGenerator.class).addClass(SimpleKeyGenerator.class)
				.addClass(JWTAuthenticationFilter.class).addClass(AuthenticatedUserProducer.class)
				.addClass(JWTAuthorizationFilter.class).addClass(AuthenticatedUser.class).addClass(Secured.class)
				.addClass(LoggerProducer.class).addClass(JAXRSApplication.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml").addAsLibraries(files);
	}

	@Before
	public void initWebTarget() {
		client = ClientBuilder.newClient();
		authTarget = client.target(baseURL).path("api/users");
	}

	@Test
	@InSequence(1)
	public void shouldBlockUnauthorizedAccess() {
		response = authTarget.request(APPLICATION_JSON).get();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(2)
	public void shouldFailLogin() {
		Form form = new Form();
		form.param("login", "dummyLogin");
		form.param("password", "dummyPassword");

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));
	}

	@Test
	@InSequence(3)
	public void shouldSignupUser() {
		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(TEST_USER, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		// check the created user
		String location = response.getHeaderString("location");
		assertNotNull(location);

		userId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(4)
	public void shouldSignupAdmin() {
		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(TEST_ADMIN, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		// check the created user
		String location = response.getHeaderString("location");
		assertNotNull(location);

		adminId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(5)
	public void shouldLoginUser() {
		Form form = new Form();
		form.param("login", TEST_USER.getLogin());
		form.param("password", TEST_USER.getPassword());

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertNotNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));

		authUserToken = response.getHeaderString(HttpHeaders.AUTHORIZATION);
	}

	@Test
	@InSequence(6)
	public void shouldCheckLogedInUserJWToken() {
		String userToken = authUserToken.substring("Bearer".length()).trim();
		Key key = new SimpleKeyGenerator().generateKey();
		assertEquals(1, Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getHeader().size());
		assertEquals("HS512", Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getHeader().getAlgorithm());
		assertEquals(5, Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getBody().size());
		assertEquals(TEST_USER.getLogin(),
				Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getBody().getSubject());
		assertEquals(baseURL.toString().concat("api/users/login"),
				Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getBody().getIssuer());
		assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getBody().getIssuedAt());
		assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(userToken).getBody().getExpiration());
	}

	@Test
	@InSequence(7)
	public void shouldLoginAdmin() {
		Form form = new Form();
		form.param("login", TEST_ADMIN.getLogin());
		form.param("password", TEST_ADMIN.getPassword());

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertNotNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));

		authAdminToken = response.getHeaderString(HttpHeaders.AUTHORIZATION);
	}

	@Test
	@InSequence(8)
	public void shouldCheckLogedInAdminJWToken() {
		String adminToken = authAdminToken.substring("Bearer".length()).trim();
		Key key = new SimpleKeyGenerator().generateKey();
		assertEquals(1, Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getHeader().size());
		assertEquals("HS512", Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getHeader().getAlgorithm());
		assertEquals(5, Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getBody().size());
		assertEquals(TEST_ADMIN.getLogin(),
				Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getBody().getSubject());
		assertEquals(baseURL.toString().concat("api/users/login"),
				Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getBody().getIssuer());
		assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getBody().getIssuedAt());
		assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(adminToken).getBody().getExpiration());
	}

	@Test
	@InSequence(9)
	public void shouldDenyAccessToUser() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken).get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(10)
	public void shouldGrantAccessToAdmin() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken).get();
		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(2, response.readEntity(List.class).size());
	}

	@Test
	@InSequence(11)
	public void shouldDenyAccessToAdminWithUserToken() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken).get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(12)
	public void shouldDenyAccessToAdminWithExpiredToken() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION,
				"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJidWRvZGkiLCJhdWQiOiJodHRwOi8vMTI3LjAuMC4xOjgwODAvY2ZmMGEwNTctZjJiNi00MDczLWEzNjMtYTM0MmQyNzEwNWE3L2FwaS8iLCJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwODAvY2ZmMGEwNTctZjJiNi00MDczLWEzNjMtYTM0MmQyNzEwNWE3L2FwaS91c2Vycy9sb2dpbiIsImlhdCI6MTY0MDc4NjExMywiZXhwIjoxNjQwNzg3MDEzfQ.o-8M7nn9L8cfqr909QFnpjwJZsT-")
				.get();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(13)
	public void shouldAdminCountAllUser() {
		response = authTarget.path("count").request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, authAdminToken).get();
		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(Long.valueOf(2), response.readEntity(Long.class));
	}

	@Test
	@InSequence(14)
	public void shouldFailRegisterNewUserWithWeakPassword() {
		User failingUser = new User("Sam", "Sebastian", "sam", "passw!rd", "0744608510", "japhet.sebastian01@gmail.com",
				"avatar");

		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(failingUser, APPLICATION_JSON));
		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(15)
	public void shouldFailRegisterExistingUser() {
		User failingUser = new User("Japhet", "Sebastian", "jeff", "passw!rd", "0744608510",
				"japhet.sebastian01@gmail.com", "avatar");

		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(failingUser, APPLICATION_JSON));
		assertEquals(CONFLICT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(16)
	public void shouldAdminGetAllUsers() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken).get();
		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(2, response.readEntity(List.class).size());
	}

	@Test
	@InSequence(17)
	public void shouldForbidenUserFailGetAllUsers() {
		response = authTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken).get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(18)
	public void shouldAdminGetUserById() {
		response = authTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.get();
		assertEquals(OK.getStatusCode(), response.getStatus());

		userFoundInDatabase = response.readEntity(User.class);
		assertNotNull(userFoundInDatabase.getUserId());
		assertNotNull(userFoundInDatabase.getCreatedOn());
		assertNull(userFoundInDatabase.getPassword());
		assertEquals("Japhet", userFoundInDatabase.getFirstName());
	}

	@Test
	@InSequence(19)
	public void shouldAdminGetAdminProfileById() {
		response = authTarget.path(adminId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.get();
		assertEquals(OK.getStatusCode(), response.getStatus());

		User userFound = response.readEntity(User.class);
		assertNotNull(userFound.getUserId());
		assertNotNull(userFound.getCreatedOn());
		assertNull(userFound.getPassword());
		assertEquals("Budodi", userFound.getFirstName());
	}

	@Test
	@InSequence(20)
	public void shouldAuthenticatedUserFailGetAdminById() {
		response = authTarget.path(adminId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());

	}

	@Test
	@InSequence(21)
	public void shouldRegisterAnotherUser() {
		User anotherUser = new User("Joseph", "Edmund", "JEdmund", "B12js6dy$ˆ12", "0745608510", "josephe@gmail.com",
				"avatar");

		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(anotherUser, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		String location = response.getHeaderString("location");
		assertNotNull(location);

		anotherUserId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(22)
	public void shouldAuthenticatedUserFailGetAnotherUserById() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authUserToken).get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(23)
	public void shouldAuthenticatedAdminGetAnotherUserById() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authAdminToken).get();
		assertEquals(OK.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(24)
	public void shouldDenyUnauthorizedRequestToGetUserByKeyword() {
		response = authTarget.path("user").queryParam("name", "seba").request(APPLICATION_JSON).get();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(25)
	public void shouldDenyAuthorizedUserRequestToGetAnyUserByKeyword() {
		response = authTarget.path("user").queryParam("name", "seba").request(APPLICATION_JSON).get();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(26)
	public void shouldAdminGetUsersByKeyword() {
		response = authTarget.path("user").queryParam("name", "seba").request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authAdminToken).get();

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(2, response.readEntity(List.class).size());
	}

	@Test
	@InSequence(27)
	public void shouldAdminFailGetUserByUnknownKeyword() {
		response = authTarget.path("user").queryParam("name", "dani").request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authAdminToken).get();

		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(28)
	public void shouldFailResetUserPaswordWithSameOldPassword() {
		Form form = new Form();
		form.param("login", "jeff");
		form.param("oldPassword", "P@$$w0rd2o2!");
		form.param("newPassword", "P@$$w0rd2o2!");
		form.param("confirmNewPassword", "P@$$w0rd2o2!");

		response = authTarget.path("reset").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));
	}

	@Test
	@InSequence(29)
	public void shouldFailResetUserPaswordWithMismatchedNewPasswords() {
		Form form = new Form();
		form.param("login", "jeff");
		form.param("oldPassword", "P@$$w0rd2o2!");
		form.param("newPassword", "P@$$w0rd2o22$");
		form.param("confirmNewPassword", "B12js6dy$ˆ92");

		response = authTarget.path("reset").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));
	}

	@Test
	@InSequence(30)
	public void shouldResetUserPasword() {
		Form form = new Form();
		form.param("login", "jeff");
		form.param("oldPassword", "P@$$w0rd2o2!");
		form.param("newPassword", "B12js6dy$ˆ92");
		form.param("confirmNewPassword", "B12js6dy$ˆ92");

		response = authTarget.path("reset").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(31)
	public void shouldFailLoginUserAsThePasswordWasResetted() {
		Form form = new Form();
		form.param("login", TEST_USER.getLogin());
		form.param("password", TEST_USER.getPassword());

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));
	}

	@Test
	@InSequence(32)
	public void shouldLoginUserWithNewPassword() {
		Form form = new Form();
		form.param("login", TEST_USER.getLogin());
		form.param("password", "B12js6dy$ˆ92");

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertNotNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));

		authUserToken = response.getHeaderString(HttpHeaders.AUTHORIZATION);
	}

	@Test
	@InSequence(33)
	public void shouldUnauthenticatedEntityFailDeleteAnotherUser() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON).delete();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(34)
	public void shouldAunthenticatedUserFailDeleteAnotherUser() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authUserToken).delete();

		assertFalse(response.hasEntity());
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(35)
	public void shouldAuthenticatedAdminDeleteAnotherUser() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authAdminToken).delete();

		assertFalse(response.hasEntity());
		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(36)
	public void shouldAuthenticatedAdminCheckDeletedUser() {
		response = authTarget.path(anotherUserId).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authAdminToken).get();

		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(37)
	public void shouldAuthenticatedUserFailUpdateProfileWithUnknownPathId() {
		userFoundInDatabase.setAvatarUrl("Japhet Avatar");
		response = authTarget.path("6789").request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(userFoundInDatabase, APPLICATION_JSON));
		assertEquals(CONFLICT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(38)
	public void shouldAuthenticatedUserFailUpdateProfileWithNullUserObject() {
		userFoundInDatabase.setAvatarUrl("Japhet Avatar");
		response = authTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(null, APPLICATION_JSON));
		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(39)
	public void shouldAuthenticatedUserUpdateProfileWhenStoredInDatabase() {
		TEST_USER.setUserId(userFoundInDatabase.getUserId());
		TEST_USER.setAvatarUrl("Japhet Avatar");
		response = authTarget.path(String.valueOf(userFoundInDatabase.getUserId())).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authUserToken).put(Entity.entity(TEST_USER, APPLICATION_JSON));
		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(40)
	public void shouldAuthenticatedUserFailUpdateProfileWithNullPassword() {
		userFoundInDatabase.setAvatarUrl("Japhet Avatar");
		response = authTarget.path(String.valueOf(userFoundInDatabase.getUserId())).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(userFoundInDatabase, APPLICATION_JSON));
		assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(41)
	public void shouldAuthenticatedUserFailUpdateProfileWithNullUserId() {
		userFoundInDatabase.setAvatarUrl("Japhet Avatar");
		userFoundInDatabase.setUserId(null);
		response = authTarget.path(String.valueOf(userFoundInDatabase.getUserId())).request(APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(userFoundInDatabase, APPLICATION_JSON));
		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(42)
	public void shouldAdminGetUpdateUserObject() {
		response = authTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.get();
		assertEquals(OK.getStatusCode(), response.getStatus());

		User updatedUser = response.readEntity(User.class);
		assertFalse(updatedUser.equals(userFoundInDatabase));
	}

}
