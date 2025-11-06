package co.edu.puj.secchub_backend.planning.exception;

public class PlanningServerErrorException extends RuntimeException {
    public PlanningServerErrorException(String message) {
        super(message);
    }
}
