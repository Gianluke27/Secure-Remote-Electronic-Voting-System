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
public class ElGamalCT implements Serializable{
    BigInteger u;
    BigInteger v;
    
    public ElGamalCT(BigInteger u,BigInteger v){
        this.u = u;
        this.v = v;
    }
    
     public ElGamalCT(ElGamalCT CT){
        this.u = CT.u;
        this.v = CT.v;
    }
}
