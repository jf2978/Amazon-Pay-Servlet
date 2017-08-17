package amazonPay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class ButtonServlet extends HttpServlet {

	protected static final String AMAZON_PAY_URL = "payments.amazon.com";
	private static final String HTTP_VERB = "POST";
	
	/* ==========Java Servlet to facilitate Amazon Pay flow========== */
	
	public ButtonServlet(){
        super();
	}

	// Provides account/order parameters to front-end button widget's $.getJSON
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		MMPaymentServer.log.info("JSON -> Javascript Button Parameters");
		
		try{
			// Set required Parameters (Express Custom Integration) + Sign
			Map<String,String> parameters = new HashMap<>();
			AmazonParameters.setExpressParams(parameters);
			String stringToSign = AmazonSignature.calculateStringToSignV2(parameters, AMAZON_PAY_URL, HTTP_VERB);
			String signature = AmazonSignature.sign(stringToSign, AmazonParameters.MWS_SECRET);
			
			// Add Signature to parameters + wrap in JSONObject
			parameters.put("signature", AmazonSignature.urlEncode(signature));
			JSONObject data = new JSONObject(parameters);
			
			// Write response to front-end widget
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*"); // To test locally
			PrintWriter resp = response.getWriter();
			resp.print(data);
			resp.close();
			
		} catch(InvalidKeyException e){
			MMPaymentServer.log.info("Key used to initialize Mac object was invalid\n");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			MMPaymentServer.log.info("Invalid Message Authentication Code (MAC) algorithm. Must be HmacMD5, HmacSHA1 or HmacSHA256\n");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			MMPaymentServer.log.info("The named encoding is not supported. UTF-8 is strongly recommended\n");
			e.printStackTrace();
		} catch (IOException e) {
			MMPaymentServer.log.info("There was an issue with the opening/writing to the response PrintWriter object\n");
			e.printStackTrace();
		}
	}
}