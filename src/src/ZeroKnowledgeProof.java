/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.math.BigInteger;

/**
 *
 * @author Shacalli
 */
public class ZeroKnowledgeProof {
    public static ZKP generateProof(BigInteger m, BigInteger r){
        return new ZKP(m,r);
    }
    
    public static ZKP generateProof(BigInteger c,SchnorrSignature sign){
        return new ZKP(c,sign);
    }
    
    public static boolean isVerified(ZKP proof){
        return true;
    }
}
