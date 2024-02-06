/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Shacalli
 */
public class Blockchain {

    static HashMap<Integer, Integer> numTransaction;
    static int numTypes;
    static File[] transactionFiles;

    public static void init(int numTypeTransaction) throws FileNotFoundException, IOException {
        transactionFiles = new File[numTypeTransaction];
        for (int i = 0; i < numTypeTransaction; i++) {
            String path = "C:\\blockchain\\";
            String nameFile = path + "type" + (i + 1) + ".txt";
            transactionFiles[i] = new File(nameFile);
            RemoveFileIfExists(transactionFiles[i]);
        }

        numTransaction = new HashMap();
        for (int i = 0; i < numTypeTransaction; i++) {
            numTransaction.put(i + 1, 0);
        }
        numTypes = numTypeTransaction;
    }

    public static void initReading() throws FileNotFoundException, IOException {
        transactionFiles = new File[8];
        for (int i = 0; i < 8; i++) {
            String path = "C:\\blockchain\\";
            String nameFile = path + "type" + (i + 1) + ".txt";
            transactionFiles[i] = new File(nameFile);
        }
    }

    private static void RemoveFileIfExists(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static void close() throws IOException {
        for (int i = 0; i < transactionFiles.length; i++) {
            RemoveFileIfExists(transactionFiles[i]);
        }
    }

    public static void addTransaction(Transaction TR, int type) throws Exception {
        Transaction[] oldTransaction = getTransactions(type);

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(transactionFiles[type - 1]));

        if (oldTransaction != null) {
            for (int i = 0; i < oldTransaction.length; i++) {
                out.writeObject(oldTransaction[i]);
            }
        }

        out.writeObject(TR);

        Integer num = numTransaction.get(type) + 1;
        numTransaction.put(type, num);
        out.close();
    }

    public static Transaction[] getTransactions(int type) throws FileNotFoundException, IOException, ClassNotFoundException {
        try ( ObjectInputStream stream = new ObjectInputStream(new FileInputStream(transactionFiles[type - 1]))) {
            Transaction[] trs = new Transaction[numTransaction.get(type)];
            for (int i = 0; i < trs.length; i++) {
                trs[i] = (Transaction) stream.readObject();
            }
            return trs;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static Transaction[] getOnlyReadTransactions(int type) throws FileNotFoundException, IOException, ClassNotFoundException {
        ArrayList<Transaction> array = new ArrayList<Transaction>();

        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(transactionFiles[type - 1]));

        while (true) {
            try {
                Transaction tran = (Transaction) stream.readObject();
                array.add(tran);
            } catch (EOFException e) {
                break;
            }
        }
        stream.close();
        return array.toArray(new Transaction[0]);
    }

    public static void createGenesisBlock(Transaction[] type1, Transaction[] type2, Transaction type3) throws IOException, Exception {
        for (int i = 0; i < type1.length; i++) {
            addTransaction(type1[i], 1);
        }
        for (int i = 0; i < type2.length; i++) {
            addTransaction(type2[i], 2);
        }
        addTransaction(type3, 3);
    }
}
