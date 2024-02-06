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
public class Transaction implements Serializable{
    private ElGamalPK PK;
    private ElGamalCT CT;
    private SchnorrSignature sign;
    private BigInteger value;
    
    /**
     *  transaction type 1
     */
    public Transaction(ElGamalPK PK,ElGamalCT CT,SchnorrSignature sign){
        this.PK = PK;
        this.CT = CT;
        this.sign = sign;
    }
    
    /**
     *  transaction type 2-3-4
     */
    public Transaction(ElGamalPK PK){
        this.PK = PK;
    }
    
    /**
     *  transaction type 5
     */
    public Transaction(ElGamalPK PK,ElGamalCT CT){
        this.PK = PK;
        this.CT = CT;
        
    }
    
    /**
     *  transaction type 6
     */
    public Transaction(ElGamalCT CT){
        this.CT = CT;
    }
    
    /**
     *  transaction type 7-8
     */
    public Transaction(BigInteger value){
        this.value = value;
    }

    @Override
    public String toString() {
        return "Transaction{" + "PK=" + PK + ", CT=" + CT + ", sign=" + sign + ", value=" + value + '}';
    }

    public ElGamalPK getPK() {
        return PK;
    }

    public ElGamalCT getCT() {
        return CT;
    }

    public SchnorrSignature getSign() {
        return sign;
    }

    public BigInteger getValue() {
        return value;
    }
}
