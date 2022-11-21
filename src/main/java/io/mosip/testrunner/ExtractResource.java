package io.mosip.testrunner;

import java.io.File; 
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * The class to extract resource from jar 
 * 
 * @author Vignesh
 *
 */
public class ExtractResource {
	
	private static final Logger LOGGER = Logger.getLogger(ExtractResource.class);
	
	/**
	 * Pass what are resources to be extract from jar
	 */
	public static void extractResourceFromJar() {
		getListOfFilesFromJarAndCopyToExternalResource("preReg/");
		getListOfFilesFromJarAndCopyToExternalResource("config/");
		getListOfFilesFromJarAndCopyToExternalResource("masterdata/");
		getListOfFilesFromJarAndCopyToExternalResource("syncdata/");
		getListOfFilesFromJarAndCopyToExternalResource("ida/");
		getListOfFilesFromJarAndCopyToExternalResource("kernel/");
		getListOfFilesFromJarAndCopyToExternalResource("preReg/");
		getListOfFilesFromJarAndCopyToExternalResource("config/");
		getListOfFilesFromJarAndCopyToExternalResource("regProc/");
    	getListOfFilesFromJarAndCopyToExternalResource("idRepository/");
		/* getListOfFilesFromJarAndCopyToExternalResource("Registration/"); */
		getListOfFilesFromJarAndCopyToExternalResource("admin/");
		getListOfFilesFromJarAndCopyToExternalResource("resident/");
		getListOfFilesFromJarAndCopyToExternalResource("partner/");
		/* getListOfFilesFromJarAndCopyToExternalResource("reg/"); */
		getListOfFilesFromJarAndCopyToExternalResource("customize-emailable-report-template.html");
		getListOfFilesFromJarAndCopyToExternalResource("testngapi.xml");
		getListOfFilesFromJarAndCopyToExternalResource("metadata.xml");
		getListOfFilesFromJarAndCopyToExternalResource("log4j.properties");
		getListOfFilesFromJarAndCopyToExternalResource("healthCheck/");
		getListOfFilesFromJarAndCopyToExternalResource("labels_ar.properties");
		getListOfFilesFromJarAndCopyToExternalResource("labels_en.properties");
		getListOfFilesFromJarAndCopyToExternalResource("labels_fr.properties");
		getListOfFilesFromJarAndCopyToExternalResource("messages_en.properties");
		getListOfFilesFromJarAndCopyToExternalResource("messages_ar.properties");
		getListOfFilesFromJarAndCopyToExternalResource("messages_fr.properties");
		getListOfFilesFromJarAndCopyToExternalResource("spring.properties");
		getListOfFilesFromJarAndCopyToExternalResource("validations.properties");
		/* getListOfFilesFromJarAndCopyToExternalResource("db"); */
		getListOfFilesFromJarAndCopyToExternalResource("dbFiles/");
	}
	
	/**
	 * The method to get list of resource from jar and copy to external resource
	 * 
	 * @param key
	 */
	public static void getListOfFilesFromJarAndCopyToExternalResource(String key) {
		try {
			CodeSource src = MosipTestRunner.class.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL jar = src.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				while (true) {
					ZipEntry e = zip.getNextEntry();
					if (e == null)
						break;
					String name = e.getName();
					if (name.startsWith(key) & name.contains(".")) {
						if (copyFilesFromJarToOutsideResource(name))
							LOGGER.info("Copied the file: " + name + " to external resource successfully..!");
						else
							LOGGER.error("Fail to copy file: " + name + " to external resource");
					}
				}
			} else {
				LOGGER.error("Something went wrong with jar location");
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured in extracting resource: " + e.getMessage());
		}
	}
	
	/**
	 * The method to copy resource from jar to outside jar
	 * 
	 * @param path
	 * @return
	 */
	private static boolean copyFilesFromJarToOutsideResource(String path) {
		try {
			File resourceFile = new File(MosipTestRunner.jarUrl).getParentFile();
			File destinationFile = new File(resourceFile.getAbsolutePath()+"/MosipTestResource/" + path);
			org.apache.commons.io.FileUtils.copyInputStreamToFile(MosipTestRunner.class.getResourceAsStream("/" + path),
					destinationFile);
			return true;
		} catch (Exception e) {
			LOGGER.error(
					"Exception Occured in copying the resource from jar. Kindly build new jar to perform smooth test execution: "
							+ e.getMessage());
			return false;
		}
	}	
	
	/**
	 * The method to remove old generated mosip test resource
	 */
	public static void removeOldMosipTestTestResource() {
		File mosipTestFile = new File(MosipTestRunner.getGlobalResourcePath());
		if (mosipTestFile.exists())
			if (deleteDirectory(mosipTestFile))
				LOGGER.info("Old MosipTestResource folder successfully deleted!!");
			else
				LOGGER.error("Old MosipTestResource folder not deleted.");
	}
	
	/**
	 * The method to delete directory and its all file inside the directory
	 * 
	 * @param dir
	 * @return boolean 
	 */
	public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
	}
	private static void copyDbInTarget() {
		File db=new File(MosipTestRunner.getGlobalResourcePath()+"/db");
		File targetDb=new File(MosipTestRunner.getGlobalResourcePath().substring(0,MosipTestRunner.getGlobalResourcePath().lastIndexOf("\\"))+"/db");
		try {
			FileUtils.copyDirectory(db,targetDb);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
