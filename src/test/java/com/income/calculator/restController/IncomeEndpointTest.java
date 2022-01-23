package com.income.calculator.restController;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.income.calculator.commonMath.MathEngine;
import com.income.calculator.filter.AuthenticatedUser;
import com.income.calculator.filter.AuthenticatedUserProducer;
import com.income.calculator.filter.JWTAuthenticationFilter;
import com.income.calculator.filter.JWTAuthorizationFilter;
import com.income.calculator.filter.Secured;
import com.income.calculator.jwt.JWTHandler;
import com.income.calculator.listener.EMIAnalyzerListner;
import com.income.calculator.listener.EMICalculationListener;
import com.income.calculator.model.Income;
import com.income.calculator.model.IncomeCategory;
import com.income.calculator.model.LoanRecord;
import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;
import com.income.calculator.repository.IncomeRepository;
import com.income.calculator.repository.UserRepository;
import com.income.calculator.service.IncomeService;
import com.income.calculator.service.UserService;
import com.income.calculator.util.KeyGenerator;
import com.income.calculator.util.LoggerProducer;
import com.income.calculator.util.PasswordDigest;
import com.income.calculator.util.PasswordValidator;
import com.income.calculator.util.SimpleKeyGenerator;
import com.income.calculator.util.UserInputValidator;

@RunWith(Arquillian.class)
@RunAsClient
public class IncomeEndpointTest {

	private static final User TEST_ADMIN = new User("Budodi", "Sebastian", "budodi", "P@$$w0rd_2o2!", UserRole.ADMIN,
			"0754608510", "japhetseba@gmail.com", "avatar");
	private static final Income UNTAXABL_INCOME = new Income(IncomeCategory.Untaxable_Class, 0L, 270000L, 0L, 0L,
			"description");
	private static final Income LOW_INCOME = new Income(IncomeCategory.Low_Class, 270000L, 520000L, 0L, 8L,
			"description");
	private static final Income HIGH_INCOME = new Income(IncomeCategory.High_Class, 1000000L, Long.MAX_VALUE, 128000L, 30L,
			"description");

	private Response response;
	private static String untaxableIncomeId;
	private static String lowIncomeId;
	private static String authToken;

	@Deployment(testable = false)
	public static Archive<?> createDeploymentPackage() {
		// Import Maven runtime dependencies
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class).addClass(Income.class).addClass(IncomeCategory.class)
				.addClass(IncomeRepository.class).addClass(IncomeService.class).addClass(MathEngine.class)
				.addClass(IncomeEndpoint.class).addClass(User.class).addClass(UserRole.class).addClass(LoanRecord.class)
				.addClass(EMICalculationListener.class).addClass(EMIAnalyzerListner.class)
				.addClass(UserRepository.class).addClass(UserService.class).addClass(JWTHandler.class)
				.addClass(UserEndpoint.class).addClass(PasswordDigest.class).addClass(UserInputValidator.class)
				.addClass(PasswordValidator.class).addClass(KeyGenerator.class).addClass(SimpleKeyGenerator.class)
				.addClass(JWTAuthenticationFilter.class).addClass(AuthenticatedUserProducer.class)
				.addClass(JWTAuthorizationFilter.class).addClass(AuthenticatedUser.class).addClass(Secured.class)
				.addClass(LoggerProducer.class).addClass(JAXRSApplication.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml").addAsLibraries(files);
	}

