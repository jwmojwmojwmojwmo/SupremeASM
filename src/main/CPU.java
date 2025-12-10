package main;

import java.util.Scanner;

public class CPU {
    private final Register[] regs;
    private final PC pc;
    private static MainMemory mem;
    private static final Scanner userInput = new Scanner(System.in);
    private boolean isRunning;
    // insEnd is EXCLUSIVE. instructions go to insEnd - 1
    private int insEnd;

    public CPU() {
        regs = new Register[]{new Register(), new Register(), new Register(), new Register(), new Register(),
                new Register(), new Register(), new Register(), new Register(), new Register()};
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
                regs[0].write(-1);
            }
            if (regs[0].read() == -1) {
                System.out.println("segfault triggered at instruction @" + pc.read());
            }
            if (!isRunning) {
                userInput.close();
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
                switch (insNum0) {
                    case 0:
                        return move(insNum1, insNum2);
                    case 1:
                        return increment(insNum2);
                    case 2:
                        return decrement(insNum2);
                    case 3:
                        return add(insNum1, insNum2);
                    case 4:
                        return not(insNum2);
                    case 5:
                        return and(insNum1, insNum2);
                    case 6:
                        return shift(insNum1, (byte) insImmByte);
                    case 7:
                        return multiply(insNum1, insNum2);
                    case 8:
                        return divide(insNum1, insNum2);
                    case 9:
                        return modulus(insNum1, insNum2);
                    default:
                        return -1;
                }
            case 0xA:
                if (insNum0 == 0) {
                    return indirectJump((short) insImm);
                }
                if (insNum0 == 1) {
                    return ifEqualIndirectJump((short) insImm, insNum1);
                }
                if (insNum0 == 2) {
                    return ifGreaterIndirectJump((short) insImm, insNum1, insNum2);
                }
                return -1;
            case 0xE:
                if (insNum0 == 0) {
                    return logRegister(insNum2);
                }
                if (insNum0 == 1) {
                    return logMemory(insNum2, insNum1);
                }
                if (insNum0 == 2) {
                    return logFormatRegister(insNum2);
                }
                if (insNum0 == 3) {
                    return logFormatMemory(insNum2, insNum1);
                }
                return -1;
            case 0xF:
                switch (insNum0) {
                    case 0:
                        return 1;
                    case 2:
                        return deallocateMemory(insNum2);
                    case 3:
                        return mem.defragmentMemory();
                    case 4:
                        return getUserInput();
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
        regs[register].write(value);
        return 1;
    }

    // ld offset(reg1), reg2
    private int loadMemory(short offset, int reg1, int reg2) {
        int address = regs[reg1].read() + offset;
        regs[reg2].write(mem.read(address));
        return 1;
    }

    // ld (reg1 + rego), reg2
    private int loadMemoryIndexed(int rego, int reg1, int reg2) {
        int address = regs[reg1].read() + regs[rego].read();
        regs[reg2].write(mem.read(address));
        return 1;
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
        regs[reg2].write(regs[reg1].read());
        return 1;
    }

    // inc register
    private int increment(int register) {
        regs[register].write(regs[register].read() + 1);
        return 1;
    }

    // dec register
    private int decrement(int register) {
        regs[register].write(regs[register].read() - 1);
        return 1;
    }

    // add reg1, reg2
    private int add(int reg1, int reg2) {
        regs[reg2].write(regs[reg1].read() + regs[reg2].read());
        return 1;
    }

    // not register
    private int not(int register) {
        regs[register].write(~regs[register].read());
        return 1;
    }

    // and reg1, reg2
    private int and(int reg1, int reg2) {
        regs[reg2].write(regs[reg1].read() & regs[reg2].read());
        return 1;
    }

    // shl/shr $v, register
    private int shift(int register, byte shift) {
        if (shift < 0) {
            regs[register].write(regs[register].read() >> -shift);
        } else {
            regs[register].write(regs[register].read() << shift);
        }
        return 1;
    }

    // multiply reg1, reg2
    private int multiply(int reg1, int reg2) {
        regs[reg2].write(regs[reg1].read() * regs[reg2].read());
        return 1;
    }

    // divide reg1, reg2
    private int divide(int reg1, int reg2) {
        regs[reg2].write(regs[reg1].read() / regs[reg2].read());
        return 1;
    }

    // modulus reg1, reg2
    private int modulus(int reg1, int reg2) {
        regs[reg2].write(regs[reg1].read() % regs[reg2].read());
        return 1;
    }

    // j $o
    private int indirectJump(short offset) {
        pc.write(pc.read() + offset);
        return 1;
    }

    // j $o if reg == 0
    private int ifEqualIndirectJump(short offset, int register) {
        if (regs[register].read() == 0) {
            pc.write(pc.read() + offset);
        }
        return 1;
    }

    // j $o if reg1 > reg2
    private int ifGreaterIndirectJump(short offset, int reg1, int reg2) {
        if (regs[reg1].read() > regs[reg2].read()) {
            pc.write(pc.read() + offset);
        }
        return 1;
    }

    // j address
    private int directJump(int address) {
        pc.write(address);
        return 1;
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

    private int logMemory(int offset, int register) {
        int address = regs[register].read() + offset;
        System.out.print(mem.read(address));
        return 1;
    }

    private int logFormatMemory(int offset, int register) {
        int address = regs[register].read() + offset;
        char c = (char) (mem.read(address) & 0xFF);
        System.out.print(c);
        return 1;
    }

    private int getUserInput() {
        System.out.print("INPUT> ");
        String input = userInput.nextLine();
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

    private int dump() {
        System.out.println("\nRegisters:");
        for (int i = 0; i < regs.length; i++) {
            System.out.println(i + ": " + regs[i].read());
        }
        System.out.println("PC: " + pc.read());
        return 1;
    }
}
