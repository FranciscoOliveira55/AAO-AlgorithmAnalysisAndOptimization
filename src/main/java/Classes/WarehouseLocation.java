package Classes;

public class WarehouseLocation {
    private int i;
    private int capacity;
    private double fixedCost;

    public WarehouseLocation(int i, int capacity, double fixedCost) {
        this.i = i;
        this.capacity = capacity;
        this.fixedCost = fixedCost;
    }

    //Method to make a deep copy of the given warehouseLocation
    public WarehouseLocation(WarehouseLocation warehouseLocationToDeepCopy) {
        this.i = warehouseLocationToDeepCopy.getI();
        this.capacity = warehouseLocationToDeepCopy.getCapacity();
        this.fixedCost = warehouseLocationToDeepCopy.getFixedCost();
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getFixedCost() {
        return fixedCost;
    }

    public void setFixedCost(double fixedCost) {
        this.fixedCost = fixedCost;
    }

    @Override
    public String toString() {
        return "WarehouseLocation{" +
                "i=" + i +
                ", capacity=" + capacity +
                ", fixedCost=" + fixedCost +
                '}';
    }
}
