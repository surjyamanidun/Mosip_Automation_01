package io.mosip.authentication.fw.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Reporter;

import com.google.common.base.Verify;
import com.ibm.icu.text.Transliterator;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.authentication.fw.dto.OutputValidationDto;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.precon.MessagePrecondtion;
import io.mosip.authentication.fw.precon.XmlPrecondtion;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;
 
/**
 * Class to hold dependency method for ida tests automation
 * 
 * @author Vignesh
 *
 */
public class AuthTestsUtil extends BaseTestCase {
	
	private static final Logger IDASCRIPT_LOGGER = Logger.getLogger(AuthTestsUtil.class);
	private static String testCaseName;
	private static int testCaseId;
	private static File testFolder;
	private static File demoAppBatchFilePath;
	public static final String AUTHORIZATHION_COOKIENAME="Authorization";
	public static final String authHeaderValue="Some String";
	protected static String responseJsonToVerifyDigtalSignature;
	protected static String responseDigitalSignatureValue;
	protected static String responseDigitalSignatureKey="response-signature";
	protected static String logFileName="id-auth.log";
	
	/**
	 * The method will get current test execution folder
	 * 
	 * @return File
	 */
	public static File getTestFolder() {
		return testFolder;
	}

	/**
	 * The method will set current test execution folder
	 * 
	 * @param testFolder
	 */
	public static void setTestFolder(File testFolder) {
		AuthTestsUtil.testFolder = testFolder;
	}

	/**
	 * The method will get current test execution test case name
	 * 
	 * @return String, test case name
	 */
	public static String getTestCaseName() {
		return testCaseName;
	}

	/**
	 * The method will set test case name for current test execution
	 * 
	 * @param testCaseName
	 */
	public static void setTestCaseName(String testCaseName) {
		AuthTestsUtil.testCaseName = testCaseName;
	}

	/**
	 * The method will get current execution of test case id
	 * 
	 * @return Integer
	 */
	public static int getTestCaseId() {
		return testCaseId;
	}

	/**
	 * The method will set current execution of test case id
	 * 
	 * @param testCaseId
	 */
	public static void setTestCaseId(int testCaseId) {
		AuthTestsUtil.testCaseId = testCaseId;
	}

