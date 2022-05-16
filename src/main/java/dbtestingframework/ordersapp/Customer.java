
package ordersapp;

import java.util.ArrayList;

public class Customer{

    private long uid;
    private String name;
    private ArrayList<String> addresses;


    public Customer(){

    }

    public Customer(long uid, String name){

        this.uid = uid;
        this.name = name;
        this.addresses = new ArrayList<String>();

    }

    public Customer(long uid, String name, ArrayList<String> addresses){
        this(uid, name);
        this.addresses = addresses;
    }

    public long getUID(){
        return this.uid;
    }

    public void setUID(long uid){
        this.uid = uid;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public ArrayList<String> getAddresses(){
        return this.addresses;
    }

    public void addAddress(String address){
        this.addresses.add(address);
    }




}