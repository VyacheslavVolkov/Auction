package domain;

/**
 * represents one transaction
 */
public class Transaction {

    private long numberBuy; //number of buy order
    private long numberSell; // number of sell order
    private int volume;  // volume of transaxtion
    private int priceVolume; // volume*market price (price of transaction)

    public long getNumberBuy() {
        return numberBuy;
    }

    public void setNumberBuy(long numberBuy) {
        this.numberBuy = numberBuy;
    }

    public long getNumberSell() {
        return numberSell;
    }

    public void setNumberSell(long numberSell) {
        this.numberSell = numberSell;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getPriceVolume() {
        return priceVolume;
    }

    public void setPriceVolume(int priceVolume) {
        this.priceVolume = priceVolume;
    }
}
