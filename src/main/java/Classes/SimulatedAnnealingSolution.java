package Classes;

import java.util.*;

public class SimulatedAnnealingSolution {

    private final static double initialTemperature = 100000;
    private final static double coolingRate = 0.995;
    private final static long maxRunDuration = 50000; //milliseconds
    private final static double chanceOfIndividualPointStateChange = 0.20;

    private static ProblemScenario problemScenario = null;

    private double currentTemperature;
    private int[] currentStateArray;
    private double currentSolutionCost;

    //Makes an instance with a random  initial solution
    public SimulatedAnnealingSolution() {
        this.currentStateArray = SimulatedAnnealingSolution.generateRandomStateArray();
        this.currentTemperature = initialTemperature;
        this.calculateTotalCost();
    }

    //Makes an instance with a given initial solution
    public SimulatedAnnealingSolution(int[] initialSolution) {
        this.currentStateArray = SimulatedAnnealingSolution.generateCurrentStateArrayDeepCopy(initialSolution);
        this.currentTemperature = initialTemperature;
        this.calculateTotalCost();
    }

    //Makes a deep copy of the given solution
    public SimulatedAnnealingSolution(SimulatedAnnealingSolution simulatedAnnealingSolutionToDeepCopy) {
        this.currentStateArray = SimulatedAnnealingSolution.generateCurrentStateArrayDeepCopy(
            simulatedAnnealingSolutionToDeepCopy.getCurrentSolutionArray());
        this.currentSolutionCost = simulatedAnnealingSolutionToDeepCopy.getCurrentSolutionCost();
        this.currentTemperature = simulatedAnnealingSolutionToDeepCopy.getCurrentTemperature();
    }

    /**
     * Generates and returns a deep copy of the provided solution
     */
    private static int[] generateCurrentStateArrayDeepCopy(int[] originalStateArray) {
        //Make deep copy of the stateArray
        int[] stateArrayDeepCopy = new int[originalStateArray.length];
        for (int i = 0; i < originalStateArray.length; i++) {
            stateArrayDeepCopy[i] = originalStateArray[i];
        }
        return stateArrayDeepCopy;
    }

    //Sets the problem scenario ... needs to be set BEFORE the constructor
    public static void setProblemScenario(ProblemScenario problemScenario) {
        SimulatedAnnealingSolution.problemScenario = problemScenario;
    }

    public int[] getCurrentSolutionArray() {
        return currentStateArray;
    }

    public double getCurrentSolutionCost() {
        return currentSolutionCost;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }


    /**
     * Generates and returns a random StateArray
     */
    private static int[] generateRandomStateArray() {
        Random rand = new Random();
        //Creates a list to store the generated genes
        int[] generatedStateArray = new int[problemScenario.getWarehouseLocations().length];

        for (int i = 0; i < generatedStateArray.length; i++)
            generatedStateArray[i] = rand.nextInt(0, 2);
        //Returns generated StateArray
        return generatedStateArray;
    }

