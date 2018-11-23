package com.amazonaws.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon S3. For more information on Amazon
 * S3, see http://aws.amazon.com/s3.
 * <p>
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (~/.aws/credentials) where the sample code will load the credentials from.
 * <p>
 * <b>WARNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 *
 * http://aws.amazon.com/security-credentials
 */
public class S3Sample {

	public static void main(String[] args) throws IOException {
		String bucketName = "mobomo-github-members";
		String key = "MyObjectKey";

		AmazonS3 s3 = CreateBucket(bucketName);
		UploadObject(s3, bucketName, key);
		S3Object s3obj = GetObject(s3, bucketName, key);
		Names[] Names = GetMembers();
	}

	public static AmazonS3 CreateBucket(String bucketName) {
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

		System.out.println("===========================================");
		System.out.println("Getting Started with Amazon S3");
		System.out.println("===========================================\n");

		try {
			/*
			 * Create a new S3 bucket - Amazon S3 bucket names are globally unique, so once
			 * a bucket name has been taken by any user, you can't create another bucket
			 * with that same name.
			 *
			 * You can optionally specify a location for your bucket if you want to keep
			 * your data closer to your applications or users.
			 */
			System.out.println("Creating bucket " + bucketName + "\n");
			s3.createBucket(bucketName);
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

		return s3;
	}

	public static void UploadObject(AmazonS3 s3, String bucketName, String key, Names[] Names) throws IOException {
		try {

			/*
			 * Upload an object to your bucket - You can easily upload a file to S3, or
			 * upload directly an InputStream if you know the length of the data in the
			 * stream. You can also specify your own metadata when uploading to S3, which
			 * allows you set a variety of options like content-type and content-encoding,
			 * plus additional metadata specific to your applications.
			 */
			System.out.println("Uploading a new object to S3 from a file\n");
			s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile(Names)));

			/*
			 * Download an object - When you download an object, you get all of the object's
			 * metadata and a stream from which to read the contents. It's important to read
			 * the contents of the stream as quickly as possibly since the data is streamed
			 * directly from Amazon S3 and your network connection will remain open until
			 * you read all the data or close the input stream.
			 *
			 * GetObjectRequest also supports several other options, including conditional
			 * downloading of objects based on modification times, ETags, and selectively
			 * downloading a range of an object.
			 */

			/*
			 * List objects in your bucket by prefix - There are many options for listing
			 * the objects in your bucket. Keep in mind that buckets with many objects might
			 * truncate their results when listing their objects, so be sure to check if the
			 * returned object listing is truncated, and use the
			 * AmazonS3.listNextBatchOfObjects(...) operation to retrieve additional
			 * results.
			 */

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

	public static S3Object GetObject(AmazonS3 s3, String bucketName, String key) throws IOException {
		S3Object object = null;
		try {
			System.out.println("Downloading an object");

			object = s3.getObject(new GetObjectRequest(bucketName, key));
			System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());

			displayTextInputStream(object.getObjectContent());

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
		return object;
	}

	private static File createSampleFile(Names[] Names) throws IOException {
		File file = File.createTempFile("LoginNames", ".json");
		file.deleteOnExit();

		Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		for (int i = 0; i < Names.length; i++) {
			writer.write(Names[i].getLogin());
			writer.close();
		}
		return file;
	}

	/**
	 * Displays the contents of the specified input stream as text.
	 *
	 * @param input
	 *            The input stream to display as text.
	 *
	 * @throws IOException
	 */

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
