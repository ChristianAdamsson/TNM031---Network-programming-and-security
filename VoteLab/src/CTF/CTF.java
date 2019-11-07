package CTF;

import java.security.*;
import javax.net.ssl.*;
import java.io.*;
import java.util.*;

public class CTF { 
    private int port;

    static final int DEFAULT_PORT = 8192;
    static final String KEYSTORE = "src/Server/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/Server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
   
    private BufferedReader socketReaderFromCLA;
    private PrintWriter socketPrinterToCLA;
    private SSLSocket socketCLA;
    private SSLSocket socketClient;
    private boolean running = true;

    private SSLSocket streamCLA;
    private SSLSocket streamClient;
    private BufferedReader socketReaderFromClient;
    private PrintWriter socketPrinterToClient;

    private List <Vote> registeredVotes;
    private List <String> validationNumbers;

    private int counter;

    public void run() {
        try {

            registeredVotes = new ArrayList<Vote>();
            validationNumbers = new ArrayList<String>();

            //Load Keystore
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

            //Load Truststore
            KeyStore ts = KeyStore.getInstance("JCEKS");
            ts.load(new FileInputStream(TRUSTSTORE), TRUSTSTOREPASS.toCharArray());

            //Init Keystore
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KEYSTOREPASS.toCharArray() );
            
            //Init Truststore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);

            //Create a SSLContext object with the keystore and truststore, no extra protocol
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();

            //Set ports for the clients and CLA
            SSLServerSocket socketClient = (SSLServerSocket) sslServerFactory.createServerSocket(1500);
            SSLServerSocket socketCLA = (SSLServerSocket) sslServerFactory.createServerSocket(DEFAULT_PORT);

            //Set the ciphersuites to the ones supported by the Client and CLA
            socketClient.setEnabledCipherSuites(socketClient.getSupportedCipherSuites());
            socketCLA.setEnabledCipherSuites(socketCLA.getSupportedCipherSuites());

            //Set clientAuth
            socketClient.setNeedClientAuth(true);
            socketCLA.setNeedClientAuth(true);

            System.out.println("CTF active");

            streamClient = (SSLSocket) socketClient.accept();
            streamCLA = (SSLSocket) socketCLA.accept();

            socketReaderFromCLA = new BufferedReader(new InputStreamReader(streamCLA.getInputStream()));
            socketPrinterToCLA = new PrintWriter(streamCLA.getOutputStream(), true);

            socketReaderFromClient = new BufferedReader(new InputStreamReader(streamClient.getInputStream()));
            socketPrinterToClient = new PrintWriter(streamClient.getOutputStream(), true);
            
            //System.out.println(socketReaderFromClient.readLine());

            String allowedVoters = socketReaderFromCLA.readLine();
            socketPrinterToClient.println(allowedVoters);
            boolean verify = false;
            while(true){
                while(counter < Integer.parseInt(allowedVoters)) {
                    String messageFromCLA = socketReaderFromCLA.readLine();
                    String messageFromClient = socketReaderFromClient.readLine();

                    System.out.println(messageFromClient);
                    String[] parts = messageFromClient.split(":");
                    if(parts[0].equals("Verify")) {  
                        verify = true;      
                        verifyVoter(parts);
                    }
                    if(!verify) {
                        if(registerVote(parts, messageFromCLA, allowedVoters))
                            break;
                    }
                verify = false;
            }
            
            	
                String messageFromClient = socketReaderFromClient.readLine();
                String[] parts = messageFromClient.split(":");
                
                if(parts[0].equals("Result")) {
                     sendResults();
                }

                else {//if(parts[0].equals("Verify")) {
                    verifyVoter(parts);
                }                
            }
              
            }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResults() {

        String result = "";
        for(Vote v : registeredVotes) {
           result +=(v.toString());
           result += ":";
        }
        socketPrinterToClient.println(result); 
    }

    public void verifyVoter(String[] parts) {
        String voted = "";

        for(Vote v : registeredVotes) {
            if(parts[2].equals(v.getIdentificationNumber()))
               voted = v.toString();
            }

        if(!voted.equals(""))
            socketPrinterToClient.println(voted);
        else
            socketPrinterToClient.println("You have not voted yet");
    }

    public boolean registerVote(String[] parts, String messageFromCLA, String allowedVoters) {
        if(parts[0].equals(messageFromCLA) && !parts[0].equals("Cheater")){
            if(!validationNumbers.contains(parts[0])) {
                registeredVotes.add(new Vote(parts[0], parts[1], parts[2]));
                validationNumbers.add(parts[0]);
                if(counter == (Integer.parseInt(allowedVoters)-1)) {
                    socketPrinterToClient.println("Voting Completed");
                    counter++;
                    return true;
                }
                else {
                    socketPrinterToClient.println("You voted on " + parts[2]);
                    counter++;
                    return false;
                }   
            }
        }
        else {
            socketPrinterToClient.println("You have already voted");
            return false;
        }
        return false;
    }

    public static void main(String[] args) {
        CTF server = new CTF();
        server.run();
    }
    
}