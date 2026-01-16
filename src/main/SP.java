package main;
// Represents rA, the stack pointer register
public class SP extends Register {
    //bounds are inclusive
    private Integer lowerBound;
    private Integer upperBound;

    public SP() {
        super();
    }

    @Override
    public int write(int value) {
        if (lowerBound != null) {
            if (value < lowerBound || value > upperBound) {
                return -1;
            }
        }
        return super.write(value);
    }

    public void setBounds(int size) {
        lowerBound = this.read();
        upperBound = this.read() + size - 1;
    }
}
