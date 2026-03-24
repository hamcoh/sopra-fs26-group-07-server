package ch.uzh.ifi.hase.soprafs26.constant;

/**
 * We use enums for constants because it's safer
 * s.t people if people write "run" somewhere else 
 * we dont get some weird bugs
 * We can then use these definitions across the app
 */
public enum Verdict {
     CORRECT_ANSWER, WRONG_ANSWER, COMPILE_ERROR
    
}
