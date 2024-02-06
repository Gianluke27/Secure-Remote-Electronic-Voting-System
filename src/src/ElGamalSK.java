/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.math.BigInteger;

/**
 *
 * @author Shacalli
 */
public class ElGamalSK {
    ElGamalPK PK;
    BigInteger x;
    
    public ElGamalSK(BigInteger x, ElGamalPK PK){
        this.x = x;
        this.PK = PK;
    }
}
