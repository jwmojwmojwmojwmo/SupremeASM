package main;
// Represents the special program counter register (PC), storing the address of the next instruction
public class PC extends Register {
    public PC() {
        super();
    }

    public int inc() {
        this.write(this.read() + 1);
        return this.read();
    }

    public int dec() {
        this.write(this.read() - 1);
        return this.read();
    }
}
