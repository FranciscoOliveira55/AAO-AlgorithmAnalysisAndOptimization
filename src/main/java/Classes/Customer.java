package Classes;

import java.util.HashMap;
import java.util.Map;

public class Customer {
    private int j;
    private int demand;
    private Map<Integer, Double> costOfAllocation; // i from warehouse, cost

    public Customer(int j, int demand) {
        this.j = j;
        this.demand = demand;
        this.costOfAllocation = new HashMap<Integer, Double>();
    }

    public Customer(int j, int demand, Map<Integer, Double> costOfAllocation) {
        this.j = j;
        this.demand = demand;
        this.costOfAllocation = new HashMap<Integer, Double>();
        this.costOfAllocation.putAll(costOfAllocation);
    }

    //Constructor to make deep copy
    public Customer(Customer customerToDeepCopy) {
        this.j = customerToDeepCopy.j;
        this.demand = customerToDeepCopy.demand;
        //Make deep copy of map
        this.costOfAllocation = new HashMap<Integer, Double>();
        this.costOfAllocation.putAll(customerToDeepCopy.getCostOfAllocation());
    }


    /**
     * Adds a costOfAllocation between the customer and a warehouseLocation
     * @param WarehouseLocationI
     * @param costOfAllocation
     */
    public void addCostOfAllocation(int WarehouseLocationI, double costOfAllocation) {
        this.costOfAllocation.put(WarehouseLocationI, costOfAllocation);
    }


    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public Map<Integer, Double> getCostOfAllocation() {
        return costOfAllocation;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "j=" + j +
                ", demand=" + demand +
                ", costOfAllocation=" + costOfAllocation.toString() +
                '}';
    }
}
