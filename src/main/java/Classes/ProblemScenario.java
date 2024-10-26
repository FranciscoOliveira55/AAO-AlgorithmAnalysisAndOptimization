package Classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ProblemScenario {

    private String name;
    private WarehouseLocation[] warehouseLocations;
    private Customer[] customers;
    private double optimalSolutionValue;

    //Constructor to make deep copy
    public ProblemScenario(ProblemScenario problemScenarioToDeepCopy) {
        this.name = problemScenarioToDeepCopy.getName();
        //Make deep copy of warehouseLocations
        this.warehouseLocations = new WarehouseLocation[problemScenarioToDeepCopy.getWarehouseLocations().length];
        for (int i = 0; i < problemScenarioToDeepCopy.getWarehouseLocations().length; i++) {
            this.warehouseLocations[i] = new WarehouseLocation(problemScenarioToDeepCopy.getWarehouseLocations()[i]);
        }
        //Make deep copy of customers
        this.customers = new Customer[problemScenarioToDeepCopy.getCustomers().length];
        for (int i = 0; i < problemScenarioToDeepCopy.getCustomers().length; i++) {
            this.customers[i] = new Customer(problemScenarioToDeepCopy.getCustomers()[i]);
        }
    }

    public ProblemScenario() {
    }

    public ProblemScenario(String scenarioFileName, double optimalSolutionValue) {
        this.name = scenarioFileName;
        this.optimalSolutionValue = optimalSolutionValue;
    }


    /**
     * Adds a location to the warehouseLocations array
     *
     * @param warehouseLocationI
     * @param warehouseLocation
     */
    public void addWarehouseLocation(int warehouseLocationI, WarehouseLocation warehouseLocation) {
        this.warehouseLocations[warehouseLocationI] = warehouseLocation;
    }

    /**
     * Adds a customer to the customers array
     *
     * @param customerJ
     * @param customer
     */
    public void addCustomer(int customerJ, Customer customer) {
        this.customers[customerJ] = customer;
    }

    public String getName() {
        return name;
    }

    public double getOptimalSolutionValue() {
        return optimalSolutionValue;
    }

    public WarehouseLocation[] getWarehouseLocations() {
        return warehouseLocations;
    }

    public Customer[] getCustomers() {
        return customers;
    }

    /**
     * I want to read all the data about this scenario from a file
     */
    public void ReadScenarioFromFile(Path problemScenarioFilePath) {
        System.out.println("Reading: " + problemScenarioFilePath);
        try {
            // Read all lines from the file
            List<String> scenarioData = Files.readAllLines(problemScenarioFilePath)
                    .stream()
                    .filter(line -> !line.trim().isEmpty())  // Filters out empty or whitespace-only lines
                    .collect(Collectors.toList());
            ;
            for (int i = 0; i < scenarioData.size(); i++) {
                //Remove white spaces in the start and end of the line
                String line = scenarioData.get(i).trim();
                //Remove all the points (.) in the end of the line
                scenarioData.set(i, line.replaceAll("\\.$", ""));
            }

            int numberOfWarehouseLocations = -1;
            int numberOfCustomers = -1;
            int currentCustomerIndex = -1;
            int currentWarehouseIndex = -1;

            // For each line in the scenario file
            for (int i = 0; i < scenarioData.size(); i++) {

                switch (i) {
                    //If we are in line 0, get the number of warehouses and clients
                    case 0: {
                        //Get the number of warehouses and customers from splitting the line
                        String[] lineParts = scenarioData.get(i).trim().split(" ");
                        numberOfWarehouseLocations = Integer.parseInt(lineParts[0]);
                        numberOfCustomers = Integer.parseInt(lineParts[lineParts.length - 1]);
                        //Use those numbers to update the size of the arrays
                        warehouseLocations = new WarehouseLocation[numberOfWarehouseLocations];
                        customers = new Customer[numberOfCustomers];
                        //Logger.WriteMessage("NumberOfWarehouseLocations: " + numberOfWarehouseLocations + "\t" + "NumberOfCostumers: " + numberOfCustomers);
                        break;
                    }
                    default: {
                        //If we are reading the lines 1-numberOfWarehouseLocations, get the info about warehouses
                        if (i >= 1 && i <= numberOfWarehouseLocations) {
                            //removes white spaces
                            String[] lineParts = scenarioData.get(i).trim().split("\\s+");
                            int warehouseCapacity = Integer.parseInt(lineParts[0]);
                            double warehouseFixedCost = Double.parseDouble(lineParts[lineParts.length - 1]);
                            //Store warehouse
                            WarehouseLocation newWarehouseLocation = new WarehouseLocation(i - 1, warehouseCapacity, warehouseFixedCost);
                            this.warehouseLocations[i - 1] = newWarehouseLocation;
                            Logger.WriteMessage(newWarehouseLocation.toString());
                            break;
                        } else {
                            //Get info about costumers
                            // Trim the line and split by spaces
                            String[] parts = scenarioData.get(i).trim().split("\\s+");
                            // Check if the first part is an integer (likely demand)
                            try {
                                int demand = Integer.parseInt(parts[0]);
                                //If so, create a new customer
                                currentCustomerIndex++;
                                currentWarehouseIndex = -1;
                                Customer newCustomer = new Customer(currentCustomerIndex, demand);
                                //Store it in the array
                                customers[currentCustomerIndex] = newCustomer;
                                Logger.WriteMessage(newCustomer.toString());
                            } catch (NumberFormatException e) {
                                // Not an integer, consider it as a cost line
                                for (String cost : parts) {
                                    currentWarehouseIndex++;

                                    customers[currentCustomerIndex].addCostOfAllocation(currentWarehouseIndex, Double.parseDouble(cost));
                                    //Logger.WriteMessage("warehouseLocationI: " + currentWarehouseIndex + "\t" + "costOfAllocation: " + cost);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
