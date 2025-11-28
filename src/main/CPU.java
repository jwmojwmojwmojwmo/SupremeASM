package main;

public class CPU {
    private final Register[] regs;
    private final PC pc;
    private static MainMemory mem;
    private boolean isRunning;
    // memStart and memEnd are inclusive, so memory can be at memStart and memEnd,
    // just not past it
    private int memStart;
    private int memEnd;
    private int insEnd;

    public CPU() {
        regs = new Register[] { new Register(), new Register(), new Register(), new Register(), new Register(),
                new Register(), new Register(), new Register(), new Register(), new Register() };
        pc = new PC();
        mem = MainMemory.getInstance();
        memStart = -1;
        memEnd = -1;
    }

    public int load(byte[] instructions) {
        int insStart = mem.requestMemoryBlock(instructions.length / 4);
        if (insStart != -1) {
            insEnd = mem.read(insStart - 1) + insStart;
            for (int i = 0; i < instructions.length; i += 4) {
                int val = ((instructions[i] & 0xFF) << 24) | ((instructions[i + 1] & 0xFF) << 16)
                        | ((instructions[i + 2] & 0xFF) << 8) | ((instructions[i + 3] & 0xFF));
                mem.write(insStart + i / 4, val);
            }
        }
        pc.write(insStart);
        return insStart;
    }

    public void run() {
        int firstIns;
        isRunning = true;
        while (pc.read() <= insEnd) {
            firstIns = mem.read(pc.read());
            if (((firstIns >> 16) & 0xFF) == 0xEE) {
                regs[0].write(decodeAndExecute(firstIns, mem.read(pc.inc()))); // r0 is default return register
            } else {
                regs[0].write(decodeAndExecute(firstIns));
            }
            if (regs[0].read() == -1) {
                System.out.println("segfault triggered at instruction @" + pc.read());
            }
            if (!isRunning) {
                return;
            }
            pc.inc();
        }
    }

    private int decodeAndExecute(int instruction) {
        int insOp = ((instruction >> 28) & 0xF);
        int insNum0 = ((instruction >> 24) & 0xF);
        int insNum1 = ((instruction >> 20) & 0xF);
        int insNum2 = ((instruction >> 16) & 0xF);
        switch (insOp) {
            case 0:
                return loadMemory(insNum1, insNum0, insNum2);
            case 1:
                return storeMemory(insNum2, insNum0, insNum1);
            case 2:
                return move(insNum1, insNum2);
            case 0xE:
                if (insNum0 == 0) {
                    if (insNum1 == 0) {
                        return logRegister(insNum2);
                    } else {
                        return logMemory(insNum2, insNum1);
                    }
                } else {
                    if (insNum1 == 0) {
                        return logFormatRegister(insNum2);
                    } else {
                        return logFormatMemory(insNum2, insNum1);
                    }
                }
            case 0xF:
                switch (insNum0) {
                    case 0:
                        return 1;
                    case 2:
                        return deallocateMemory(insNum2);
                    case 3:
                        return mem.defragmentMemory();
                    case 0xD:
                        return dump();
                    case 0xE:
                        return mem.dump();
                    case 0xF:
                        isRunning = false;
                        return 0;
                    default:
                        return -1;
                }
            default:
                return -1;
        }

    }

    private int decodeAndExecute(int firstIns, int secondIns) {
        int insOp = ((firstIns >> 28) & 0xF);
        int insNum0 = ((firstIns >> 24) & 0xF);
        switch (insOp) {
            case 0:
                return loadValue(secondIns, insNum0);
            case 0xF:
                if (insNum0 == 1) {
                    return allocateMemory(secondIns);
                } else if (insNum0 == 2) {
                    return deallocateMemory(secondIns);
                }
            default:
                return -1;
        }
    }

    private int allocateMemory(int size) {
        memStart = mem.requestMemoryBlock(size);
        if (memStart != -1) {
            memEnd = mem.read(memStart - 1) + memStart; // only time you are allowed to access past the memStart
        }
        return memStart;
    }

    private int deallocateMemory(int register) {
        if (register > 9 || register < 0) {
            return -1; // undefined behavior
        }
        mem.freeMemoryBlock(regs[register].read());
        return 1;
    }

    // ld $v, reg1
    private int loadValue(int value, int register) {
        if (register > 9 || register < 0) {
            return -1; // undefined behavior
        }
        regs[register].write(value);
        return 1;
    }

    // ld offset(reg1), reg2
    private int loadMemory(int offset, int reg1, int reg2) {
        int address = regs[reg1].read() + offset;
        if (reg1 > 9 || reg1 < 0 || reg2 > 9 || reg2 < 0 || address < memStart || address > memEnd) {
            return -1; // undefined behavior
        }
        regs[reg2].write(address);
        return 1;
    }

    // st reg1, offset(reg2)
    private int storeMemory(int offset, int reg1, int reg2) {
        int address = regs[reg2].read() + offset;
        if (reg1 > 9 || reg1 < 0 || reg2 > 9 || reg2 < 0 || address < memStart || address > memEnd) {
            return -1; // undefined behavior
        }
        mem.write(address, regs[reg1].read());
        return 1;
    }

    // mov reg1, reg2
    private int move(int reg1, int reg2) {
        if (reg1 > 9 || reg1 < 0 || reg2 > 9 || reg2 < 0) {
            return -1; // undefined behavior
        }
        regs[reg2].write(regs[reg1].read());
        return 1;
    }

    private int logRegister(int register) {
        if (register > 9 || register < 0) {
            return -1; // undefined behavior
        }
        System.out.print(regs[register].read());
        return 1;
    }

    private int logFormatRegister(int register) {
        if (register > 9 || register < 0) {
            return -1; // undefined behavior
        }
        char c = (char) (regs[register].read() & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int logMemory(int offset, int register) {
        int address = regs[register].read() + offset;
        if (register > 9 || register < 0 || address < memStart || address > memEnd) {
            return -1; // undefined behavior
        }
        System.out.print(mem.read(address));
        return 1;
    }

    private int logFormatMemory(int offset, int register) {
        int address = regs[register].read() + offset;
        if (register > 9 || register < 0 || address < memStart || address > memEnd) {
            return -1; // undefined behavior
        }
        char c = (char) (mem.read(address) & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int dump() {
        System.out.println("Registers:");
        for (int i = 0; i < regs.length; i++) {
            System.out.println(i + ": " + regs[i].read());
        }
        return 1;
    }

    private int validateCPU() {
        if (memStart == -1 || memEnd == -1 || regs.length != 10) {
            return -1;
        }
        return 1;
    }
}
