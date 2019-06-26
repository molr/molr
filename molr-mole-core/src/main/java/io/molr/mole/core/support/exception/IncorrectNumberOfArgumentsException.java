package io.molr.mole.core.support.exception;

public class IncorrectNumberOfArgumentsException extends Exception {
    public IncorrectNumberOfArgumentsException(int expected, int actual) {
        super(String.format("Incorrect number of arguments. Expected : %s, Actual : %s", expected, actual));
    }
}
