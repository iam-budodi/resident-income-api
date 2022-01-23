package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
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
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
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
import com.income.calculator.repository.LoanRecordRepository;
import com.income.calculator.repository.UserRepository;
import com.income.calculator.service.LoanRecordService;
import com.income.calculator.service.UserService;
import com.income.calculator.util.KeyGenerator;
import com.income.calculator.util.LoggerProducer;
import com.income.calculator.util.PasswordDigest;
import com.income.calculator.util.PasswordValidator;
import com.income.calculator.util.SimpleKeyGenerator;
import com.income.calculator.util.UserInputValidator;

@RunWith(Arquillian.class)
@RunAsClient
public class LoanRecordEndpointTest {

	private static final User TEST_USER = new User("Japhet", "Sebastian", "jeff", "P@$$w0rd2o2!", "0744608510",
			"japhet.sebastian01@gmail.com", "avatar");

	private static final User TEST_ADMIN = new User("Budodi", "Sebastian", "budodi", "P@$$w0rd_2o2!", UserRole.ADMIN,
			"0754608510", "japhetseba@gmail.com", "avatar");

	private static final LoanRecord LOAN = new LoanRecord("EXIM", "Emergency", 950000L, 0L, 1L);
	private static final LoanRecord ANOTHER_LOAN = new LoanRecord("EXIM", "Staff", 7650000L, 8L, 5L);

	private static String userId;
	private static String adminId;
//	private static String loanId;
	private static String authUserToken;
	private static String authAdminToken;

	private Client client;
	private WebTarget authTarget;
	private WebTarget webTarget;
	private Response response;

	@ArquillianResource
	private URI baseURL;

	@Deployment(testable = false)
	public static Archive<?> createDeploymentPackage() {
		// Import Maven runtime dependencies
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class).addClass(User.class).addClass(UserRole.class)
				.addClass(LoanRecord.class).addClass(UserRepository.class).addClass(UserService.class)
				.addClass(JWTHandler.class).addClass(LoanRecordRepository.class).addClass(EMICalculationListener.class)
				.addClass(EMIAnalyzerListner.class).addClass(LoanRecordService.class).addClass(LoanRecordEndpoint.class)
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
//		client = ClientBuilder.newClient();
		client = ClientBuilder.newBuilder().build();
		authTarget = client.target(baseURL).path("api/users");
		webTarget = client.target(baseURL).path("api/loans");
	}

	@Test
	@InSequence(1)
	public void shouldSignupUser() {
		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(TEST_USER, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		// check the created user
		String location = response.getHeaderString("location");
		assertNotNull(location);

		userId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(2)
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
	@InSequence(3)
	public void shouldRegisterAdmin(@ArquillianResteasyResource("api/users") WebTarget authTarget) {
		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(TEST_ADMIN, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		// check the created user
		String location = response.getHeaderString("location");
		assertNotNull(location);

		adminId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(4)
	public void shouldLoginAdmin(@ArquillianResteasyResource("api/users") WebTarget authTarget) {
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
	@InSequence(5)
	public void shouldUnauthenticatedUserFailToAddLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).put(Entity.entity(LOAN, APPLICATION_JSON));

		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(6)
	public void shouldAuthenticatedUserFailToAddLoanRecordWithUnmatchedId() {
		response = webTarget.path("2").request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(LOAN, APPLICATION_JSON));

		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(7)
	public void shouldAuthenticatedAdminFailToAddLoanRecordWithUserId() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.put(Entity.entity(LOAN, APPLICATION_JSON));

		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(8)
	public void shouldAuthenticatedAdminFailToAddLoanRecordWithUserToken() {
		response = webTarget.path(adminId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(LOAN, APPLICATION_JSON));

		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(9)
	public void shouldAuthenticatedUserAddLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(LOAN, APPLICATION_JSON));

		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}
	
	@Test
	@InSequence(10)
	public void shouldAuthenticatedUserAddAnotherLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.put(Entity.entity(ANOTHER_LOAN, APPLICATION_JSON));
		
		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(11)
	public void shouldAuthenticatedAdminFailToFetchLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.get();

		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(12)
	public void shouldAuthenticatedUserFetchLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.get();

		System.out.println("\n\n#### LOANS FROM TEST : " + response.readEntity(List.class) + "\n\n");
		assertEquals(OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	@InSequence(13)
	public void shouldUnauthenticatedUserFailToCheckMonthlyDeduction() {
		response = webTarget.path("installment/" + userId).request(TEXT_PLAIN).get();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}
	
	@Test
	@InSequence(14)
	public void shouldAuthenticatedUserFailToCheckOtherUserDeduction() {
		response = webTarget.path("installment/" + userId).request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, authAdminToken)
				.get();
		assertEquals(FORBIDDEN.getStatusCode(), response.getStatus());
	}
	
	@Test
	@InSequence(15)
	public void shouldAuthenticatedUserGetTotalDeduction() {
		response = webTarget.path("installment/" + userId).request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.get();

		System.out.println("\n\n#### INSTALLMENT FROM TEST : " + response.readEntity(String.class) + "\n\n");
		assertEquals(OK.getStatusCode(), response.getStatus());
	}
	
	// TODO: figure out how to delete all and fix and test delete endpoints
	
	@Test
	@InSequence(16)
	public void shouldAuthenticatedUserDeleteLoanRecord() {
		response = webTarget.path(userId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authUserToken)
				.get();

		System.out.println("\n\n#### LOANS FROM TEST : " + response.readEntity(List.class) + "\n\n");
		assertEquals(OK.getStatusCode(), response.getStatus());
	}
	
}
