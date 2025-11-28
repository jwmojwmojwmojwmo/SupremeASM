package main;
// Represents a register
public class Register {
    private int value;

    public Register() {
        value = 0;
    }

    public int read() {
        return value;
    }

    public void write(int value) {
        this.value = value;
    }
}
