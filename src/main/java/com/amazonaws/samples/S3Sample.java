package com.amazonaws.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

public class S3Sample {

	public static void main(String[] args) throws IOException {

		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials(); // Credentials are in my pc in a .aws file.
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}

		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion("us-east-2").build();

		// --------------- Getting the old array from S3 --------------
		ObjectMapper mapper = new ObjectMapper(); // jackson object
		String first_array_string = readFromS3(ClassToCreateBucket.bucketName, ClassToMakeFirstFile.key, s3); //The old names were stored in S3 if you ran ClassToMakeFirstFile, we take it back
		Names[] old_names = mapper.readValue(first_array_string, Names[].class);// ReadValue method wants a string with names in it and a class with the names properties
																				// in order to configure them together to make a java object.

		// ---------------- Getting New Array (Its a file for testing purposes now)
		// As my "new" names I used a made up file to test my code. When you use actual mobomo organization. You need to use the GetMembers method to 
		// get to the actual json files. Hook up the GetMembers method to the actual mobomo organization.
		// later use the real website (GetMembers Method)), In GetMembers use the actual mobomo organization link when you are calling the JsonGetRequest method.
		File test = new File("C:\\Users\\vehbi\\Desktop\\CSCPRACTICE\\Mobomo\\TestMembers.json"); // made up "new" names to test.
		Names[] new_names = mapper.readValue(test, Names[].class); //
		// --------------- Compares and Stores in Collection List
		List<String> test1 = compareToSeeNewNames(old_names, new_names); // This List has the new names
		List<String> test2 = compareToSeeIfDeleted(old_names, new_names); // This list has the old names.

		// --------Adding the Deleted AND New Members to S3 Bucket ------------
		File deletedFiles = File.createTempFile("deletedMembers", ".json");
		File addedFiles = File.createTempFile("addedMembers", ".json"); // 2 temp files created to upload the new and deleted name arrays. We need to create Files to put into Bucket
		mapper.writeValue(deletedFiles, test2);
		mapper.writeValue(addedFiles, test1); // The new and deleted arrays are getting put into the files.
		UploadObject(s3, ClassToCreateBucket.bucketName, "Deleted_Members", deletedFiles); 
		UploadObject(s3, ClassToCreateBucket.bucketName, "Added_Members", addedFiles); // The added names and deleted names get uploaded to Bucket
		DeleteObjectFromBucket(s3, ClassToCreateBucket.bucketName, "Member_File"); 
		UploadObject(s3, ClassToCreateBucket.bucketName, ClassToMakeFirstFile.key, test); // deleting the "old" json file and uploading the "new" json.
		// the "new" one will be "old" the next run

	}

	public static ArrayList compareToSeeIfDeleted(Names[] old_names, Names[] new_names) {
		ArrayList deletedList = new ArrayList();
		for (int oldarray = 0; oldarray < old_names.length; oldarray++) {
			boolean deleted = true;

			for (int newarray = 0; newarray < new_names.length; newarray++) {
				if (old_names[oldarray].getLogin().equals(new_names[newarray].getLogin())) {
					deleted = false;
					break;
					
				}

			}
			if (deleted) {

				deletedList.add(old_names[oldarray].getLogin());

			}
		}

		return deletedList;

	}

	public static ArrayList compareToSeeNewNames(Names[] old_names, Names[] new_names) {

		ArrayList addedList = new ArrayList();
		for (int newarray = 0; newarray < new_names.length; newarray++) {
			boolean is_it_new = true;
			for (int oldarray = 0; oldarray < old_names.length; oldarray++) {

				if ((new_names[newarray].getLogin().equals(old_names[oldarray].getLogin()))) {
					is_it_new = false;
					break;
				}

			}
			if (is_it_new) {
				addedList.add(new_names[newarray].getLogin());
			}

		}

		return addedList;
	}

	public static void UploadObject(AmazonS3 s3, String bucketName, String key, File test) throws IOException {
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

	public static String readFromS3(String bucketName, String key, AmazonS3 s3) throws IOException {

		S3Object s3object = s3.getObject(new GetObjectRequest(

				bucketName, key));

		BufferedReader reader = new BufferedReader(new InputStreamReader(s3object.getObjectContent()));

		String line;

		String text = "";
		while ((line = reader.readLine()) != null) {

			// can copy the content locally as well

			// using a buffered writer
			text += line;

		}
		return text;
	}

	public static void DeleteObjectFromBucket(AmazonS3 S3, String bucketName, String keyName) {

		S3.deleteObject(new DeleteObjectRequest(bucketName, keyName));

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
