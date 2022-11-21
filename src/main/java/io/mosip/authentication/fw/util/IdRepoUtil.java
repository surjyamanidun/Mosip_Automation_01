package io.mosip.authentication.fw.util;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.precon.XmlPrecondtion;

/**
 * Class to store all the UIN data or json using idrepo
 * 
 * @author Athila
 *
 */
public class IdRepoUtil extends AuthTestsUtil {

	private static final Logger IDREPO_UTILITY_LOGGER = Logger.getLogger(IdRepoUtil.class);

	/**
	 * Get the uin data or json using idrepo api and save it in output file
	 * 
	 * @param uinNumber
	 * @return true or false
	 */
	public static boolean retrieveIdRepo(String uinNumber) {
		String retrievePath = RunConfigUtil.objRunConfig.getIdRepoRetrieveDataPath().replace("$uin$", uinNumber);
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl() + retrievePath;
		String cookieValue = getAuthorizationCookie(getCookieRequestFilePathForResidentAuth(),
				RunConfigUtil.objRunConfig.getIdRepoEndPointUrl() + RunConfigUtil.objRunConfig.getClientidsecretkey(),
				AUTHORIZATHION_COOKIENAME);
		if (!FileUtil.checkFileExistForIdRepo(uinNumber + ".json")) {
			IDREPO_UTILITY_LOGGER.info("To retrieve identity Sending get request to: " + url);
			if (FileUtil.createAndWriteFileForIdRepo(uinNumber + ".json",
					getResponseWithCookie(url + "?type=all", AuthTestsUtil.AUTHORIZATHION_COOKIENAME, cookieValue)))
				return true;
			else
				return false;
		}
		return true;
	}

	/**
	 * Get field data for the key from saved uin data or json
	 * 
	 * @param mapping
	 * @param uinNumber
	 * @return uin data or json
	 */
	/*
	 * public static String retrieveDataFromIdRepo(String mapping, String uinNumber)
	 * { try { String mappingTopass = null; if (mapping.contains("dateOfBirth") &&
	 * mapping.contains("age")) { mappingTopass=mapping.replaceAll("PLUS",
	 * "").replaceAll("MINUS", ""); } else mappingTopass=mapping; if
	 * (uinNumber.length() == 16) {
	 * VIDUtil.getVidPropertyValue(RunConfigUtil.getVidPropertyPath()); for
	 * (Entry<String, String> entry : VidDto.getVid().entrySet()) { if
	 * (entry.getValue().contains(uinNumber)) { uinNumber = entry.getKey(); } } } if
	 * (retrieveIdRepo(uinNumber)) { String value =
	 * XmlPrecondtion.getValueFromXmlUsingMapping( Paths.get(new
	 * File(RunConfigUtil.getResourcePath() +
	 * RunConfigUtil.objRunConfig.getStoreUINDataPath() + "/" + uinNumber + ".json")
	 * .getAbsolutePath()) .toString(), new File(RunConfigUtil.getResourcePath() +
	 * RunConfigUtil.objRunConfig.getUinIdentityMapper() + "/mapping.properties")
	 * .getAbsolutePath(), mappingTopass); if (mapping.contains("dateOfBirth") &&
	 * mapping.contains("input")) { Date valuedate = new
	 * SimpleDateFormat("yyyy/MM/dd").parse(value); SimpleDateFormat date = new
	 * SimpleDateFormat("dd/MM/yyyy"); value = date.format(valuedate); } if
	 * (mapping.contains("dateOfBirth") && mapping.contains("age")) { Date valuedate
	 * = new SimpleDateFormat("yyyy/MM/dd").parse(value); SimpleDateFormat date =
	 * new SimpleDateFormat("dd"); SimpleDateFormat month = new
	 * SimpleDateFormat("MM"); SimpleDateFormat year = new SimpleDateFormat("yyyy");
	 * if (mapping.contains("PLUS")) value =
	 * String.valueOf(Integer.parseInt(calculateAge(Integer.parseInt(year.format(
	 * valuedate)), Integer.parseInt(month.format(valuedate)),
	 * Integer.parseInt(date.format(valuedate)))) +
	 * Integer.parseInt(AuthTestsUtil.randomize(2))); else if
	 * (mapping.contains("MINUS")) value =
	 * String.valueOf(Integer.parseInt(calculateAge(Integer.parseInt(year.format(
	 * valuedate)), Integer.parseInt(month.format(valuedate)),
	 * Integer.parseInt(date.format(valuedate)))) +
	 * Integer.parseInt(AuthTestsUtil.randomize(2))); else value =
	 * calculateAge(Integer.parseInt(year.format(valuedate)),
	 * Integer.parseInt(month.format(valuedate)),
	 * Integer.parseInt(date.format(valuedate))); } if (value.contains("Null"))
	 * return "$REMOVE$"; else return value.toString(); } else return
	 * "No Record found for the UIN: " + uinNumber; } catch (Exception e) {
	 * IDREPO_UTILITY_LOGGER.error("Exceptione in fetching the data from id repo" +
	 * e); if (e.toString().contains("Null")) { return "$REMOVE$"; } return
	 * "Exceptione in fetching the data from id repo: " + e.toString(); } }
	 */

