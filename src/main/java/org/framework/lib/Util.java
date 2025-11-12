package org.framework.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.framework.logger.FrameworkServiceManager;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

public final class Util extends FrameworkServiceManager {

	private static final String DEFAULT_JIRA_URL = "https://americanbureauofshipping.atlassian.net";
	private static final String DEFAULT_API_TOKEN = (new String(
			Base64.getDecoder().decode(System.getenv("JIRA_API_TOKEN").getBytes())));
	private static final String DEFAULT_EMAIL = System.getProperty("user.name") + "@eagle.org";

	public static boolean deleteFlag = false;
	public Properties prop;
	public static String projectPath = Paths.get("").toAbsolutePath().toString();
	public ExtentHtmlReporter htmlReporter;
	public static ExtentReports extent;
	static String browser = null;

	public static String XMLSUITE_NAME = null;
	File reportBackupDir;
	File rootDir;

	private Util() {
	}

	private static Util instance = new Util();
	private static final List<Status> STATUS_HIERARCHY = Arrays.asList(Status.FATAL, Status.FAIL, Status.ERROR,
			Status.SKIP, Status.PASS, Status.INFO, Status.WARNING, Status.DEBUG);

	public static Util getInstance() {
		return instance;
	}

	public Properties GetPropertyObject() {
		return prop;
	}

	public void CreatePropertyfile() {
		try {
			// Initialize the properties object for configuration
			prop = new Properties();
			Path configFilePath = Paths.get("").toAbsolutePath().resolve("configuration/config.properties");

			// Check if the configuration file exists, create it if it does not
			if (Files.notExists(configFilePath)) {
				Files.createFile(configFilePath);
			}

			// Load the properties from the configuration file
			try (FileInputStream in = new FileInputStream(configFilePath.toFile())) {
				prop.load(in);
			}

			// Load all other properties files in the configuration folder
			File configDir = new File(Paths.get("").toAbsolutePath().resolve("configuration").toString());
			File[] files = configDir
					.listFiles((dir, name) -> name.endsWith(".properties") && !name.equals("config.properties"));

			if (files != null) {
				for (File file : files) {
					Properties tempProp = new Properties();
					try (FileInputStream in = new FileInputStream(file)) {
						tempProp.load(in);
					}
					String fileName = file.getName().replace(".properties", "");
					for (String key : tempProp.stringPropertyNames()) {
						prop.setProperty(fileName + "." + key, tempProp.getProperty(key));
					}
				}
			}
		} catch (IOException e) {
			// Print the stack trace if an IOException occurs
			e.printStackTrace();
		}
	}

	public static String decryptString(String text) throws IllegalArgumentException {
		byte[] decodedBytes = Base64.getDecoder().decode(text.getBytes());
		return new String(decodedBytes);
	}

	public static String encryptString(String text) throws IllegalArgumentException {
		byte[] encodeBytes = Base64.getEncoder().encode(text.getBytes());
		return new String(encodeBytes);
	}

	public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
			throws IOException {
		File sourceDirectory = new File(sourceDirectoryLocation);
		File destinationDirectory = new File(destinationDirectoryLocation);
		FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
	}

