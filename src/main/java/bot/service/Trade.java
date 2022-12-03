package bot.service;

public interface Trade {

    void open();

    void close();

    void sendOrder(String positionQuantity);

    void updateFunding();

    double availableQuantity();

}
