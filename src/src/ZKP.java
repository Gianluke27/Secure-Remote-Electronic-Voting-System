/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Shacalli
 */
public class ZKP implements Serializable{
    BigInteger value;
    BigInteger randomness;
    SchnorrSignature sign;
    
    public ZKP(BigInteger m,BigInteger r){
        this.value = m;
        this.randomness = r;
    }
    
    public ZKP(BigInteger c, SchnorrSignature sign){
        this.value = c;
        this.sign = sign;
    }
}
