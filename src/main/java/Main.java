import Classes.*;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("#################################");
        System.out.println("### -- The Great Optimizer -- ###");
        System.out.println("#################################");

        //Scenarios relative directories paths
        String path70s = "\\ProblemScenariosData\\ORLIB\\ORLIB-uncap\\70\\";
        String path100s = "\\ProblemScenariosData\\ORLIB\\ORLIB-uncap\\100\\";
        String path130s = "\\ProblemScenariosData\\ORLIB\\ORLIB-uncap\\130\\";
        String pathACs = "\\ProblemScenariosData\\ORLIB\\ORLIB-uncap\\a-c\\";
        String pathMs = "\\ProblemScenariosData\\M\\";


        //Read the problem scenarios data
        ProblemScenarios problemScenarios = new ProblemScenarios();
        problemScenarios.ReadScenariosFromDir(path70s); // <-- Change the path to read the scenarios from here

        boolean feedGreedyNonOptimalSolutionsToTheSimulatedAnnealing = false;


        for (ProblemScenario problemScenario : problemScenarios.getProblemScenarios()) {
            System.out.println("\n##################################################################");
            System.out.println("###################\t" + "Problem: " + problemScenario.getName() + "\t########################");
            System.out.println("##################################################################");

            //Solve the scenario with the Genetic Algorithm
            GeneticAlgorithmSolution.setProblemScenario(problemScenario);
            GeneticAlgorithmSolution.runEvolution(false);


            //Get a good solution with the GreedyAlgorithm
            GreedyAlgorithmSolution.setProblemScenario(problemScenario);
            GreedyAlgorithmSolution bestGreedySolution = GreedyAlgorithmSolution.run(false);


            //If the solution from the greedy algorithm is different from the optimal one, feed it to the SimulatedAnnealing
            if (bestGreedySolution.getCurrentSolutionCost() != problemScenario.getOptimalSolutionValue()
                    && feedGreedyNonOptimalSolutionsToTheSimulatedAnnealing) {
                //Solve the scenario with the SimulatedAnnealingAlgorithm // With the GREEDY sub optimal solution
                System.out.println("Feeding non optimal greedy solution to SimulatedAnnealing algorithm");
                SimulatedAnnealingSolution.setProblemScenario(problemScenario);
                SimulatedAnnealingSolution instance = new SimulatedAnnealingSolution(bestGreedySolution.getCurrentSolutionArray());
                instance.run(false);
            } else {
                //Solve the scenario with the SimulatedAnnealingAlgorithm // With RANDOM initial solution
                SimulatedAnnealingSolution.setProblemScenario(problemScenario);
                SimulatedAnnealingSolution instance = new SimulatedAnnealingSolution();
                instance.run(false);
            }


        }


    }
}
