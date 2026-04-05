package ch.uzh.ifi.hase.soprafs26.seed;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import java.util.List;
/**
 * It's a Seed JSON mapping class for problems.
 * This class is used to map the data from the JSON files in the "resources/problems" 
 * folder to Java objects that we can then use to create Problem 
 * and TestCase entities and save them in the database.
 */
public class ProblemSeedData {

    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private GameDifficulty gameDifficulty;
    private GameLanguage gameLanguage;
    private String sampleSolution;
    private List<TestCaseSeedData> testCases;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public GameDifficulty getGameDifficulty() {
        return gameDifficulty;
    }

    public void setGameDifficulty(GameDifficulty gameDifficulty) {
        this.gameDifficulty = gameDifficulty;
    }

    public GameLanguage getGameLanguage() {
        return gameLanguage;
    }

    public void setGameLanguage(GameLanguage gameLanguage) {
        this.gameLanguage = gameLanguage;
    }

    public String getSampleSolution() {
        return sampleSolution;
    }

    public void setSampleSolution(String sampleSolution) {
        this.sampleSolution = sampleSolution;
    }

    public List<TestCaseSeedData> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseSeedData> testCases) {
        this.testCases = testCases;
    }
}