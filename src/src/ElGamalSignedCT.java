/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Shacalli
 */
public class ElGamalSignedCT {
    private final SchnorrSignature sign;
    private final ElGamalCT cyphertext;

    public ElGamalSignedCT(ElGamalCT cyphertext, SchnorrSignature sign) {
        this.sign = sign;
        this.cyphertext = cyphertext;
    }

    public SchnorrSignature getSign() {
        return sign;
    }

    public ElGamalCT getCyphertext() {
        return cyphertext;
    }
}
