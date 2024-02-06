import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * @author Shacalli
 */
public class ShamirSecretSharing {

    private final BigInteger[] poly;

    private final int t;
    private final int n;

    public ShamirSecretSharing(int t, int n, ElGamalParameters GroupParam, BigInteger secret) {
        this.t = t;
        this.n = n;

        SecureRandom src = new SecureRandom(); // oggetto secure random

        // Nuovo polinomio
        poly = new BigInteger[t];

        //Generazione del polinomio
        poly[0] = secret;

        String out = "Polinomio: " + poly[0].toString();

        for (int i = 1; i < t; i++) {
            poly[i] = new BigInteger(GroupParam.securityparameter, src).mod(GroupParam.q);
            out = out + " " + poly[i].toString() + "x^" + i;
        }

        System.out.println(out);
    }

    public BigInteger SecretSharing(ElGamalPK PK_Actor) {
        BigInteger share; // parti del segreto per ogni attore

        share = this.poly[0];
        for (int j = 1; j < this.t; j++) {
            // a*x^j
            share = share.add(this.poly[j].multiply(PK_Actor.y.pow(j)).mod(PK_Actor.q));
        }
        share = share.mod(PK_Actor.q);
        
        return share;
    }
    
    public static BigInteger FindResult(BigInteger g_m, ElGamalParameters groupParameters, int n_candidate, int bit_per_candidate) { 
        BigInteger i = BigInteger.ZERO;
        BigInteger limit = BigInteger.valueOf(2).pow(n_candidate*bit_per_candidate);
        
        for(; i.compareTo(limit) < 0; i=i.add(BigInteger.ONE)){
            if(groupParameters.g.modPow(i, groupParameters.p).compareTo(g_m) == 0){
                return i;
            }
        }
        return null;
    }
    
    static void PrintResults(BigInteger results){
        //4 candidati, 4 bit per candidato
        BigInteger maschered = BigInteger.valueOf(2).pow(4).add((BigInteger.ONE.negate()));
        
        System.out.println("\nRISULTATI ELEZIONI\n");
        
        for(int i=0; i<(Actor.numActor()-3)/2; i++){
            System.out.println(Actor.ActorName.values()[i] + ": "+results.and(maschered).shiftRight(i*4)+" voti");
            maschered = maschered.multiply(BigInteger.valueOf(2).pow(4));
        }
    }
}