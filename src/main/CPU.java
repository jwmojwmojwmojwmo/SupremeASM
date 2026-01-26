package main;

import java.util.Scanner;

public class CPU {
    private Register[] regs;
    private PC pc;
    private static MainMemory mem;
    private Scanner userInput;
    public volatile boolean isRunning;
    // insEnd is EXCLUSIVE. instructions go to insEnd - 1
    private int insEnd;
    
    public CPU() {
        regs = new Register[]{new Register(), new Register(), new Register(), new Register(), new Register(),
                new Register(), new Register(), new Register(), new Register(), new Register(), new SP()};
        pc = new PC();
        mem = MainMemory.getInstance();
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
        pc.inc();
        return insStart;
    }

    public void run() {
        int firstIns;
        isRunning = true;
        while (pc.read() <= insEnd) {
            firstIns = mem.read(pc.read() - 1);
            try {
                if (((firstIns >> 16) & 0xFF) == 0xEE) {
                    regs[0].write(decodeAndExecute(firstIns, mem.read(pc.inc() - 1))); // r0 is default return register
                } else {
                    regs[0].write(decodeAndExecute(firstIns));
                }
            } catch (Exception e) {
                if (!isRunning) {
                    return;
                }
                regs[0].write(-1);
            }
            if (regs[0].read() == -1) {
                System.out.println("segfault triggered at instruction @" + (pc.read() - 1));
                System.out.println("Machine code representation at instruction location is as follows: " +
                        Integer.toHexString(mem.read(pc.read() - 1)));
            }
            if (!isRunning) {
                return;
            }
            pc.inc();
        }
    }

    public void reset() {
        regs = new Register[]{new Register(), new Register(), new Register(), new Register(), new Register(),
                new Register(), new Register(), new Register(), new Register(), new Register(), new SP()};
        pc = new PC();
        mem.reset();
        mem = MainMemory.getInstance();
        userInput = null;
    }

    private int decodeAndExecute(int instruction) {
        int insOp = ((instruction >> 28) & 0xF);
        int insNum0 = ((instruction >> 24) & 0xF);
        int insNum1 = ((instruction >> 20) & 0xF);
        int insNum2 = ((instruction >> 16) & 0xF);
        int insNum3 = ((instruction >> 12) & 0xF);
        int insImm = instruction & 0xFFFF;
        int insImmByte = instruction & 0xFF;
        switch (insOp) {
            case 0:
                if (insNum0 == 0) {
                    return loadMemory((short) insImm, insNum1, insNum2);
                }
                if (insNum0 == 1) {
                    return loadMemoryIndexed(insNum2, insNum1, insNum3);
                }
                return -1;
            case 1:
                if (insNum0 == 0) {
                    return storeMemory((short) insImm, insNum1, insNum2);
                }
                if (insNum0 == 1) {
                    return storeMemoryIndexed(insNum3, insNum1, insNum2);
                }
                return -1;
            case 2:
                return switch (insNum0) {
                    case 0 -> move(insNum1, insNum2);
                    case 1 -> increment(insNum2);
                    case 2 -> decrement(insNum2);
                    case 3 -> add(insNum1, insNum2);
                    case 4 -> not(insNum2);
                    case 5 -> and(insNum1, insNum2);
                    case 6 -> shift(insNum1, (byte) insImmByte);
                    case 7 -> multiply(insNum1, insNum2);
                    case 8 -> divide(insNum1, insNum2);
                    case 9 -> modulus(insNum1, insNum2);
                    default -> -1;
                };
            case 0xA:
                if (insNum0 == 0) {
                    return indirectJump((short) insImm);
                }
                if (insNum0 == 1) {
                    return ifEqualIndirectJump((short) insImm, insNum1, insNum2);
                }
                if (insNum0 == 2) {
                    return ifGreaterIndirectJump((short) insImm, insNum1, insNum2);
                }
                if (insNum0 == 3) {
                    return getProgramCounter((short) insImm, insNum1);
                }
                if (insNum0 == 4) {
                    return directJump(insNum1);
                }
                return -1;
            case 0xE:
                if (insNum0 == 0) {
                    return logRegister(insNum2);
                }
                if (insNum0 == 1) {
                    return logMemory((short) insImm, insNum1);
                }
                if (insNum0 == 2) {
                    return logMemoryIndexed(insNum1, insNum2);
                }
                if (insNum0 == 3) {
                    return logFormatRegister(insNum2);
                }
                if (insNum0 == 4) {
                    return logFormatMemory((short) insImm, insNum1);
                }
                if (insNum0 == 5) {
                    return logFormatMemoryIndexed(insNum1, insNum2);
                }
                return -1;
            case 0xF:
                return switch (insNum0) {
                    case 0 -> 1;
                    case 1 -> setBounds();
                    case 2 -> deallocateMemory(insNum2);
                    case 3 -> mem.defragmentMemory();
                    case 4 -> getUserInput();
                    case 0xD -> dump();
                    case 0xE -> mem.dump();
                    case 0xF -> halt();
                    default -> -1;
                };
            default:
                return -1;
        }
    }

    public int halt() {
        isRunning = false;
        return 0;
    }

