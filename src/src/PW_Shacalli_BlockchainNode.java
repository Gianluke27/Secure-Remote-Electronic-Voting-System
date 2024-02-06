/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import com.sun.net.ssl.internal.ssl.Provider;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author GianlucaPC
 */
public class PW_Shacalli_BlockchainNode {

    public static String s = "BlockchainNode";

    static SSLServerSocket createServerSocket(int port, String n) throws Exception {
        String truststore = "truststore" + n + ".jks";
        String trustpass = n + "pass";

        System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustpass);

        String keystore = "keystore" + n + ".jks";
        String keypass = n + "pass";
        System.setProperty("javax.net.ssl.keyStore", keystore);
        //specifing the password of the trustStore file
        System.setProperty("javax.net.ssl.keyStorePassword", keypass);

        SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketfactory.createServerSocket(port);
        sslServerSocket.setNeedClientAuth(true);

        return sslServerSocket;
    }

    static ElGamalPK ProtocolGetPK(Socket sslSocket) throws Exception {
        System.out.println("session GETPK started.");
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        ElGamalPK PK = (ElGamalPK) inputStream.readObject();
        SchnorrSignature Sign = (SchnorrSignature) inputStream.readObject();

        sslSocket.close();

        if (Schnorr.Verify(Sign, PK, PK.y)) {
            System.out.println("PK dell'attore ricevuta: " + PK.y);
            System.out.println("session GETPK closed.\n");
            return PK;
        }

        System.out.println("session GETPK closed.");
        System.out.println("PK dell'attore non conforme alla firma.\n");
        return null;
    }

    static ElGamalPK[] ProtocolGetAllPK(SSLServerSocket sslServerSocket) throws Exception {
        ElGamalPK[] attori = new ElGamalPK[Actor.numActor()];
        for (int i = 0; i < Actor.numActor(); i++) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            System.out.println(Actor.nameOfCandidate(i) + " connesso");
            attori[i] = ProtocolGetPK(sslSocket);
        }
        return attori;
    }

    static ElGamalPK ProtocolRequestCredential(Socket sslSocket, ElGamalParameters groupParams) throws Exception {
        System.out.println("session GETPK Voters started.");

        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        //Invio i parametri del gruppo
        outputStream.writeObject(groupParams);

        //Ottengo la PK del SID
        ElGamalPK PK_SID = (ElGamalPK) inputStream.readObject();

        //Ottengo la sign(PK_SID)
        SchnorrSignature SID_sign = (SchnorrSignature) inputStream.readObject();

        if (Schnorr.Verify(SID_sign, PK_SID, PK_SID.y) == false) {
            sslSocket.close();
            System.out.println("session GETPK Voters closed.");
            System.out.println("PK del cittadino non conforme alla firma.\n");
            return null;
        }

        if (MG.canSubscribeCAG(PK_SID) == false) {
            sslSocket.close();
            System.out.println("Cittadino non ammesso al voto!");
            System.out.println("session GETPK Voters closed.\n");
            return null;
        }

        //Ottengo la PK
        ElGamalPK PK = (ElGamalPK) inputStream.readObject();

        //Ottengo la sign(PK_E)
        SchnorrSignature sign = (SchnorrSignature) inputStream.readObject();

        sslSocket.close();

        if (Schnorr.Verify(sign, PK, PK.y)) {
            System.out.println("PK dell'elettore generata: " + PK.y);
            System.out.println("session GETPK Voters closed.\n");
            return PK;
        }

        System.out.println("session GETPK Voters closed.");
        System.out.println("PK dell'elettore non conforme alla firma.\n");

        return null;
    }

    static ElGamalPK[] ProtocolRequestAllCredential(SSLServerSocket sslServerSocket, int nElettori, ElGamalParameters groupParams) throws Exception {
        ElGamalPK[] elettori = new ElGamalPK[nElettori];
        for (int i = 0; i < nElettori; i++) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            System.out.println("Cittadino connesso");
            elettori[i] = ProtocolRequestCredential(sslSocket, groupParams);

            Blockchain.addTransaction(new Transaction(elettori[i]), 4);
        }
        return elettori;
    }

    static Transaction ProtocolGetVote(Socket sslSocket) throws Exception {
        System.out.println("session Get Vote started.");

        ObjectOutputStream outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        //Ottengo la PK
        ElGamalPK PK = (ElGamalPK) inputStream.readObject();

        //Controllo se l'elettore è ammesso al voto
        Boolean canVote = false;
        for (Transaction tr : Blockchain.getOnlyReadTransactions(4)) {
            if (tr.getPK().y.compareTo(PK.y) == 0) {
                canVote = true;
            }
        }

        if (canVote == false) {
            sslSocket.close();
            System.out.println("session Get Vote closed.\n");
            System.out.println("Elettore non ammesso al voto!\n");
            return null;
        }

        //Ottengo il voto, TIMESTAMP, sign(voto|TIMESTAMP)
        ElGamalCT votoCifrato = (ElGamalCT) inputStream.readObject();
        Long timestamp = inputStream.readLong();
        SchnorrSignature sign = (SchnorrSignature) inputStream.readObject();

        //Calcolo voto|TIMESTAMP
        BigInteger votePlusTimestamp = new BigInteger(String.valueOf(votoCifrato.v) + String.valueOf(timestamp));

        //Ottengo la ZKP del formato del voto
        ZKP zkpVote = (ZKP) inputStream.readObject();

        sslSocket.close();

        if (Schnorr.Verify(sign, PK, votePlusTimestamp)) {
            if (ZeroKnowledgeProof.isVerified(zkpVote)) {
                //Attendo un tempo r
                long r = 1;
                //TimeUnit.SECONDS.sleep(r);
                TimeUnit.MILLISECONDS.sleep(r);
                System.out.println("Voto espresso correttamente");
                System.out.println("session Get Vote closed.\n");
                return new Transaction(PK, votoCifrato);
            }
            System.out.println("session Get Vote closed.\n");
            System.out.println("ZKP non verificata!\n");
            return null;
        }

        System.out.println("session Get Vote closed.\n");
        System.out.println("Firma non verificata!\n");
        return null;
    }

    static void ProtocolGetAllVote(SSLServerSocket sslServerSocket, int nElettori) throws Exception {
        ElGamalPK[] elettori = new ElGamalPK[nElettori];
        for (int i = 0; i < nElettori; i++) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            System.out.println("Elettore connesso");
            Transaction tr = ProtocolGetVote(sslSocket);

            Blockchain.addTransaction(tr, 5);
        }
    }

    static Transaction ProtocolGetContribute(Socket sslSocket) throws Exception {
        System.out.println("session Get Contribute started.");
        ObjectInputStream inputStream = new ObjectInputStream(sslSocket.getInputStream());

        //Ottengo la PK, il contributo cifrato e la firma dello stesso
        ElGamalPK PK = (ElGamalPK) inputStream.readObject();
        BigInteger contributoCifrato = (BigInteger) inputStream.readObject();
        SchnorrSignature sign = (SchnorrSignature) inputStream.readObject();

        //Ottengo la ZKP del contributo
        ZKP zkpContribute = (ZKP) inputStream.readObject();

        sslSocket.close();

        if (Schnorr.Verify(sign, PK, contributoCifrato)) {
            if (ZeroKnowledgeProof.isVerified(zkpContribute)) {
                System.out.println("Contributo ricevuto correttamente");
                System.out.println("session Get Contribute closed.\n");
                return new Transaction(contributoCifrato);
            }
            System.out.println("session Get Contribute closed.\n");
            System.out.println("ZKP non verificata!\n");
            return null;
        }

        System.out.println("session Get Contribute closed.\n");
        System.out.println("Firma non verificata!\n");

        return null;
    }

    static void ProtocolGetAllContributes(SSLServerSocket sslServerSocket) throws Exception {
        BigInteger[] contributiCifrati = new BigInteger[Actor.numActor()];
        for (int i = 0; i < Actor.numActor(); i++) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
            System.out.println("Attore connesso");

            Transaction tr = ProtocolGetContribute(sslSocket);

            Blockchain.addTransaction(tr, 7);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(
            String[] args)
            throws Exception {

        //Inizializzazione Server BlockchainNode
        System.out.println("Avvio server BlockchainNode\n");
        SSLServerSocket sslServerSocket = createServerSocket(4000, "BN01");

        System.out.println("Avviato\n");

        //Raccoglimento chiavi pubbliche degli attori
        System.out.println("Inizio raccoglimento PK degli attori\n");
        ElGamalPK[] attori = ProtocolGetAllPK(sslServerSocket);
        System.out.println("PK degli attori raccolte\n\n");

        //Fase di generazione del segreto tramite la Macchina della Giustizia
        System.out.println("Inizio generazione e distribuzione del segreto\n");
        ElGamalParameters groupParams = new ElGamalParameters(attori[0]);

        //PK di Blockchain node (nel certificato)
        ElGamalSK BlockchainNode01SK = ElGamal.init(groupParams);
        MG MacchinaGiustizia = new MG(groupParams, Actor.numActor());

        //Creazione del blocco Genesi
        MacchinaGiustizia.createBlockGenesys(attori, BlockchainNode01SK.PK);

        // Fase di richiesta delle credenziali di voto
        System.out.println("Contributi cifrati degli attori calcolati\n");

        //TimeUnit.SECONDS.sleep(1);
        //Richiesta generazione credenziali
        System.out.println("\nINIZIO GENERAZIONE DELLE CREDENZIALI\n");
        int nElettori = 10;
        ElGamalPK[] elettori = ProtocolRequestAllCredential(sslServerSocket, nElettori, groupParams);//new ElGamalPK[nElettori];
        System.out.println("FINE GENERAZIONE DELLE CREDENZIALI\n");

        /* IMPORTANTE
        // Si deve creare il blocco Type5
         */
        System.out.println("\nINIZIO VOTAZIONI\n");
        ProtocolGetAllVote(sslServerSocket, nElettori);
        System.out.println("\nFINE VOTAZIONI\n");

        /* IMPORTANTE
        // Avvia somma dei voti
         */
        System.out.println("\nINIZIO CREAZIONE CYPHERTEXT SOMMA\n");
        HashMap<ElGamalPK, ElGamalCT> votesOnBlockchain = new HashMap<ElGamalPK, ElGamalCT>();

        for (Transaction tr : Blockchain.getOnlyReadTransactions(5)) {
            votesOnBlockchain.put(tr.getPK(), tr.getCT());
        }

        ElGamalCT encryptedSumVotes = ElGamal.Homomorphism(votesOnBlockchain.values().toArray(new ElGamalCT[0]), groupParams);

        Blockchain.addTransaction(new Transaction(encryptedSumVotes), 6);
        System.out.println("\nFINE CREAZIONE CYPHERTEXT SOMMA\n");

        /* IMPORTANTE
        // Ottenimento dei contributi
         */
        System.out.println("\nINIZIO OTTENIMENTO CONTRIBUTI DAGLI ATTORI\n");
        ProtocolGetAllContributes(sslServerSocket);
        System.out.println("\nFINE OTTENIMENTO CONTRIBUTI DAGLI ATTORI\n");

        /* IMPORTANTE
        // decifratura
         */
        //ElGamal.DecryptExponentThreshold(shares, encryptedSumVotes);
        BigInteger[] contributes = new BigInteger[Actor.numActor()];

        int i = 0;
        for (Transaction tr : Blockchain.getOnlyReadTransactions(7)) {
            contributes[i] = tr.getValue();
            i++;
        }

        BigInteger finalResults = ElGamal.findResult(attori, contributes, encryptedSumVotes);

        Blockchain.addTransaction(new Transaction(finalResults), 8);

        System.out.println("Risultati finali: " + finalResults);

        ShamirSecretSharing.PrintResults(finalResults);
    }
}
