package amazonPay;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.amazon.pay.impl.PayLogUtil;

// @Co-Author: Dan McCafferty (SSL Configuration)
// @Co-Author: Jeffrey Fabian  

public class MMPaymentServer {
	public static final String[] SUPPORTED_CIPHER_SUITES = new String[] {};
	private static final String KEYSTORE_PASS = null;
	protected static final Logger log = Logger.getLogger(PayLogUtil.class);
	
	public static void main(String[] args){
		Server server = new Server();
		
        // Create the HttpConnectionFactory
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecurePort(8080);
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);

        // Create the SslConnectionFactory
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(MMPaymentServer.class.getResource("<KEYSTORE-PATH>").toExternalForm());
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASS);
        sslContextFactory.setKeyManagerPassword(KEYSTORE_PASS);
        sslContextFactory.setIncludeCipherSuites(SUPPORTED_CIPHER_SUITES);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");
        
        ServerConnector serverConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[] { serverConnector });
	     
        ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(ButtonServlet.class, "/button");	
		handler.addServletWithMapping(ChaChing.class, "/processPayment");
		server.setHandler(handler);
		
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}