    private int decodeAndExecute(int firstIns, int secondIns) {
        int insOp = ((firstIns >> 28) & 0xF);
        int insNum0 = ((firstIns >> 24) & 0xF);
        switch (insOp) {
            case 0:
                return loadValue(secondIns, insNum0);
            case 0xF:
                if (insNum0 == 1) {
                    return mem.requestMemoryBlock(secondIns);
                }
                return -1;
            case 0xA:
                return directJump(secondIns);
            default:
                return -1;
        }
    }

    private int deallocateMemory(int register) {
        mem.freeMemoryBlock(regs[register].read());
        return 1;
    }

    // ld $v, reg1
    private int loadValue(int value, int register) {
        return regs[register].write(value);
    }

    // ld offset(reg1), reg2
    private int loadMemory(short offset, int reg1, int reg2) {
        int address = regs[reg1].read() + offset;
        return regs[reg2].write(mem.read(address));
    }

    // ld (reg1 + rego), reg2
    private int loadMemoryIndexed(int rego, int reg1, int reg2) {
        int address = regs[reg1].read() + regs[rego].read();
        return regs[reg2].write(mem.read(address));
    }

    // st reg1, offset(reg2)
    private int storeMemory(int offset, int reg1, int reg2) {
        int address = regs[reg2].read() + offset;
        mem.write(address, regs[reg1].read());
        return 1;
    }

    // st reg1, (reg2 + rego)
    private int storeMemoryIndexed(int rego, int reg1, int reg2) {
        int address = regs[reg2].read() + regs[rego].read();
        mem.write(address, regs[reg1].read());
        return 1;
    }

    // mov reg1, reg2
    private int move(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read());
    }

    // inc register
    private int increment(int register) {
        return regs[register].write(regs[register].read() + 1);
    }

    // dec register
    private int decrement(int register) {
        return regs[register].write(regs[register].read() - 1);
    }

    // add reg1, reg2
    private int add(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read() + regs[reg2].read());
    }

    // not register
    private int not(int register) {
        return regs[register].write(~regs[register].read());
    }

    // and reg1, reg2
    private int and(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read() & regs[reg2].read());
    }

    // shl/shr $v, register
    private int shift(int register, byte shift) {
        if (shift < 0) {
            return regs[register].write(regs[register].read() >> -shift);
        } else {
            return regs[register].write(regs[register].read() << shift);
        }
    }

    // multiply reg1, reg2
    private int multiply(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read() * regs[reg2].read());
    }

    // divide reg1, reg2
    private int divide(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read() / regs[reg2].read());
    }

    // modulus reg1, reg2
    private int modulus(int reg1, int reg2) {
        return regs[reg2].write(regs[reg1].read() % regs[reg2].read());
    }

    // j $o
    private int indirectJump(short offset) {
        return pc.write(pc.read() + offset);
    }

    // j $o if reg1 == reg2
    private int ifEqualIndirectJump(short offset, int reg1, int reg2) {
        if (regs[reg1].read() == regs[reg2].read()) {
            return pc.write(pc.read() + offset);
        }
        return 1;
    }

    // j $o if reg1 > reg2
    private int ifGreaterIndirectJump(short offset, int reg1, int reg2) {
        if (regs[reg1].read() > regs[reg2].read()) {
            return pc.write(pc.read() + offset);
        }
        return 1;
    }

    private int getProgramCounter(short offset, int register) {
        return regs[register].write(pc.read() + offset);
    }

    // goto register
    private int directJump(int register) {
        return pc.write(regs[register].read());
    }

    private int logRegister(int register) {
        System.out.print(regs[register].read());
        return 1;
    }

    private int logFormatRegister(int register) {
        char c = (char) (regs[register].read() & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int logMemory(short offset, int register) {
        int address = regs[register].read() + offset;
        System.out.print(mem.read(address));
        return 1;
    }

    private int logMemoryIndexed(int reg1, int reg2) {
        int address = regs[reg1].read() + regs[reg2].read();
        System.out.println(mem.read(address));
        return 1;
    }

    private int logFormatMemory(short offset, int register) {
        int address = regs[register].read() + offset;
        char c = (char) (mem.read(address) & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int logFormatMemoryIndexed(int reg1, int reg2) {
        int address = regs[reg1].read() + regs[reg2].read();
        char c = (char) (mem.read(address) & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int getUserInput() {
        if (userInput == null) {
            userInput = new Scanner(System.in);
        }
        System.out.print("INPUT> ");
        String input = userInput.next();
        try {
            if (input.startsWith("0x") || input.startsWith("0X")) {
                return Integer.parseInt(input.substring(2), 16);
            } else {
                return Integer.parseInt(input);
            }
        } catch (Exception e) {
            return input.charAt(0);
        }
    }

    private int setBounds() {
        int size = mem.read(regs[10].read() - 1);
        SP sp = (SP) regs[10];
        sp.setBounds(size);
        return 1;
    }

    private int dump() {
        System.out.println("\nRegisters:");
        for (int i = 0; i < regs.length; i++) {
            System.out.println(i + ": " + regs[i].read());
        }
        System.out.println("PC: " + pc.read());
        return 1;
    }
}
