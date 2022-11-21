package io.mosip.kernel.util;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
//import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

/**
 * @author Arunakumar Rati
 *
 */
public class KernelAuthentication extends BaseTestCase{
	// Declaration of all variables
	String folder="kernel";
	String cookie;
	static String dataKey = "response";
    static String errorKey = "errors";
    static Map<String, String> tokens = new HashMap<String,String>();
	CommonLibrary clib= new CommonLibrary();
	public final Map<String, String> props = clib.readProperty("Kernel");
	
	private String individual_appid=props.get("individual_appid");
	private String individual_password=props.get("individual_password");
	private String individual_userName=props.get("individual_userName");
	
	private String regProc_appid=props.get("regProc_appid");
	private String regProc_password=props.get("regProc_password");
	private String regProc_userName=props.get("regProc_userName");
	
	private String admin_appid=props.get("admin_appid");
	private String admin_password=props.get("admin_password");
	private String admin_userName=props.get("admin_userName");
	
	private String partner_appid=props.get("partner_appid");
	private String partner_password=props.get("partner_password");
	private String partner_userName=props.get("partner_userName");
	
	private String registrationAdmin_appid=props.get("registrationAdmin_appid");;
	private String registrationAdmin_password=props.get("registrationAdmin_password");
	private String registrationAdmin_userName=props.get("registrationAdmin_userName");
	
	private String ida_appid=props.get("ida_appid");
	private String ida_password=props.get("ida_password");
	private String ida_userName=props.get("ida_userName");
	
	private String registrationOfficer_appid=props.get("registrationOfficer_appid");
	private String registrationOfficer_password=props.get("registrationOfficer_password");
	private String registrationOfficer_userName=props.get("registrationOfficer_userName");
	
	private String registrationSupervisor_appid=props.get("registrationSupervisor_appid");
	private String registrationSupervisor_password=props.get("registrationSupervisor_password");
	private String registrationSupervisor_userName=props.get("registrationSupervisor_userName");
	
	private String zonalAdmin_appid=props.get("zonalAdmin_appid");
	private String zonalAdmin_password=props.get("zonalAdmin_password");
	private String zonalAdmin_userName=props.get("zonalAdmin_userName");
	
	private String zonalApprover_appid=props.get("zonalApprover_appid");
	private String zonalApprover_password=props.get("zonalApprover_password");
	private String zonalApprover_userName=props.get("zonalApprover_userName");
	
	private String authenticationEndpoint = props.get("authentication");
	private String authenticationInternalEndpoint = props.get("authenticationInternal");
	private String sendOtp = props.get("sendOtp");
	private String useridOTP = props.get("useridOTP");
	private String testsuite="/Authorization";	
	private ApplicationLibrary appl=new ApplicationLibrary();
	private String authRequest="Config/Authorization/request.json";
	private String authInternalRequest="Config/Authorization/internalAuthRequest.json";
	private String preregSendOtp= props.get("preregSendOtp");
	private String preregValidateOtp= props.get("preregValidateOtp");

	private String authidmRequest="Config/Authorization/idmAuthRequest.json";
	private String selecttenant="Config/Authorization/selecttenant.json";
	
	public String getTokenByRole(String role)
	{
		String insensitiveRole = null;
		if(role!=null)
			insensitiveRole = role.toLowerCase();
		else return "";
	
		
		
		switch(insensitiveRole) {
		
		case "idm":
			if(!kernelCmnLib.isValidToken(idmCookie))
				idmCookie = kernelAuthLib.getAuthForidm();
			
			return idmCookie;
			
			
		case "idms":
			if(!kernelCmnLib.isValidToken(idmsCookie))
				idmsCookie = kernelAuthLib.getAuthForidmsselect();
			
			return idmsCookie;
			
		
			
		default:
			if(!kernelCmnLib.isValidToken(adminCookie))
				adminCookie = kernelAuthLib.getAuthForAdmin();
			return adminCookie;
		}
		 
	}
	

