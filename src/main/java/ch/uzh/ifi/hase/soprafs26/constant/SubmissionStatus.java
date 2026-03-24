package ch.uzh.ifi.hase.soprafs26.constant;

/**
 * We use enums for constants because it's safer
 * s.t people if people write "run" somewhere else 
 * we dont get some weird bugs
 * We can then use these definitions across the app
 * 
 * Correpsonds to the execution of the source code via Judge
 * e.g the submission is still running
 * PENDING = Is perhaps in an execution queue and hasn't run yet
 */
public enum SubmissionStatus {
    PENDING, RUNNING, FINISHED, FAILED 
}
