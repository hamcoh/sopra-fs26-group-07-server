package ch.uzh.ifi.hase.soprafs26.constant;

/**
 * We use enums for constants because it's safer
 * s.t people if people write "run" somewhere else 
 * we dont get some weird bugs
 * We can then use these definitions across the app
 */
public enum Verdict {
     PENDING, // If the submission is still being judged, the verdict is pending
     CORRECT_ANSWER, // If all results are correct answer, the whole submission is correct answer
     WRONG_ANSWER, // If any result is wrong answer, the whole submission is wrong answer
     COMPILE_ERROR, // If any result is compile error, the whole submission is compile error
     TIME_LIMIT_EXCEEDED, // if any result is time limit exceeded, the whole submission is time limit exceeded
     INTERNAL_ERROR // If something is really wrong with our backend (this makes it easier for us to debug internally)
    
}
