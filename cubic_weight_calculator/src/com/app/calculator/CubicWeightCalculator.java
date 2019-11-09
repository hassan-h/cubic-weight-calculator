/**
 * 
 */
package com.app.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Hassan Hanif
 * @version v1.0
 * @date 9-NOV-2019
 */
public class CubicWeightCalculator {

	private static int CM_TO_M_UNIT_CONVERSION_FACTOR = 100;
	private static int CUBIC_WEIGHT_CONVERSION_FACTOR = 250;
	private static String STARTING_URI = "/api/products/1";
	private static String API_URL = "http://wp8m3he1wt.s3-website-ap-southeast-2.amazonaws.com";

	/**
	 * This method can be used recursively for paginated API.
	 * 
	 * @param uri
	 * @return jsonObject
	 */
	private static JsonObject hitEndPoint(String uri) {
		JsonObject jsonObject = null;
		try {

			URL url = new URL(API_URL + uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			/* Goolge GSON library to parse JSON string returned by Web API. */
			Gson gson = new Gson();
			
			/* Convert API response to JSON Object. */
			jsonObject = gson.fromJson(br, JsonObject.class);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	public static void main(String[] args) {

		String uri = STARTING_URI;
		JsonObject jsonObject = null;
		
		do { /* Atleast one page to process. */
			
			/* Get JSON Object from the API trigger. */
			jsonObject = hitEndPoint(uri);
			
			/* Get items given in any page as array. */
			JsonArray itemsJsonArray = jsonObject.get("objects").getAsJsonArray(); 

			/* Loop through items given in any page. */
			for (int i = 0; i < itemsJsonArray.size(); i++) {
				/* Get each item in page as JSON Object */
				JsonObject itemJsonObject = itemsJsonArray.get(i).getAsJsonObject();

				/* Only consider "Air Conditioners" for calculation. */
				if (itemJsonObject.get("category").getAsString().equalsIgnoreCase("Air Conditioners")) {

					JsonObject sizeJsonObject = itemJsonObject.get("size").getAsJsonObject();

					/*
					 * Get parcel dimensions in centimeters and convert them to meter.
					 */
					float width = sizeJsonObject.get("width").getAsFloat() / CM_TO_M_UNIT_CONVERSION_FACTOR;
					float length = sizeJsonObject.get("length").getAsFloat() / CM_TO_M_UNIT_CONVERSION_FACTOR;
					float height = sizeJsonObject.get("height").getAsFloat() / CM_TO_M_UNIT_CONVERSION_FACTOR;

					/* Calculate cubic weight in Kilograms using Formula. */
					float cubicWeight = width * length * height * CUBIC_WEIGHT_CONVERSION_FACTOR;

					/*
					 * Print Item (AC) with its cubic weight.
					 */
					System.out.println(itemJsonObject);
					System.out.println(cubicWeight);
					System.out.println();
				}
			}

			/*
			 * Set uri for next page if Exists.
			 */
			if (!jsonObject.get("next").isJsonNull()) {
				uri = jsonObject.get("next").getAsString();
			}

		} while (!jsonObject.get("next").isJsonNull()); /* Exit loop if no more pages Exists. */
	}
}
