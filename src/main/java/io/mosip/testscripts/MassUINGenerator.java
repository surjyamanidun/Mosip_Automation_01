package io.mosip.testscripts;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.dto.OutputValidationDto;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthTestsUtil;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.authentication.fw.util.OutputValidationUtil;
import io.mosip.authentication.fw.util.ReportUtil;
import io.mosip.authentication.fw.util.RestClient;
import io.mosip.authentication.fw.util.RunConfigUtil;
import io.mosip.kernel.util.KernelAuthentication;
import io.restassured.response.Response;

public class MassUINGenerator extends AdminTestUtil implements ITest {
	private static final Logger logger = Logger.getLogger(MassUINGenerator.class);
	protected String testCaseName = "";
	private Map<String, String> storeUinData = new HashMap<String, String>();
	private Map<String, String> storeRidData = new HashMap<String, String>();
	private static long perTCUinCount;
	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		perTCUinCount = Long.parseLong(context.getCurrentXmlTest().getLocalParameters().get("perTCUinCount"));
		logger.info("Started executing yml: "+ymlFile);
		return getYmlTestData(ymlFile);
	}

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws AuthenticationTestException
	 * @throws AdminTestException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws AuthenticationTestException, AdminTestException {		
		testCaseName = testCaseDTO.getTestCaseName();
		for(int i=0; i<perTCUinCount; i++)
		{
			createUins(testCaseDTO, i);
		}
		
	}
	
	public void createUins(TestCaseDTO testCaseDTO, int count) throws AuthenticationTestException, AdminTestException {
		String uin = JsonPrecondtion
				.getValueFromJson(RestClient.getRequestWithCookie(ApplnURI+"/v1/idgenerator/uin", MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, COOKIENAME, new KernelAuthentication().getTokenByRole(testCaseDTO.getRole())).asString(), "response.uin");
		DateFormat dateFormatter = new SimpleDateFormat("YYYYMMddHHmmss");
		Calendar cal = Calendar.getInstance();
		String timestampValue = dateFormatter.format(cal.getTime());
		//String genRid = "278476573600025" + timestampValue;
		String genRid = "27847" + RandomStringUtils.randomNumeric(10) + timestampValue;
		String inputJson =  getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate());
		inputJson = inputJson.replace("$UIN$", uin);
		inputJson = inputJson.replace("$RID$", genRid);
		Response response = postWithBodyAndCookie(ApplnURI + testCaseDTO.getEndPoint(), inputJson, COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName());
		
		Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil
				.doJsonOutputValidation(response.asString(), getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()));
		Reporter.log(ReportUtil.getOutputValiReport(ouputValid));
		boolean publishResult = OutputValidationUtil.publishOutputResult(ouputValid);
		if (!publishResult)
			throw new AdminTestException("Failed at output validation");
		else {
			storeUinData.put(testCaseName+"-"+count, uin);
			storeRidData.put(uin, genRid);
		}
	}
	
	public void writeUinRid(Map<String, String> uinMap, Map<String, String> ridMap, String testCaseName) {
		AuthTestsUtil.generateMappingDic(RunConfigUtil.getResourcePath()+"idRepository/uinrids/"+testCaseName+"UIN.properties", uinMap);
		AuthTestsUtil.generateMappingDic(RunConfigUtil.getResourcePath()+"idRepository/uinrids/"+testCaseName+"RID.properties", ridMap);
	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		logger.info("Storing UIN and RID");
		writeUinRid(storeUinData, storeRidData, testCaseName);
		logger.info("Stored UIN and RID");
		storeUinData = new HashMap<String, String>();
		storeRidData = new HashMap<String, String>();
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}	
}
