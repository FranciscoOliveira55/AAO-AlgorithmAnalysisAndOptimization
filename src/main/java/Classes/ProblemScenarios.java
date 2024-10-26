package Classes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProblemScenarios {

    private ArrayList<ProblemScenario> problemScenarios;

    public ProblemScenarios() {
        problemScenarios = new ArrayList<>();
    }

    public ArrayList<ProblemScenario> getProblemScenarios() {
        return problemScenarios;
    }

    /**
     * I want to read all the problem scenarios in a directory and store them in the array
     * @param problemScenariosDirPartialPath
     */
    public void ReadScenariosFromDir(String problemScenariosDirPartialPath){

        // Get the user's project directory
        String projectDir = System.getProperty("user.dir");

        // Get the scenarios directory
        String problemScenariosDirCompletePath = projectDir + problemScenariosDirPartialPath;

        // Construct the absolute path to the file containing the scenarios list
        Path scenariosFilePath = Paths.get(problemScenariosDirCompletePath + "files.lst");

        Logger.WriteMessage("Reading scenarios from " + scenariosFilePath.toString());

        // Contruct the absolute path to the optimal solutions
        Path optimalSolutionsFilePath = Paths.get(projectDir + "\\ProblemScenariosData\\optimal.txt");
        System.out.println(optimalSolutionsFilePath.toString());
        System.out.println(scenariosFilePath.toString());


        try {
            // Read all scenarios from the scenariosFilePath
            List<String> scenarioFileNames = Files.readAllLines(scenariosFilePath);

            // Read all optimalSolutions from the optimalSolutionsFilePath
            List<String> optimalSolutions = Files.readAllLines(optimalSolutionsFilePath, Charset.forName("Windows-1252"));

            // For each scenarioFile
            for (String scenarioFileName : scenarioFileNames) {
                Logger.WriteMessage("fileName:" + scenarioFileName);


                //Get the optimal solution
                double optimalSolution = -1;
                for(String optSolLine : optimalSolutions){
                    //Split the line
                    String[] parts = optSolLine.trim().split("\\s+");
                    //If the part 0 is equal to the name
                    if ((parts[0]+".txt").equalsIgnoreCase(scenarioFileName)){
                        //then part 1 is the optimal value
                        optimalSolution = Double.parseDouble(parts[parts.length-1]);
                        Logger.WriteMessage("Optimal Solution: " + optimalSolution);
                        break;
                    }
                }

                //Create a ProblemScenario
                ProblemScenario problemScenario = new ProblemScenario(scenarioFileName, optimalSolution);
                //Read the info from that problemScenario
                problemScenario.ReadScenarioFromFile(Paths.get(problemScenariosDirCompletePath + scenarioFileName));
                //Store problemScenario in the list
                this.problemScenarios.add(problemScenario);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
