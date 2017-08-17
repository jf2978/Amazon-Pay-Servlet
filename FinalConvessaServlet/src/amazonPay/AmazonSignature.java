package amazonPay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AmazonSignature {
    
	protected static final String CHARACTER_ENCODING = "UTF-8";
    protected final static String ALGORITHM = "HmacSHA256";

    /* ==========Generate Amazon request signature========== */
    
    // Prepares parameter String to be signed
    protected static String calculateStringToSignV2(Map<String, String> parameters, String domain, String httpVerb) 
    		throws UnsupportedEncodingException {

        // Create flattened (String) representation
        StringBuilder data = new StringBuilder();
        data.append(httpVerb + "\n");
        data.append(domain);
        data.append("\n/");
        data.append("\n");
        
        // Implicitly sort parameters by putting them in a Tree Map
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);
        
        // Go through parameters, encode key/value, and append
        Iterator<Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            
            data.append(urlEncode(pair.getKey()));
            data.append("=");
            data.append(urlEncode(pair.getValue()));
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }
    
    // Encrypts string using the MWS secret key + returns generated signature
    protected static String sign(String data, String secretKey) 
    		throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
    	
    	Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(CHARACTER_ENCODING), ALGORITHM));
        byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
        return new String(DatatypeConverter.printBase64Binary(signature));
    }
    
    // Encodes given URL string (replacing URL-unsafe characters)
    protected static String urlEncode(String rawValue) throws UnsupportedEncodingException {
        String value = (rawValue == null) ? "" : rawValue;
        String encoded = null;

        encoded = URLEncoder.encode(value, CHARACTER_ENCODING)
	        .replace("+", "%20")
	        .replace("*", "%2A")
	        .replace("%7E", "~");
        
        return encoded;
    }
}