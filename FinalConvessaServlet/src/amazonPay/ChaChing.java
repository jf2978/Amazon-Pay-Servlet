package amazonPay;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazon.pay.exceptions.AmazonServiceException;

@SuppressWarnings("serial")
public class ChaChing extends HttpServlet{

	private static final String LANDING_PAGE = "https://mastermindbot.com/faq/"; // placeholder landing page
		
	/* ==========Java Servlet to handle payment return URL========== */
	
	// Parse returnURL query parameters + process payment
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){	
		try{
			MMPaymentServer.log.info("Parse Return Url -> Charge + Update Account");
			String query = request.getQueryString();
			
			PaymentProcessor pay = new PaymentProcessor(query);
			pay.processGET();
			
			//Redirect to landing page
			MMPaymentServer.log.info("Redirecting to " + LANDING_PAGE);
			response.encodeRedirectURL(LANDING_PAGE);
			response.sendRedirect(LANDING_PAGE);
		
		}catch (AmazonServiceException e){
			MMPaymentServer.log.info("Amazon was unable to fulfill the request (service-side issue)\n");
			e.printStackTrace();
		} catch (IOException e){	
			MMPaymentServer.log.info("Servlet could not redirect to landing page\n");
			e.printStackTrace();
		}
	}
	
	/* ================================================================================
	 * Adapted from Amazon Pay Java SDK Example: https://github.com/amzn/amazon-pay-sdk-java
	 ================================================================================== */
	
	// Handles incoming Amazon Instant Payment Notifications (IPN)
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try{
			MMPaymentServer.log.info("==========Instant Payment Notification==========");
			PaymentProcessor pay = new PaymentProcessor(request);
			pay.processPOST();
			
		} catch (AmazonServiceException e){
			MMPaymentServer.log.info("Amazon was unable to fulfill the request (service-side issue)\n");
			e.printStackTrace();
		} catch (IOException e){
			MMPaymentServer.log.info("There was an issue reading the IPN Message body\n");
			e.printStackTrace();
		}
	}
}