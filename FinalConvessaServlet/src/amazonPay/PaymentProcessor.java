package amazonPay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.amazon.pay.Client;
import com.amazon.pay.exceptions.AmazonServiceException;
import com.amazon.pay.impl.ipn.NotificationFactory;
import com.amazon.pay.response.ipn.model.AuthorizationNotification;
import com.amazon.pay.response.ipn.model.CaptureNotification;
import com.amazon.pay.response.ipn.model.Notification;
import com.amazon.pay.response.ipn.model.NotificationType;
import com.amazon.pay.response.ipn.model.OrderReferenceNotification;
import com.amazon.pay.response.ipn.model.RefundNotification;

public class PaymentProcessor {
	private Client amazonClient = AmazonPayAPI.client; // API Client object
	private Map<String,String> data; // All data gathered from API calls
	private Map<String,String> parameters; // return URL parameters
	private Notification notification; // Amazon Notification object (IPN)
	private NotificationType type; // Notification Type (to determine appropriate calls)
	
	// Handles GET
	public PaymentProcessor(String returnURL){
		this.data = new HashMap<>();
		this.parameters = parseQuery(returnURL);
	}
	
	// Handles POST
	public PaymentProcessor(HttpServletRequest req) throws IOException{
		Map<String,String> ipnHeaders = AmazonParameters.getIPNHeaders(req);
		String ipnBody = AmazonParameters.getIPNBody(req);
		
		this.data = new HashMap<>();
		this.notification = NotificationFactory.parseNotification(ipnHeaders, ipnBody);
	    this.type = notification.getNotificationType();
	}
	
	protected void processGET() throws AmazonServiceException{
		if(isSuccess())
			processSuccess();
		else
			processFailure();
	}
	
	protected void processPOST() throws AmazonServiceException{
		// Cast Notification object according to case + handle  notification data
	    switch (type) {
	    	// Notification occurs when an Order Reference transitions to a "closed" state
		    case OrderReferenceNotification:
		        OrderReferenceNotification on = (OrderReferenceNotification) notification;
		        
		        MMPaymentServer.log.info(on.getOrderReference().getOrderReferenceStatus());
		        break;
		    // Notification occurs when Authorization Status transitions to a "closed" state
		    case AuthorizationNotification:
	        	AuthorizationNotification an = (AuthorizationNotification) notification;
	        	
		        MMPaymentServer.log.info(an.getAuthorizationDetails().getAuthorizationStatus());
	        	
	            break;
	        // Notification occurs when Capture Status transitions to a "completed" state    
		    case CaptureNotification:
                CaptureNotification cn = (CaptureNotification) notification;
                
                MMPaymentServer.log.info(cn.getCaptureDetails().getCaptureStatus());
				MMPaymentServer.log.info("==PAYMENT FOR " + cn.getCaptureDetails().getCaptureReferenceId() + " SUCCESSFUL==");
				
				/* --TODO: Update Mastermind Account Here-- */
				
				MMPaymentServer.log.info("==MASTERMIND USER NOW HAS PAID ACCESS==");
                break;
            case BillingAgreementNotification:
                //BillingAgreementNotification bn = (BillingAgreementNotification) notification;
                
                /* BillingAgreements are for recurring payments (so if a subscription model becomes relevant in the future, use this) */
                
                break;
 
            case ProviderCreditNotification:
               // ProviderCreditNotification pc = (ProviderCreditNotification) notification;
                
                break;
            case ProviderCreditReversalNotification:
               // ProviderCreditReversalNotification pcrn = (ProviderCreditReversalNotification) notification;
                
                break;
            case RefundNotification:
                RefundNotification rn = (RefundNotification) notification;
                
                MMPaymentServer.log.info(rn.getRefundDetails().getRefundStatus());
                MMPaymentServer.log.info("==REFUND PROCESSED==");
                
                /* TODO: Update Mastermind Account*/
                
                MMPaymentServer.log.info("==MASTERMIND USER" + rn.getRefundDetails().getRefundReferenceId() + "NO LONGER HAS PAID ACCESS==");
                break;
            case SolutionProviderMerchantNotification:
                //SolutionProviderMerchantNotification sp = (SolutionProviderMerchantNotification) notification;
                break;
        }
	}
	/* ==========HELPER METHODS========== */
	
	// Splits return URL query parameters and puts them into a Map<>
	private Map<String,String> parseQuery(String query){
		Map<String,String> map = new HashMap<>();
		String[] params = query.split("&");  
	    
		for (String param : params){  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    } 
	    return map; 
	}
	
	// Method to determine GET processing success/failure
	private boolean isSuccess(){
		return (parameters.get("resultCode").compareTo("Success") == 0) ? true : false;
	}
	
	// For "success" result codes in the GET request
	private void processSuccess() throws AmazonServiceException{
		
		// API Call: GetOrderReferenceDetails
		String amazonOrderReferenceId = parameters.get("orderReferenceId");
		Map<String,String> orderData = new HashMap<>();
		orderData = AmazonPayAPI.getOrderData(amazonOrderReferenceId, amazonClient);
		updateData(orderData);
		
		String orderState = data.get("Order_Reference_State");
		String amount = data.get("Amount");
		
		// API Call: Authorize
		if(orderState.compareTo("Open") == 0){
			String authId = AmazonParameters.getUUID(); // unique, seller-defined authorization identifier
			Map<String,String> authData = new HashMap<>();
			authData = AmazonPayAPI.authorizeOrder(amazonOrderReferenceId, authId, amount, amazonClient);
			updateData(authData);
			
			String authState = data.get("Authorization_State");
			String amazonAuthorizationId = data.get("Amazon_Authorization_Id");
			
			// API Call: Capture
			if(authState.compareTo("Open") == 0){
				String capId = AmazonParameters.getUUID(); // unique, seller-defined capture identifier
				Map<String,String> capData = new HashMap<>();
				
				capData = AmazonPayAPI.captureFunds(amazonAuthorizationId, capId, amount, amazonClient); // IPN Triggered here
				updateData(capData);

			}else if(authState.compareTo("Declined") == 0 || authState.compareTo("Closed") == 0){
				AmazonPayAPI.handleFailedAuthorization(data, amazonClient);
			}
		}
	}
	
	// For the "failure" result code in the GET request
	private void processFailure(){
		String failureReason = parameters.get("failureCode");
		MMPaymentServer.log.info("ERROR: " + failureReason);
		
		/* TODO: Collect data + handle different failure cases accordingly */
		switch(failureReason){
			case "BuyerAbandoned":
				// buyer left the flow
			case "AmazonRejected":
				// Amazon could not authenticate account
			case "RequestSignatureFailure":
				// Signed incorrectly (checked for earlier though)
			case "InvalidParameterValue":
				// Flow could not be created
			case "MissingParameter":
				// Flow could not be created
			case "InvalidSellerAccountStatus":
				// Issue with Amazon Pay account
			case "TemporarySystemIssue":
				// Issue with Amazon Pay server-side stuff
		}
	}	
	private void updateData(Map<String,String> newData){
		data.putAll(newData);
	}
	protected Map<String,String> getQueryParameters(){
		return parameters;
	}
	
	protected Map<String,String> getData(){
		return data;
	}
	
	protected String getIPN(){
		return notification.toJSON();
	}
}