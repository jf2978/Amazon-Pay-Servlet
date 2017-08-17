package amazonPay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public class AmazonParameters {
	
	// Amazon credentials
	protected static final String MWS_ACCESS_ID = null;
	protected static final String MWS_SECRET = null;
	protected static final String CLIENT_ID = null;
	protected static final String SELLER_ID = null;
	protected static final String API_VERSION = null; // depends on MWS API being used
	
	// Button parameters
	private static final String AMOUNT = "20.00";
	private static final String CURRENCY_CODE = "USD";
	private static final String RETURN_URL = "https://pay.convessa.com:8080/processPayment";
	private static final String SELLER_NOTE = "This purchase grants you full access to Mastermind";
	
	/* ==========Handles Amazon request parameters========== */
	
	// Sets button parameters (express integration)
	protected static void setExpressParams(Map<String,String> params)
			throws UnsupportedEncodingException{
		params.put("accessKey", MWS_ACCESS_ID);
		params.put("amount", AMOUNT);
		params.put("currencyCode", CURRENCY_CODE);
		params.put("lwaClientId", CLIENT_ID);
		params.put("paymentAction", "None");	
		params.put("returnURL", RETURN_URL);
		params.put("sellerId", SELLER_ID);
		params.put("sellerNote", SELLER_NOTE);
		params.put("sellerOrderId", getUUID());
		params.put("shippingAddressRequired", "false");
	}
	
	// Sets parameters for making MWS calls
	protected static void setMWSParams(Map<String,String> params, String action) 
			throws UnsupportedEncodingException {
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String timestamp = formatter.format(new Date(System.currentTimeMillis()));	
		
		params.put("AWSAccessKeyId", MWS_ACCESS_ID);
		params.put("Action", action);
		params.put("SellerId", MWS_ACCESS_ID);
		params.put("AWSAccessKeyId", SELLER_ID);
		params.put("SignatureVersion", "2");
		params.put("SignatureMethod","HmacSHA256");
		params.put("Timestamp", timestamp);
		params.put("Version", API_VERSION);
	}
	
	// Prepares headers for parsing POST notifications (IPN)
	protected static Map<String,String> getIPNHeaders(HttpServletRequest req){
		
		Map<String,String> headers = new HashMap<>();
		
		headers.put("x-amz-sns-message-type",req.getHeader("x-amz-sns-message-type"));
		headers.put("x-amz-sns-message-id", req.getHeader("x-amz-sns-message-id"));
		headers.put("x-amz-sns-topic-arn", req.getHeader("x-amz-sns-topic-arn"));
		headers.put("Content-Length", req.getHeader("Content-Length"));
		headers.put("Host", req.getHeader("Host"));
		headers.put("Connection", req.getHeader("Connection"));
		headers.put("User-Agent", req.getHeader("User-Agent"));
		
		return headers;
	}
	
	protected static String getIPNBody(HttpServletRequest req) throws IOException{
		
	    Scanner scan = new Scanner(req.getInputStream());
	    StringBuilder builder = new StringBuilder();
	    while (scan.hasNextLine()){
	    	builder.append(scan.nextLine());
	    }
	    
	    String body = builder.toString();
	    scan.close();
	    
	    return body;
	}
		
	/* ==========HELPER METHODS========== */
	
	// Due to Amazon constraints, unique id must be no longer than 32 characters
	protected static String getUUID(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/* ==========ADDITIONAL NOTES========== */
	
	/* In this context, using Amazon MWS extends Amazon Pay functionality; 
	 * enabling Amazon sellers to programmatically access information that
	 * would otherwise only be accessible on seller central. Though some of the MWS 
	 * calls have been replaced by the Amazon Pay API (namely the Off-Amazon-Payments API),
	 * I thought programmatically accessing things like transaction reports, settlement reports
	 * and bulk listing of your Amazon account financial information, makes it
	 * a useful component to hold on to. 
	 */
}
