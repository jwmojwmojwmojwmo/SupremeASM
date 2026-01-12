package exceptions;

public class BadCodeException extends Exception {
    private int errLine;

    public BadCodeException() {
    }

    public BadCodeException(int line) {
        errLine = line;
    }

    public int getErrLine() {
        return errLine;
    }
}
