import domain.Direction;
import domain.Order;
import domain.Transaction;
import domain.Type;
import utils.CSVProcessor;

import java.util.*;

/**
 * discrete auction
 */
public class DiscreteAuction {

    private String sourceFile;
    //volume of market sell
    private Integer sellMarketVolume = 0;
    //volume of market buy
    private Integer buyMarketVolume = 0;
    //sorted by price map of price volume of buy bids
    private TreeMap<Integer, Integer> buyPriceMap = new TreeMap<Integer, Integer>();
    //sorted by price map of price volume of sell bids
    private TreeMap<Integer, Integer> sellPriceMap = new TreeMap<Integer, Integer>();
    //market price
    private int marketPrice;
    // market volume
    private long marketVolume;
    //sorted set for buy bits
    private TreeSet<Order> buyOrders = new TreeSet<Order>(getBuyComparator());
    //sorted set for sell bits
    private TreeSet<Order> sellOrders = new TreeSet<Order>(getSellComparator());
    //possible to do auction
    private boolean validCSV;

    private int minSellPrice;
    private int maxBuyPrice;

    public DiscreteAuction(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void perform(String resultFile) {
        CSVProcessor csvProcessor = new CSVProcessor() {
            @Override
            public void onElementParse(Order bid) {
                if (bid.getDirection() == Direction.S) {
                    sellOrders.add(bid);
                    if (bid.getType() == Type.M) {
                        sellMarketVolume += bid.getVolume();
                    } else {
                        validCSV = true;
                        if (minSellPrice > bid.getPrice()||minSellPrice==0) {
                            minSellPrice = bid.getPrice();
                        }
                        addToPriceMap(sellPriceMap, bid);
                    }
                } else {
                    buyOrders.add(bid);
                    if (bid.getType() == Type.M) {
                        buyMarketVolume += bid.getVolume();
                    } else {
                        validCSV = true;
                        if (maxBuyPrice < bid.getPrice()||maxBuyPrice==0) {
                            maxBuyPrice = bid.getPrice();
                        }
                        addToPriceMap(buyPriceMap, bid);
                    }
                }

            }
        };
        csvProcessor.parse(sourceFile);
        validatePrices();
        if (validCSV) {
            calculateMarketPrice();
            csvProcessor.write(resultFile, marketPrice, marketVolume, auction());
        } else {
            csvProcessor.error(resultFile);
        }
    }


    /**
     * get comparator sorted by type (market, limit) of the bid, ascending price and ascending number
     *
     * @return Comparator<Order>
     */
    private Comparator<Order> getSellComparator() {
        return new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                // market then limit
                int i = -o1.getType().compareTo(o2.getType());
                if (i != 0) return i;
                //ascending price
                if (o2.getPrice() != null && o1.getPrice() != null) {
                    i = o1.getPrice() - o2.getPrice();
                    if (i != 0) return i;
                }
                //ascending number
                return o1.getNumber().compareTo(o2.getNumber());
            }
        };
    }

    /**
     * get comparator sorted by type (market, limit) of the bid, descending price and ascending number
     *
     * @return Comparator<Order>
     */
    private Comparator<Order> getBuyComparator() {
        return new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                // market then limit
                int i = -o1.getType().compareTo(o2.getType());
                if (i != 0) return i;
                //descending price
                if (o2.getPrice() != null && o1.getPrice() != null) {
                    i = o2.getPrice() - o1.getPrice();
                    if (i != 0) return i;
                }
                //ascending number
                return o1.getNumber().compareTo(o2.getNumber());
            }
        };
    }

    /**
     * add volume of the order to the price map
     *
     * @param map   TreeMap<Integer, Integer>
     * @param order Order
     */
    private void addToPriceMap(TreeMap<Integer, Integer> map, Order order) {
        Integer mapItem = map.get(order.getPrice());
        if (mapItem != null) {
            map.put(order.getPrice(), order.getVolume() + mapItem);
        } else {
            map.put(order.getPrice(), order.getVolume());
        }
    }

    /**
     * calculate market price
     *
     * @return int market price
     */
    private int calculateMarketPrice() {
        //marketPrice

        Set<Integer> priceSet = new HashSet<Integer>();
        //add all prices as points of extrema
        priceSet.addAll(sellPriceMap.keySet());
        priceSet.addAll(buyPriceMap.keySet());
        //for each price
        for (Integer price : priceSet) {
            int sellVolume = sellMarketVolume;
            for (Integer sale : sellPriceMap.tailMap(price, true).values()) {
                sellVolume += sale;
            }
            int buyVolume = buyMarketVolume;
            for (Integer buy : buyPriceMap.headMap(price, true).values()) {
                buyVolume += buy;
            }
            long volume = price * (sellVolume > buyVolume ? buyVolume : sellVolume);
            if (marketVolume < volume) {
                marketVolume = volume;
                marketPrice = price;
            }
        }
        return marketPrice;
    }

    /**
     * perform auction
     *
     * @return List<Transaction>
     */
    private List<Transaction> auction() {
        List<Transaction> transactions = new ArrayList<Transaction>();
        while (true) {
            Order buyOrder = buyOrders.first();
            Order sellOrder = sellOrders.first();
            if (buyOrder == null || sellOrder == null || (buyOrder.getPrice() != null && buyOrder.getPrice() > marketPrice) || (sellOrder.getPrice() != null && sellOrder.getPrice() < marketPrice)) {
                break;
            }
            Transaction transaction = new Transaction();
            transaction.setNumberBuy(buyOrder.getNumber());
            transaction.setNumberSell(sellOrder.getNumber());
            if (buyOrder.getVolume() > sellOrder.getVolume()) {
                buyOrder.setVolume(buyOrder.getVolume() - sellOrder.getVolume());
                transaction.setVolume(sellOrders.pollFirst().getVolume());
            } else {
                sellOrder.setVolume(sellOrder.getVolume() - buyOrder.getVolume());
                transaction.setVolume(buyOrders.pollFirst().getVolume());
            }
            transaction.setPriceVolume(marketPrice * transaction.getVolume());
            transactions.add(transaction);
        }
        return transactions;
    }

    /**
     * validate if minn price to buy less then max price ti sell
     */
    private void validatePrices() {
        if (minSellPrice > maxBuyPrice) {
            validCSV = false;
        }
    }

}