	public ExtentReports initExtentReport(Properties prop) {
		// extent report
		try {
			// Backup directory
			String backUpDirPath = "C:\\TestReportBackup"; // use prop object

			// creating report directory
			rootDir = new File(projectPath + "\\test-output\\extent-report");
			rootDir.mkdirs();

			// Create Backup report directory
			File backupDir = new File(backUpDirPath);
			backupDir.mkdir();

			// Create Backup Subdirectory
			DateFormat dirDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date date = new Date();
			reportBackupDir = new File(backUpDirPath + "\\" + dirDateFormat.format(date));
			reportBackupDir.mkdir();

			// Take backup
			Util.copyDirectory(projectPath + "\\test-output\\extent-report", reportBackupDir.getAbsolutePath());

			// clean the extent report directory and retain snapshot folder
			for (File file : rootDir.listFiles()) {
				if (!file.isDirectory())
					file.delete();
			}

			// clean snapshot directory
			File rootSnapDir = new File(projectPath + "\\test-output\\extent-report\\snapshot");
			if (rootSnapDir.exists()) { // check snapshot directory exist
				for (File file : rootSnapDir.listFiles()) {
					file.delete();
				}
			}

			// Initialize HTML reporter
			htmlReporter = new ExtentHtmlReporter(
					rootDir.getAbsolutePath() + "\\" + Paths.get(projectPath).getFileName() + "_TestResult.html");
			htmlReporter.loadXMLConfig(projectPath + "\\configuration\\html-config.xml");

			// Set document title and report name if XMLSUITE_NAME is present
			Optional.ofNullable(XMLSUITE_NAME).ifPresent(name -> {
				htmlReporter.config().setDocumentTitle(name);
				htmlReporter.config().setReportName("ABS-AUTOMATION REPORT:" + name);
			});

			// Initialize ExtentReports and attach the HTML reporter
			extent = new ExtentReports();
			setExtentReportStatusHierarchy();
			extent.attachReporter(htmlReporter);
			extent.setSystemInfo("Operating System", System.getProperty("os.name").toUpperCase());
			extent.setSystemInfo("Browser", prop.getProperty("env.browser").toUpperCase());

			// Set environment and testing type
			String environment = Optional.ofNullable(System.getProperty("env.environment")).map(String::toUpperCase)
					.orElse(prop.getProperty("env.environment"));
			extent.setSystemInfo("Environment", environment);
			String feature = Optional.ofNullable(System.getProperty("testingType")).map(String::toUpperCase)
					.orElse(prop.getProperty("env.feature"));
			extent.setSystemInfo("Testing Type", feature);

			// Set executed by
			extent.setSystemInfo("Executed By", System.getProperty("user.name").toUpperCase());

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return extent;
	}

	public void setExtentReportStatusHierarchy() {
		extent.config().statusConfigurator().setStatusHierarchy(STATUS_HIERARCHY);
	}
	

	public static String decrypt(String encryptedPassword) {
		StandardPBEStringEncryptor encryptor = null;
		String encryptedString = null; 
		try {
			encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
			encryptedString = encryptor.decrypt(encryptedPassword);
		} catch (EncryptionOperationNotPossibleException e) {

			//throw new Exception(e);
			System.out.println(e.getMessage());
		}

		return encryptedString;
	}

	// created 11/11

	/*
	 * public String createIssue(String summary) { // Resolve base URL String
	 * projectKey = "PLAYW"; String issueType = "Bug";
	 * 
	 * String baseUrl = firstNonBlank(System.getProperty("jira.url"),
	 * System.getenv("JIRA_BASE_URL"),
	 * "https://americanbureauofshipping.atlassian.net"); String email =
	 * firstNonBlank(System.getProperty("jira.email"), System.getenv("JIRA_EMAIL"),
	 * "vnarkhede@eagle.org"); String token =
	 * firstNonBlank(System.getProperty("jira.token"),
	 * System.getenv("JIRA_API_TOKEN"),
	 * "ATATT3xFfGF0EfJ-tn2pdg8QqEQjwaLUj3lvqBj-_rs5D9tJhzmdHG-D_hYPZJTn-Yua-kz6bROC334OKuRFXU868QL6zri1Nw9QXJAmA6HiUdDkQ3TCopokAp8J6Nbk8cs9_TZgvGAZeG6GKOROUwzVkopXHb-spBAmlK-9Kl1bQ8jm3XjCe4A=08539929"
	 * );
	 * 
	 * if (email == null || token == null) { System.err.println(
	 * "Missing credentials: set -Djira.email/-Djira.token or env vars JIRA_EMAIL/JIRA_API_TOKEN"
	 * ); return null; } String endpoint = (baseUrl.endsWith("/") ? baseUrl :
	 * baseUrl + "/") + "rest/api/2/issue"; String json = buildJson(projectKey,
	 * issueType, summary); try { HttpURLConnection conn = (HttpURLConnection) new
	 * URL(endpoint).openConnection(); conn.setRequestMethod("POST");
	 * conn.setRequestProperty("Authorization", "Basic " +
	 * Base64.getEncoder().encodeToString((email + ":" +
	 * token).getBytes(StandardCharsets.UTF_8)));
	 * conn.setRequestProperty("Content-Type", "application/json");
	 * conn.setRequestProperty("Accept", "application/json");
	 * conn.setDoOutput(true); try (OutputStream os = conn.getOutputStream()) {
	 * os.write(json.getBytes(StandardCharsets.UTF_8)); } int status =
	 * conn.getResponseCode(); String resp; try (BufferedReader reader = new
	 * BufferedReader( new InputStreamReader(status >= 200 && status < 300 ?
	 * conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
	 * StringBuilder sb = new StringBuilder(); String line; while ((line =
	 * reader.readLine()) != null) sb.append(line); resp = sb.toString(); } if
	 * (status < 200 || status >= 300) { System.err.println("HTTP " + status + ": "
	 * + resp); return null; } writeFile("newKey.json", resp);
	 * info("-->Bug Created :" + parseKey(resp)); return parseKey(resp); } catch
	 * (IOException e) { System.err.println("Error creating issue: " +
	 * e.getMessage()); return null; } }
	 */

	/*
	 * private static String buildJson(String projectKey, String issueType, String
	 * summary) { return "{\"fields\":{\"project\":{\"key\":\"" + esc(projectKey) +
	 * "\"}," + "\"issuetype\":{\"name\":\"" + esc(issueType) + "\"}," +
	 * "\"summary\":\"" + esc(summary) + "\"}}"; }
	 */

	/*
	 * private static String firstNonBlank(String... vals) { for (String v : vals) {
	 * if (v != null && !v.trim().isEmpty()) return v.trim(); } return null; }
	 */

//===================

	public void createIssueWithAttachment(String summary, String description) {

		String projectKey = "PLAYW";// args[0];
		String issueType = "Bug";// args[1];

		String attachmentPath = "";

		String issueKey = createIssue(projectKey, issueType, summary, description);

		if (issueKey == null) {
			System.err.println("Issue creation failed.");
			System.exit(2);
		}

		File directory = new File(Paths.get("").toAbsolutePath().toString() + "/test-output/extent-report");
		String[] flist = directory.list();

		if (flist == null) {
			System.out.println("Empty directory.");
			return;
		} else {
			for (String eachFile : flist) {
				if (eachFile.contains("TestResult.html")) {
					System.out.println(eachFile + " found");
					attachmentPath = Paths.get("").toAbsolutePath().toString() + "/test-output/extent-report/"
							+ eachFile;
					break;
				}
			}
		}

		System.out.println("Created issue: " + issueKey);
		fail("JIRA Bug created : " + issueKey ,false);
		if (attachmentPath != null) {
			attachFile(issueKey, attachmentPath);
					
			String jiraID = String.format("[<a href='https://americanbureauofshipping.atlassian.net/browse/%s' target='_blank'>%s</a>]", issueKey,issueKey);
				
			info("Attachment upload status for JIRA bug " + jiraID );
		}

		// attaching
	//	attachFile(issueKey, attachmentPath);

	}

	public static String createIssue(String projectKey, String issueType, String summary, String description) {
		// Resolve base URL
		String baseUrl = DEFAULT_JIRA_URL;
		String email = DEFAULT_EMAIL;
		String token = DEFAULT_API_TOKEN;
		if (email == null || token == null) {
			System.err.println(
					"Missing credentials: set -Djira.email/-Djira.token or env vars JIRA_EMAIL/JIRA_API_TOKEN");
			return null;
		}
		String endpoint = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + "rest/api/2/issue";
		String json = buildJson(projectKey, issueType, summary, description);

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Basic "
					+ Base64.getEncoder().encodeToString((email + ":" + token).getBytes(StandardCharsets.UTF_8)));
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(json.getBytes(StandardCharsets.UTF_8));
			}
			int status = conn.getResponseCode();
			String resp;
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream(),
							StandardCharsets.UTF_8))) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null)
					sb.append(line);
				resp = sb.toString();
			}
			if (status < 200 || status >= 300) {
				System.err.println("HTTP " + status + ": " + resp);
				return null;
			}
			writeFile("newKey.json", resp);
			return parseKey(resp);
		} catch (IOException e) {
			System.err.println("Error creating issue: " + e.getMessage());
			return null;
		}
	}

	private static String buildJson(String projectKey, String issueType, String summary, String description) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"fields\":{");
		sb.append("\"project\":{\"key\":\"").append(esc(projectKey)).append("\"},");
		sb.append("\"issuetype\":{\"name\":\"").append(esc(issueType)).append("\"},");
		sb.append("\"summary\":\"").append(esc(summary)).append("\"");
		if (description != null && !description.isBlank()) {
			sb.append(",\"description\":\"").append(esc(description)).append("\"");
		}
		sb.append("}}\n");
		return sb.toString();
	}

	private static boolean attachFile(String issueKey, String path) {

		File file = new File(path);
		if (!file.exists() || !file.isFile()) {
			System.err.println("Attachment path invalid: " + path);
			return false;
		}
		String baseUrl = DEFAULT_JIRA_URL;
		String email = DEFAULT_EMAIL;
		String token = DEFAULT_API_TOKEN;
		if (email == null || token == null) {
			System.err.println("Missing credentials for attachment upload");
			return false;
		}
		String endpoint = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + "rest/api/2/issue/" + issueKey
				+ "/attachments";
		String boundary = "----JiraBoundary" + System.currentTimeMillis();
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Basic "
					+ Base64.getEncoder().encodeToString((email + ":" + token).getBytes(StandardCharsets.UTF_8)));
			conn.setRequestProperty("X-Atlassian-Token", "no-check"); // required to bypass CSRF check for attachments
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setDoOutput(true);
			try (OutputStream os = conn.getOutputStream(); FileInputStream fis = new FileInputStream(file)) {
				// Write multipart preamble
				String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"file\"; filename=\""
						+ file.getName() + "\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n";
				os.write(header.getBytes(StandardCharsets.UTF_8));
				// Stream file bytes
				byte[] buf = new byte[8192];
				int len;
				while ((len = fis.read(buf)) != -1) {
					os.write(buf, 0, len);
				}
				// Closing boundary
				os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
			}
			int status = conn.getResponseCode();
			if (status < 200 || status >= 300) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null)
						sb.append(line);
					System.err.println("Attachment upload failed HTTP " + status + ": " + sb);
				}
				return false;
			}
			return true;
		} catch (IOException e) {
			System.err.println("Attachment upload error: " + e.getMessage());
			return false;
		}
	}

	private static String esc(String s) {
		return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private static void writeFile(String name, String content) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(name, StandardCharsets.UTF_8))) {
			bw.write(content);
		} catch (IOException e) {
			System.err.println("Failed to write " + name + ": " + e.getMessage());
		}
	}

	private static String parseKey(String json) {
		if (json == null)
			return null;
		int i = json.indexOf("\"key\"");
		if (i == -1)
			return null;
		int c = json.indexOf(':', i);
		int q1 = json.indexOf('"', c + 1);
		int q2 = json.indexOf('"', q1 + 1);
		if (c == -1 || q1 == -1 || q2 == -1)
			return null;
		return json.substring(q1 + 1, q2);
	}
}