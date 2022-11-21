package io.mosip.testscripts;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
/*import org.json.simple.JSONObject;*/
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
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.authentication.fw.util.OutputValidationUtil;
import io.mosip.authentication.fw.util.ReportUtil;
import io.mosip.service.BaseTestCase;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.json.JSONObject;

public class SimplePostForSelectTenant extends AdminTestUtil implements ITest {
	private static final Logger logger = Logger.getLogger(SimplePostForSelectTenant.class);
	protected String testCaseName = "";
	public Response response = null;

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
		logger.info("Started executing yml: " + ymlFile);
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
		String[] templateFields = testCaseDTO.getTemplateFields();

		String inputJson = getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate());

		if (inputJson.contains("$1STLANG$"))
			inputJson = inputJson.replace("$1STLANG$", BaseTestCase.languageList.get(0));
		if (inputJson.contains("$2NDLANG$"))
			inputJson = inputJson.replace("$2NDLANG$", BaseTestCase.languageList.get(1));
		if (inputJson.contains("$3RDLANG$"))
			inputJson = inputJson.replace("$3RDLANG$", BaseTestCase.languageList.get(2));

		if (testCaseDTO.getTemplateFields() != null && templateFields.length > 0) {
			ArrayList<JSONObject> inputtestCases = AdminTestUtil.getInputTestCase(testCaseDTO);
			ArrayList<JSONObject> outputtestcase = AdminTestUtil.getOutputTestCase(testCaseDTO);
			// adding...
			List<String> languageList = new ArrayList<>();
			languageList = Arrays.asList(System.getProperty("env.langcode").split(","));
			for (int i = 0; i < languageList.size(); i++) {
				Innerloop: for (int j = i; j < languageList.size();) {
					response = postWithBodyAndCookie(ApplnURI + testCaseDTO.getEndPoint(),
							getJsonFromTemplate(inputtestCases.get(i).toString(), testCaseDTO.getInputTemplate()),
							COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName());

					Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(
							response.asString(),
							getJsonFromTemplate(outputtestcase.get(j).toString(), testCaseDTO.getOutputTemplate()));
					Reporter.log(ReportUtil.getOutputValiReport(ouputValid));

					if (!OutputValidationUtil.publishOutputResult(ouputValid))
						throw new AdminTestException("Failed at output validation");
					break Innerloop;
				}
			}
		} else {
			response = postWithBodyAndCookie(ApplnURI + testCaseDTO.getEndPoint(), inputJson, "Authentication",
					testCaseDTO.getRole(), testCaseDTO.getTestCaseName());
			for (Header header : response.getHeaders()) {
				if ("Set-Cookie".equals(header.getName())) {
					aspNetCoreSessionId = header.getValue().split(";")[0].split("=")[1];
					break;
				}
			}
			Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(
					response.asString(), getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()));
			Reporter.log(ReportUtil.getOutputValiReport(ouputValid));

			if (!OutputValidationUtil.publishOutputResult(ouputValid))
				throw new AdminTestException("Failed at output validation");
		}

	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
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
