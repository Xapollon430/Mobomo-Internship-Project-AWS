package com.amazonaws.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassToMakeFirstFile {

	static String key = "Member_File";

	public static void main(String[] args) throws IOException { /*This class goes to the github organization and gets the json file
	and puts it into the S3.
	 To run the the S3 Sample class we need a file that has the json names already there.
	put into the S3. This class has to be ran once before the S3 Sample is ran. S3 Sample class depends on it to run.
	You shouldnt need to use this class after running it once. If you are going to use Lambda, dont put this in there. Only put S3 Sample.
	*/
		

		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}

		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion("us-east-2").build();

		Names[] Names = GetMembers();

		File member_file_to_upload = File.createTempFile("members", ".json"); // temporary file to store JSON Object
		ObjectMapper mapper = new ObjectMapper(); // To convert JAVA object to JSON object
		mapper.writeValue(member_file_to_upload, Names); // To convert JAVA object to JSON object

		UploadObject(s3, ClassToCreateBucket.bucketName, key, member_file_to_upload);

	}

	public static void UploadObject(AmazonS3 s3, String bucketName, String key, File test) {
		try {

			System.out.println("Uploading a new object to S3 from a file\n");
			s3.putObject(new PutObjectRequest(bucketName, key, test));

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public static Names[] GetMembers() {

		String website_content = (jsonGetRequest("https://api.github.com/orgs/mobomo/public_members"));

		ObjectMapper objmapper = new ObjectMapper();

		Names[] NameList = null;

		try {
			NameList = objmapper.readValue(website_content, Names[].class);
		} catch (Exception e) {

			e.printStackTrace();
		}

		return NameList;
	}

	public static String streamToString(InputStream inputStream) {
		String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
		return text;
	}

	public static String jsonGetRequest(String urlQueryString) {
		String json = null;
		try {
			URL url = new URL(urlQueryString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "utf-8");
			connection.connect();
			InputStream inStream = connection.getInputStream();
			json = streamToString(inStream); // input stream to string
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return json;
	}

}
