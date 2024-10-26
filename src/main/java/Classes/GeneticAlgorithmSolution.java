package Classes;

import com.sun.net.httpserver.Authenticator;

import java.text.DecimalFormat;
import java.util.*;

public class GeneticAlgorithmSolution implements Comparable<GeneticAlgorithmSolution> {

    private final static int numberOfSolutionsPerGeneration = 100;
    private final static int xNumberOfBestSolutionsForSelectionFunction = 40; //Even Number if possible

    private final static int xPopulationSizeGoalForCrossoverFunction = 100;
    private final static int yNumberOfEliteNonCrossedSolutions = 6; //Even Number if possible

    private final static int xPopulationSizeGoalForMutationFunction = 120;
    private final static int yNumberOfEliteNonMutatedSolutions = 6;
    private final static double chanceOfSolutionMutation = 0.60;
    private final static double chanceOfGeneMutation = 0.20;

    private final static int maxNumberOfGenerations = 200;
    private final static long maxRunEvolutionDuration = 50000; //milliseconds


    private static ProblemScenario problemScenario = null;

    private int[] chromosome;
    private double currentSolutionCost;

    /**
     * Makes a solution with the given chromosome
     * Also calculates the total cost of the created solution
     */
    public GeneticAlgorithmSolution(int[] chromosome) {
        this.chromosome = GeneticAlgorithmSolution.generateChromosomeDeepCopy(chromosome);
        this.calculateTotalCost();
    }

    /**
     * Makes a deep copy with the given solution
     */
    public GeneticAlgorithmSolution(GeneticAlgorithmSolution geneticAlgorithmSolutionToDeepCopy) {
        this.chromosome = GeneticAlgorithmSolution.generateChromosomeDeepCopy(geneticAlgorithmSolutionToDeepCopy.getChromosome());
        this.currentSolutionCost = geneticAlgorithmSolutionToDeepCopy.getCurrentSolutionCost();
    }

    //Sets the problem scenario ... needs to be set BEFORE the constructor
    public static void setProblemScenario(ProblemScenario problemScenario) {
        GeneticAlgorithmSolution.problemScenario = problemScenario;
    }

    public int[] getChromosome() {
        return chromosome;
    }

    public double getCurrentSolutionCost() {
        return currentSolutionCost;
    }

    /**
     * Generates and returns a deep copy of the provided Chromosome
     */
    private static int[] generateChromosomeDeepCopy(int[] originalChromosome) {
        //Make deep copy of the chromosome
        int[] chromosomeDeepCopy = new int[originalChromosome.length];
        for (int i = 0; i < originalChromosome.length; i++) {
            chromosomeDeepCopy[i] = originalChromosome[i];
        }
        return chromosomeDeepCopy;
    }


    /**
     * Generates and returns a random chromosome
     */
    private static int[] generateRandomChromosome() {
        Random rand = new Random();
        //Creates a list to store the generated genes
        int[] generatedChromosome = new int[problemScenario.getWarehouseLocations().length];

        for (int i = 0; i < generatedChromosome.length; i++)
            generatedChromosome[i] = rand.nextInt(0, 2);
        //Returns generated chromosome
        return generatedChromosome;
    }

    /**
     * Generates and returns a random solution
     */
    private static GeneticAlgorithmSolution generateRandomSolution() {
        //Creates a solution with a random chromosome
        return new GeneticAlgorithmSolution(GeneticAlgorithmSolution.generateRandomChromosome());
    }

    /**
     * Generates and returns a list with X number of random solutions
     */
    private static List<GeneticAlgorithmSolution> generateXRandomSolutions(int numberOfRandomSolutions) {
        //Creates a list to store the generated solutions
        List<GeneticAlgorithmSolution> generatedSolutions = new ArrayList<>();
        //Generates random solutions and stores them in the list
        for (int i = 0; i < numberOfRandomSolutions; i++)
            generatedSolutions.add(GeneticAlgorithmSolution.generateRandomSolution());
        //Sorts the generated Solutions
        generatedSolutions.sort(Comparator.naturalOrder());
        //Returns the generated solutions
        return generatedSolutions;
    }

