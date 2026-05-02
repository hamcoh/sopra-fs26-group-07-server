package ch.uzh.ifi.hase.soprafs26.seed;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.repository.ProblemRepository;
import ch.uzh.ifi.hase.soprafs26.service.ProblemService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Every time the application starts, this seeder will check if there are already problems in the database.
 * If there are, it will do nothing. If there aren't, it will look for all JSON files in the "resources/problems" folder,
 * read the problem data from the JSON files, create Problem and TestCase entities, and save
 * them in the database using the ProblemService. This way, we can easily add new problems 
 * to our application by simply adding new JSON files to the "resources/problems" folder.
 * 
 */
@Component
public class ProblemSeeder implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final ProblemService problemService;
    // This is a built-in Spring utility that can parse JSON strings into Java Maps and Lists.
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    // We use constructor injection to get the ProblemRepository and ProblemService beans from the Spring context.
    public ProblemSeeder(ProblemRepository problemRepository, ProblemService problemService) {
        this.problemRepository = problemRepository;
        this.problemService = problemService;
    }

    // This method will be executed when the application starts.
    @Override
    public void run(String... args) throws Exception {
        if (problemRepository.count() > 0) {
            return;
        }

        // This is a Spring utility that allows us to read all resources (files) that match a certain pattern. 
        // In our case this is resources/problems/**/*.json 
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:problems/**/*.json");

        // We loop through all the found JSON files, read their content, 
        // parse the JSON data, create Problem and TestCase entities, and save them in the database.
        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Object> data = jsonParser.parseMap(json);

                Problem problem = new Problem();
                problem.setTitle(String.valueOf(data.get("title")));
                problem.setDescription(String.valueOf(data.get("description")));
                problem.setInputFormat(String.valueOf(data.get("inputFormat")));
                problem.setOutputFormat(String.valueOf(data.get("outputFormat")));
                problem.setConstraints(String.valueOf(data.get("constraints")));
                problem.setSampleSolution(String.valueOf(data.get("sampleSolution")));

                if (data.containsKey("hint")) {
                    problem.setHint(String.valueOf(data.get("hint")));
                } else {
                    problem.setHint("No hint available for this problem."); 
                }

                problem.setGameDifficulty(
                    GameDifficulty.valueOf(String.valueOf(data.get("gameDifficulty")).toUpperCase())
                );
                problem.setGameLanguage(
                    GameLanguage.valueOf(String.valueOf(data.get("gameLanguage")).toUpperCase())
                );

                List<TestCase> testCases = new ArrayList<>();
                List<Object> rawTestCases = (List<Object>) data.get("testCases");

                for (Object rawTestCase : rawTestCases) {
                    Map<String, Object> testCaseMap = (Map<String, Object>) rawTestCase;

                    TestCase testCase = new TestCase();
                    testCase.setInput(String.valueOf(testCaseMap.get("input")));
                    testCase.setExpectedOutput(String.valueOf(testCaseMap.get("expectedOutput")));
                    testCases.add(testCase);
                }

                problem.setTestCases(testCases);
                problemService.createProblem(problem);
            }
        }
    }
}