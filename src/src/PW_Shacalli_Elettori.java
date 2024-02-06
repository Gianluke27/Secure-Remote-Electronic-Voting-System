
import com.sun.jmx.snmp.Timestamp;
import com.sun.net.ssl.internal.ssl.Provider;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author GianlucaPC
 */
public class PW_Shacalli_Elettori {

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

    static ElGamalSK ProtocolRequestCredential(Socket sslSocket) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        //Ottengo i parametri del gruppo
        ElGamalParameters groupParams = (ElGamalParameters) inputStream.readObject();

        //Crea le chiavi
        ElGamalSK SID = ElGamal.init(groupParams);
        ElGamalSK newKey = ElGamal.init(groupParams);

        //Invio il SID
        outputStream.writeObject(SID.PK);
        
        //Firmo la PK con SK e invio
        SchnorrSignature SID_sign = Schnorr.Sign(SID, SID.PK.y);
        outputStream.writeObject(SID_sign);
        
        //Invio la PK
        outputStream.writeObject(newKey.PK);

        //Firmo la PK con SK e invio
        SchnorrSignature sign = Schnorr.Sign(newKey, newKey.PK.y);
        outputStream.writeObject(sign);

        return newKey;
    }

    static void ProtocolRequestAllCredential(SSLContext sslContext, ElGamalSK[] elettori) throws Exception {
        int serverPort = 4000;
        String serverName = "BN01";
        for (int i = 0; i < elettori.length; i++) {
            SSLSocketFactory sslClientSocket = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslClientSocket.createSocket(serverName, serverPort);
            sslSocket.startHandshake();

            System.out.println("Richiesta di ammissione al voto del cittadino "+(i+1));

            elettori[i] = ProtocolRequestCredential(sslSocket);

            sslSocket.close();
        }
    }

    static void ProtocolVote(Socket sslSocket, ElGamalSK elettore, ElGamalPK keyvotePK, int voto) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        //Invio PK dell'elettore
        outputStream.writeObject(elettore.PK);

        BigInteger votoBinario = BigInteger.ONE.shiftLeft((voto - 1) * 4);

        ElGamalCT votoCifrato = new ElGamalCT(ElGamal.EncryptExponent(keyvotePK, votoBinario));

        //TIMESTAMP
        Timestamp time = new Timestamp(System.currentTimeMillis());

        long timestamp = time.getDateTime();

        //voto|TIMESTAMP
        BigInteger votePlusTimestamp = new BigInteger(String.valueOf(votoCifrato.v) + String.valueOf(timestamp));

        //Invio voto, TIMESTAMP, sign(voto|TIMESTAMP)
        outputStream.writeObject(votoCifrato);
        outputStream.writeLong(timestamp);
        outputStream.writeObject(Schnorr.Sign(elettore, votePlusTimestamp));

        //ZKP del formato del voto
        ZKP zkpVote = ZeroKnowledgeProof.generateProof(votoCifrato.v, BigInteger.ONE);
        outputStream.writeObject(zkpVote);

        System.out.println("\nVoto inviato!!!\n");
    }

    static SSLSocket giveSSLSocket(SSLContext sslContext) throws Exception {
        int serverPort = 4000;
        String serverName = "BN01";
        SSLSocketFactory sslClientSocket = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslClientSocket.createSocket(serverName, serverPort);
        sslSocket.startHandshake();
        return sslSocket;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        int nElettori = 10;
        ElGamalSK[] elettori = new ElGamalSK[nElettori];

        //
        //SSLContext sslContext = createSSLContextDefault("Elettori");
        SSLContext sslContext = createSSLContext("Elettori");

        try {
            ProtocolRequestAllCredential(sslContext, elettori);
        } catch (Exception ex) {
            System.err.println("Error Happened : " + ex.toString());
        }

        System.out.println("\nRichiesta credenziali completato\n");

        //Inizio voto
        Blockchain.initReading();

        ElGamalPK keyvotePK = Blockchain.getOnlyReadTransactions(3)[0].getPK();

        for (int i = 0; i < nElettori; i++) {
            try {
                System.out.println("Votazione dell'elettore " + (i + 1));
                for (int j = 0; j < (Actor.numActor() - 3) / 2; j++) {
                    System.out.println("Digita: \n\"" + (j + 1) + "\" per votare il candidato " + Actor.nameOfCandidate(j));
                }
                Scanner keyboardInput = new Scanner(System.in);
                int voto = keyboardInput.nextInt();
                if (voto > 0 && voto <= (Actor.numActor() - 3) / 2) {
                    SSLSocket sslSocket = giveSSLSocket(sslContext);

                    ProtocolVote(sslSocket, elettori[i], keyvotePK, voto);

                    sslSocket.close();
                }

            } catch (Exception ex) {
                System.err.println("Error Happened : " + ex.toString());
            }
        }

        //Attendo il caricamento in blockchain
        System.out.println("Digita \"go\" per sapere il risultato delle elezioni");

        Scanner keyboardInput = new Scanner(System.in);
        String val = keyboardInput.nextLine();
        while (val.compareTo("go") != 0) {
            val = keyboardInput.nextLine();
        }

        ShamirSecretSharing.PrintResults(Blockchain.getOnlyReadTransactions(8)[0].getValue());
    }

}