	@Test
	@InSequence(1)
	public void shouldGetNoIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).get();

		assertFalse(response.hasEntity());
		assertEquals(-1, response.getLength());
		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());

	}

	@Test
	@InSequence(2)
	public void shouldRegisterAdmin(@ArquillianResteasyResource("api/users") WebTarget authTarget) {
		response = authTarget.request(APPLICATION_JSON).post(Entity.entity(TEST_ADMIN, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(3)
	public void shouldLoginAdmin(@ArquillianResteasyResource("api/users") WebTarget authTarget) {
		Form form = new Form();
		form.param("login", TEST_ADMIN.getLogin());
		form.param("password", TEST_ADMIN.getPassword());

		response = authTarget.path("login").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertNotNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));

		authToken = response.getHeaderString(HttpHeaders.AUTHORIZATION);
	}

	@Test
	@InSequence(4)
	public void shouldUnauthorizedUserFailCreateIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).post(Entity.entity(UNTAXABL_INCOME, APPLICATION_JSON));
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(5)
	public void shouldAuthenticatedAdminCreateUntaxableIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(UNTAXABL_INCOME, APPLICATION_JSON));

		assertEquals(CREATED.getStatusCode(), response.getStatus());

		String location = response.getHeaderString("location");
		assertNotNull(location);
		untaxableIncomeId = location.substring(location.lastIndexOf("/") + 1);
	}

	@Test
	@InSequence(6)
	public void shouldAuthenticatedAdminCreateLowIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(LOW_INCOME, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());

		String location = response.getHeaderString("location");
		assertNotNull(location);
		lowIncomeId = location.substring(location.lastIndexOf("/") + 1);
	}
	
	@Test
	@InSequence(7)
	public void shouldAuthenticatedAdminCreateHighIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(HIGH_INCOME, APPLICATION_JSON));
		assertEquals(CREATED.getStatusCode(), response.getStatus());
		
		String location = response.getHeaderString("location");
		assertNotNull(location);
	}

	@Test
	@InSequence(8)
	public void shouldAllAuthenticatedAndUnauthenticatedUserGetAllIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).get();

		assertTrue(response.hasEntity());
		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(3, response.readEntity(List.class).size());

	}

	@Test
	@InSequence(9)
	public void shouldAllAuthenticatedAndUnauthenticatedUserFindCreatedIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(untaxableIncomeId).request(APPLICATION_JSON).get();
		assertEquals(OK.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(10)
	public void shouldAuthenticatedAdminFindCreatedIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.get();

		assertEquals(OK.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(11)
	public void shouldCheckUntaxableIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(untaxableIncomeId).request(APPLICATION_JSON).get();

		Income incomeFound = response.readEntity(Income.class);

		assertNotNull(incomeFound.getId());
		assertTrue(incomeFound.getIncomeLimit() == 270000L);
		assertEquals(IncomeCategory.Untaxable_Class, incomeFound.getCategory());
	}

	@Test
	@InSequence(12)
	public void shouldCheckLowIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).get();

		Income incomeFound = response.readEntity(Income.class);

		assertNotNull(incomeFound.getId());
		assertTrue(incomeFound.getIncomeLimit() == 520000L);
		assertEquals(IncomeCategory.Low_Class, incomeFound.getCategory());
	}

	@Test
	@InSequence(13)
	public void shouldGetUntaxableIcomeGivenAmount(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "240000");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());

		@SuppressWarnings("unchecked")
		Map<String, Double> resultString = response.readEntity(HashMap.class);
		assertEquals(270000.0, resultString.get("individualClassLimit"), 0.0);
	}

	@Test
	@InSequence(14)
	public void shouldGetUntaxableIcomeGivenUntaxableIncomeLimit(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "270000");
		form.param("heslb", "true");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());

		@SuppressWarnings("unchecked")
		Map<String, Double> resultString = response.readEntity(HashMap.class);
		assertEquals(270000.0, resultString.get("individualClassLimit"), 0.0);
	}

	@Test
	@InSequence(15)
	public void shouldGetLowIcomeGivenAmount(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "300001");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());

		@SuppressWarnings("unchecked")
		Map<String, Double> resultString = response.readEntity(HashMap.class);
		assertEquals(520000.0, resultString.get("individualClassLimit"), 0.0);
	}

	@Test
	@InSequence(16)
	public void shouldGetLowIcomeGivenLowIncomeLimit(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "520000");
		form.param("heslb", "true");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());

		@SuppressWarnings("unchecked")
		Map<String, Double> resultString = response.readEntity(HashMap.class);
		assertEquals(520000.0, resultString.get("individualClassLimit"), 0.0);
	}
	
	@Test
	@InSequence(17)
	public void shouldGetLowIcomeGivenHighIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "1120000");
		form.param("heslb", "true");
		
		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));
		
		assertEquals(OK.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(18)
	public void shouldAuthenticatedAdminCreateMiddleIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Income middleIncome = new Income(IncomeCategory.Middle_Class, 520000L, 760000L, 20000L, 20L, "description");

		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(middleIncome, APPLICATION_JSON));

		assertEquals(CREATED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(19)
	public void shouldGetMiddleIcomeGivenMiddleIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "780000");
		form.param("heslb", "true");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(OK.getStatusCode(), response.getStatus());

		@SuppressWarnings("unchecked")
		Map<String, Double> resultString = response.readEntity(HashMap.class);
		assertEquals(760000.0, resultString.get("individualClassLimit"), 0.0);
	}

	@Test()
	@InSequence(20)
	public void shouldNotGetIcomeGivenUnknownArguementValue(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "-1");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test()
	@InSequence(21)
	public void shouldNotGetIcomeGivenEmptyArguementValue(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Form form = new Form();
		form.param("income", "");

		response = webTarget.path("compute").request(APPLICATION_JSON)
				.post(Entity.entity(form, APPLICATION_FORM_URLENCODED));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(22)
	public void shouldUnauthorizedUserFailToUpdateLowIncomeMedium(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).get();

		Income incomeFound = response.readEntity(Income.class);
		incomeFound.setCategory(IncomeCategory.Middle_Class);
		incomeFound.setIncomeLimit(760000L);
		incomeFound.setIncome(520000L);

		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON)
				.put(Entity.entity(incomeFound, APPLICATION_JSON));

		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(23)
	public void shouldAuthorizedAdminUpdateLowIncomeMedium(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Income incomeFound = webTarget.path(lowIncomeId).request(APPLICATION_JSON).get().readEntity(Income.class);
		incomeFound.setCategory(IncomeCategory.Middle_Class);
		incomeFound.setIncomeLimit(760000L);
		incomeFound.setIncome(520000L);

		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.put(Entity.entity(incomeFound, APPLICATION_JSON));

		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(24)
	public void shouldUnAuthorizedUserFailDeleteCreatedIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).delete();
		assertEquals(UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(25)
	public void shouldAuthorizedAdminDeleteCreatedIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.delete();

		assertFalse(response.hasEntity());
		assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(26)
	public void shouldCheckTheDeleteIncome(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(lowIncomeId).request(APPLICATION_JSON).get();

		assertFalse(response.hasEntity());
		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(27)
	public void shouldGetOnlyTwoIncomeRecord(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).get();

		assertEquals(OK.getStatusCode(), response.getStatus());
		assertEquals(3, response.readEntity(List.class).size());
	}

	@Test
	@InSequence(28)
	public void shouldAuthorizedAdminFailToCreateNullIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken).post(null);

		assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
		String location = response.getHeaderString("location");
		assertNull(location);
	}

	@Test
	@InSequence(29)
	public void shouldAuthorizedAdminFailToCreateIncomeWithNullCategory(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Income income = new Income(null, 0L, 270000L, 0L, 0L, "description");

		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(income, APPLICATION_JSON));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

	}

	@Test
	@InSequence(30)
	public void shouldAuthorizedAdminFailToCreateIncomeWithLowIncome(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Income income = new Income(IncomeCategory.Untaxable_Class, -1L, 270000L, 0L, 0L, "description");
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(income, APPLICATION_JSON));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

	}

	@Test
	@InSequence(31)
	public void shouldAuthorizedAdminFailToCreateIncomeWithLowIncomeLimit(
			@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		Income income = new Income(IncomeCategory.Untaxable_Class, 0L, 260000L, 0L, 0L, "description");
		response = webTarget.request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.post(Entity.entity(income, APPLICATION_JSON));

		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());

	}

	@Test(expected = Exception.class)
	@InSequence(32)
	public void shouldFailInvokingFindIncomeByNullId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(null).request(APPLICATION_JSON).get();
	}

	@Test
	@InSequence(33)
	public void shouldNotFindIncomeByUnknownId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path("999").request(APPLICATION_JSON).get();
		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test(expected = Exception.class)
	@InSequence(34)
	public void shouldFailInvokingDeleteIncomeByNullId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path(null).request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken).delete();
	}

	@Test
	@InSequence(35)
	public void shouldFailInvokingFindIncomeByZeroId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path("0").request(APPLICATION_JSON).get();
		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(36)
	public void shouldFailInvokingDeleteIncomeByZeroId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path("0").request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken).delete();
		assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	@InSequence(37)
	public void shouldNotDeleteIncomeByUnknownId(@ArquillianResteasyResource("api/incomes") WebTarget webTarget) {
		response = webTarget.path("999").request(APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, authToken)
				.delete();
		assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
	}

}
