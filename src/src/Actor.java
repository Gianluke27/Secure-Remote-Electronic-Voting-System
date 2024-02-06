
import java.math.BigInteger;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Shacalli
 */
public class Actor {

    public enum ActorName {
        Candidate1, Candidate2, Candidate3, Candidate4,
        PersonOfLaw1, PersonOfLaw2, PersonOfLaw3, PersonOfLaw4,
        PRep, PReg, PProv
    };

    static int numActor() {
        return ActorName.values().length;
    }

    static int numCandidate() {
        return (ActorName.values().length - 3) / 2;
    }

    static String nameOfCandidate(int i) {
        return ActorName.values()[i].name();
    }

    private String name;
    private ElGamalSK SK;
    private BigInteger share;
    private BigInteger contribute;
    private SchnorrSignature firmedContribute;

    public Actor(ElGamalParameters groupParam, String name) {
        this.SK = ElGamal.init(groupParam);
        this.name = name;
    }

    /**
     * Get the name of the actor
     *
     * @return the name of the actor
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of SK
     *
     * @return the value of SK
     */
    private ElGamalSK getSK() {
        return SK;
    }

    public BigInteger Decrypt(ElGamalCT CT) {
        return ElGamal.Decrypt(getSK(), CT);
    }

    public SchnorrSignature SignPK() {
        return Schnorr.Sign(getSK(), getPK().y);
    }

    public SchnorrSignature Sign(BigInteger m) {
        return Schnorr.Sign(getSK(), m);
    }

    /**
     * Get the value of PK
     *
     * @return the value of PK
     */
    public ElGamalPK getPK() {
        return getSK().PK;
    }

    public BigInteger getShare() {
        return share;
    }

    public void setShare(BigInteger share) {
        this.share = share;
    }

    public BigInteger getContribute() {
        return contribute;
    }

    public void setContribute(BigInteger contribute) {
        this.contribute = contribute;
    }

    public SchnorrSignature getFirmedContribute() {
        return firmedContribute;
    }

    public void setFirmedContribute(SchnorrSignature firmedContribute) {
        this.firmedContribute = firmedContribute;
    }
}