    /**
     * Calculate the total cost value of the current solution
     */
    private void calculateTotalCost() {
        double totalCost = 0;
        //For each warehouse with 1 value in the chromosome, add the fixed cost
        //For each chromosome
        for (int i = 0; i < this.chromosome.length; i++) {
            //if the chromosome is 1 (warehouse is open)
            if (this.chromosome[i] == 1) {
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
                if (this.chromosome[entry.getKey()] == 1) {
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
     * Calculates the total cost value of each solution in a list
     */
    private static void calculateTotalCosts(List<GeneticAlgorithmSolution> solutionsList) {
        //For each solution in the solutionsList, it calculates the total cost
        for (GeneticAlgorithmSolution solution : solutionsList)
            solution.calculateTotalCost();
    }

    /**
     * Selects the X best solutions and returns a list with them
     */
    private static List<GeneticAlgorithmSolution> selectionFunctionXBest(
        List<GeneticAlgorithmSolution> solutionsList, 
        int xNumberOfBestSolutions
        ) {
        //Creates a list to store the xBest solutions
        List<GeneticAlgorithmSolution> xBestSolutions = new ArrayList<>();
        //Sorts the solutionsList so that the best ones come first
        solutionsList.sort(Comparator.naturalOrder());
        //Adds the X first (and therefore best) solutions to the storage list
        for (int i = 0; i < xNumberOfBestSolutions; i++)
            xBestSolutions.add(solutionsList.get(i));
        //Returns array with X best solutions
        return xBestSolutions;
    }


    /**
     * Given an array with 2 initial solutions, returns an array with the 2 resulting solutions from crossing of the 2 initial ones
     */
    private static GeneticAlgorithmSolution[] crossoverFunction(GeneticAlgorithmSolution initial2SolutionFirst, GeneticAlgorithmSolution initial2SolutionSecond) {
        //Get initial chromosomes
        int[] initialChromosome1 = initial2SolutionFirst.getChromosome();
        int[] initialChromosome2 = initial2SolutionSecond.getChromosome();
        //Get the random part where u will part them (from position 1 to the one before the last)
        int randomPartPosition = (new Random()).nextInt(1, initialChromosome1.length - 1);

        //Build the new crossed chromosomes
        int[] crossedChromosome1 = new int[initialChromosome1.length];
        int[] crossedChromosome2 = new int[initialChromosome1.length];

        // Copy the first part from each parent into the new chromosomes
        System.arraycopy(initialChromosome1, 0, crossedChromosome1, 0, randomPartPosition);
        System.arraycopy(initialChromosome2, 0, crossedChromosome2, 0, randomPartPosition);

        // Copy the second part from the other parent into the new chromosomes
        System.arraycopy(initialChromosome2, randomPartPosition, crossedChromosome1, randomPartPosition, initialChromosome1.length - randomPartPosition);
        System.arraycopy(initialChromosome1, randomPartPosition, crossedChromosome2, randomPartPosition, initialChromosome2.length - randomPartPosition);

        //Build new SolutionArray
        GeneticAlgorithmSolution[] crossed2SolutionArray = new GeneticAlgorithmSolution[2];
        crossed2SolutionArray[0] = new GeneticAlgorithmSolution(crossedChromosome1);
        crossed2SolutionArray[1] = new GeneticAlgorithmSolution(crossedChromosome2);

        //All done, return
        return crossed2SolutionArray;
    }

    /**
     * Crossover the existing solutions into a total of X population
     * Elites are the Y number of solutions that we keep unchanged
     */
    private static List<GeneticAlgorithmSolution> crossoverFunctionIntoXPopulation(
            List<GeneticAlgorithmSolution> originalSolutionsList,
            int xPopulationSizeGoal,
            int yNumberOfEliteUnchangedSolutions) {


        Random rand = new Random();

        //Creates a list to store the crossed solutions
        List<GeneticAlgorithmSolution> crossedSolutions = new ArrayList<>();
        //Adds the elites to the crossedSolutions
        for (int i = 0; i < yNumberOfEliteUnchangedSolutions; i++)
            crossedSolutions.add(new GeneticAlgorithmSolution(originalSolutionsList.get(i)));
        //For each 2 non elite solution
        for (int i = yNumberOfEliteUnchangedSolutions; i + 1 < originalSolutionsList.size(); i++) {
            GeneticAlgorithmSolution originalSolution1 = new GeneticAlgorithmSolution(originalSolutionsList.get(i));
            GeneticAlgorithmSolution originalSolution2 = new GeneticAlgorithmSolution(originalSolutionsList.get(i + 1));

            //Makes crossover of the 2 original solutions
            GeneticAlgorithmSolution[] crossed2SolutionsArray = GeneticAlgorithmSolution.crossoverFunction(originalSolution1, originalSolution2);

            //Adds the 2 non elite crossed solutions to the list
            crossedSolutions.add(crossed2SolutionsArray[0]);
            crossedSolutions.add(crossed2SolutionsArray[1]);
            //Increments a second time the counter (we modified 2 solutions)
            i++;
        }


        //While number of solutions is below the population goal
        for (int i = originalSolutionsList.size(); i + 1 < xPopulationSizeGoal; i++) {
            //adds new crossed versions
            GeneticAlgorithmSolution originalSolution1 = new GeneticAlgorithmSolution(originalSolutionsList.get(rand.nextInt(1, originalSolutionsList.size())));
            GeneticAlgorithmSolution originalSolution2 = new GeneticAlgorithmSolution(originalSolutionsList.get(rand.nextInt(1, originalSolutionsList.size())));

            //Makes crossover of the 2 original solutions
            GeneticAlgorithmSolution[] crossed2SolutionsArray = GeneticAlgorithmSolution.crossoverFunction(originalSolution1, originalSolution2);

            //Adds the 2 crossed solutions to the list
            crossedSolutions.add(crossed2SolutionsArray[0]);
            crossedSolutions.add(crossed2SolutionsArray[1]);
            //Increments a second time the counter (we modified 2 solutions)
            i++;
        }

        //Sorts the new list
        crossedSolutions.sort(Comparator.naturalOrder());
        //Returns the list with the mutated solutions
        return crossedSolutions;
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
     * Mutates a gene
     */
    private static int mutateGene(int originalGene) {

        //Simple gene mutation ... if 0 becomes 1, if 1 becomes 0
        if (originalGene == 0) {
            return 1;
        } else {
            return 0;
        }
    }


    /**
     * Mutates a chromosome
     */
    private static int[] mutateChromosome(
            int[] originalChromosome,
            double chanceOfMutatedGene) throws Exception {
        Random rand = new Random();
        //Creates an array to store the mutated genes
        int[] mutatedChromosome = new int[originalChromosome.length];

        //For each gene in the originalChromosome
        for (int i = 0; i < originalChromosome.length; i++) {
            int mutatedGene = originalChromosome[i];
            //If given the chance of gene mutation, it mutates
            if (GeneticAlgorithmSolution.chanceEventRoulette(chanceOfMutatedGene)) {
                mutatedGene = GeneticAlgorithmSolution.mutateGene(mutatedGene);
                //With the mutation done, stores the mutated gene in the mutated chromosome
                mutatedChromosome[i] = mutatedGene;
            } else {
                //If not stores the gene without mutation
                mutatedChromosome[i] = mutatedGene;
            }
        }
        //With all the mutations done, returns the mutated chromosome
        return mutatedChromosome;
    }

    /**
     * Mutates a Solution
     */
    private void mutateSolution(double chanceOfGeneMutation) throws Exception {
        //Mutates the current solution
        this.chromosome = GeneticAlgorithmSolution.mutateChromosome(this.chromosome, chanceOfGeneMutation);
        this.calculateTotalCost();
    }

    /**
     * Mutates the solutions in the array on a chance base except for X number of elites
     * Obs: if the xPopulationSizeGoal is different from the originalSolutionsList size, it will add new mutation variations
     * till the populationSizeGoal is achieved
     */
    private static List<GeneticAlgorithmSolution> mutationFunctionIntoXPopulationChanceBased(
            List<GeneticAlgorithmSolution> originalSolutionsList,
            int xPopulationSizeGoal,
            int yNumberOfEliteUnchangedSolutions,
            double chanceOfSolutionMutation,
            double chanceOfGeneMutation) throws Exception { 
        Random rand = new Random();
        //Creates a list to store the mutated solutions
        List<GeneticAlgorithmSolution> mutatedSolutions = new ArrayList<>();
        //Adds the elites to the mutatedSolutions
        for (int i = 0; i < yNumberOfEliteUnchangedSolutions; i++)
            mutatedSolutions.add(new GeneticAlgorithmSolution(originalSolutionsList.get(i)));
        //For each non elite solution
        for (int i = yNumberOfEliteUnchangedSolutions; i < originalSolutionsList.size(); i++) {
            GeneticAlgorithmSolution mutatedSolution = new GeneticAlgorithmSolution(originalSolutionsList.get(i));
            //If given the chanceOfSolutionMutation, it mutates
            if (GeneticAlgorithmSolution.chanceEventRoulette(chanceOfSolutionMutation))
                mutatedSolution.mutateSolution(chanceOfGeneMutation);
            //Adds the non elite solution to the list
            mutatedSolutions.add(mutatedSolution);
        }
        //While number of solutions is below the population goal
        for (int i = originalSolutionsList.size(); i < xPopulationSizeGoal; i++) {
            //adds new mutated versions
            GeneticAlgorithmSolution mutatedSolution = new GeneticAlgorithmSolution(originalSolutionsList.get(rand.nextInt(originalSolutionsList.size())));
            mutatedSolution.mutateSolution(chanceOfGeneMutation);
            mutatedSolutions.add(mutatedSolution);
        }
        //Sorts the new list
        mutatedSolutions.sort(Comparator.naturalOrder());
        //Returns the list with the mutated solutions
        return mutatedSolutions;
    }


    /**
     * Runs the evolution, finds and returns the best solution
     */
    public static GeneticAlgorithmSolution runEvolution(boolean showProgressMessages) throws Exception {

        System.out.println("\n#################################");
        System.out.println("### -- The Great Evolution -- ###");
        System.out.println("#################################");
        System.out.println("ScenarioName: " + problemScenario.getName() + "\t" + "OptimalSolution: " + problemScenario.getOptimalSolutionValue());
        System.out.println("NumberOfWarehouses: " + problemScenario.getWarehouseLocations().length + "\t" + "NumberOfCustomers: " + problemScenario.getCustomers().length);
        System.out.println("MaxGeneration: " + maxNumberOfGenerations + "\t" + "PopulationSize: " + numberOfSolutionsPerGeneration + "\t" + "MaxTime(ms): " + maxRunEvolutionDuration);


        //Generates X numberOfRandomSolutions (initial population)
        List<GeneticAlgorithmSolution> generatedSolutions = GeneticAlgorithmSolution.generateXRandomSolutions(numberOfSolutionsPerGeneration);

        //While X, Selects, Mutates and repeat
        int currentGeneration = 0;
        long startTime = System.currentTimeMillis();
        long maxDuration = maxRunEvolutionDuration;// 0.5 seconds in milliseconds

        //Stops when you reach max number of solutions, max number of time or u get the optimal solution
        while (currentGeneration < maxNumberOfGenerations
                && (System.currentTimeMillis() - startTime) <= maxDuration
                && !(String.format("%.2f", generatedSolutions.get(0).currentSolutionCost)).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue()))
        ) {
            //Selects top solutions
            generatedSolutions = GeneticAlgorithmSolution.selectionFunctionXBest(generatedSolutions, GeneticAlgorithmSolution.xNumberOfBestSolutionsForSelectionFunction);

            //Crosses the solutions (not the elites)
            generatedSolutions = GeneticAlgorithmSolution.crossoverFunctionIntoXPopulation(
                    generatedSolutions,
                    xPopulationSizeGoalForCrossoverFunction,
                    yNumberOfEliteNonCrossedSolutions);

            //Mutates the solutions (not the elites)
            generatedSolutions = GeneticAlgorithmSolution.mutationFunctionIntoXPopulationChanceBased(
                    generatedSolutions,
                    xPopulationSizeGoalForMutationFunction,
                    yNumberOfEliteNonMutatedSolutions,
                    chanceOfSolutionMutation,
                    chanceOfGeneMutation);

            //Print Progress every few generations
            if ((currentGeneration % (maxNumberOfGenerations / 40) == 0) && showProgressMessages) {
                System.out.println("\n#################################");
                System.out.println("CurrentGeneration: " + currentGeneration + "\t" + "CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
                System.out.println("BestSolution: " + generatedSolutions.get(0));
                System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/generatedSolutions.get(0).currentSolutionCost)));
                System.out.println("#################################");
            }

            //Increase counters
            currentGeneration++;
        }

        //returns Best Solution //the solutions all get sorted during the process
        GeneticAlgorithmSolution bestSolution = generatedSolutions.get(0);
        System.out.println("#################################");
        System.out.println("CurrentGeneration: " + (currentGeneration - 1) + "\t" + "CompilationTime(ms): " + (System.currentTimeMillis() - startTime) + " milliseconds");
        System.out.println("BestSolution: " + bestSolution.toString());
        System.out.println("OptimalRatio: " + String.format("%.5f", (problemScenario.getOptimalSolutionValue()/bestSolution.currentSolutionCost)));
        //Sometimes u might get a very small decimal case of difference ... so lets round it up
        if ((String.format("%.2f", bestSolution.currentSolutionCost)).equals(String.format("%.2f", problemScenario.getOptimalSolutionValue())))
            System.out.println("U GOT THE OPTIMAL SOLUTION!");
        System.out.println("#################################");
        return bestSolution;
    }

    @Override
    public int compareTo(GeneticAlgorithmSolution o) {
        //If the total cost of the current solution is bigger than the one from 0 solution, return 1, otherwise return -1.
        if (this.currentSolutionCost == o.currentSolutionCost) {
            return 0;
        } else if (this.currentSolutionCost > o.currentSolutionCost) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "GeneticAlgorithmSolution{" +
                "currentSolutionCost=" + String.format("%.3f", currentSolutionCost) +
                ", chromosome=" + Arrays.toString(chromosome) +
                '}';
    }
}
