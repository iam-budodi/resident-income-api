package com.income.calculator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@PasswordQualifier
public class PasswordValidator implements UserInputValidator {

//	private static final String PASSWORD_PATTERN = "^(?!.*\\\\s)(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,32}$";
//
//	private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

//	public boolean isValid(final String password) {
//		Matcher matcher = pattern.matcher(password);
//		return matcher.matches();
//	} 

	@Override
	public boolean isValid(String userInput) {
		Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(userInput);
		return matcher.matches();
	}
}
