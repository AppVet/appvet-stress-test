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
package gov.nist.appvetstresstest;

import gov.nist.appvetstresstest.util.Logger;
import gov.nist.appvetstresstest.util.Xml;

import java.io.File;

/**
 * @author steveq@nist.gov
 */
public class Properties {
	/** Github release version number. */
	public static final String version = "1.0";
	public static String APPVET_STRESSTEST_HOME = null;
	public static String JAVA_HOME = null;
	public static final String PROPERTIES_FILE_NAME = "AppvetStressTestProperties.xml";
	public static final String logFileName = "log.txt";
	public static String appvetURL = null;
	public static String appvetURLPort = null;
	public static String username = null;
	public static String password = null;
	public static String appsDirectory = null;
	public static String appType = null;
	public static int submissionDelay = 0;
	public static int maxApps = 0;
    public static Logger log = null;
	public static String logPath = null;

    // The following are consumed by Logger
	public static String LOG_LEVEL = null;
	public static boolean LOG_TO_CONSOLE = true;
	
	static {
		
		JAVA_HOME = System.getenv("JAVA_HOME");
		if (JAVA_HOME == null) {
			System.err.println("Environment variable JAVA_HOME not set.");
		}
		
		APPVET_STRESSTEST_HOME = System.getenv("APPVET_STRESSTEST_HOME");
		if (APPVET_STRESSTEST_HOME == null) {
			System.err
					.println("Environment variable APPVET_STRESSTEST_HOME not set. Aborting.");
			System.exit(1);
		}
		
		// Load XML property file
		File configFile = new File(APPVET_STRESSTEST_HOME + "/" + PROPERTIES_FILE_NAME);
		if (!configFile.exists()) {
			System.err.println("AppvetStressTest config file does not exist. Aborting.");
			System.exit(1);
		}
		
		// GET XML PROPERTIES
		final Xml xml = new Xml(configFile);
		

		logPath = APPVET_STRESSTEST_HOME + "/" + logFileName;
		if (!new File(logPath).exists()) {
			System.err.println("Log " + logPath + " does not exist. Create log.txt in APPVET_STRESSTEST_HOME directory.");
			System.exit(1);
		}
		
		LOG_LEVEL = xml.getXPathValue("LogLevel");
		if (LOG_LEVEL == null || (!LOG_LEVEL.equals("DEBUG") && !LOG_LEVEL.equals("INFO") && !LOG_LEVEL.equals("WARNING") && !LOG_LEVEL.equals("ERROR"))) {
			System.err.println("Log level " + LOG_LEVEL + " is invalid. Must be [DEBUG | INFO | WARNING | ERROR]. Aborting.");
			System.exit(1);
		}
		
		// LOGGER IS READY
		log = new Logger(logPath, "APPVET_STRESSTEST");

		// AppVet URL
		appvetURL = xml.getXPathValue("AppvetURL");
		if (appvetURL == null) {
			log.error("AppVet URL is null. Aborting.");
			System.exit(1);
		}
		log.debug("AppvetURL: " + appvetURL);
		
		// AppVet port
		appvetURLPort = xml.getXPathValue("AppvetURLPort");
		if (appvetURLPort == null) {
			log.error("Appvet URL port is null. Aborting.");
			System.exit(1);
		}
		log.debug("AppvetURL port: " + appvetURLPort);
		
		// Username
		username = xml.getXPathValue("Username");
		if (username == null) {
			log.error("Username is null. Aborting.");
			System.exit(1);
		}
		log.debug("Username: " + username);
		
		// Password
		password = xml.getXPathValue("Password");
		if (password == null) {
			log.error("Password is null. Aborting.");
			System.exit(1);
		}
		log.debug("Password: " + password);
		
		// Directory of apps
		appsDirectory = xml.getXPathValue("AppsDirectory");
		if (appsDirectory == null) {
			log.error("Apps directory is null. Aborting.");
			System.exit(1);
		}
		File appsDirectoryFile = new File(appsDirectory);
		if (!appsDirectoryFile.exists()) {
			log.error("Apps directory " + appsDirectory + " does not exist. Aborting.");
			System.exit(1);
		}
		log.debug("Apps directory: " + appsDirectory);
		
		// App type
		appType = xml.getXPathValue("AppType");
		if (appType == null || (!appType.equals("ANDROID") && !appType.equals("IOS") && !appType.equals("BOTH"))) {
			log.error("App type " + appType + " is invalid. Must be [ANDROID | IOS | BOTH]. Aborting.");
			System.exit(1);
		}
		log.debug("App type: " + appType);
		
		// Submission delay
		submissionDelay = new Integer(xml.getXPathValue("SubmissionDelay")).intValue();
		if (submissionDelay <= 0) {
			log.error("Submission delay must be greater than 0. Aborting.");
			System.exit(1);
		}		
		log.debug("Submission Delay: " + submissionDelay);
		
		// Max apps
		maxApps = new Integer(xml.getXPathValue("MaxApps")).intValue();
		if (maxApps <= 0) {
			log.error("Max apps must be greater than 0. Aborting.");
			System.exit(1);
		}		
		log.debug("Max apps: " + maxApps);		

	}
}
