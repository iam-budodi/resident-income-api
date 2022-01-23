package com.income.calculator.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.income.calculator.listener.EMIAnalyzerListner;
import com.income.calculator.listener.EMICalculationListener;
import com.income.calculator.model.LoanRecord;
import com.income.calculator.model.User;
import com.income.calculator.model.UserRole;
import com.income.calculator.util.PasswordDigest;

@RunWith(Arquillian.class)
public class UserRepositoryTest {

	private static Long userId;

	@Inject
	private UserRepository userRepository;

	@Deployment
	public static Archive<?> createDeploymentPackage() {
		return ShrinkWrap.create(JavaArchive.class).addClass(User.class).addClass(UserRole.class)
				.addClass(EMICalculationListener.class).addClass(EMIAnalyzerListner.class).addClass(LoanRecord.class)
				.addClass(PasswordDigest.class).addClass(UserRepository.class)
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml");
	}
	
	@Test
	@InSequence(1)
	public void shouldBeDeployed() {
		assertNotNull(userRepository);
	}

	@Test
	@InSequence(2)
	public void shouldGetNoUser() {
		assertEquals(Long.valueOf(0), userRepository.countAllUsers());
	}

	@Test
	@InSequence(3)
	public void shouldCreateUser() {
		User user = new User("Japhet", "Sebastian", "jeff", "passw0rd", "+0744608510", "japhet.sebastian01@gmail.com",
				"avatar");
		user = userRepository.createUser(user);

		assertNotNull(user);
		assertNotNull(user.getUserId());
		userId = user.getUserId();
	}

	@Test
	@InSequence(4)
	public void shouldGetOneUser() {
		assertEquals(1, userRepository.findAllUsers().size());
	}

	@Test
	@InSequence(5)
	public void shouldGetNullPassword() {
		assertEquals(userRepository.findUserByLogin("jeff").get(0).getPassword(),
				userRepository.findUserById(userId).getPassword());
	}

	@Test
	@InSequence(6)
	public void shouldValidateCreatedUser() {
		User user = userRepository.findUserById(userId);

		assertNotNull(user);
		assertEquals(userRepository.findUserByLoginAndPassword("jeff", "passw0rd"), user);
		assertEquals(UserRole.USER, user.getUserRole());
	}

	@Test
	@InSequence(7)
	public void shouldNotFindUserGivenLastname() {
		User user = userRepository.findUsersByKeyword("sebas").get(0);
		assertNotNull(user);
		assertEquals("japhet", user.getFirstName().toLowerCase());
	}

	@Test
	@InSequence(8)
	public void shouldNotFindUserGivenFirstname() {
		User user = userRepository.findUsersByKeyword("het").get(0);
		assertNotNull(user);
		assertEquals("sebastian", user.getLastName().toLowerCase());
	}

	// @Test
	// @InSequence(7)
	// public void shouldUpdateProfile() {
	// }
	//
	// @Test
	// @InSequence(8)
	// public void shouldResetPassword() {
	// }

	@Test
	@InSequence(9)
	public void shouldNotFindUserGivenUnknownId() {
		assertNull(userRepository.findUserById(9999999L));
	}

	@Test
	@InSequence(10)
	public void shouldDeleteCreatedUser() {
		userRepository.deleteUser(userId);
	}

	@Test
	@InSequence(11)
	public void shouldGetNoMoreUser() {
		assertNull(userRepository.findUserById(userId));

	}

	@Test(expected = Exception.class)
	@InSequence(12)
	public void shouldFailToCreateNullUser() {
		userRepository.createUser(null);
	}

	@Test(expected = Exception.class)
	@InSequence(13)
	public void shouldFailToCreateUserWithNullUsername() {
		userRepository.createUser(new User("Japhet", "Sebastian", null, "passw0rd", "0744608510",
				"japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(14)
	public void shouldFailToCreateUserWithNullFirstname() {
		userRepository.createUser(new User(null, "Sebastian", "jeff", "passw0rd", "0744608510",
				"japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(15)
	public void shouldFailToCreateUserWithNullLastname() {
		userRepository.createUser(
				new User("Japhet", null, "jeff", "passw0rd", "0744608510", "japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(16)
	public void shouldFailToCreateUserWithNullPassword() {
		userRepository.createUser(
				new User("Japhet", "Sebastian", "jeff", null, "0744608510", "japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(17)
	public void shouldFailToCreateUserWithEmptyPassword() {
		userRepository.createUser(
				new User("Japhet", "Sebastian", "jeff", "", "0744608510", "japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(18)
	public void shouldFailToCreateUserWithBlankPassword() {
		userRepository.createUser(
				new User("Japhet", "Sebastian", "jeff", "  ", "0744608510", "japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(19)
	public void shouldFailToCreateUserWithNullPhoneNumber() {
		userRepository.createUser(
				new User("Japhet", "Sebastian", "jeff", "passw0rd", null, "japhet.sebastian01@gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(20)
	public void shouldFailToCreateUserWithNullEmail() {
		userRepository.createUser(new User("Japhet", "Sebastian", "jeff", "passw0rd", "0744608510", null, "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(21)
	public void shouldFailToCreateUserWithImproperEmail() {
		userRepository.createUser(new User("Japhet", "Sebastian", "jeff", "passw0rd", "0744608510",
				"japhet.sebastian01$gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(22)
	public void shouldFailToCreateUserWithAnotherImproperEmail() {
		userRepository.createUser(
				new User("Japhet", "Sebastian", "jeff", "passw0rd", "0744608510", "japhet.sebastian01@", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(23)
	public void shouldFailToCreateUserYetWithAnotherImproperEmail() {
		userRepository.createUser(new User("Japhet", "Sebastian", "jeff", "passw0rd", "0744608510",
				"japhet.sebastian01gmail.com", "avatar"));
	}

	@Test(expected = Exception.class)
	@InSequence(24)
	public void shouldFailToFindUserGivenNullId() {
		userRepository.findUserById(null);
	}

	@Test(expected = Exception.class)
	@InSequence(25)
	public void shouldFailToDeleteUserGivenNullId() {
		userRepository.deleteUser(null);
	}

	@Test(expected = Exception.class)
	@InSequence(26)
	public void shouldFailToDeleteUserGivenUnknownId() {
		userRepository.deleteUser(888888L);
	}

}
