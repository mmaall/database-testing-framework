
package orm;

public class Customer{

    private long uid;
    private String name;
    private String address;
    private int age;


    public Customer(){

    }

    public Customer(long uid, String name, String address, int age){

        this.uid = uid;
        this.name = name;
        this.address = address;
        this.age = age; 

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

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){
        this.address = address;
    }
    public int getAge(){
        return this.age;
    }

    public void setAge(int age){
        this.age = age;
    }




}