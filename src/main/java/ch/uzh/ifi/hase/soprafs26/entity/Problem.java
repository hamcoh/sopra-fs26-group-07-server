package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import jakarta.persistence.*;

/**
 *  Problem entity that will be stored in the database.
 */
@Entity
@Table(name = "problems")
public class Problem implements Serializable{
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long problemId; 

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // This lets us store superlong strings
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputFormat;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputFormat;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String constraints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameDifficulty gameDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameLanguage gameLanguage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sampleSolution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "problem_hints", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "hint", columnDefinition = "TEXT")
    private List<String> hints = new ArrayList<>();

    /**
     *  Relationship of 1:M between Problem and TestCase 
     *  cascade = CascadeType.ALL means saving/deleting a Problem also affects its test cases
     *  orphanRemoval = true means if a test case is removed from the problem’s list, it gets removed properly too
     * */
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true) 
    private List<TestCase> testCases = new ArrayList<>(); // we will then be able to insert the testCases into the ArrayList which has just been initialized

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public List<String> getHints() {
        return hints;
    }

    public void setHints(List<String> hints) {
        this.hints = hints;
    }

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

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
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




    

    



}
