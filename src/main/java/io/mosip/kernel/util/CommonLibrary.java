package io.mosip.kernel.util;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.service.BaseTestCase;
import io.mosip.testrunner.MosipTestRunner;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

/**
 *
 * @author Ravikant
 *
 */
public class CommonLibrary extends BaseTestCase {

	private static Logger logger = Logger.getLogger(CommonLibrary.class);
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	/**
	 * @param response
	 *            this method is for validating the response time is in UTC
	 */
	public void checkResponseUTCTime(Response response) {
		System.out.println(response.asString());
		JSONObject responseJson = null;
		String responseTime = null;
		try {
			responseJson = (JSONObject) new JSONParser().parse(response.asString());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			logger.info(e1.getMessage());
			return;
		}
		if(responseJson!=null && responseJson.containsKey("responsetime"))
			responseTime = response.jsonPath().get("responsetime").toString();
		else return;
		String cuurentUTC = (String) getCurrentUTCTime();
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		try {
			Date d1 = sdf.parse(responseTime.substring(14, 16));
			Date d2 = sdf.parse(cuurentUTC.substring(14, 16));

			long elapse = Math.abs(d1.getTime() - d2.getTime());
			if (elapse > 300000) {
				Assert.assertTrue(false, "Response time is not UTC, response time : " + responseTime);
			}

		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return this method is for getting the curretn UTC time in
	 *         yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format
	 */
	public Object getCurrentUTCTime() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(Clock.systemUTC());
		String utcTime = time.format(dateFormat);
		return utcTime;

	}

	/**
	 * this method is for getting the local current time in
	 * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format
	 * 
	 * @return
	 */
	public Object getCurrentLocalTime() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now();
		String currentTime = time.format(dateFormat);
		return currentTime;

	}

	/**
	 * @param folderRelativePath
	 * @param isfolder(it
	 *            should be true if u want to get list of folders and false for list
	 *            of files)
	 * @return this method is for returning the list of relative path of each folder
	 *         or files in a given path
	 */
	public List<String> getFoldersFilesNameList(String folderRelativePath, boolean isfolder) {
		String configPath = folderRelativePath;
		List<String> listFoldersFiles = new ArrayList<>();

		final File file = new File(getResourcePath() + folderRelativePath);
		logger.info("=====" + getResourcePath() + folderRelativePath);
		logger.info("=======" + file.getAbsolutePath());
		logger.info("=========" + file.getPath());
		for (File f : file.listFiles()) {
			if (f.isDirectory() == isfolder)
				listFoldersFiles.add(configPath + "/" + f.getName());
		}
		return listFoldersFiles;
	}

	/**
	 * The method to return class loader resource path
	 * 
	 * @return String
	 */
	public String getResourcePath() {
		return MosipTestRunner.getGlobalResourcePath() + "/";
	}
	
	public String getResourcePathForKernel() {
		return MosipTestRunner.getResourcePath() + "/";
	}

	/**
	 * @param path
	 * @param isRelative
	 * @return this method is for reading the jsonData object from the given relative/absolute path.
	 */
	public JSONObject readJsonData(String path, boolean isRelative) {
		if(isRelative)
			path = getResourcePath() + path;
		File fileToRead = new File(path);
		InputStream isOfFile = null;
		try {
			isOfFile = new FileInputStream(fileToRead);
		} catch (FileNotFoundException e1) {
			logger.info("File Not Found at the given path");
		}
		JSONObject jsonData = null;
		try {
			jsonData = (JSONObject) new JSONParser().parse(new InputStreamReader(isOfFile, "UTF-8"));
		} catch (IOException | ParseException | NullPointerException e) {
			logger.info(e.getMessage());
		}
		return jsonData;
	}

