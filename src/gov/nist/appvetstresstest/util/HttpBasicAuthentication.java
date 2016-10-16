/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.appvetstresstest.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * This class defines the status of an app.
 * 
 * @author steveq@nist.gov
 */
public class HttpBasicAuthentication {

	public static String createAuthorizationHeaderValue(String username, String password) {
		// Encode basic authentication using Base64
		String usernameAndPassword = username + ":" + password;  // Combine username and password with colon
		String base64encodedString = null;
		try {
			base64encodedString = Base64.getEncoder().encodeToString(usernameAndPassword.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Add Authorization and Basic keywords
		return "Authorization: Basic " + base64encodedString;
	}

	public static String[] getUsernameAndPassword(String authorizationHeaderValue) {
		try {
			// Requester is attempting to authenticate
			System.out.println("authHeaderValue is " + authorizationHeaderValue);
			// Authorization header value will have the form: "Authorization: Basic <base64EncodedUsernamePassword>"
			String[] tokens = authorizationHeaderValue.split("\\s");
			// Encoded username and password will be in token at index 2
			String encodedUsernamePassword = tokens[2];
			// Decode Base64
			byte[] base64decodedBytes = Base64.getDecoder().decode(encodedUsernamePassword);
			String userNamePassword = new String(base64decodedBytes, "utf-8");
			// Username and password string will be delimitted by a colon
			return userNamePassword.split(":");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
