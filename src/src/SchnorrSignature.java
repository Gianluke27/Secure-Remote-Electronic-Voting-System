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
public class SchnorrSignature implements Serializable{
    BigInteger a;
    BigInteger c;
    BigInteger z;
    
    public SchnorrSignature(BigInteger a, BigInteger c,BigInteger z){
        this.a = a;
        this.c = c;
        this.z = z;
    }
}
