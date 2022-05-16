
package ordersapp;

import java.util.Date;

public class Order {

    private long orderUID;
    private long customerUID;
    private Date createDate;
    private String address;


    public Order() {

    }

    public Order(long orderUID, long customerUID, Date createDate, String address) {
        this.orderUID = orderUID;
        this.customerUID = customerUID;
        this.createDate = createDate;
        this.address = address;
    }

    public long getOrderUID() {
        return this.orderUID;
    }


    public long getCustomerUID() {
        return this.customerUID;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public String getAddress() {
        return this.address;
    }

}