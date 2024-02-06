/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.asn1.oiw.ElGamalParameter;

/**
 *
 * @author Shacalli
 */
public class ElGamal {

    public static ElGamalSK init(int securityparameter) {
        BigInteger p, q, g, y;
        SecureRandom src = new SecureRandom();
        while (true) {
            q = BigInteger.probablePrime(securityparameter, src);
            p = BigInteger.valueOf(2).multiply(q).add(BigInteger.ONE);
            if (p.isProbablePrime(50)) {
                break;
            }
        }

        g = new BigInteger("4");
        BigInteger x = new BigInteger(securityparameter, src).mod(q);
        y = g.modPow(x, p);
        ElGamalPK PK = new ElGamalPK(q, p, g, y, securityparameter);
        return new ElGamalSK(x, PK);
    }

    public static ElGamalSK init(ElGamalParameters parameter) {
        SecureRandom src = new SecureRandom();
        BigInteger x = new BigInteger(parameter.securityparameter, src).mod(parameter.q);
        BigInteger y = parameter.g.modPow(x, parameter.p);
        ElGamalPK PK = new ElGamalPK(parameter.q, parameter.p, parameter.g, y, parameter.securityparameter);
        return new ElGamalSK(x, PK);
    }

    public static ElGamalCT Encrypt(ElGamalPK PK, BigInteger m) {
        SecureRandom sc = new SecureRandom();
        BigInteger r = new BigInteger(PK.securityparameter, sc).mod(PK.q);
        BigInteger v = m.multiply(PK.y.modPow(r, PK.p));
        v = v.mod(PK.p);
        BigInteger u = PK.g.modPow(r, PK.p);
        return new ElGamalCT(u, v);
    }

    public static BigInteger Decrypt(ElGamalSK SK, ElGamalCT CT) {
        BigInteger tmp = CT.u.modPow(SK.x, SK.PK.p);  // tmp=u^s mod p
        tmp = tmp.modInverse(SK.PK.p);

        BigInteger M = tmp.multiply(CT.v).mod(SK.PK.p); // M=tmp*C mod p
        return M;
    }

    public static ElGamalCT EncryptExponent(ElGamalPK PK, BigInteger m) {
        SecureRandom sc = new SecureRandom();
        BigInteger M = PK.g.modPow(m, PK.p); // M=g^m mod p
        BigInteger r = new BigInteger(PK.securityparameter, sc).mod(PK.q);
        BigInteger v = M.multiply(PK.y.modPow(r, PK.p)).mod(PK.p);
        BigInteger u = PK.g.modPow(r, PK.p);
        return new ElGamalCT(u, v);
    }

    public static BigInteger DecryptExponent(ElGamalSK SK, ElGamalCT CT) {
        BigInteger tmp = CT.u.modPow(SK.x, SK.PK.p).modInverse(SK.PK.p);
        BigInteger res = tmp.multiply(CT.v).mod(SK.PK.p);

        BigInteger M = new BigInteger("0");
        while (true) {
            if (SK.PK.g.modPow(M, SK.PK.p).compareTo(res) == 0) {  // if g^M=res stop and return M
                return M;
            }
            M = M.add(BigInteger.ONE); // otherwise M++
        }
    }

    public static ElGamalCT Homomorphism(ElGamalCT[] CT, ElGamalParameters groupParams) {
        BigInteger u = BigInteger.ONE;
        BigInteger v = BigInteger.ONE;

        for (ElGamalCT ct : CT) {
            u = u.multiply(ct.u).mod(groupParams.p);
            v = v.multiply(ct.v).mod(groupParams.p);
        }

        return new ElGamalCT(u, v);
    }

    public static BigInteger findResult(ElGamalPK[] PKActorKey, BigInteger[] contributes, ElGamalCT encryptedResult) {
        ElGamalParameters groupParams = new ElGamalParameters(PKActorKey[0]);
        
        BigInteger w = BigInteger.ONE;
        int i = 0;
        for (ElGamalPK i_PK : PKActorKey) {
            BigInteger dj = BigInteger.ONE;
            for (ElGamalPK j_PK : PKActorKey) {
                if (j_PK.y != i_PK.y) {
                    dj = dj.multiply(j_PK.y.subtract(i_PK.y).modInverse(groupParams.q));
                    dj = j_PK.y.multiply(dj).mod(groupParams.q);
                }
            }
            w = w.multiply(contributes[i].modPow(dj.mod(groupParams.q), groupParams.p)).mod(groupParams.p);
            i++;
        }

        BigInteger g_m = w.modInverse(groupParams.p).multiply(encryptedResult.v).mod(groupParams.p);

        BigInteger plaintextResult = ShamirSecretSharing.FindResult(g_m, groupParams, PKActorKey.length - 2, PKActorKey.length);
        return plaintextResult;
    }
}
