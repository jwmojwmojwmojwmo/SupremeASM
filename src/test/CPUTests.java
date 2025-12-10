package test;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import static java.lang.System.setOut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import main.CPU;

//TODO REFACTOR

public class CPUTests {
    private String runProgram(byte[] program) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        setOut(new PrintStream(out));

        try {
            CPU cpu = new CPU();
            cpu.load(program);
            cpu.run();
        } finally {
            System.setOut(original);
        }

        return out.toString();
    }

    // @Test
    // public void testMalloc() {
    // // Program:
    // // malloc(4 * 5) -> f1ee----00000005
    // // dumpMem() -> fe------
    // // halt -> ffffffff
    // byte[] program = new byte[] {
    // // malloc 5 and 8 slots
    // (byte) 0xF1, (byte) 0xEE, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x05,
    // (byte) 0xF1, (byte) 0xEE, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x08,
    // // dumpMem
    // (byte) 0xFE, 0x00, 0x00, 0x00,

    // // halt
    // (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    // };
    // String dump = runProgram(program);
    // try (FileWriter writer = new FileWriter("output.txt")) {
    // writer.write(dump);
    // System.out.println("File written successfully!");
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // assertTrue(dump.contains("0: 4"));
    // assertTrue(dump.contains("61: 5"));
    // assertTrue(dump.contains("67: 1"));
    // assertTrue(dump.contains("68: 8"));
    // assertTrue(dump.contains("77: 1"));
    // }

    @Test
    public void testLoadImmediateAndPrint() {
        // r1 = 123
        byte[] program = new byte[] {
                (byte) 0x01, (byte) 0xEE, (byte) 0x00, (byte) 0x00, // opcode 0, reg=1, EE → load immediate
                0x00, 0x00, 0x00, 0x7B, // value = 0x7B = 123

                (byte) 0xE0, 0x01, 0x00, 0x00, // print r1 as number

                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF // halt
        };
        String out = runProgram(program).trim();
        assertEquals("123", out);
    }

    @Test
    public void testLoadAndStoreImmediateOffset() {
        byte[] program = new byte[] {
                (byte) 0xF1, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06,
                (byte) 0x20, (byte) 0x09, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x01, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0F,
                (byte) 0x10, (byte) 0x19, (byte) 0x5F, (byte) 0xFF,
                (byte) 0x00, (byte) 0x95, (byte) 0x2F, (byte) 0xFF,
                (byte) 0xE0, (byte) 0x02, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        String out = runProgram(program).trim();
        assertEquals(out, "15");
    }

    @Test
    public void testLoadAndStoreIndexed() {
        byte[] program = new byte[] {
                (byte) 0xF1, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06,

                (byte) 0x20, (byte) 0x09, (byte) 0xFF, (byte) 0xFF,

                (byte) 0x01, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0F,

                (byte) 0x03, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,

                (byte) 0x11, (byte) 0x19, (byte) 0x3F, (byte) 0xFF,

                (byte) 0x01, (byte) 0x93, (byte) 0x2F, (byte) 0xFF,

                (byte) 0xE0, (byte) 0x02, (byte) 0xFF, (byte) 0xFF,

                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        String out = runProgram(program).trim();
        assertEquals(out, "15");
    }

    @Test
    public void testMoveRegister() {
        byte[] program = new byte[] {
                // r2 = 5
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x05,

                // mov r2 -> r3 (20rs---- → r=2, s=3)
                (byte) 0x20, 0x23, 0x00, 0x00,

                // print r3
                (byte) 0xE0, 0x03, 0x00, 0x00,

                // halt
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        String out = runProgram(program).trim();
        assertEquals("5", out);
    }

    @Test
    public void testAddRegisters() {
        byte[] program = new byte[] {
                // r1 = 10
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A,

                // r2 = 7
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07,

                // add r1, r2 (23rs---- → r=1, s=2)
                (byte) 0x23, 0x12, 0x00, 0x00,

                // print r2
                (byte) 0xE0, 0x02, 0x00, 0x00,

                // halt
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        String out = runProgram(program).trim();
        assertEquals("17", out);
    }

    @Test
    public void testIncrementRegister() {
        byte[] program = new byte[] {
                (byte) 0x21, (byte) 0x03, 0x00, 0x00,
                (byte) 0xE0, (byte) 0x03, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("1", runProgram(program).trim());
    }

    @Test
    public void testDecrementRegister() {
        byte[] program = new byte[] {
                (byte) 0x04, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,

                (byte) 0x22, (byte) 0x04, 0x00, 0x00,

                (byte) 0xE0, (byte) 0x04, 0x00, 0x00,

                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("0", runProgram(program).trim());
    }

    @Test
    public void testNotRegister() {
        byte[] program = new byte[] {
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,

                (byte) 0x24, (byte) 0x01, 0x00, 0x00,

                (byte) 0xE0, (byte) 0x01, 0x00, 0x00,

                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("-1", runProgram(program).trim());
    }

    @Test
    public void testAndRegisters() {
        byte[] program = new byte[] {
                // r1 = 6
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x06,

                // r2 = 3
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x03,

                // r1 & r2 → r2
                (byte) 0x25, (byte) 0x12, 0x00, 0x00,

                // print r2
                (byte) 0xE0, (byte) 0x02, 0x00, 0x00,

                // halt
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("2", runProgram(program).trim());
    }

    @Test
    public void testShiftLeftImmediate() {
        // Opcode: 26r---vv (vv > 0 is Left Shift)
        // r1 = 10
        // Shift left by 2 -> 40
        byte[] program = new byte[] {
                // Load r1 = 10
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A,

                // Shift r1 Left by 2
                // 26 r- -- vv -> 26 10 00 02
                (byte) 0x26, (byte) 0x10, 0x00, 0x02,

                // Print r1
                (byte) 0xE0, 0x01, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("40", runProgram(program).trim());
    }

    @Test
    public void testShiftRightImmediate() {
        // Opcode: 26r---vv (vv < 0 is Right Shift)
        // r1 = 40
        // Shift right by 2 -> 10
        // vv = -2 (0xFE)
        byte[] program = new byte[] {
                // Load r1 = 40
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x28,

                // Shift r1 Right by 2 (vv = FE)
                // 26 10 00 FE
                (byte) 0x26, (byte) 0x10, 0x00, (byte) 0xFE,

                // Print r1
                (byte) 0xE0, 0x01, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("10", runProgram(program).trim());
    }

    @Test
    public void testMultiplyRegisters() {
        // Opcode: 27rs---- (r[r] * r[s] -> r[s])
        byte[] program = new byte[] {
                // r1 = 5
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05,
                // r2 = 4
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,

                // Multiply r1 * r2 -> r2
                // 27 12 00 00
                (byte) 0x27, 0x12, 0x00, 0x00,

                // Print r2 (Should be 20)
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("20", runProgram(program).trim());
    }

    @Test
    public void testDivideRegisters() {
        // Opcode: 28rs---- (r[r] / r[s] -> r[s])
        byte[] program = new byte[] {
                // r1 = 100 (Numerator)
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64,

                // r2 = 6 (Divisor)
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06,

                // Divide r1 / r2 -> r2 = 16.6666...
                (byte) 0x28, 0x12, 0x00, 0x00,

                // Print r2 (Should be 16)
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        byte[] program2 = new byte[] {
                // r1 = 100 (Numerator)
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64,

                // r2 = 5 (Divisor)
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05,

                // Divide r1 / r2 -> r2.
                (byte) 0x28, 0x12, 0x00, 0x00,

                // Print r2 (Should be 20)
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("16", runProgram(program).trim());
        assertEquals("20", runProgram(program2).trim());
    }

    @Test
    public void testModuloRegisters() {
        // Opcode: 29rs---- (r[r] % r[s] -> r[s])
        // r1 = 10
        // r2 = 3
        // 10 % 3 = 1
        byte[] program = new byte[] {
                // r1 = 10
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A,

                // r2 = 3
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03,

                // Modulo r1 % r2 -> r2
                // 29 12 00 00
                (byte) 0x29, 0x12, 0x00, 0x00,

                // Print r2 (Should be 1)
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("1", runProgram(program).trim());
    }

    @Test
    public void testAsciiPrint() {
        byte[] program = new byte[] {
                // r3 = 65
                (byte) 0x03, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x41,

                // print ASCII from r3: e10r
                (byte) 0xE1, (byte) 0x03, 0x00, 0x00,

                // halt
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("A", runProgram(program));
    }

    @Test
    public void testNop() {
        byte[] program = new byte[] {
                (byte) 0xF0, 0x00, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        assertEquals("", runProgram(program).trim());
    }

    @Test
    public void testUnconditionalJump() {
        byte[] program = new byte[] {
                // r1 = 1
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,

                // Jump forward 1 instruction (offset 1)
                (byte) 0xA0, 0x00, 0x00, 0x01,

                // r1 = 99 (Skipped)
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x63,

                // Print r1
                (byte) 0xE0, 0x01, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertEquals("1", runProgram(program).trim());
    }

    @Test
    public void testJumpIfZeroTaken() {
        byte[] program = new byte[] {
                // r1 is 0 by default. Jump forward 1.
                (byte) 0xA1, 0x10, 0x00, 0x01,

                // r2 = 99 (Skipped)
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x63,

                // Print r2 (Should be 0)
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertEquals("0", runProgram(program).trim());
    }

    @Test
    public void testJumpIfZeroNotTaken() {
        byte[] program = new byte[] {
                // r1 = 5
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05,

                // Jump if r1 == 0 (Not taken)
                (byte) 0xA1, 0x10, 0x00, 0x01,

                // r2 = 99 (Executed)
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x63,

                // Print r2
                (byte) 0xE0, 0x02, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertEquals("99", runProgram(program).trim());
    }

    @Test
    public void testJumpIfGreaterThan() {
        byte[] program = new byte[] {
                // r1 = 10, r2 = 5
                (byte) 0x01, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A,
                (byte) 0x02, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05,

                // If r1 > r2 (True), Jump 1
                (byte) 0xA2, 0x12, 0x00, 0x01,

                // r3 = 1 (Skipped)
                (byte) 0x03, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,

                // Print r3 (Should be 0)
                (byte) 0xE0, 0x03, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertEquals("0", runProgram(program).trim());
    }
}