	/**
	 * @param propertyFileName
	 * @return this method is for reading property file from config folder by
	 *         passing the property file name
	 */
	public Map<String, String> readProperty(String propertyFileName) {
		Properties prop = new Properties();
		try {
			File propertyFile = new File(getResourcePathForKernel() + "config/" + propertyFileName + ".properties");
			prop.load(new FileInputStream(propertyFile));

		} catch (IOException e) {

			logger.info(e.getMessage());
		}

		Map<String, String> mapProp = prop.entrySet().stream()
				.collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));

		return mapProp;
	}

	/**
	 * @param response
	 *            This method is for checking the authentication is pass or fail in
	 *            rest services
	 */
	public void responseAuthValidation(Response response) {
		JSONArray errors = null;
		String errorCode = null;
		String errorMessage = null;
		int statusCode = response.getStatusCode();
		// fetching json array of objects from response
		try {
			if (statusCode > 500)
				Assert.assertTrue(false, "Service is Unavailable and the statusCode=" + statusCode);

			errors = (JSONArray) ((JSONObject) new JSONParser().parse(response.asString())).get("errors");
		} catch (ParseException pe) {
			Assert.assertTrue(false, "Response from the service is not able to read and exception is " + pe.getClass());
		} catch (NullPointerException npe) {
			Assert.assertTrue(false, "Errors in the response is not null and exception is " + npe.getClass());
		}
		if (errors != null) {
			try {
				errorCode = ((JSONObject) errors.get(0)).get("errorCode").toString();
				errorMessage = ((JSONObject) errors.get(0)).get("message").toString();
			} catch (IndexOutOfBoundsException aibe) {
				Assert.assertTrue(false,
						"Not able to find the errorCode or errorMessage from errors array and exception is "
								+ aibe.getClass());
			}

			if (errorCode.contains("ATH")) {
				Assert.assertTrue(false,
						"Failed due to Authentication failure. Error message is='" + errorMessage + "'");
			}
		}
	}
	
	/**
	 * @param requestJson
	 * @param responseJson
	 * @return this method is for comapring 2 json, and return boolean value accordingly.
	 * @throws AdminTestException 
	 */
	public boolean jsonComparator(String requestJson, String responseJson) throws AdminTestException
	{
		try {
			JSONAssert.assertEquals(requestJson, responseJson, false);
			return true;
		} catch (JSONException |  AssertionError e) {
			// TODO Auto-generated catch block
			logger.info("EXPECTED AND ACTUAL DATA MISMATCH");
			logger.info("MISMATCH DETAILS:");
			logger.info(e.getMessage());
			logger.info("Obtained ACTUAL RESPONSE is:== "+responseJson);
			throw new AdminTestException("Failed at output validation");
		}
	}
	/**
	 * @param cookie
	 * @return boolean
	 * this method is for checking cookie(token) is expired or not.
	 */
	public boolean isValidToken(String cookie) {
		
		logger.info("========= Revalidating the token =========");
		Response response = applicationLibrary.getWithoutParams("/v1/authmanager/authorize/admin/validateToken", cookie);
		JSONObject responseJson =null;
		try {
			responseJson = (JSONObject) ((JSONObject) new JSONParser().parse(response.asString()))
					.get("response");
		} catch (ParseException | NullPointerException e) {
			logger.info(e.getMessage());
		}

		if (responseJson!=null && responseJson.get("errors")==null)
			{
			logger.info("========= Valid Token =========");
			return true;
			}
		else
		{
			
			logger.info("========= InValid Token =========");
			return false;
		}

	}

	/**
	 * This method is for generating the random alphanumeric string of required
	 * length
	 */
	public String randomAlphaNumeric(int lengthOfString) {
		StringBuilder builder = new StringBuilder();
		while (lengthOfString-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	/**
		 * The method to remove the json element from the json file
		 * 
		 * @return String
		 * @throws ParseException 
		 */
		
		public String removeJsonElement(String readFilePath,ArrayList<String> eleToRemove) throws ParseException {
			String jsnString = null;
			String val = null;
			
			try {
				String yourActualJSONString = new String(Files.readAllBytes(Paths.get(readFilePath)), StandardCharsets.UTF_8);
				DocumentContext jsonContext = JsonPath.parse(yourActualJSONString);
				
				for (int i = 0; i < eleToRemove.size(); i++) 
			    {
			    	val=eleToRemove.get(i);
			    	jsonContext.delete(val);
			    	jsnString = jsonContext.jsonString();
			    	/*JSONParser parser = new JSONParser(); 
			        jsnObject = (JSONObject) parser.parse(myOutput);
			    	*/
			    }
				
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsnString;
			
		}

	/**
	 * @param response
	 *            this method is for logging the response in case of error only.
	 *            this is used in get request response logging
	 */
	public void responseLogger(Response response) {
		int statusCode = response.statusCode();
		if (statusCode < 200 || statusCode > 299) {
			logger.info(response.asString());
		} else
			logger.info("status code: " + statusCode + "(success)");

	}

	// rest services methods.

	// Post Requests:
	/**
	 * @param url
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for post request with authentication(cookie) and only
	 *         with jsonData in request body.
	 */
	public Response postWithoutJson(String url, String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().contentType(contentHeader)
				.accept(acceptHeader).log().all().when().post(url).then().log().all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @return this method is for post request without authentication(cookie) and
	 *         only with jsonData in request body.
	 */
	public Response postWithJson(String url, Object body, String contentHeader, String acceptHeader) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Response postResponse = given().relaxedHTTPSValidation().body(body).contentType(contentHeader)
				.accept(acceptHeader).log().all().when().post(url).then().log().all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for post request with authentication(cookie) and only
	 *         with jsonData in request body.
	 */
	public Response postWithJson(String url, Object body, String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().post(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param pathParams
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for post request with authentication(cookie) with
	 *         pathParams and jsonData in request body.
	 */
	public Response postWithPathParams(String url, Object body, HashMap<String, String> pathParams,
			String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.body(body).contentType(contentHeader).accept(acceptHeader).log().all().when().post(url).then().log()
				.all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param file
	 * @param url
	 * @param cookie
	 * @return this method is for post request with authentication(cookie) and only
	 *         with File in request body.
	 */
	public Response postWithOnlyFile(String url, File file, String fileKeyName, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().multiPart(fileKeyName, file)
				.expect().when().post(url).then().log().all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param file
	 * @param url
	 * @param cookie
	 * @return this method is for post request with authentication(cookie), jsonData
	 *         and with File in request body.
	 */
	public Response postWithFile(String url, Object body, File file, String fileKeyName, String contentHeader,
			String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().multiPart(fileKeyName, file)
				.body(body).contentType(contentHeader).expect().when().post(url).then().log().all().extract()
				.response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param formParams
	 * @param file
	 * @param url
	 * @param contentHeader
	 * @param cookie
	 * @return this method is for post request with authentication(cookie)
	 *         containing file and Map of form params(name, value).
	 */
	public Response postWithFileFormParams(String url, HashMap<String, String> formParams, File file,
			String fileKeyName, String contentHeader, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		logger.info("Name of the file is" + file.getName());
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().multiPart(fileKeyName, file)
				.formParams(formParams).contentType(contentHeader).expect().when().post(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param formParams
	 * @param file
	 * @param url
	 * @param contentHeader
	 * @param pathParams
	 * @param cookie
	 * @return this method is for post request with authentication(cookie)
	 *         containing file, Map of pathParams(name, value) and Map of
	 *         formParams(name, value).
	 */
	public Response postWithFilePathParamsFormParams(String url, HashMap<String, String> pathParams,
			HashMap<String, String> formParams, File file, String fileKeyName, String contentHeader, String cookie) {
		logger.info("REST:ASSURED:Sending post request to" + url);
		logger.info("Name of the file is" + file.getName());

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.multiPart(fileKeyName, file).formParams(formParams).contentType(contentHeader).expect().when()
				.post(url);
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @param queryparams
	 * @param cookie
	 * @return this method is for post request with authentication(cookie)
	 *         containing json body with Map of queryPaarams(name, value).
	 */
	public Response postWithQueryParams(String url, HashMap<String, String> queryparams, Object body,
			String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a POST request to " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.queryParams(queryparams).contentType(contentHeader).accept(acceptHeader).log().all().when().post(url)
				.then().log().all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param headers
	 * @param contentHeader
	 * @param regProcAuthToken
	 * @return this method is for post request with authentication(cookie)
	 *         containing json body and Map of headers(name, value). Used by
	 *         RegProc.
	 */
	public Response postWithMultiHeaders(String endpoint, Object body, HashMap<String, String> headers,
			String contentHeader, String cookie) {
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = given().cookie(builder.build()).headers(headers).relaxedHTTPSValidation()
				.body("\"" + body + "\"").contentType(contentHeader).log().all().when().post(endpoint).then().log()
				.all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from request is: " + postResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	/**
	 * @param jsonString
	 * @param serviceUri
	 * @param cookie
	 * @return this method is specifically for email notification with
	 *         authorization(cookie) and multiPart data.
	 */
	public Response postRequestEmailNotification(String serviceUri, JSONObject jsonString, String cookie) {
		logger.info("REST-ASSURED: Sending a POST request to " + serviceUri);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response postResponse = null;
		if (jsonString.get("attachments").toString().isEmpty()) {
			postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().contentType("multipart/form-data")
					.multiPart("mailContent", (String) jsonString.get("mailContent"))
					.multiPart("mailTo", (String) jsonString.get("mailTo"))
					.multiPart("mailSubject", (String) jsonString.get("mailSubject"))
					.multiPart("mailCc", (String) jsonString.get("mailCc")).post(serviceUri).then().log().all()
					.extract().response();
		} else {
			postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().contentType("multipart/form-data")
					.multiPart("attachments", new File(getResourcePath() + (String) jsonString.get("attachments")))
					.multiPart("mailContent", (String) jsonString.get("mailContent"))
					.multiPart("mailTo", (String) jsonString.get("mailTo"))
					.multiPart("mailSubject", (String) jsonString.get("mailSubject"))
					.multiPart("mailCc", (String) jsonString.get("mailCc")).post(serviceUri).then().log().all()
					.extract().response();
		}

		// log then response
		logger.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}

	// Patch requests:
	/**
	 * @param url
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for Patch Request with authentication(cookie) and Json
	 *         body.
	 */
	public Response patchRequest(String url, Object body, String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a Patch request to " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().patch(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	// Get Requests:
	/**
	 * @param url
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and
	 *         without any param.
	 */
	public Response getWithoutParams(String url, String cookie) {
		logger.info("REST-ASSURED: Sending a Get request to " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: the response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param patharams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and with
	 *         pathParams Map(name, Value).
	 */
	public Response getWithPathParam(String url, HashMap<String, String> patharams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(patharams).log()
				.all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param queryParams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and with
	 *         queryParams Map(name, Value).
	 */
	public Response getWithQueryParam(String url, HashMap<String, String> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().queryParams(queryParams).log()
				.all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param queryParams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and with
	 *         List of queryParams(Map(name, List<Value>)) with same name.
	 */
	public Response getWithQueryParamList(String url, HashMap<String, List<String>> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().queryParams(queryParams).log()
				.all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param queryParams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie),
	 *         pathParams Map(name, Value) and with queryParams Map(name, Value).
	 */
	public Response getWithPathQueryParam(String url, HashMap<String, String> pathParams,
			HashMap<String, String> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.queryParams(queryParams).log().all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param queryParams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie),
	 *         pathParams Map(name, Value) and with queryParams(Map(name,
	 *         List<Value>)) with same name.
	 */
	public Response getWithPathParamQueryParamList(String url, HashMap<String, String> pathParams,
			HashMap<String, List<String>> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.queryParams(queryParams).log().all().when().get(url);
		// log then response
		responseLogger(getResponse);
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}
	// Put Requests:

	/**
	 * @param url
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for put request with Authentication(cookie) and
	 *         without any data.
	 */
	public Response putWithoutData(String url, String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a PUT request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().contentType(contentHeader)
				.accept(acceptHeader).log().all().when().put(url).then().log().all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from the request is: " + putResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	/**
	 * @param url
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for Put request with authentication(cookie) and only
	 *         with jsonData in request body
	 */
	public Response putWithJson(String url, Object body, String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a PUT request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().put(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from the request is: " + putResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for Put request with authentication(cookie) and only
	 *         with pathParams.
	 */
	public Response putWithPathParams(String url, HashMap<String, String> pathParams, String contentHeader,
			String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a PUT request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().put(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from the request is: " + putResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for Put request with authentication(cookie) and only
	 *         with pathParams.
	 */
	public Response putWithQueryParams(String url, HashMap<String, String> queryParams, String contentHeader,
			String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a PUT request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().queryParams(queryParams)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().put(url).then().log().all()
				.extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from the request is: " + putResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param body
	 * @param contentHeader
	 * @param acceptHeader
	 * @param cookie
	 * @return this method is for Put request with authentication(cookie) with
	 *         pathParams and jsonData in request body
	 */
	public Response putWithPathParamsBody(String url, HashMap<String, String> pathParams, Object body,
			String contentHeader, String acceptHeader, String cookie) {
		logger.info("REST-ASSURED: Sending a PUT request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response putResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.body(body).contentType(contentHeader).accept(acceptHeader).log().all().when().put(url).then().log()
				.all().extract().response();
		// log then response
		logger.info("REST-ASSURED: The response from the request is: " + putResponse.asString());
		logger.info("REST-ASSURED: The response Time is: " + putResponse.time());
		return putResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param cookie
	 * @return this method is for delete request with authorization(cookie) and
	 *         pathParam Map(name, value).
	 */
	public Response deleteWithPathParams(String url, HashMap<String, String> pathParams, String cookie) {
		logger.info("REST-ASSURED: Sending a DELETE request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams).log()
				.all().when().delete(url).then().log().all().extract().response();
		logger.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param cookie
	 * @return this method is for delete request with authorization(cookie) and
	 *         queryParam Map(name, value).
	 */
	public Response deleteWithQueryParams(String url, HashMap<String, String> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a DELETE request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().queryParams(queryParams).log()
				.all().when().delete(url).then().log().all().extract().response();
		logger.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param queryParams
	 * @param cookie
	 * @return this method is for delete request with authorization(cookie),
	 *         queryParam Map(name, value) and pathParam Map(name, value).
	 */
	public Response deleteWithPathQueryParams(String url, HashMap<String, String> pathParams,
			HashMap<String, String> queryParams, String cookie) {
		logger.info("REST-ASSURED: Sending a DELETE request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(pathParams)
				.queryParams(queryParams).log().all().when().delete(url).then().log().all().extract().response();
		logger.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + getResponse.time());
		return getResponse;
	}

	// config property reader

	/**
	 * @param url
	 *            (complete url)
	 * @return reads the config property
	 */
	public Response getConfigProperties(String url) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);
		Response getResponse = given().relaxedHTTPSValidation().log().all().when().get(url).then().log().all().extract()
				.response();
		// log then response
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * @param url
	 * @param pathParams
	 * @param cookie
	 * @return this method is for delete request with authorization(cookie)
	 */
	public Response deleteWithoutParams(String url, String cookie) {
		logger.info("REST-ASSURED: Sending a DELETE request to   " + url);
		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when().delete(url)
				.then().log().all().extract().response();
		logger.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + getResponse.time());
		return getResponse;
	}

	/**
	 * Ashish
	 * 
	 * @param body
	 * @param file
	 * @param url
	 * @param contentHeader
	 * @param cookie
	 * @return
	 */
	public Response postJSONwithFile(Object body, File file, String url, String contentHeader, String cookie) {
		Response getResponse = null;
		/*
		 * Fetch to get the param name to be passed in the request
		 */

		String Document_request = readProperty("IDRepo").get("req.Documentrequest");

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().multiPart("file", file)
				.formParam(Document_request, body).contentType(contentHeader).expect().when().post(url);
		logger.info("REST:ASSURED: The response from request is:" + getResponse.asString());
		logger.info("REST-ASSURED: the response time is: " + getResponse.time());
		return getResponse;
	}
}