	/**
	 * The method will post request and generate output file
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return true or false
	 */
	protected boolean postRequestAndGenerateOuputFile(File[] listOfFiles, String urlPath, String keywordToFind,
			String generateOutputFileKeyword, int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					Response response=null;
					String responseJson = "";
					if (code == 0)
						response = postRequestWithAuthHeader(listOfFiles[j].getAbsolutePath(), urlPath, AUTHORIZATHION_COOKIENAME, authHeaderValue);
					else
						response = postRequestWithAuthHeader(listOfFiles[j].getAbsolutePath(), urlPath, code, AUTHORIZATHION_COOKIENAME, authHeaderValue);
					responseJson=response.asString();
					responseJsonToVerifyDigtalSignature=responseJson;
					responseDigitalSignatureValue=response.getHeader(responseDigitalSignatureKey);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(responseJson) + "</pre>");
					responseJson=JsonPrecondtion.toPrettyFormat(responseJson);
					fos.write(responseJson.getBytes());
					fos.flush();
					fos.close();
				}
			}
			return true;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return false;
		}
	}
	
	/**
	 * The method will post request and generate output file
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return true or false
	 */
	protected boolean postRequestAndGenerateOuputFileForIntenalAuth(File[] listOfFiles, String urlPath, String keywordToFind,
			String generateOutputFileKeyword,String cookieName, String cookieValue,int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					Response response;
					if (code == 0)
						response = postRequestWithCookieAndHeader(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue, AUTHORIZATHION_COOKIENAME, authHeaderValue);
					else
						response = postRequestWithCookieAndHeader(listOfFiles[j].getAbsolutePath(), urlPath, code,cookieName,cookieValue, AUTHORIZATHION_COOKIENAME, authHeaderValue);
					responseJsonToVerifyDigtalSignature=response.asString();
					responseDigitalSignatureValue=response.getHeader(responseDigitalSignatureKey);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(response.asString()) + "</pre>");
					fos.write(response.asString().getBytes());
					fos.flush();
					fos.close();
				}
			}
			return true;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return false;
		}
	}
	
	/**
	 * The method will post request and generate output file with return repose
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return String , response for post request
	 */
	protected String postRequestAndGenerateOuputFileWithResponse(File[] listOfFiles, String urlPath,
			String keywordToFind, String generateOutputFileKeyword,String cookieName,String cookieValue, int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					Response responseJson;
					if (code == 0)
						responseJson = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue);
					else
						responseJson = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath, code,cookieName,cookieValue);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(responseJson.asString()) + "</pre>");
					fos.write(responseJson.asString().getBytes());
					fos.flush();
					fos.close();
					return responseJson.asString().toString();
				}
			}
			return "NoResponse";
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return e.getMessage();
		}
	}
	
	protected Response postRequestWithCookie(String filename, String url, int expCode,String cookieName,String cookieValue) {
		Response response=null;
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			response = RestClient.postRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON,cookieName,cookieValue);
			Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
			List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
			objList.add(verifyStatusCode(response,expCode));
			objMap.put("Status Code", objList);
			Reporter.log(ReportUtil.getOutputValiReport(objMap));
			Verify.verify(OutputValidationUtil.publishOutputResult(objMap));
			return response;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return response;
		}
	}
	protected Response postRequestWithCookieAndHeader(String filename, String url, int expCode,String cookieName,String cookieValue, String authHeaderName, String authHeaderValue) {
		Response response=null;
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			response = RestClient.postRequestWithCookieAndHeader(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON,cookieName,cookieValue, authHeaderName, authHeaderValue);
			Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
			List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
			objList.add(verifyStatusCode(response,expCode));
			objMap.put("Status Code", objList);
			Reporter.log(ReportUtil.getOutputValiReport(objMap));
			Verify.verify(OutputValidationUtil.publishOutputResult(objMap));
			return response;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return response;
		}
	}	
	
	/**
	 * The method will post request and generate output file for UIN generation
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return true or false
	 */
	protected boolean postRequestAndGenerateOuputFileForUINGeneration(File[] listOfFiles, String urlPath,
			String keywordToFind, String generateOutputFileKeyword,String cookieName,String cookieValue,int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					Response responseJson;
					if (code == 0)
						responseJson = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue);
					else
						responseJson = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath, code,cookieName,cookieValue);
					if (responseJson.asString().contains("Invalid UIN")) {
						fos.flush();
						fos.close();
						return false;
					} else {
						Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
								+ ReportUtil.getTextAreaJsonMsgHtml(responseJson.asString()) + "</pre>");
						fos.write(responseJson.asString().getBytes());
						fos.flush();
						fos.close();
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return false;
		}
	}
	
	/**
	 * The method will post request and generate output file for UIN update
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return true or false
	 */
	protected boolean postRequestAndGenerateOuputFileForUINUpdate(File[] listOfFiles, String urlPath, String keywordToFind,
			String generateOutputFileKeyword, String cookieName,String cookieValue,int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					String responseJson = "";
					if (code == 0)
						responseJson = patchRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue).asString();
					/*else
						responseJson = patchRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath, code);*/
					Reporter.log("<b><u>Actual Patch Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(responseJson) + "</pre>");
					fos.write(responseJson.getBytes());
					fos.flush();
					fos.close();
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return false;
		}
	}
	
	protected Response postRequestWithAuthHeader(String filename, String url, String authHeaderName, String authHeaderValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.postRequestWithAuthHeader(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, authHeaderName, authHeaderValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	
	/**
	 * The method will help to patch request
	 * 
	 * @param filename
	 * @param url
	 * @return String, Reponse for request
	 */
	protected String patchRequest(String filename, String url) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient.patchRequest(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON).asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
    /**
     * The method will help to patch request and verify status code
     * 
     * @param filename
     * @param url
     * @param expCode
     * @return
     */
	protected String patchRequest(String filename, String url, int expCode) {
		Response response=null;
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			response = RestClient.patchRequest(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON);
			Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
			List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
			objList.add(verifyStatusCode(response,expCode));
			objMap.put("Status Code", objList);
			Reporter.log(ReportUtil.getOutputValiReport(objMap));
			Verify.verify(OutputValidationUtil.publishOutputResult(objMap));
			return response.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return response.asString();
		}
	}
	
	/**
	 * The method perform status code verification
	 * 
	 * @param response
	 * @param expCode
	 * @return object of Output Validation
	 */
	private OutputValidationDto verifyStatusCode(Response response, int expCode) {
		OutputValidationDto objOpDto = new OutputValidationDto();
		if (response.statusCode() == expCode) {
			objOpDto.setFieldHierarchy("STATUS CODE");
			objOpDto.setFieldName("STATUS CODE");
			objOpDto.setActualValue(String.valueOf(response.statusCode()));
			objOpDto.setExpValue(String.valueOf(expCode));
			objOpDto.setStatus("PASS");
		} else {
			objOpDto.setFieldHierarchy("STATUS CODE");
			objOpDto.setFieldName("STATUS CODE");
			objOpDto.setActualValue(String.valueOf(response.statusCode()));
			objOpDto.setExpValue(String.valueOf(expCode));
			objOpDto.setStatus("FAIL");
		}
		return objOpDto;
	}
	
	protected Response postRequestWithAuthHeader(String filename, String url, int expCode, String authHeaderName, String authHeaderValue) {
		Response response=null;
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			response = RestClient.postRequestWithAuthHeader(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, authHeaderName, authHeaderValue);
			Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
			List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
			objList.add(verifyStatusCode(response,expCode));
			objMap.put("Status Code", objList);
			Reporter.log(ReportUtil.getOutputValiReport(objMap));
			Verify.verify(OutputValidationUtil.publishOutputResult(objMap));
			return response;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return response;
		}
	}
	
	/**
	 * The method will get response for url and type
	 * 
	 * @param url, endpoint url
	 * @param type, BIO,DEMO,ALL
	 * @return String, Response
	 */
	protected static String getResponseWithCookie(String url, String cookieName, String cookieValue) {
		try {
			return RestClient.getRequestWithCookie(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
					cookieName, cookieValue).asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	/**
	 * The method will get response for url
	 * 
	 * @param url
	 * @return String, Response
	 */
	protected static String getResponse(String url) {
		try {
			return RestClient.getRequest(url, MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON).asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	/**
	 * The method will get response for url and type
	 * 
	 * @param type, BIO,DEMO,ALL
	 * @return String, Response
	 */
	protected static String getResponseWithCookieForIdaUinGenerator(String url, String cookieName) {
		try {
			return RestClient.getRequestWithCookie(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
					cookieName, getAuthorizationCookie(getCookieRequestFilePathForUinGenerator(), getCookieUrlPath(), cookieName)).asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	/**
	 * The method will get response for url and type
	 * 
	 * @param url, endpoint url
	 * @param type, BIO,DEMO,ALL
	 * @return String, Response
	 */
	protected static String getResponseWithCookieForIdRepoUinGenerator(String url, String cookieName) {
		try {
			return RestClient.getRequestWithCookie(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
					cookieName, getAuthorizationCookie(AuthTestsUtil.getCookieRequestFilePathForUinGenerator(), getCookieUrlPath(), cookieName)).asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	/**
	 * The method will get encoded data from file from list of files
	 * 
	 * @param listOfFiles , List of files
	 * @param keywordToFind , keyword to find from list
	 * @return String, encoded data
	 */
	protected String getEnodedData(File[] listOfFiles,String keywordToFind) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordToFind)) {
				return EncryptDecrptUtil.getEncode(listOfFiles[j].getAbsolutePath());
			}
		}
		return null;
	}
	
	/**
	 * The method will get decoded data from file from list of files
	 * 
	 * @param listOfFiles , List of file
	 * @param keywordToFind , keyword to find from list
	 * @return String, decoded data
	 */
	protected String getDecodedData(File[] listOfFiles,String keywordToFind) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordToFind)) {
				return EncryptDecrptUtil.getEncode(listOfFiles[j].getAbsolutePath());
			}
		}
		return null;
	}
	
	/**
	 * The method will get decoded data from string content 
	 * 
	 * @param content
	 * @return String, decoded data
	 */
	protected static String getDecodedData(String content) {
		return EncryptDecrptUtil.getDecodeFromStr(content);
	}
	
	/**
	 * The method will display content in file
	 * 
	 * @param listOfFiles
	 * @param keywordToFind
	 */
	protected void displayContentInFile(File[] listOfFiles, String keywordToFind) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					String responseJson = getContentFromFile(listOfFiles[j]);
					Reporter.log("<pre>" + ReportUtil.getTextAreaJsonMsgHtml(responseJson) + "</pre>");
				}
			}
		} catch (Exception e) {
			IDASCRIPT_LOGGER.info("Exception : " + e);
		}
	}
	
	/**
	 * The method will get content from file
	 * 
	 * @param listOfFiles
	 * @param keywordToFind
	 * @return String, content in file
	 */
	protected String getContentFromFile(File[] listOfFiles, String keywordToFind) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					return getContentFromFile(listOfFiles[j]);
				}
			}
		} catch (Exception e) {
			IDASCRIPT_LOGGER.info("Exception : " + e);
			return e.getMessage();
		}
		return null;
	}
	
	/**
	 * The method will get content from file
	 * 
	 * @param file
	 * @return String, content in file
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static String getContentFromFile(File file) throws IOException {
		try {
		return FileUtils.readFileToString(file.getAbsoluteFile());
		}
		catch(Exception e)
		{
			IDASCRIPT_LOGGER.error("Exception: "+e.getMessage());
			return e.getMessage();
		}
	}
	
	/**
	 * The method will modify json request
	 * 
	 * @param listOfFiles
	 * @param fieldvalue
	 * @param propFileName
	 * @param keywordinFile
	 * @return true or false
	 */
	protected boolean modifyRequest(File[] listOfFiles, Map<String, String> fieldvalue, String propFileName,
			String keywordinFile) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordinFile))
				{
					MessagePrecondtion jsonPrecon = new JsonPrecondtion();
					jsonPrecon.parseAndWriteFile(listOfFiles[j].getAbsolutePath(), fieldvalue,
							listOfFiles[j].getAbsolutePath(), propFileName);
				}
			}
			return true;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception occured:" + e.getMessage());
			return false;
		}
	}
	
	/**
	 * The method get encryptedsessionkey, request and hmac value
	 * 
	 * @param listOfFiles
	 * @param keywordToFind
	 * @return map
	 */
	protected Map<String, String> getEncryptKeyvalue(File[] listOfFiles, String keywordToFind) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordToFind)) {
				return EncryptDecrptUtil.getEncryptSessionKeyValue(listOfFiles[j].getAbsolutePath());
			}
		}
		return null;
	}
	
	/**
	 * The method get internal encryptedsessionkey, request and hmac value
	 * 
	 * @param listOfFiles
	 * @param keywordToFind
	 * @return map
	 */
	protected Map<String, String> getInternalEncryptKeyvalue(File[] listOfFiles, String keywordToFind) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordToFind)) {
				return EncryptDecrptUtil.getInternalEncryptSessionKeyValue(listOfFiles[j].getAbsolutePath());
			}
		}
		return null;
	}
	
	/**
	 * To display current execution of test case log
	 * 
	 * @param testCaseName
	 * @param testCaseNumber
	 */
	protected void displayLog(File testCaseName, int testCaseNumber) {
		IDASCRIPT_LOGGER.info(
				"**************************************************************************************************************************************");
		IDASCRIPT_LOGGER.info("*          Test Case Id: " + testCaseNumber);
		IDASCRIPT_LOGGER.info("*          Test Case Name: " + testCaseName.getName());
		IDASCRIPT_LOGGER.info(
				"**************************************************************************************************************************************");
	}
	
	/**
	 * The method get property value for the key
	 * 
	 * @param key
	 * @return string
	 */
	public static String getPropertyValue(String key) {
		return getRunConfigData().getProperty(key);
	}
	
	/**
	 * The method get env config details
	 * 
	 * @return properties
	 */
	private static Properties getRunConfigData() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			RunConfigUtil.objRunConfig.setUserDirectory();
			input = new FileInputStream(new File(RunConfigUtil.getResourcePath()+"/ida/TestData/RunConfig/envRunConfig.properties").getAbsolutePath());
			prop.load(input);
			input.close();
			return prop;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e.getMessage());
			return prop;
		}
	}
	
	/**
	 * The method get otp value from database
	 * 
	 * @param inputFilePath
	 * @param mappingFileName
	 * @param otpMappingFieldName
	 * @return String , OTP Value
	 */
		public String getOtpValue(String inputFilePath, String mappingFileName, String otpMappingFieldName) {
		String value = JsonPrecondtion.getValueFromJson(inputFilePath, mappingFileName, otpMappingFieldName);
		if (value.contains(":")) {
			if(proxy) return "111111";
			String[] otpKeyword = value.split(":");
			String otpQuery = otpKeyword[0];
			String waitTime = otpKeyword[1];
			wait(Integer.parseInt(waitTime) * 1000);
			return DbConnection.getDataForQuery(otpQuery, "KERNEL").get("otp");
		} else
			return value;
	}
	
	/**
	 * The method retrieve value from json
	 * 
	 * @param listOfFiles
	 * @param mappingFileName
	 * @param mappingFieldName
	 * @param keywordinFile
	 * @return String
	 */
	public String getValueFromJson(File[] listOfFiles, String mappingFileName, String mappingFieldName,
			String keywordinFile) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordinFile)) {
				return JsonPrecondtion.getValueFromJson(listOfFiles[j].getAbsolutePath(), mappingFileName,
						mappingFieldName);
			}
		}
		return "No Value Found From Json, Check mapping field or file name and input json file";
	}
    
	/**
	 * The method to wait for period of time
	 * 
	 * @param time
	 */
	public static void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.info("Exception :" + e);
		}
	}
	
	/**
	 * The method will perform language converter
	 * 
	 * @param inputString
	 * @param langSourceCode
	 * @param langDestCode
	 * @return String
	 */
	public static String languageConverter(String inputString, String langSourceCode, String langDestCode) {
		final String language_translation_code = langSourceCode + "-" + langDestCode;
		Transliterator input = Transliterator.getInstance(language_translation_code);
		String transliteratedString = input.transliterate(inputString);
		return transliteratedString;
	}
	
	/**
	 * Create generated UIN number and its test case name in property file
	 * 
	 * @param filePath
	 */
	public static void generateMappingDic(String filePath, Map<String, String> map) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(filePath);
			for (Entry<String, String> entry : map.entrySet()) {
				prop.setProperty(entry.getKey(), entry.getValue());
			}
			prop.store(output, null);
			output.close();
			output.flush();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Excpetion in storing the data in propertyFile" + e.getMessage());
		}
	}
	
	/**
	 * The method will update the existing mapping dictionary in property file
	 * 
	 * @param filePath
	 * @param map
	 */
	public static void updateMappingDic(String filePath, Map<String, String> map) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Properties props = new Properties();
			props.load(in);
			in.close();
			FileOutputStream out = new FileOutputStream(filePath);
			for (Entry<String, String> entry : map.entrySet()) {
				props.setProperty(entry.getKey(), entry.getValue());
			}
			props.store(out, null);
			out.close();
			out.flush();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception in updating the property file" + e.getMessage());
		}
	}
	
	/**
	 * The method will update mapping dictionary for email notification 
	 * 
	 * @param filePath
	 * @param map
	 */
	public void updateMappingDicForEmailOtpNotification(String filePath, Map<String, String> map) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Properties props = new Properties();
			props.load(in);
			in.close();
			for (Entry<String, String> entry : map.entrySet()) {
				if (entry.getValue().contains("$otp$")) {
					String value = entry.getValue().replace("$otp$", map.get("email.otp"));
					props.setProperty(entry.getKey(), value);
				} else
					props.setProperty(entry.getKey(), entry.getValue());
			}
			props.store(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"), null);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception in updating the property file" + e.getMessage());
		}
	}
	
	/**
	 * The method will get value from property file
	 * 
	 * @param filepath
	 * @param key
	 * @return string
	 */
	public static String getValueFromPropertyFile(String filepath, String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(filepath);
			prop.load(input);
			input.close();
			return prop.getProperty(key).toString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e.getMessage());
			return e.getMessage();
		}
	}
	
	/**
	 * The method will get property from file path
	 * 
	 * @param filepath
	 * @return properties
	 */
	public static Properties getPropertyFromFilePath(String filepath) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(filepath);
			prop.load(input);
			input.close();
			return prop;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e.getMessage());
			return prop;
		}
	}
    /**
     * The method will get property from relative file path
     * 
     * @param path
     * @return properties
     */
	public static Properties getPropertyFromRelativeFilePath(String path) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(new File(RunConfigUtil.getResourcePath() + path).getAbsolutePath());
			prop.load(input);
			input.close();
			return prop;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception occured in fetching data property file " + e.getMessage());
			return prop;
		}
	}
	/**
	 * The method will get property as map
	 * 
	 * @param filepath
	 * @return map
	 */
	public static Map<String, String> getPropertyAsMap(String filepath) {
		Properties prop = new Properties();
		InputStream input = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			input = new FileInputStream(filepath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				map.put(key, prop.getProperty(key));
			}
			input.close();
			return map;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e.getMessage());
			return map;
		}
	}
	
	/*public static void regenerateVID() {
		if (!AuthenticationTestsListener.currentTestMethod.toString()
				.contains("authentication.idRepository.prerequiste")) {
			IDASCRIPT_LOGGER.info("Regenerating Used VID...");
			VIDUtil.regenerateVID();
		}
	}*/
	
	public static void initiateAuthTest() {
		copyAuthTestResource();
		//IdRepoTestsUtil.copyIdrepoTestResource();
		//ResidentTestUtil.initiateResidentTest();
	}
	//static KernelDataBaseAccess masterDB = new KernelDataBaseAccess();
	/*
	 * public static void createDeviceManagementData() { String crtQuerKeys[] =
	 * queries.get("crtAuthDevicesData").toString().split(","); List<String>
	 * crtQueries = new LinkedList<String>(); for(String queryKeys: crtQuerKeys)
	 * crtQueries.add(queries.get(queryKeys).toString()); if
	 * (masterDB.executeQueries(crtQueries, "authdevice"))
	 * logger.info("created device management data for automation"); else logger.
	 * info("not able to create device management data, IDA authentications will fail"
	 * ); } public static void deleteDeviceManagementData() { String dltQueryKeys[]
	 * = queries.get("dltAuthDevicesData").toString().split(","); List<String>
	 * dltQueries = new LinkedList<String>(); for(String queryKeys: dltQueryKeys)
	 * dltQueries.add(queries.get(queryKeys).toString()); if
	 * (masterDB.executeQueries(dltQueries, "authdevice"))
	 * logger.info("deleted created device management data for automation"); else
	 * logger.info("not able to delete device management data"); }
	 */

	/**
	 * The method will create bat or sh file to run demoApp jar in windows or linux OS respectively
	 */
	public static void createBatOrShFileForDemoApp() {
		try {
			String javaHome = System.getenv("JAVA_HOME");
			String demoAppJarPath = null;
			String content = null;
			if (getOSType().toString().equals("WINDOWS")) {
				demoAppJarPath = new File("C:/Users/" + System.getProperty("user.name")
						+ "/.m2/repository/io/mosip/authentication/authentication-partnerdemo-service/" + getDemoAppVersion()
						+ "/authentication-partnerdemo-service-" + getDemoAppVersion() + ".jar").getAbsolutePath();
				demoAppBatchFilePath = new File(RunConfigUtil.getResourcePath()+"/demoApp.bat");
				content = '"' + javaHome + "/bin/java" + '"'
						+ " -Dspring.cloud.config.label=QA_IDA -Dspring.profiles.active=test"+RunConfigUtil.getRunEvironment()+" -Dspring.cloud.config.uri=http://104.211.212.28:51000 -jar "
						+ '"' + demoAppJarPath.toString() + '"';
			} else if (getOSType().toString().equals("OTHERS")) {
				IDASCRIPT_LOGGER.info("Maven Path: " + RunConfigUtil.getLinuxMavenPath());
			    String mavenPath = RunConfigUtil.getLinuxMavenPath();
				String settingXmlPath = mavenPath+ "/conf/settings.xml";
				String repoPath = XmlPrecondtion.getValueFromXmlFile(settingXmlPath, "//localRepository");
				demoAppJarPath = new File(repoPath + "/io/mosip/authentication/authentication-partnerdemo-service/"
						+ getDemoAppVersion() + "/authentication-partnerdemo-service-" + getDemoAppVersion() + ".jar")
								.getAbsolutePath();
				RunConfigUtil.getRunConfigObject("ida");
				RunConfigUtil.objRunConfig.setUserDirectory();
				demoAppBatchFilePath = new File(RunConfigUtil.getResourcePath()+"/demoApp.sh");
				content = "nohup java -Dspring.cloud.config.label=QA_IDA -Dspring.cloud.config.uri=http://104.211.212.28:51000 -Dspring.profiles.active=test"+RunConfigUtil.getRunEvironment()+" -Djava.net.useSystemProxies=true -jar "
						+ '"' + demoAppJarPath.toString() + '"' +" &";
				fileDemoAppJarPath = new File(demoAppJarPath.toString());
				if (fileDemoAppJarPath.exists())
					IDASCRIPT_LOGGER.info("DemoApp Jar FILE IS AVAILABLE");
				else
					IDASCRIPT_LOGGER.error("DemoApp Jar FILE IS NOT AVAILABLE");

				Path path = Paths.get(fileDemoAppJarPath.getAbsolutePath());
				changeFilePermissionInLinux(path);
			}
			IDASCRIPT_LOGGER.info("DemoApp Jar: " + demoAppJarPath);
			IDASCRIPT_LOGGER.info("Cmd Path: " + content);
			FileOutputStream fos = new FileOutputStream(demoAppBatchFilePath);
			DataOutputStream dos = new DataOutputStream(fos);
			dos.writeBytes(content);
			dos.close();
			fos.flush();
			fos.close();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception in creating the bat file for demoApp application " + e.getMessage());
		}
	}
	
	/**
	 * The method will change the file permission
	 * 
	 * @param path
	 */
	private static void changeFilePermissionInLinux(Path path) {
		try {
			Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class).permissions();
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.add(PosixFilePermission.GROUP_WRITE);
			perms.add(PosixFilePermission.GROUP_READ);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			perms.add(PosixFilePermission.OTHERS_WRITE);
			perms.add(PosixFilePermission.OTHERS_READ);
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(path, perms);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception in change the file permission:" + e.getMessage());
		}
	}
	
	/**
	 * The method will help to run the demoApp bat file through command prompt
	 */
	public static void batDemoAppRunner() {
		try {
			Runtime.getRuntime().exec(
					new String[] { "cmd", "/c", "start", "cmd.exe", "/K", demoAppBatchFilePath.getAbsolutePath() });
			Thread.sleep(60000);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Execption in launching demoApp application: " + e.getMessage());
		}
	}
	private static File fileDemoAppJarPath;
	/**
	 * The method will help to run the demoApp sh file through shell command
	 */
	public static void shDemoAppRunner() {
		try {
			Path path = Paths.get(demoAppBatchFilePath.getAbsolutePath());
			changeFilePermissionInLinux(path);
			Runtime.getRuntime().exec(new String[] { "sh",demoAppBatchFilePath.getAbsolutePath() });
			IDASCRIPT_LOGGER.info("sh file: "+demoAppBatchFilePath.getAbsolutePath());			
			Thread.sleep(60000); 
			IDASCRIPT_LOGGER.info("File path:"+fileDemoAppJarPath.getParentFile()+"/nohup.out");	
			if(new File(fileDemoAppJarPath.getParentFile()+"/nohup.out").exists())
			{
				IDASCRIPT_LOGGER.info("NOHUP FILE AVAILABLE");	
				IDASCRIPT_LOGGER.info(FileUtil.readInput(fileDemoAppJarPath.getParentFile()+"/nohup.out"));
			}
			else
				IDASCRIPT_LOGGER.error("NOHUP FILE NOT AVAILABLE");	
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Execption in launching demoApp application: " + e.getMessage());
		}
	}
	
	/**
	 * The method will terminate demoApp bat file
	 */
	public static void exitDemoAppBatchRunner() {
		try {
			Runtime.getRuntime()
					.exec(new String[] { "cmd", "/c", "start", "cmd.exe", "/K", "taskkill /f /im conhost.exe" });
			Runtime.getRuntime()
					.exec(new String[] { "cmd", "/c", "start", "cmd.exe", "/K", "taskkill /f /im java.exe" });
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Execption in terminating demoApp application" + e.getMessage());
		}
	}
	
	/**
	 * The method use to add partnerID and License key in endpoint url
	 * 
	 * @param file
	 * @return PartnerID and LicenseKey
	 */
	public String getExtendedUrl(File file) {
		if (file.exists()) {
			Map<String, String> urlProperty = getPropertyAsMap(file.getAbsolutePath());
			if (urlProperty.containsKey("partnerIDMispLK")) {
				return "/" + urlProperty.get("partnerIDMispLK").toString();
			} else if (urlProperty.containsKey("partnerID") && urlProperty.containsKey("mispLK")&& urlProperty.containsKey("apiKey")) {
				return "/" + urlProperty.get("mispLK").toString() + "/" + urlProperty.get("partnerID").toString() + "/" + urlProperty.get("apiKey").toString();
			}
		} else
			return "";
		return "NO Value Found in TestData";
	}
	
	/**
	 * The method will get current dempApp version from pom file
	 * 
	 * @return version of demoApp as string
	 */
	public static String getDemoAppVersion() {
		String expression = "//dependency/artifactId[text()='authentication-partnerdemo-service']//following::version";
		return RunConfigUtil.getdemoAppVersion();
	}
	
	public File getFile(File[] listOfFiles, String keywordToFind) {
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].getName().contains(keywordToFind)) {
				return listOfFiles[j];
			}
		}
		return null;
	}
	
	/**
	 * The method will get otp value
	 * 
	 * @param value
	 * @return OTP value
	 */
	public String getOtpValue(String value) {
		if (value.contains(":")) {
			if(proxy) return "111111";
			String[] otpKeyword = value.split(":");
			String otpQuery = otpKeyword[0];
			String waitTime = otpKeyword[1];
			wait(Integer.parseInt(waitTime) * 1000);
			return DbConnection.getDataForQuery(otpQuery, "KERNEL").get("otp");
		} else
			return value;
	}
	
	/**
	 * The method returns run config path
	 */
	public String getRunConfigFile() {
		return RunConfigUtil.getGlobalResourcePath()+"/ida/TestData/RunConfig/runConfiguration.properties";
	}
	
	/**
	 * The method return test data path from config file
	 * 
	 * @param className
	 * @param index
	 * @return string
	 */
	public String getTestDataPath(String className, int index) {
		return getPropertyAsMap(new File(getRunConfigFile()).getAbsolutePath().toString())
				.get(className + ".testDataPath[" + index + "]");
	}
	
	/**
	 * The method will return test data file name from config file
	 * 
	 * @param className
	 * @param index
	 * @return string
	 */
	public String getTestDataFileName(String className, int index) {
		return getPropertyAsMap(new File(getRunConfigFile()).getAbsolutePath().toString())
				.get(className + ".testDataFileName[" + index + "]");
	}
	
	protected static String getAuthorizationCookie(String filename, String urlPath,String cookieName) {
		JSONObject objectData = null;
		try {
			String json = getContentFromFile(new File(filename));
			json=json.replace("$TIMESTAMPZ$", AdminTestUtil.generateCurrentUTCTimeStamp());
			objectData = (JSONObject) new JSONParser().parse(json);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception Occured :" + e.getMessage());
		}
		return RestClient.getCookie(urlPath, objectData.toJSONString(), MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON,cookieName);
	}
	
	protected Response postRequestWithCookie(String filename, String url,String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.postRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	protected Response postRequestWithCookieAndHeader(String filename, String url,String cookieName, String cookieValue, String authHeaderName, String authHeaderValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.postRequestWithCookieAndHeader(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue, authHeaderName, authHeaderValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}	
	protected String postRequestWithCookie(String filename, String url,String cookieName, String cookieValue,int code) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.postRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}

	protected static String postStrContentRequestWithCookie(String content, String url,String cookieName, String cookieValue) {
		try {
			return RestClient
					.postRequestWithCookie(url, content, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	protected static String getCookieRequestFilePath() {
		return RunConfigUtil.getResourcePath()
				+ "ida/TestData/Security/GetCookie/"+RunConfigUtil.getRunEvironment()+".getCookieRequest.json".toString();
	}
	
	protected static String getCookieRequestFilePathForUinGenerator() {
		return RunConfigUtil.getResourcePath()
				+ "ida/TestData/Security/GetCookie/"+RunConfigUtil.getRunEvironment()+".getCookieForUinGenerator.json".toString();
	}
	
	protected static String getCookieRequestFilePathForInternalAuth() {
		return RunConfigUtil.getResourcePath()
				+ "ida/TestData/Security/GetCookie/"+RunConfigUtil.getRunEvironment()+".getCookieForInternalAuth.json".toString();
	}
	
	protected static String getCookieRequestFilePathForResidentAuth() {
		return RunConfigUtil.getResourcePath()
				+ "ida/TestData/Security/GetCookie/"+RunConfigUtil.getRunEvironment()+".residentServiceCredential.json".toString();
	}
	
	protected Response patchRequestWithCookie(String filename, String url,String cookieName,String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient.patchRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON,cookieName, cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	
	protected Response patchRequestWithCookie(String filename, String url, int expCode,String cookieName,String cookieValue) {
		Response response=null;
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			response = RestClient.patchRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON,cookieName,cookieValue);
			Map<String, List<OutputValidationDto>> objMap = new HashMap<String, List<OutputValidationDto>>();
			List<OutputValidationDto> objList = new ArrayList<OutputValidationDto>();
			objList.add(verifyStatusCode(response,expCode));
			objMap.put("Status Code", objList);
			Reporter.log(ReportUtil.getOutputValiReport(objMap));
			Verify.verify(OutputValidationUtil.publishOutputResult(objMap));
			return response;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return response;
		}
	}
	
	protected static String getCookieUrlPath() {
		return RunConfigUtil.objRunConfig.getEndPointUrl() + RunConfigUtil.objRunConfig.getClientidsecretkey();
	}
	
	/**
	 * Method return random integer value for number of digit
	 * 
	 * @param digit
	 * @return string
	 */
	public static String randomize(int digit){
        Random r = new Random();
        String randomNumber="";
        for (int i = 0; i < digit; i++) {
        	randomNumber=randomNumber+r.nextInt(9);
        }
        return randomNumber;
    }
	
	public static String getVidRequestContent() {
		try {
			return getContentFromFile(new File(RunConfigUtil.getResourcePath()
					+ "ida/VIDData/VIDGeneration/VIDGenerate_smoke/vid-request.json"));
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception Occured in getting the VID request file" + e.getMessage());
			return e.getMessage();
		}
	}
	
	public static String getVidRequestContentTemplate() {
		try {
			return getContentFromFile(new File(RunConfigUtil.getResourcePath()
					+ "ida/TestData/VIDData/VIDGeneration/input/vid-request.json"));
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception Occured in getting the VID request file" + e.getMessage());
			return e.getMessage();
		}
	}
	
	/**
	 * The method will post request and generate output file for VID generation
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return true or false
	 */
	public static String postRequestAndGetResponseForVIDGeneration(String content, String urlPath, String cookieName,
			String cookieValue) {
		try {
			return postStrContentRequestWithCookie(content, urlPath, cookieName, cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return e.getMessage();
		}
	}
	
	protected boolean verifyResponseUsingDigitalSignature(String resonseContent, String digitalSignature) {
		String dgPath = RunConfigUtil.objRunConfig.getValidateSignaturePath().replace("$signature$", digitalSignature);
		String signatureApiPath = RunConfigUtil.objRunConfig.getEncryptUtilBaseUrl() + dgPath;
		Response response = RestClient.postRequest(signatureApiPath, resonseContent, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
		if (response.asString().contains("success"))
			return true;
		else
			return false;
	}
	
	public static void copyAuthTestResource() {
		try {
			File source = new File(RunConfigUtil.getGlobalResourcePath() + "/ida");
			File config = new File(RunConfigUtil.getGlobalResourcePath() + "/config");
			File destination = new File(RunConfigUtil.getGlobalResourcePath() + "/"+RunConfigUtil.resourceFolderName);
			FileUtils.copyDirectoryToDirectory(source, destination);
			FileUtils.copyDirectoryToDirectory(config, destination);
			
			IDASCRIPT_LOGGER.info("Copied the authentication test resource successfully");
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception occured while copying the file: "+e.getMessage());
		}
	}

	public static void removeOldMosipTempTestResource() {
		File authTestFile = new File(RunConfigUtil.getGlobalResourcePath() + "/"+RunConfigUtil.resourceFolderName);
		if (authTestFile.exists())
			if (FileUtil.deleteDirectory(authTestFile))
				IDASCRIPT_LOGGER.info("Old "+RunConfigUtil.resourceFolderName+" folder successfully deleted!!");
			else
				IDASCRIPT_LOGGER.error("Old "+RunConfigUtil.resourceFolderName+" folder not deleted.");
	}
	/**
	 * The method will get request and generate output file with return repose
	 * 
	 * @param listOfFiles
	 * @param urlPath
	 * @param keywordToFind
	 * @param generateOutputFileKeyword
	 * @param code
	 * @return String , response for post request
	 */
	protected String getRequestAndGenerateOuputFileWithResponse(String parentFile,String urlPath,
			String generateOutputFileKeyword,String cookieName,String cookieValue) {
		try {
					FileOutputStream fos = new FileOutputStream(
							parentFile + "/" + generateOutputFileKeyword + ".json");
					String responseJson = getResponseWithCookie(urlPath, cookieName, cookieValue);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(responseJson) + "</pre>");
					fos.write(responseJson.getBytes());
					fos.flush();
					fos.close();
					return responseJson.toString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return e.getMessage();
		}
	}
	
	public static int getNumberOfTimeWordPresentInString(String data, String keyword) {
		int i = 0;
		Pattern p = Pattern.compile(keyword);
		Matcher m = p.matcher(data);
		while (m.find()) {
			i++;
		}
		return i;
	}
	protected String putRequestWithparm(String filename, String url,String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.putRequestWithParm(url, objectData, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	protected String putRequestWithCookie(String filename, String url,String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.putRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}	
	
	public String getUinHashWithSalt(String uin) {
		Long uinModulo = (Long.parseLong(uin) % 1000);
		String uinSaltQuery = "select salt from ida.uin_hash_salt where id='" + uinModulo.toString() + "'";
		Map<String, String> uinSalt = DbConnection.getDataForQuery(uinSaltQuery, "IDA");
		return HMACUtils.digestAsPlainTextWithSalt(uin.getBytes(), uinSalt.get("salt").getBytes());
	}
	
	public static int generateRandomIntRange(int min, int max) {
	    Random r = new Random();
	    return r.nextInt((max - min) + 1) + min;
	}
	
	// Added by Admin Test Team
	protected Response getRequestWithPathParm(String filename, String url, String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient.getRequestWithCookieAndPathParm(url, objectData, MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, cookieName, cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}

	protected Response getRequestWithQueryParm(String filename, String url, String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient.getRequestWithCookieAndQueryParm(url, objectData, MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, cookieName, cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	
	protected boolean patchRequestAndGenerateOuputFileForIntenalAuth(File[] listOfFiles, String urlPath, String keywordToFind,
			String generateOutputFileKeyword,String cookieName, String cookieValue,int code) {
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					Response response;
					if (code == 0)
						response = patchRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue);
					else
						response = patchRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath, code,cookieName,cookieValue);
					responseJsonToVerifyDigtalSignature=response.asString();
					responseDigitalSignatureValue=response.getHeader(responseDigitalSignatureKey);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(response.asString()) + "</pre>");
					fos.write(response.asString().getBytes());
					fos.flush();
					fos.close();
				}
			}
			return true;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return false;
		}
	}
	
	protected Response postRequestAndGenerateOuputFileAndReturnResponse(File[] listOfFiles, String urlPath, String keywordToFind,
			String generateOutputFileKeyword,String cookieName, String cookieValue,int code) {
		Response response=null;
		try {
			for (int j = 0; j < listOfFiles.length; j++) {
				if (listOfFiles[j].getName().contains(keywordToFind)) {
					FileOutputStream fos = new FileOutputStream(
							listOfFiles[j].getParentFile() + "/" + generateOutputFileKeyword + ".json");
					
					if (code == 0)
						response = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath,cookieName,cookieValue);
					else
						response = postRequestWithCookie(listOfFiles[j].getAbsolutePath(), urlPath, code,cookieName,cookieValue);
					responseJsonToVerifyDigtalSignature=response.asString();
					responseDigitalSignatureValue=response.getHeader(responseDigitalSignatureKey);
					Reporter.log("<b><u>Actual Response Content: </u></b>(EndPointUrl: " + urlPath + ") <pre>"
							+ ReportUtil.getTextAreaJsonMsgHtml(response.asString()) + "</pre>");
					fos.write(response.asString().getBytes());
					fos.flush();
					fos.close();
				}
			}
			return response;
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception " + e);
			return response;
		}
	}
	
	/*
	 * public static boolean verifyAuthStatusTypeInDB(String uin,String type, String
	 * authType,String status) { if(type.equals("VID"))
	 * uin=UINUtil.getUinForVid(uin); String query =
	 * "select * from ida.uin_auth_lock where uin = '" + uin +
	 * "' and auth_type_code = '" + authType + "' order by cr_dtimes desc limit 1";
	 * Map<String, String> actualRecord = DbConnection.getDataForQuery(query,
	 * "IDA"); if (actualRecord==null ||
	 * !actualRecord.get("status_code").equals(status)) {
	 * IDASCRIPT_LOGGER.error("No Data Found in DB with query: "+query);
	 * IDASCRIPT_LOGGER.error("Result of the query: "+actualRecord); return false; }
	 * return true; }
	 */
	
	protected Response deleteRequestWithPathParm(String filename, String url,String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.deleteRequestWithCookieAndPathParm(url,objectData, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	protected String putRequestWithQueryparm(String filename, String url,String cookieName, String cookieValue) {
		try {
			JSONObject objectData = (JSONObject) new JSONParser().parse(new FileReader(filename));
			return RestClient
					.putRequestWithQueryParm(url, objectData, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	protected String putRequestWithParameter(JSONObject objectData, String url,String cookieName, String cookieValue) {
		try {
			return RestClient
					.putRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue)
					.asString();
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return e.toString();
		}
	}
	
	
	protected Response patchRequestWithParameter(JSONObject objectData, String url,String cookieName,String cookieValue) {
		try {
			return RestClient.patchRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON,cookieName, cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
	
	
	protected Response postRequestWithCookieforPublishPolicy(JSONObject objectData, String url,String cookieName, String cookieValue) {
		try {
			return RestClient
					.postRequestWithCookie(url, objectData.toJSONString(), MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,cookieName,cookieValue);
		} catch (Exception e) {
			IDASCRIPT_LOGGER.error("Exception: " + e);
			return null;
		}
	}
} 


