package utils;

import domain.Order;
import domain.Direction;
import domain.Transaction;
import domain.Type;

import java.io.*;
import java.util.List;

/**
 * csv reader/writer
 */
public abstract class CSVProcessor {

    private final String cvsSplitBy = ";";  // semicolon as separator

    /**
     * parse csv file
     *
     * @param csvFile String path
     */
    public void parse(String csvFile) {
        String line;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                //separate
                String[] values = line.split(cvsSplitBy);
                //for incorrect data
                try {
                    //fill order object
                    Order order = fillBid(values);
                    onElementParse(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close reader
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * write to the csv file
     *
     * @param csvFile      csvFile path
     * @param marketPrice  Integer
     * @param marketVolume long
     * @param transactions List<Transaction>
     */
    public void write(String csvFile, Integer marketPrice, long marketVolume, List<Transaction> transactions) {
        try {
            FileWriter writer = new FileWriter(csvFile);
            if (transactions.isEmpty()) {
                writer.append("FAILED");
            } else {
                writer.append("OK");
                writer.append(cvsSplitBy);
                writer.append(String.valueOf(marketPrice));
                writer.append(cvsSplitBy);
                writer.append(String.valueOf(marketVolume));
                writer.append('\n');
                for (Transaction transaction : transactions) {
                    writer.append(String.valueOf(transaction.getNumberBuy()));
                    writer.append(cvsSplitBy);
                    writer.append(String.valueOf(transaction.getNumberSell()));
                    writer.append(cvsSplitBy);
                    writer.append(String.valueOf(transaction.getVolume()));
                    writer.append(cvsSplitBy);
                    writer.append(String.valueOf(transaction.getPriceVolume()));
                    writer.append('\n');
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write result error
     *
     * @param csvFile  String path
     */
    public void error(String csvFile) {
        try {
            FileWriter writer = new FileWriter(csvFile);
            writer.append("FAILED");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * fill order object
     *
     * @param values String[]
     * @return Order
     */
    private Order fillBid(String[] values) {
        Order order = new Order();
        order.setNumber(Long.parseLong(values[0]));
        order.setDirection(Direction.valueOf(values[1]));
        order.setType(Type.valueOf(values[2]));
        order.setVolume(Integer.parseInt(values[3]));
        if (order.getType() == Type.L)
            order.setPrice(Integer.parseInt(values[4]));
        return order;
    }

    /**
     * parse logic for each element
     *
     * @param order Order
     */
    public abstract void onElementParse(Order order);
}
