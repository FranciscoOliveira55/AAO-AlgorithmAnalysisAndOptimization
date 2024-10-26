package Classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GreedyAlgorithmSolution {


    private static ProblemScenario problemScenario = null;
    private final static long maxRunDuration = 50000; //milliseconds

    private int[] currentSolutionArray;
    private double currentSolutionCost;

    //Makes an instance with a given initial solution
    public GreedyAlgorithmSolution(int[] initialSolution) {
        this.currentSolutionArray = GreedyAlgorithmSolution.generateSolutionArrayDeepCopy(initialSolution);
        this.calculateTotalCost();
    }

    //Makes a deep copy of the given solution
    public GreedyAlgorithmSolution(GreedyAlgorithmSolution greedyAlgorithmSolutionToDeepCopy) {
        this.currentSolutionArray = GreedyAlgorithmSolution.generateSolutionArrayDeepCopy(greedyAlgorithmSolutionToDeepCopy.getCurrentSolutionArray());
        this.currentSolutionCost = greedyAlgorithmSolutionToDeepCopy.getCurrentSolutionCost();
    }

    /**
     * Generates and returns a deep copy of the provided solution array
     */
    private static int[] generateSolutionArrayDeepCopy(int[] originalSolutionArray) {
        //Make deep copy of the stateArray
        int[] stateArrayDeepCopy = new int[originalSolutionArray.length];
        for (int i = 0; i < originalSolutionArray.length; i++) {
            stateArrayDeepCopy[i] = originalSolutionArray[i];
        }
        return stateArrayDeepCopy;
    }

    public double getCurrentSolutionCost() {
        return currentSolutionCost;
    }

    public int[] getCurrentSolutionArray() {
        return currentSolutionArray;
    }

    //Sets the problem scenario ... needs to be set BEFORE the constructor
    public static void setProblemScenario(ProblemScenario problemScenario) {
        GreedyAlgorithmSolution.problemScenario = problemScenario;
    }

    /**
     * Calculate the total cost value of the current solution
     */
    private void calculateTotalCost() {
        double totalCost = 0;
        //For each warehouse with 1 value in the chromosome, add the fixed cost
        //For each chromosome
        for (int i = 0; i < this.currentSolutionArray.length; i++) {
            //if the chromosome is 1 (warehouse is open)
            if (this.currentSolutionArray[i] == 1) {
                //Add the fixed cost of the warehouse to the total cost
                totalCost += this.problemScenario.getWarehouseLocations()[i].getFixedCost();
                //System.out.println("i: " + i + " cost: " + this.problemScenario.getWarehouseLocations()[i].getFixedCost());
            }
        }
        //System.out.println("TotalFixedCost: " + totalCost);
        //For each customer, add the costOfAllocation of the nearest opened warehouse
        //For each customer
        for (int i = 0; i < this.problemScenario.getCustomers().length; i++) {
            //Get the cost of allocations
            Map<Integer, Double> costOfAllocation = this.problemScenario.getCustomers()[i].getCostOfAllocation();
            // Create a list of the map entries
            List<Map.Entry<Integer, Double>> entries = new ArrayList<>(costOfAllocation.entrySet());

            // Sort the list by values
            entries.sort(Map.Entry.comparingByValue());

            // Iterate over the sorted entries
            for (Map.Entry<Integer, Double> entry : entries) {
                //Logger.WriteMessage("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                // Until we find the first opened warehouse
                if (this.currentSolutionArray[entry.getKey()] == 1) {
                    //If so ... Add the value to the total cost and break the loop
                    totalCost += entry.getValue();
                    break;
                }
            }
        }
        //If all the warehouses are closed (the cost is 0.0, then make the cost a very large number)
        if (totalCost == 0) {
            totalCost = Double.POSITIVE_INFINITY;
        }

        //All done ... set the new cost for the current solution
        this.currentSolutionCost = totalCost;
    }


    public static GreedyAlgorithmSolution run(boolean showProgressMessages) {

        System.out.println("\n#################################");
        System.out.println("### ---- The Greedy One  ---- ###");
        System.out.println("#################################");
        System.out.println("ScenarioName: " + problemScenario.getName() + "\t" + "OptimalSolution: " + problemScenario.getOptimalSolutionValue());
        System.out.println("NumberOfWarehouses: " + problemScenario.getWarehouseLocations().length + "\t" + "NumberOfCustomers: " + problemScenario.getCustomers().length);
        System.out.println("#################################");

        //Save starting time
        long startTime = System.currentTimeMillis();

        //Initialize solution with all warehouses closed (default int is 0)
        GreedyAlgorithmSolution bestSolution = new GreedyAlgorithmSolution(new int[problemScenario.getWarehouseLocations().length]);

        int iterationCounter = 0;
        int improvementCounter;

        //Do while bestSolution != optimal one
        while ((System.currentTimeMillis() - startTime) <= maxRunDuration
                && !(String.format("%.2f", bestSolution.getCurrentSolutionCost())).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue()))
        ) {
            //Store the best improvement difference
            GreedyAlgorithmSolution newBestSolution = new GreedyAlgorithmSolution(bestSolution);
            //Counts the improvements in each for cycle
            improvementCounter = 0;

            //For each position in the array (each warehouse)
            for (int i = 0; i < problemScenario.getWarehouseLocations().length; i++) {
                //Make a solutionArray (deep copy of the best one)
                int[] newSolutionArray = GreedyAlgorithmSolution.generateSolutionArrayDeepCopy(bestSolution.currentSolutionArray);
                //Change the current warehouse to open
                newSolutionArray[i] = 1;
                //And make a solution with it
                GreedyAlgorithmSolution newSolution = new GreedyAlgorithmSolution(newSolutionArray);

                //Now, check if the cost of the new solution is lower than the current new best solution
                if (newSolution.getCurrentSolutionCost() < newBestSolution.getCurrentSolutionCost()) {
                    //If so, make the current solution the new best one (use deep copy)
                    newBestSolution = new GreedyAlgorithmSolution(newSolution);
                    improvementCounter++;
                }
            }

            //If improvements were made, make the best solution the new best one
            if (improvementCounter != 0) {
                bestSolution = new GreedyAlgorithmSolution(newBestSolution);
            } else {
                //Else, break the cycle (no new improvements are possible)
                break;
            }

            //Print Progress every once in a while
            if ((iterationCounter % 1) == 0 && showProgressMessages) {
                System.out.println("\n#################################");
                System.out.println("CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
                System.out.println("BestSolution: " + bestSolution.toString());
                System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/bestSolution.currentSolutionCost)));
                System.out.println("#################################");
            }
            iterationCounter++;
        }

        //All done
        //Print the result and return the best solution

        System.out.println("CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
        System.out.println("BestSolution: " + bestSolution.toString());
        System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/bestSolution.currentSolutionCost)));
        if ((String.format("%.2f", bestSolution.currentSolutionCost)).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue())))
            System.out.println("U GOT THE OPTIMAL SOLUTION!");
        System.out.println("#################################");

        return bestSolution;
    }

    @Override
    public String toString() {
        return "GreedyAlgorithmSolution{" +
                "currentSolutionCost=" + String.format("%.3f", currentSolutionCost) +
                ", currentSolutionArray=" + Arrays.toString(currentSolutionArray) +
                '}';
    }
}
