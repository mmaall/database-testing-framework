
package ordersapp;

import java.util.Date;

public class Item {

    private long itemUID;
    private long orderUID;
    private long customerUID;
    private int quantity;
    private Date orderDate;

    public Item() {

    }

    public Item(long itemUID, long orderUID, long customerUID, int quantity, Date orderDate) {
        this.itemUID = itemUID;
        this.orderUID = orderUID;
        this.customerUID = customerUID;
        this.quantity = quantity;
        this.orderDate = orderDate;
    }

    public long getItemUID() {
        return this.itemUID;
    }

    public long getOrderUID() {
        return this.orderUID;
    }

    public long getCustomerUID() {
        return this.customerUID;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public Date getOrderDate() {
        return this.orderDate;
    }
}