    /**
     * Calculate the total cost value of the current solution
     */
    private void calculateTotalCost() {
        double totalCost = 0;
        //For each warehouse with 1 value in the chromosome, add the fixed cost
        //For each chromosome
        for (int i = 0; i < this.currentStateArray.length; i++) {
            //if the chromosome is 1 (warehouse is open)
            if (this.currentStateArray[i] == 1) {
                //Add the fixed cost of the warehouse to the total cost
                totalCost += this.problemScenario.getWarehouseLocations()[i].getFixedCost();
            }
        }

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
                if (this.currentStateArray[entry.getKey()] == 1) {
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

    /**
     * Based on a eventSuccessChance, returns true of false if the event succeeds or not
     */
    private static boolean chanceEventRoulette(double eventSuccessChance) throws Exception {
        if (eventSuccessChance < 0 || eventSuccessChance > 1)
            throw new Exception("eventSuccessChance must be between 0 and 1");
        if (eventSuccessChance == 1)
            return true;
        //Makes random number between 0 (inclusive) and 101(exclusive)
        int randomNumber = (new Random()).nextInt(0, 101);
        //Gets eventSuccessChance in % form
        int eventSuccessChanceNumber = (int) (eventSuccessChance * 100);
        //if eventSuccessChanceNumber >= randomNumber it means that the event had success, otherwise it failed
        return (eventSuccessChanceNumber >= randomNumber);
    }

    /**
     * Change a IndividualPointState
     */
    private static int changeIndividualPointState(int originalIndividualPointState) {

        //Simple state change ... if 0 becomes 1, if 1 becomes 0
        if (originalIndividualPointState == 0) {
            return 1;
        } else {
            return 0;
        }
    }


    /**
     * Changes a stateArray
     */
    private static int[] changeStateArray(
            int[] originalStateArray,
            double chanceOfIndividualStateChange) throws Exception {
        Random rand = new Random();
        //Creates an array to store the changedStateArray
        int[] changedStateArray = new int[originalStateArray.length];

        //For each IndividualStatePoint in the originalStateArray
        for (int i = 0; i < originalStateArray.length; i++) {
            int changedIndividualPointState = originalStateArray[i];
            //If given the chance of individualStatePointChange it changes
            if (SimulatedAnnealingSolution.chanceEventRoulette(chanceOfIndividualStateChange)) {
                changedIndividualPointState = SimulatedAnnealingSolution.changeIndividualPointState(changedIndividualPointState);
                //With the change, stores the changed individualStatePoint in the changedStateArray
                changedStateArray[i] = changedIndividualPointState;
            } else {
                //If not stores the individualStatePoint without change
                changedStateArray[i] = changedIndividualPointState;
            }
        }
        //With all the changes done, returns the changedStateArray
        return changedStateArray;
    }

    /**
     * Changes a Solution
     */
    private void changeSolution(double chanceOfIndividualStateChange) throws Exception {
        //Changes the currentStateArray
        this.currentStateArray = SimulatedAnnealingSolution.changeStateArray(this.currentStateArray, chanceOfIndividualStateChange);
        //Updates the cost
        this.calculateTotalCost();
    }

    /**
     * Runs the simulation and finds the optimal solution (or a good one )
     * Can't be static because we use the "initial solution" that may be random or not ...
     */
    public SimulatedAnnealingSolution run(boolean showProgressMessages) throws Exception {

        System.out.println("\n#################################");
        System.out.println("### -- The Great Annealing -- ###");
        System.out.println("#################################");
        System.out.println("ScenarioName: " + problemScenario.getName() + "\t" + "OptimalSolution: " + problemScenario.getOptimalSolutionValue());
        System.out.println("NumberOfWarehouses: " + problemScenario.getWarehouseLocations().length + "\t" + "NumberOfCustomers: " + problemScenario.getCustomers().length);
        System.out.println("#################################");


        //Saves stating time and creates random instance
        long startTime = System.currentTimeMillis();
        Random rand = new Random();

        //Variable to store the best solution
        SimulatedAnnealingSolution bestStateSolution = new SimulatedAnnealingSolution(this.currentStateArray);

        //Iteration counter
        int iterationCounter = 0;

        //Runs simulation while temperature > 1 && time < maxRunDuration && current solution != optimal one
        while (currentTemperature > 1 && (System.currentTimeMillis() - startTime) <= maxRunDuration
                && !(String.format("%.2f", currentSolutionCost)).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue()))
        ) {

            //Create nextState solution from the current one
            SimulatedAnnealingSolution nextStateSolution = new SimulatedAnnealingSolution(this.currentStateArray);
            //Change it
            nextStateSolution.changeSolution(chanceOfIndividualPointStateChange);

            //Calculate costDifferences (negative value is GOOD (the changed state has a cost that is LOWER than the current one))
            double costDifference = nextStateSolution.getCurrentSolutionCost() - this.currentSolutionCost;

            //If the changed cost is lower, or if the worse cost is "acceptable" ... then make it the current one
            if (costDifference < 0 || Math.exp(-costDifference / currentTemperature) > rand.nextDouble()) {
                //Make the current solution the changed one (use deep copy)
                this.currentStateArray = SimulatedAnnealingSolution.generateCurrentStateArrayDeepCopy(nextStateSolution.getCurrentSolutionArray());
                this.calculateTotalCost();

                //If it is a new best solution
                if (this.currentSolutionCost < bestStateSolution.getCurrentSolutionCost()) {
                    //Then make the best solution, the current one
                    bestStateSolution = new SimulatedAnnealingSolution(this.currentStateArray);
                }
            }

            //Print Progress every once in a while
            if ((iterationCounter % 40) == 0 && showProgressMessages) {
                System.out.println("\n#################################");
                System.out.println("CurrentTemperature: " + String.format("%.3f", this.currentTemperature) + "\t" + "CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
                System.out.println("BestSolution: " + bestStateSolution.toString());
                System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/bestStateSolution.currentSolutionCost)));
                System.out.println("#################################");
            }

            //Decrease temperature //Increase counter
            iterationCounter++;
            this.currentTemperature *= coolingRate;
        }
        //returns Best Solution

        System.out.println("CurrentTemperature: " + String.format("%.3f", this.currentTemperature) + "\t" + "CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
        System.out.println("BestSolution: " + bestStateSolution.toString());
        System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/bestStateSolution.currentSolutionCost)));
        if ((String.format("%.2f", bestStateSolution.currentSolutionCost)).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue())))
            System.out.println("U GOT THE OPTIMAL SOLUTION!");
        System.out.println("#################################");

        return bestStateSolution;
    }


    @Override
    public String toString() {
        return "SimulatedAnnealingAlgorithm{" +
                "currentSolutionCost=" + String.format("%.3f", currentSolutionCost) +
                //", currentTemperature=" + currentTemperature +
                ", currentSolutionArray=" + Arrays.toString(currentStateArray) +
                '}';
    }
}
