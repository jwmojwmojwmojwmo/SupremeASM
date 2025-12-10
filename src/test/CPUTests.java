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
}
