public class Main {

    //source path
    private static final String sourceFile = "resource\\test.csv";
    //result path
    private static final String resultFile = "output\\Result.csv";

    public static void main(String[] args) {
        DiscreteAuction auction = new DiscreteAuction(sourceFile);
        auction.perform(resultFile);
    }
}
