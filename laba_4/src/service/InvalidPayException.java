package service;

public class InvalidPayException extends Exception {
    public InvalidPayException(String message) {
        super(message);
    }
}