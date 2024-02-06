/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * @author Shacalli
 */
public class ElGamalParameters implements Serializable{
    BigInteger q;
    BigInteger p;
    BigInteger g;
    int securityparameter;

    public ElGamalParameters(int securityparameter) {
        this.securityparameter = securityparameter;

        SecureRandom src = new SecureRandom();
        while (true) {
            this.q = BigInteger.probablePrime(securityparameter, src);
            this.p = BigInteger.valueOf(2).multiply(this.q).add(BigInteger.ONE);
            if (this.p.isProbablePrime(50)) {
                break;
            }
        }

        this.g = new BigInteger("4");
    }

    public ElGamalParameters(ElGamalPK PK) {
        this.q = PK.q;
        this.p = PK.p;
        this.g = PK.g;
        this.securityparameter = PK.securityparameter;
    }
}
