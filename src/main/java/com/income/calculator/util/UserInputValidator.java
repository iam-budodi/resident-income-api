package com.income.calculator.util;

public interface UserInputValidator {

	String PASSWORD_PATTERN = "^(?!.*\\\\s)(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,32}$";
//	String USERNAME_PATTERN = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,32}[a-zA-Z0-9]$";

	boolean isValid(String userInput);
}
