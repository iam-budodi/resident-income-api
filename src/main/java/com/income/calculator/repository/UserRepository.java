package com.income.calculator.repository;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import com.income.calculator.model.User;
import com.income.calculator.util.PasswordDigest;

@Transactional(SUPPORTS)
public class UserRepository {

	@PersistenceContext(unitName = "incomeCalculatorPU")
	private EntityManager entityManager;

	public User findUserByLoginAndPassword(@NotNull String login, @NotNull String password) {
		password = PasswordDigest.digestPassword(password);
		
		TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_BY_LOGIN_PASSWORD, User.class);
		query.setParameter("login", login);
		query.setParameter("password", password);
		
		return query.getSingleResult();
	}

	public List<User> findUserByLogin(@NotNull String login) {
		TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_BY_LOGIN, User.class);
		query.setParameter("login", login);

		return query.getResultList();

//		if (!users.isEmpty()) {
////			entityManager.detach(users);
//			users.stream().forEach(user -> user.setPassword(null));
//
//		}
	}

	public User findUserById(@NotNull Long userId) {
		return entityManager.find(User.class, userId);

//		if (user != null) {
////			entityManager.detach(user);
//			user.setPassword(null);
//
//		}
//
//		return user;
	}

	public List<User> findAllUsers() {
		return entityManager.createNamedQuery(User.FIND_ALL, User.class)
				.setMaxResults(15)
				.getResultList();

//		if (!users.isEmpty()) {
////			entityManager.detach(users);
//			users.stream().forEach(user -> user.setPassword(null));
//
//		}
//
//		return users;
	}

	public Long countAllUsers() {
		return entityManager.createNamedQuery(User.COUNT_ALL, Long.class)
				.getSingleResult();
	}
 
	public List<User> findUsersByKeyword(String keyword) {
		keyword = Arrays.asList("%", keyword, "%").stream().collect(Collectors.joining()); 
//		keyword = "%" + keyword + "%";
		return entityManager.createNamedQuery(User.FIND_BY_NAME, User.class)
				.setParameter("keyword", keyword)
				.getResultList();

	}

	@Transactional(REQUIRED)
	public User createUser(@NotNull User user) { 
		entityManager.persist(user);
		return user;
	}
	
	@Transactional(REQUIRED)
	public User updateUser(@NotNull User user) { 
		return entityManager.merge(user);
	}

	@Transactional(REQUIRED)
	public void deleteUser(@NotNull Long userId) {
		entityManager.remove(entityManager.getReference(User.class, userId));
	}

}
