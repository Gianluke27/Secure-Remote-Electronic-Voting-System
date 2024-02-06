/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 *
 * @author Shacalli
 */
public class ElGamalPK  implements Serializable{
    BigInteger q;
    BigInteger p;
    BigInteger g;
    BigInteger y;
    int securityparameter;
    
    public ElGamalPK(BigInteger q,BigInteger p, BigInteger g, BigInteger y, int securityparameter){
        this.q = q;
        this.p = p;
        this.g = g;
        this.y = y;
        this.securityparameter = securityparameter;
    }   
}
