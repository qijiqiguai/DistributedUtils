package tech.qiwang;


public class MethodNotSupportedException extends RuntimeException{
    private static final long serialVersionUID = 703489799884576939L;

    public MethodNotSupportedException(String message) {
        super(message);
    }
}
