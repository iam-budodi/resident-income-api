package com.income.calculator.util;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class SimpleKeyGenerator implements KeyGenerator {

	@Override
	public Key generateKey() {
		String keyString = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c,eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
		Key key = new SecretKeySpec(keyString.getBytes(), 0, keyString.getBytes().length, "DES");
		return key;
	}

}