	/**
	 * Generate uin number using generate_uin api
	 * 
	 * @return UIN Number
	 */
	public static String generateUinNumberForIda() {
		return JsonPrecondtion
				.getValueFromJson(
						getResponseWithCookieForIdaUinGenerator(RunConfigUtil.objRunConfig.getEndPointUrl()
								+ RunConfigUtil.objRunConfig.getGenerateUINPath(), AUTHORIZATHION_COOKIENAME),
						"response.uin");
	}

	/**
	 * Generate uin number using generate_uin api
	 * 
	 * @return UIN Number
	 */
	public static String generateUinNumberForIdRepo() {
		return JsonPrecondtion
				.getValueFromJson(
						getResponseWithCookieForIdRepoUinGenerator(RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
								+ RunConfigUtil.objRunConfig.getGenerateUINPath(), AUTHORIZATHION_COOKIENAME),
						"response.uin");
	}

	/**
	 * Get Create UIN api Path for the generated uin number
	 * 
	 * @param UinNumber
	 * @return create uin path
	 */
	public static String getCreateUinPath() {
		return RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoCreateUINRecordPath();
	}

	/**
	 * The method will calculate age
	 * 
	 * @param year
	 * @param month
	 * @param date
	 * @return string, calculated age
	 */
	private static String calculateAge(int year, int month, int date) {
		LocalDate birthDate = LocalDate.of(year, month, date);
		LocalDate currentDate = LocalDate.now();
		return String.valueOf(Period.between(birthDate, currentDate).getYears());
	}

	/**
	 * Get create VID api path
	 *
	 * @return url
	 */
	public static String getCreateVidPath() {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoCreateVIDRecordPath();
		// url = url.replace("$uin$", UinNumber);
		return url;
	}

	/**
	 * Get update VID api path
	 * 
	 * @param vid number
	 * @return url
	 */
	public static String getUpdateVidStatusPath(String vidNumer) {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoUpdateVIDStatusPath();
		url = url.replace("$vid$", vidNumer);
		return url;
	}

	/**
	 * Get Retrieve VID api path
	 * 
	 * @param vid number
	 * @return url
	 */
	public static String getRetrieveUINByVIDPath(String vidNumer) {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoRetrieveUINByVIDPath();
		url = url.replace("$vid$", vidNumer);
		return url;
	}

	/**
	 * Get Retrieve Identity api path
	 * 
	 * @param uin number
	 * @return url
	 */
	public static String getRetrieveIdentityByUIN(String uinNumer) {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoRetrieveDataPath();
		url = url.replace("$uin$", uinNumer);
		return url;
	}

	/**
	 * Get Retrieve Identity api path
	 * 
	 * @param uin number
	 * @return url
	 */
	public static String getRetrieveIdentityByRid(String ridNumer) {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoRetrieveIdentityByRid();
		url = url.replace("$rid$", ridNumer);
		return url;
	}

	/**
	 * Get Retrieve Identity api path
	 * 
	 * @param uin number
	 * @return url
	 */
	public static String getRegenerateVid(String vidNumer) {
		String url = RunConfigUtil.objRunConfig.getIdRepoEndPointUrl()
				+ RunConfigUtil.objRunConfig.getIdRepoRetrieveIdentityByRid();
		url = url.replace("$vid$", vidNumer);
		return url;
	}

	public static String getIdrepoUinRefIdQuery(String regId) {
		return "select uin_ref_id from idrepo.uin where reg_id='" + regId + "'";
	}

	public static String deleteIdrepoBiometricQuery(String uinRefId) {
		return "delete from idrepo.uin_biometric where uin_ref_id='" + uinRefId + "'";
	}

	public static String deleteIdrepoUin(String uinRefId) {
		return "delete from idrepo.uin where uin_ref_id='" + uinRefId + "'";
	}

}
