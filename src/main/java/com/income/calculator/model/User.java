package com.income.calculator.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.income.calculator.util.PasswordDigest;

/**
 * Entity implementation class for Entity: User
 *
 */

@Entity
@Table(name = "t_user")
@NamedQueries({ @NamedQuery(name = User.FIND_BY_EMAIL, query = "SELECT u FROM User u WHERE u.email = :email"),
//	@NamedQuery(name = User.FIND_BY_UUID, query = "SELECT u FROM User u WHERE u.uuid = :uuid"),
		@NamedQuery(name = User.FIND_BY_LOGIN, query = "SELECT u FROM User u WHERE u.login = :login"),
		@NamedQuery(name = User.FIND_BY_LOGIN_PASSWORD, query = "SELECT u FROM User u WHERE u.login = :login AND u.password = :password"),
		@NamedQuery(name = User.FIND_BY_NAME, query = "SELECT u FROM User u WHERE LOWER(u.firstName) LIKE :keyword OR LOWER(u.lastName) LIKE :keyword"),
		@NamedQuery(name = User.FIND_ALL, query = "SELECT u FROM User u ORDER BY u.lastName DESC"),
		@NamedQuery(name = User.COUNT_ALL, query = "SELECT COUNT(u) FROM User u") })
public class User {
	public static final String FIND_BY_EMAIL = "User.findByEmail";
	public static final String FIND_BY_LOGIN = "User.findByLogin";
	public static final String FIND_BY_UUID = "User.findByUUID";
	public static final String FIND_BY_LOGIN_PASSWORD = "User.findByLoginAndPassword";
	public static final String FIND_BY_NAME = "User.findByKeyword";
	public static final String FIND_ALL = "User.findAll";
	public static final String COUNT_ALL = "User.countAll";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id", unique = true, nullable = false)
	private Long userId;

	@NotNull
	@Size(min = 3)
	@Pattern(regexp = "[A-Za-z]*", message = "must contain only letters and spaces")
	@Column(name = "first_name")
	private String firstName;

	@NotNull
	@Size(min = 3)
	@Pattern(regexp = "[A-Za-z]*", message = "must contain only letters and spaces")
	@Column(name = "last_name")
	private String lastName;

	@NotNull
	@Size(min = 10, max = 13, message = "size must be between 10 and 13")
	@Digits(fraction = 0, integer = 13)
	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	@NotNull
	@NotEmpty(message = "not a well-formed email address")
	@Email(message = "not a well-formed email address")
	@Column(name = "email_address", unique = true)
	private String email;

	@NotNull
	@Size(min = 1, max = 25)
	@Pattern(regexp = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){1,18}[a-zA-Z0-9]$", message = "invalid username")
	@Column(name = "username", unique = true)
	private String login;

	@NotNull
	@Size(min = 6, max = 256)
	private String password;

//	@NotNull
//	@Size(min = 6, max = 256)
//	private String uuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_role", length = 32, columnDefinition = "varchar(8) default 'USER'")
	private UserRole userRole = UserRole.USER;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_on")
	private Date createdOn;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@JsonIgnore // Solves the getLoans() and setLoans() problem
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LoanRecord> loans = new ArrayList<>();

	@Transient
	private String fullName;

	@PrePersist
//	@PreUpdate
	public void setUUIDandDigetPassword() {
		if (password == null || password.isBlank())
			throw new IllegalArgumentException("Invalid password");

//		uuid = UUID.randomUUID().toString().replace("-", "");
		password = PasswordDigest.digestPassword(password);
	}

	public User() {
		super();
	}

	public User(String firstName, String lastName, String login, String password, String phoneNumber, String email,
			String avatarUrl) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.login = login;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.avatarUrl = avatarUrl;
	}

	public User(String firstName, String lastName, String login, String password, UserRole userRole, String phoneNumber,
			String email, String avatarUrl) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.login = login;
		this.password = password;
		this.userRole = userRole;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.avatarUrl = avatarUrl;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String LastName) {
		this.lastName = LastName;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

//	public String getUuid() {
//		return this.uuid;
//	}
//
//	public void setUuid(String uuid) {
//		this.uuid = uuid;
//	}

	public UserRole getUserRole() {
		return this.userRole;
	}

	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public List<LoanRecord> getLoans() {
		return loans;
	}

	public void setLoans(List<LoanRecord> loans) {
		this.loans = loans;
	}

	public void addLoanRecord(LoanRecord loanRecord) {
		loans.add(loanRecord);
		loanRecord.setUser(this);
	}

	public void removeLoanRecord(LoanRecord loanRecord) {
		loans.remove(loanRecord);
		loanRecord.setUser(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		User user = (User) o;
		return Objects.equals(userId, user.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName + ", phoneNumber="
				+ phoneNumber + ", email=" + email + ", login=" + login + ", createdOn=" + createdOn + ", userRole="
				+ userRole + ", avatarUrl=" + avatarUrl + ", password=" + password + "]";
	}

}
