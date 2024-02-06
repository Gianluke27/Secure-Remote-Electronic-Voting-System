/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 *
 * @author Shacalli
 */
public class Schnorr {

    private static BigInteger HashToBigInteger(ElGamalPK PK, BigInteger a, BigInteger M) {
        String msg = PK.g.toString() + PK.y.toString() + a.toString() + M.toString(); // Hash PK+a+M to a BigInteger
        try {
            MessageDigest h = MessageDigest.getInstance("SHA256"); // hash a String using MessageDigest class
            h.update(Utils.toByteArray(msg));
            BigInteger e = new BigInteger(h.digest());
            return e.mod(PK.q);
        } catch (Exception E) {
            E.printStackTrace();
        }
        
        BigInteger e = new BigInteger("0");
        return e;
    }

    public static SchnorrSignature Sign(ElGamalSK SK, BigInteger M) {
        SecureRandom sc = new SecureRandom(); // generate secure random source
        BigInteger r = new BigInteger(SK.PK.securityparameter, sc); // choose random r
        BigInteger a = SK.PK.g.modPow(r, SK.PK.p); // a=g^r mod p
        BigInteger c = HashToBigInteger(SK.PK, a, M); // c=H(PK,a,M)
        BigInteger z = r.add(c.multiply(SK.x).mod(SK.PK.q)).mod(SK.PK.q); // z=r+cs mod q
        return new SchnorrSignature(a, c, z); // (a,e,z) is the signature of M
    }

    public static boolean Verify(SchnorrSignature sigma, ElGamalPK PK, BigInteger M) {
        BigInteger c2 = HashToBigInteger(PK, sigma.a, M); // we compute the the challenge c c=H(PK,a,M)
        BigInteger tmp = sigma.a.multiply(PK.y.modPow(c2, PK.p)).mod(PK.p); // tmp=ay^c2

        if (tmp.compareTo(PK.g.modPow(sigma.z, PK.p)) == 0) { // compare tmp with g^z mod p 
            return true;
        }
        return false;
    }
}
