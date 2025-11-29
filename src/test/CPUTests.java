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
    public void testMalloc() {
        // Program:
        // malloc(4 * 5) -> f1ee----00000005
        // dumpMem() -> fe------
        // halt -> ffffffff

        byte[] program = new byte[] {
                // malloc 5 and 8 slots]
                (byte) 0xF1, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x05,
                (byte) 0xF1, (byte) 0xEE, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x08,
                // dumpMem
                (byte) 0xFE, 0x00, 0x00, 0x00,

                // halt
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        String dump = runProgram(program);
        assertTrue(dump.contains("0: 4"));
        assertTrue(dump.contains("61: 5"));
        assertTrue(dump.contains("67: 1"));
        assertTrue(dump.contains("68: 8"));
        assertTrue(dump.contains("77: 1"));
    }
}
