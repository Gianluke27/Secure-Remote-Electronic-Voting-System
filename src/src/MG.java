import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 *
 * @author Shacalli
 */
public class MG {

    private final ElGamalSK keyVoting;
    private final ShamirSecretSharing shamirSecret;

    public MG(ElGamalParameters groupParams, int numActors) {
        this.keyVoting = ElGamal.init(groupParams);
        this.shamirSecret = new ShamirSecretSharing(numActors - 2, numActors, groupParams, keyVoting.x);
    }

    /**
     * Get the value of SK
     *
     * @return the value of SK
     */
    private ElGamalSK getSK() {
        return keyVoting;
    }

    /**
     * Get the value of PK
     *
     * @return the value of PK
     */
    public ElGamalPK getPK() {
        return getSK().PK;
    }

    /**
     * Get the value of PK
     *
     * @return the value of PK
     */
    private ElGamalSignedCT getContribute(ElGamalPK PK_Actor) {
        BigInteger secret = this.shamirSecret.SecretSharing(PK_Actor);
        return new ElGamalSignedCT(ElGamal.Encrypt(PK_Actor, secret), Schnorr.Sign(keyVoting, secret));
    }

    public void createBlockGenesys(ElGamalPK[] PK_Actor, ElGamalPK PK_BlockchainNode) throws Exception {
        ElGamalSignedCT[] encryptedContribute = new ElGamalSignedCT[PK_Actor.length];

        for (int i = 0; i < PK_Actor.length; i++) {
            encryptedContribute[i] = this.getContribute(PK_Actor[i]);
            System.out.println(encryptedContribute[i].getCyphertext().v + "\t-> Contributo cifrato " + Actor.ActorName.values()[i]);
        }

        Blockchain.init(8);

        Transaction transactionKeyVotePK = new Transaction(keyVoting.PK);

        Transaction[] transactionBlockchainNodePK = new Transaction[1];
        transactionBlockchainNodePK[0] = new Transaction(PK_BlockchainNode);

        Transaction[] transactionActorPK = new Transaction[PK_Actor.length];
        for (int i = 0; i < PK_Actor.length; i++) {
            transactionActorPK[i] = new Transaction(PK_Actor[i], encryptedContribute[i].getCyphertext(), encryptedContribute[i].getSign());
        }

        Blockchain.createGenesisBlock(transactionActorPK, transactionBlockchainNodePK, transactionKeyVotePK);

        System.out.println("Blocco genesi creato\n");
    }

    public static boolean canSubscribeCAG(ElGamalPK PK_SID) {
        //Il Centro di Autorità Governativo mi indica se quel cittadino 
        //può essere ammesso al voto
        
        return true;
    }
}
