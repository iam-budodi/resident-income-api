package com.income.calculator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.income.calculator.qualifier.UsernameQualifier;
 
@UsernameQualifier
public class UsernameValidator /*implements UserInputValidator*/ {

//	@Override
	public boolean isValid(String userInput) {
		// uncoment the paramater argument USERNAME_PATTERN and the intefarce
		Pattern pattern = Pattern.compile("USERNAME_PATTERN");
		Matcher matcher = pattern.matcher(userInput);
        return matcher.matches();
	}

}
