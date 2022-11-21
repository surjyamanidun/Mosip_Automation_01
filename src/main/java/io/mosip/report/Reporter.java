package io.mosip.report;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import io.mosip.testrunner.MosipTestRunner;

import java.io.FileReader;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;

/**
 * Reporter class act as util for additional report class or listeners
 * 
 * @author Vignesh
 *
 */
public class Reporter {

	private static final Logger REPORTLOG = Logger.getLogger(Reporter.class);

	public static String getAppDepolymentVersion() {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = null;
		try {
			model = reader.read(new FileReader(MosipTestRunner.getGlobalResourcePath()+"/metadata.xml"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			REPORTLOG.error("Exception in tagging the build number" + e.getMessage());
		}
		return model.getParent().getVersion();
	}
	
	public static String getAppEnvironment() {
		return System.getProperty("env.user");
	}

}
