
// An example class that uses the secure server socket class

package Server;

import java.io.*;
import java.math.*;
import java.security.SecureRandom;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.*;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class CLT {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8191;
	static final String KEYSTORE = "src/Server/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/Server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	final static int bitlength = 32;
	
	private Map <String,Person> VotedList;
	private List <Person> regVoters;
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	CLT( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
	public void run() {
		try {
			VotedList = new HashMap<String,Person>();
			
			regVoters = new ArrayList<Person>();
			regVoters.add(new Person("Christian", "199603231337"));
			regVoters.add(new Person("Lucas", "199605240000"));
			regVoters.add(new Person("Steffe", "199002151337"));
			
			
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			
			//CTF socket
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket CTF =  (SSLSocket)sslFact.createSocket("localhost", 8192); 
			
			
			
            CTF.setEnabledCipherSuites(sslFact.getSupportedCipherSuites());
			
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			
			//detta var innan suported ciphersuites och därför funkade det inte. 
			sss.setNeedClientAuth(true);
			CTF.setNeedClientAuth(true);
			System.out.println("\n>>>> CLA: active ");
			SSLSocket incoming = (SSLSocket)sss.accept();

			BufferedReader in = new BufferedReader( new InputStreamReader( incoming.getInputStream() ) );
			PrintWriter out = new PrintWriter( incoming.getOutputStream(), true );			
			
		
			BufferedReader inCTF = new BufferedReader( new InputStreamReader( CTF.getInputStream() ) );
			PrintWriter outCTF = new PrintWriter( CTF.getOutputStream(), true );			
			
			
			outCTF.println(regVoters.size());
			
			
			//while(true) {
				String Message = in.readLine();
				String[] MsgSplit = Message.split(":");
//				
				String ValNr = GetValidnr(MsgSplit[0],MsgSplit[1]);
				if(!ValNr.equals("Denied")){
					outCTF.println(ValNr);
				}
				out.println(ValNr);
				
	//			out.println(Message);
			//}
		
			
			
			
			
			
		}			

		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	
	public String GetValidnr(String Name, String personNr) {
		BigInteger q = BigInteger.probablePrime(bitlength, new SecureRandom());	

		Person p = new Person(Name, personNr);
		//Om han inte är registrerad
		if(regVoters.contains(p)) {
			return "Denied";
		}
		
		//om ingen har röstat
		if(VotedList.isEmpty()) {
			VotedList.put(q.toString(), p);
			return q.toString();
			
		}
		//om han inte redan har röstat add to list och skicka validationnr
		else if(!VotedList.containsKey(p.getSocialSecurityNumber())){
			
			VotedList.put(q.toString(), p);
			return q.toString();
			
		}
		//redan röstat
		else {
			return "Denied";
		}
		
		
	}
	
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		CLT addServe = new CLT( port );
		addServe.run();
	}
}

