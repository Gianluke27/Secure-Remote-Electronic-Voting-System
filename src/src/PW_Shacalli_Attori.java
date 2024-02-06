
import com.sun.net.ssl.internal.ssl.Provider;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.bouncycastle.pqc.math.linearalgebra.BigIntUtils;

/**
 *
 * @author GianlucaPC
 */
public class PW_Shacalli_Attori {

    static SSLContext createSSLContext(String n) throws Exception {
        String truststore = "truststore" + n + ".jks";
        String trustpass = n + "pass";

        System.setProperty("javax.net.ssl.trustStore", truststore);
        //specifing the password of the trustStore file
        System.setProperty("javax.net.ssl.trustStorePassword", trustpass);

        KeyManagerFactory keyFact = KeyManagerFactory.getInstance("SunX509");
        KeyStore clientStore = KeyStore.getInstance("JKS");

        String keystore = "keystore" + n + ".jks";
        String keypass = n + "pass";

        clientStore.load(new FileInputStream(keystore), keypass.toCharArray());

        keyFact.init(clientStore, keypass.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyFact.getKeyManagers(), null, null);

        return sslContext;
    }

    static SSLContext createSSLContextDefault(String n) throws Exception {
        String truststore = "truststore" + n + ".jks";
        String trustpass = n + "pass";

        System.setProperty("javax.net.ssl.trustStore", truststore);
        //specifing the password of the trustStore file
        System.setProperty("javax.net.ssl.trustStorePassword", trustpass);

        SSLContext sslContext = SSLContext.getDefault();
        //sslContext.init(keyFact.getKeyManagers(), null, null);

        return sslContext;
    }

    static void ProtocolSendPK(Socket sslSocket, Actor attore) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());

        //Invio della PK dell'attore
        outputStream.writeObject(attore.getPK());

        //Invio la firma della PK dell'attore
        outputStream.writeObject(attore.SignPK());
    }

    static void ProtocolSendAllPK(SSLContext sslContext, Actor[] attori) throws Exception {
        int serverPort = 4000;
        String serverName = "BN01";
        for (int i = 0; i < Actor.numActor(); i++) {
            SSLSocketFactory sslClientSocket = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslClientSocket.createSocket(serverName, serverPort);
            sslSocket.startHandshake();

            ProtocolSendPK(sslSocket, attori[i]);

            sslSocket.close();
        }
    }

    static void ProtocolSendContribute(Socket sslSocket, Actor attore) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());

        //Invio la PK, il contributo cifrato e la firma dello stesso
        outputStream.writeObject(attore.getPK());
        outputStream.writeObject(attore.getContribute());
        outputStream.writeObject(attore.Sign(attore.getContribute()));

        //ZKP del formato del voto
        ZKP zkpVote = ZeroKnowledgeProof.generateProof(attore.getContribute(), attore.getFirmedContribute());
        outputStream.writeObject(zkpVote);
    }

    static void ProtocolSendAllContribute(SSLContext sslContext, Actor[] attori) throws Exception {
        int serverPort = 4000;
        String serverName = "BN01";
        for (int i = 0; i < Actor.numActor(); i++) {
            SSLSocketFactory sslClientSocket = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslClientSocket.createSocket(serverName, serverPort);
            sslSocket.startHandshake();

            ProtocolSendContribute(sslSocket, attori[i]);

            sslSocket.close();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //*
        //Istanzio i parametri del gruppo
        ElGamalParameters groupParam = new ElGamalParameters(64); //64 per test

        //Istanziamento degli attori coinvolti nel segreto
        Actor[] attori = new Actor[Actor.numActor()];
        int i = 0;
        for (Actor.ActorName a : Actor.ActorName.values()) {
            attori[i] = new Actor(groupParam, a.name());
            i++;
        }

        //Invio le PK
        //SSLContext sslContext = createSSLContextDefault("Attori");
        SSLContext sslContext = createSSLContext("Attori");

        try {
            ProtocolSendAllPK(sslContext, attori);
        } catch (Exception ex) {
            System.err.println("Error Happened : " + ex.toString());
        }

        System.out.println("Invio PK completato");
        System.out.println("Digita \"go\" per decifrare il risultato delle elezioni");

        //Attendo il caricamento in blockchain
        Scanner keyboardInput = new Scanner(System.in);
        String val = keyboardInput.nextLine();
        while (val.compareTo("go") != 0) {
            val = keyboardInput.nextLine();
        }

        System.out.println("LETTURA BLOCKCHAIN");
        System.out.println("Ottengo le share");
        Blockchain.initReading();

        ElGamalPK keyvotePK = Blockchain.getOnlyReadTransactions(3)[0].getPK();

        for (int j = 0; j < Actor.numActor(); j++) {
            for (Transaction tr : Blockchain.getOnlyReadTransactions(1)) {

                BigInteger share = attori[j].Decrypt(tr.getCT());
                if (Schnorr.Verify(tr.getSign(), keyvotePK, share)) {
                    attori[j].setShare(share);
                    //System.out.println("Share appresa attore " + j + " Cyphertext: " + tr.getCT().v + "\tmessage: " + share);
                }
            }
        }
        
        System.out.println("Share ottenute!");

        ElGamalCT resultCT = Blockchain.getOnlyReadTransactions(6)[0].getCT();
        Transaction[] tr = Blockchain.getOnlyReadTransactions(1);

        for (int j = 0; j < Actor.numActor(); j++) {
            attori[j].setFirmedContribute(tr[j].getSign());
            attori[j].setContribute(resultCT.u.modPow(attori[j].getShare(), groupParam.p));
        }

        try {
            ProtocolSendAllContribute(sslContext, attori);
        } catch (Exception ex) {
            System.err.println("Error Happened : " + ex.toString());
        }
    }

}
