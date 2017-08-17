package amazonPay;

import java.util.HashMap;
import java.util.Map;

import com.amazon.pay.Client;
import com.amazon.pay.Config;
import com.amazon.pay.exceptions.AmazonServiceException;
import com.amazon.pay.impl.PayClient;
import com.amazon.pay.impl.PayConfig;
import com.amazon.pay.request.AuthorizeRequest;
import com.amazon.pay.request.CaptureRequest;
import com.amazon.pay.request.GetOrderReferenceDetailsRequest;
import com.amazon.pay.request.RefundRequest;
import com.amazon.pay.response.parser.AuthorizeResponseData;
import com.amazon.pay.response.parser.CaptureResponseData;
import com.amazon.pay.response.parser.GetOrderReferenceDetailsResponseData;
import com.amazon.pay.response.parser.RefundResponseData;
import com.amazon.pay.types.CurrencyCode;
import com.amazon.pay.types.Region;

public class AmazonPayAPI {
	
	protected static final Config config = new PayConfig()
			.withSellerId(AmazonParameters.SELLER_ID)
			.withAccessKey(AmazonParameters.MWS_ACCESS_ID)
			.withSecretKey(AmazonParameters.MWS_SECRET)
			.withCurrencyCode(CurrencyCode.USD)
			.withSandboxMode(true) // comment out or remove, when going into production
			.withRegion(Region.US);
		
	protected static final Client client = new PayClient(config);
	
	/* ==========Contains Amazon Pay API actions for the Mastermind seller account========== */
	
	protected static Map<String,String> getOrderData(String amazonOrderReferenceId, Client amazonClient) 
			throws AmazonServiceException{
		
		// Get Amazon order reference object
		GetOrderReferenceDetailsRequest getOrderReference = new GetOrderReferenceDetailsRequest(amazonOrderReferenceId);
		GetOrderReferenceDetailsResponseData resp = amazonClient.getOrderReferenceDetails(getOrderReference);
		
		// Collect order/buyer data
		Map<String,String> data = new HashMap<>();
		String orderId = resp.getDetails().getSellerOrderAttributes().getSellerOrderId();
		String orderAmount = resp.getDetails().getOrderTotal().getAmount();
		String orderReferenceState = resp.getDetails().getOrderReferenceStatus().getState();
		String buyerName = resp.getDetails().getBuyer().getName();
		String buyerEmail = resp.getDetails().getBuyer().getEmail();
		String buyerPhone = resp.getDetails().getBuyer().getPhone();
			
		// Store + return response data
		data.put("Order_Id", orderId);
		data.put("Amount", orderAmount);
		data.put("Order_Reference_State", orderReferenceState);
		data.put("Name", buyerName);
		data.put("Email", buyerEmail);
		data.put("Phone_Number", buyerPhone);
		
		return data;
	}
	
	protected static Map<String,String> authorizeOrder(String amazonOrderReferenceId, String authId, String orderAmount, Client amazonClient) 
			throws AmazonServiceException{
		// Prepare Authorization request object
		AuthorizeRequest authorizeRequest = new AuthorizeRequest(amazonOrderReferenceId , authId , orderAmount);
		authorizeRequest.setTransactionTimeout("0"); // Synchronous call
		AuthorizeResponseData resp = amazonClient.authorize(authorizeRequest);
		
		// Collect necessary data from auth response
		Map<String,String> data = new HashMap<>();
		String authorizationState = resp.getDetails().getAuthorizationStatus().getState();	
		String authorizationId = resp.getDetails().getAmazonAuthorizationId();
		String reasonCode = resp.getDetails().getAuthorizationStatus().getReasonCode();

		// Store +return response data
		data.put("Authorization_State", authorizationState);
		data.put("Amazon_Authorization_Id", authorizationId);
		data.put("Reason_Code", reasonCode);
		
		return data;
	}
	
	protected static Map<String,String> captureFunds(String amazonAuthorizationId, String capId, String orderAmount, Client amazonClient) 
			throws AmazonServiceException{

		// Prepare capture request object
		CaptureRequest captureRequest = new CaptureRequest(amazonAuthorizationId ,capId , orderAmount);
		CaptureResponseData resp = amazonClient.capture(captureRequest);
		
		Map<String,String> data = new HashMap<>();
		String captureState = resp.getDetails().getCaptureStatus().getState();
		String amazonCaptureId = resp.getDetails().getAmazonCaptureId();
		
		data.put("Capture_State", captureState);
		data.put("Amazon_Capture_Id", amazonCaptureId); // Id used for refunds
			
		return data;
	}
	
	protected static void handleFailedAuthorization(Map<String,String> authDetails, Client amazonClient) 
			throws AmazonServiceException{
		
		String state = authDetails.get("Authorization_State");
		String reason = authDetails.get("Reason_Code");
		String softDecline = authDetails.get("Soft_Decline");
		
		if(state.compareTo("Declined") == 0){
			switch(reason){
			case "InvalidPaymentMethod":
				if(softDecline.compareTo("True") == 0){
					// Authorize Again or gracefully end processing + throw error
				}
				else{
					// "Hard Decline": End processing, payment method needs to be updated by buyer
				}
			case "AmazonRejected":
				// Amazon has rejected the authorization for their own reasons, order_reference = closed
			case "ProcessingFailure":
				// Authorize Again or gracefully end processing + throw error
			case "TransactionTimedOut":
				// Adjust timeout parameter, try authorizing asynchronously, or throw error
			}
		}else if(state.compareTo("Closed") == 0){
			switch(reason){
			case "ExpiredUnused":
				// Authorization has expired and is no longer valid
			case "MaxCapturesProcessed":
				// Already Captured, cannot re-authorize
			case "AmazonClosed":
				// Amazon has closed the authorization due to amazon pay account issues
			case "OrderReferenceCanceled":
				// OR has been canceled, check reason code in OR object for details
			case "SellerClosed":
				// The Amazon Pay user has explicitly closed this authorization object
			case "InvalidPaymentMethod":
				// The Amazon Pay user has explicitly cancelled a suspended OR
			}
		}
	}
	
	protected static void initiateRefund(String amazonCaptureId, String refundId, String amount, 
			Client amazonClient) throws AmazonServiceException{
		RefundRequest refund = new RefundRequest(amazonCaptureId, refundId, amount);
		RefundResponseData resp = amazonClient.refund(refund);
		
		// Get any refund details needed
		MMPaymentServer.log.info("Refund Id: " + resp.getDetails().getAmazonRefundId());
		MMPaymentServer.log.info(resp.getDetails().getRefundAmount());
	}
}