	@SuppressWarnings("unchecked")
	public String getAuthForAdmin() {

		JSONObject actualrequest = getRequestJson(authInternalRequest);

		JSONObject request = new JSONObject();
		request.put("appId", admin_appid);
		request.put("password", admin_password);
		request.put("userName", admin_userName);
		request.put("clientId", props.get("admin_clientId"));
		request.put("clientSecret", props.get("admin_clientSecret"));
		actualrequest.put("request", request);

		Response reponse = appl.postWithJson(authenticationInternalEndpoint, actualrequest);
		String responseBody = reponse.getBody().asString();
		String token = new org.json.JSONObject(responseBody).getJSONObject(dataKey).getString("token");
		return token;
	}
	
	
	@SuppressWarnings("unchecked")
	public String getAuthForPartner() {
		JSONObject actualrequest = getRequestJson(authInternalRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", partner_appid);
		request.put("password", partner_password);
		request.put("userName", partner_userName);
		request.put("clientId", props.get("partner_clientId"));
		request.put("clientSecret", props.get("partner_clientSecret"));
		actualrequest.put("request", request);
	
		Response reponse=appl.postWithJson(authenticationInternalEndpoint, actualrequest);
		String responseBody = reponse.getBody().asString();
		String token = new org.json.JSONObject(responseBody).getJSONObject(dataKey).getString("token");
		return token;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForResident() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", props.get("resident_appid"));
		request.put("clientId", props.get("resident_clientId"));
		request.put("secretKey", props.get("resident_secretKey"));
		actualrequest.put("request", request);
		
		Response reponse=appl.postWithJson(props.get("authclientidsecretkeyURL"), actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForidm() {
			JSONObject actualrequest = getRequestJson(authidmRequest);
		
		
		
		Response reponse=appl.postWithJson(props.get("authidmURL"), actualrequest);
		System.out.println(reponse);
		
		String responseBody = reponse.getBody().asString();
        String token = new org.json.JSONObject(responseBody).getJSONObject(dataKey).getString("AuthenticationToken");
        return token;
		
		//cookie=reponse.getCookie("AuthenticationToken");
		//return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForidmsselect() {
			JSONObject actualrequest = getRequestJson(selecttenant);
		
		
		
		Response reponse=appl.postWithJson(props.get("selectURL"), actualrequest);
		System.out.println(reponse);
		
		String responseBody = reponse.getBody().asString();
        String token = new org.json.JSONObject(responseBody).getJSONObject(dataKey).getString("response");
        return token;
		
		//cookie=reponse.getCookie("AuthenticationToken");
		//return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForIndividual() {	
		// getting request and expected response jsondata from json files.
        JSONObject actualRequest_generation = getRequestJson("kernel/Authorization/OtpGeneration/request.json");
        //Getting the userId from request
        String key=((JSONObject)actualRequest_generation.get("request")).get("userId").toString();
        // getting request and expected response jsondata from json files.
        JSONObject actualRequest_validation = getRequestJson("kernel/Authorization/OtpGeneration/request.json");
        //sending for otp
        appl.postWithJson(sendOtp, actualRequest_generation);
        String otp=null;
        		if (proxy)
        			otp = "111111";
        		else {
        //Getting the status of the UIN 
        String query="SELECT o.otp FROM kernel.otp_transaction o where id='"+key+"'";
        //List<String> status_list = new KernelDataBaseAccess().getDbData( query,"kernel");
        //otp=status_list.get(0);
        		}
        ((JSONObject)actualRequest_validation.get("request")).put("otp", otp);
        Response otpValidate=appl.postWithJson(useridOTP, actualRequest_validation);
        cookie=otpValidate.getCookie("Authorization");
		return cookie;
	}
	
	public String getPreRegToken() {	
		// getting request and expected response jsondata from json files.
        JSONObject actualRequest_generation = getRequestJson("config/prereg_SendOtp.json");
        actualRequest_generation.put("requesttime", clib.getCurrentUTCTime());
        ((JSONObject)actualRequest_generation.get("request")).put("langCode", languageList.get(0));
        //Getting the userId from request
        String key=((JSONObject)actualRequest_generation.get("request")).get("userId").toString();
        // getting request and expected response jsondata from json files.
        JSONObject actualRequest_validation = getRequestJson("config/prereg_ValidateOtp.json");
        //sending for otp
        appl.postWithJson(preregSendOtp, actualRequest_generation);
        String otp=null;
        		if (proxy)
        			otp = "111111";
        		else {
        //Getting the status of the UIN
        String query="SELECT o.otp FROM kernel.otp_transaction o where id='"+key+"'";
        //List<String> status_list = new KernelDataBaseAccess().getDbData( query,"kernel");
        //otp=status_list.get(0);
        		}
        ((JSONObject)actualRequest_validation.get("request")).put("otp", otp);
        actualRequest_validation.put("requesttime", clib.getCurrentUTCTime());
        Response otpValidate=appl.postWithJson(preregValidateOtp, actualRequest_validation);
        cookie=otpValidate.getCookie("Authorization");
		return cookie;
	}

	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationProcessor() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", regProc_appid);
		request.put("password", regProc_password);
		request.put("userName", regProc_userName);
		actualrequest.put("request", request);
	
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForIDA() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", props.get("resident_appid"));
		request.put("clientId", props.get("resident_clientId"));
		request.put("secretKey", props.get("resident_secretKey"));
		actualrequest.put("request", request);
		
		Response reponse=appl.postWithJson(props.get("authclientidsecretkeyURL"), actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForIDREPO() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", props.get("idrepo_appid"));
		request.put("clientId", props.get("idrepo_clientId"));
		request.put("secretKey", props.get("idrepo_secretKey"));
		actualrequest.put("request", request);
		
		Response reponse=appl.postWithJson(props.get("authclientidsecretkeyURL"), actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationAdmin() {
		JSONObject actualrequest = getRequestJson(authRequest);

		JSONObject request=new JSONObject();
		request.put("appId", registrationAdmin_appid);
		request.put("password", registrationAdmin_password);
		request.put("userName", registrationAdmin_userName);
		actualrequest.put("request", request);
		
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationOfficer() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", registrationOfficer_appid);
		request.put("password", registrationOfficer_password);
		request.put("userName", registrationOfficer_userName);
		actualrequest.put("request", request);
		
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationSupervisor() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", registrationSupervisor_appid);
		request.put("password", registrationSupervisor_password);
		request.put("userName", registrationSupervisor_userName);
		actualrequest.put("request", request);
	
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForZonalAdmin() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", zonalAdmin_appid);
		request.put("password", zonalAdmin_password);
		request.put("userName", zonalAdmin_userName);
		actualrequest.put("request", request);
	
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForZonalApprover() {
		JSONObject actualrequest = getRequestJson(authRequest);
		
		JSONObject request=new JSONObject();
		request.put("appId", zonalApprover_appid);
		request.put("password", zonalApprover_password);
		request.put("userName", zonalApprover_userName);
		actualrequest.put("request", request);
	
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForAutoUser() {
		JSONObject actualrequest = getRequestJson(authRequest);	
		JSONObject request=new JSONObject();
		request.put("appId", props.get("autoUsr_appid"));
		request.put("password", props.get("autoUsr_password"));
		request.put("userName", props.get("autoUsr_user"));
		actualrequest.put("request", request);
		Response reponse=appl.postWithJson(authenticationEndpoint, actualrequest);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	//Reading the request file from folder
	public JSONObject getRequestJson(String filepath){
		return clib.readJsonData(filepath, true);
		
	}
}
