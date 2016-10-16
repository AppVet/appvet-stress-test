package gov.nist.appvetstresstest;

import gov.nist.appvetstresstest.util.HttpBasicAuthentication;
import gov.nist.appvetstresstest.util.Logger;
import gov.nist.appvetstresstest.util.SSLWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Note that AppVet servers/servlets should implement SSL in order to prevent
 * unauthorized access to the system.
 * 
 * HOW TO RUN:
 */
public class StressTester {
	public static Logger log = Properties.log;

	public StressTester() {
	}

	public File getRandomApp() {
		File appDir = new File(Properties.appsDirectory);
		if (appDir == null || !appDir.exists()) {
			log.error("App directory is null or does not exist. Aborting.");
			System.exit(1);
		}
		String appType = Properties.appType;
		File[] apps = appDir.listFiles();
		log.debug("Number of apps: " + apps.length);
		for (;;) {
			// Get random number
			int randomAppsIndex = 0 + (int) (Math.random() * (apps.length - 1));
			log.debug("RandomAppIndex: " + randomAppsIndex);
			File appFile = apps[randomAppsIndex];
			if (appFile.getName().toLowerCase().endsWith(".apk")) {
				if (appType.equals("ANDROID") || appType.equals("BOTH")) {
					return appFile;
				}
			} else if (appFile.getName().toLowerCase().endsWith(".ipa")) {
				if (appType.equals("IOS") || appType.equals("BOTH")) {
					return appFile;
				}
			}
		}
	}

	public String authenticate() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
		HttpConnectionParams.setSoTimeout(httpParameters, 1200000);
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		httpclient = SSLWrapper.wrapClient(httpclient);
		HttpGet httpGet = new HttpGet(Properties.appvetURL + ":" + Properties.appvetURLPort + "/appvet/AppVetServlet");
		String authHeaderValue = HttpBasicAuthentication
				.createAuthorizationHeaderValue(Properties.username, Properties.password);
		log.debug("Auth value:" + authHeaderValue);
		// Set authorization header
		httpGet.setHeader("Authorization", authHeaderValue);
		HttpResponse httpResponse;
		try {
			log.debug("Authenticating");
			httpResponse = httpclient.execute(httpGet);
			String httpResponseVal = httpResponse.getStatusLine().toString();
			log.debug("HTTP Response: " + httpResponseVal);
			if (httpResponseVal.indexOf("HTTP/1.1 200 OK") > -1) {
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream inputStream = httpEntity.getContent();
				String sessionId = getStringFromInputStream(inputStream);
				log.debug("Session ID: " + sessionId);
				return sessionId;
			} else {
				log.error(httpResponseVal);
				return null;
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public boolean sendApp(File appFile, int appIndex) {
		
		String sessionId = authenticate();
		if (sessionId == null) {
			log.error("Authentication failed. Aborting.");
			return false;
		}
		
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
		HttpConnectionParams.setSoTimeout(httpParameters, 1200000);
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		httpclient = SSLWrapper.wrapClient(httpclient);
		MultipartEntity entity = getMultipartEntity(sessionId, appFile);
		if (entity == null) {
			log.error("MultipartEntity is null. Aborting.");
			return false;
		}
		HttpPost httpPost = new HttpPost(Properties.appvetURL + ":" + 
				Properties.appvetURLPort + "/appvet/AppVetServlet");
		httpPost.setEntity(entity);
		log.info("Sending app #" + (appIndex+1) + " -- '" + appFile.getName() + "'");
		HttpResponse httpResponse;
		try {
			httpResponse = httpclient.execute(httpPost);
			String httpResponseVal = httpResponse.getStatusLine().toString();
			log.debug("RECEIVED: " + httpResponseVal);
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			String appId = getStringFromInputStream(inputStream);
			if (httpResponseVal.indexOf("HTTP/1.1 202 Accepted") > -1) {
				log.info("AppVet successfully received app #" + (appIndex+1) + " ");
			} else {
				log.error(httpResponseVal.toString());
				return false;
			}
			inputStream = null;
			httpEntity = null;
			httpResponseVal = null;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public MultipartEntity getMultipartEntity(String sessionId, File appFile) {
		final MultipartEntity entity = new MultipartEntity();
		try {
			StringBody sessionIdValue = new StringBody(sessionId,
					Charset.forName("UTF-8"));
			entity.addPart("sessionid", sessionIdValue);
			StringBody cmdValue = new StringBody("SUBMIT_APP",
					Charset.forName("UTF-8"));
			entity.addPart("command", cmdValue);
			if (!appFile.exists()) {
				log.error("App " + appFile.getAbsolutePath()
						+ " could not be found. Terminating.");
				return null;
			} else {
				FileBody fileBody = new FileBody(appFile);
				entity.addPart("fileupload", fileBody);
				log.debug("Got multipart entity");
				return entity;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void delay() {
	}

	public static void main(String[] args) {
		StressTester st = new StressTester();
		log.info("Starting AppVet Stress Tester...");
		for (int i = 0; i < Properties.maxApps; i++) {
			log.debug("App " + i);
			File appFile = st.getRandomApp();
			log.debug("Got random app: " + appFile.getName());
			boolean sentApp = st.sendApp(appFile, i);
			if (!sentApp) {
				log.error("Could not send app. Aborting.");
				System.exit(1);
			}
			// delay();
		}
		log.info("AppVet successfully sent " + Properties.maxApps + " apps. DONE.");
	}
}
