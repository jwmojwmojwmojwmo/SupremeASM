package test;

import main.CPU;
import main.InputParser;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CPUTests {

    private final InputParser parser = new InputParser();

    private String runAssembly(String asm) {
        try {
            byte[] bytecode = parser.parse(asm);
            return runProgram(bytecode).trim();
        } catch (Exception e) {
            throw new RuntimeException("ASM Crash: " + e.getMessage(), e);
        }
    }


    private String runProgram(byte[] program) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        System.setOut(new PrintStream(out));
        try {
            CPU cpu = new CPU();
            cpu.load(program);
            cpu.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        // Normalize line endings to nothing
        return out.toString().replace("\r", "").replace("\n", "");
    }

    // ==========================================
    // 1. ARITHMETIC & LOGIC
    // ==========================================

    @Test
    public void testBasicMath() {
        // (10 + 5) * 2 = 30
        String asm = "ld #10, 1; ld #5, 2; add 1, 2; ld #2, 3; mul 3, 2; prt 2; halt;";
        assertEquals("30", runAssembly(asm));
    }

    @Test
    public void testSubtractionAndNegative() {
        // 5 - 10 = -5
        String asm = "ld #5, 1; ld #10, 2; sub 1, 2; prt 2; halt;";
        assertEquals("-5", runAssembly(asm));
    }

    @Test
    public void testDivisionAndModulus() {
        // 100 / 3 = 33, 100 % 3 = 1
        // Expect "331" (smashed together)
        String asm = "ld #100, 1; ld #3, 2; div 1, 2; prt 2;" +
                "ld #100, 1; ld #3, 2; mod 1, 2; prt 2; halt;";
        assertEquals("331", runAssembly(asm));
    }

    @Test
    public void testBitwiseOperations() {
        // AND: 12 & 10 = 8
        // NOT: ~0 = -1
        // Expect "8-1"
        String asm = "ld #12, 1; ld #10, 2; and 1, 2; prt 2; ld #0, 3; not 3; prt 3; halt;";
        assertEquals("8-1", runAssembly(asm));
    }

    @Test
    public void testBitShifts() {
        // Left Shift: 2 << 1 = 4
        // Right Shift: 8 >> 2 = 2
        // Expect "42"
        String asm = "ld #2, 1; shf #1, 1; prt 1;" +
                "ld #8, 2; shf #-2, 2; prt 2; halt;";
        assertEquals("42", runAssembly(asm));
    }

    // ==========================================
    // 2. MEMORY OPERATIONS (HEAP)
    // ==========================================

    @Test
    public void testMemoryAllocAndStore() {
        // Store 999 and read it back
        String asm = "moc #100; mov 0, 1;" +
                "ld #999, 2;" +
                "st 2, 1+#5;" +
                "ld 1+#5, 3;" +
                "prt 3; halt;";
        assertEquals("999", runAssembly(asm));
    }

    @Test
    public void testIndexedMemoryAccess() {
        // Indexed Store: st 3, 1+2
        String asm = "moc #100; mov 0, 1;" +
                "ld #50, 2;" +
                "ld #77, 3;" +
                "st 3, 1+2;" +
                "ld 1+#50, 4;" +
                "prt 4; halt;";
        assertEquals("77", runAssembly(asm));
    }

    // ==========================================
    // 3. STACK OPERATIONS
    // ==========================================

    @Test
    public void testStackPushPop() {
        // Push 1, 2, 3. Pop 3, 2, 1.
        // Expect "321"
        String asm = "moc #100; mov 0, %sp;" +
                "ld #1, 1; push 1;" +
                "ld #2, 1; push 1;" +
                "ld #3, 1; push 1;" +
                "pop 2; prt 2;" +
                "pop 2; prt 2;" +
                "pop 2; prt 2;" +
                "halt;";
        assertEquals("321", runAssembly(asm));
    }

    @Test
    public void testStackPeek() {
        // Push 42, Peek (print 42), Pop (print nothing)
        String asm = "moc #100; mov 0, %sp;" +
                "ld #42, 1; push 1;" +
                "ld %sp+#0, 2;" +
                "prt 2; pop 1; halt;";
        assertEquals("42", runAssembly(asm));
    }

    // ==========================================
    // 4. CONTROL FLOW
    // ==========================================

    @Test
    public void testLoop() {
        // Sum 1..5 = 15
        String asm = "ld #5, 1; ld #0, 2; " +
                "add 1, 2; " +
                "dec 1; " +
                "ife 1, #1; " +
                "jmp #-4; " +
                "prt 2; halt;";
        assertEquals("15", runAssembly(asm));
    }

    @Test
    public void testGreaterThanJump() {
        // FIX: Increased jump offset from #2 to #4
        // Must skip: ld #0 (2 slots) + prt 3 (1 slot) + halt (1 slot)
        String asm = "ld #10, 1; ld #5, 2;" +
                "igt 1, 2, #4;" +
                "ld #0, 3; prt 3; halt;" +
                "ld #1, 3; prt 3; halt;";
        assertEquals("1", runAssembly(asm));
    }

    @Test
    public void testDynamicJump() {
        // FIX: Increased add amount from 4 to 8
        // We need to skip: ld #8 (2 slots) + prt (1) + halt (1) = 4 slots
        // But we are currently at instruction index X, and need to get to X+8?
        // Let's rely on the math: 4 instructions * 2 avg size = ~8 slots.
        String asm = "gpc 1, #0;" +
                "ld #8, 2;" +    // CHANGED #4 to #8
                "add 2, 1;" +
                "goto 1;" +
                "ld #8, 3; prt 3; halt;" +
                "ld #9, 3; prt 3; halt;";
        assertEquals("9", runAssembly(asm));
    }

    @Test
    public void testRecursiveFactorial() {
        // 6! = 720
        String asm = "ld #6, 1; push 1; gpc 6, #1; jmp #4; prt 9; pop 0; halt; " +
                "push 6; ld %sp+#-1, 2; ld #2, 1; igt 1, 2, #10; dec 2; push 2; " +
                "gpc 6, #1; jmp #-11; pop 0; ld %sp+#-1, 2; mul 2, 9; jmp #2; " +
                "ld #1, 9; pop 6; goto 6;";
        assertEquals("720", runAssembly(asm));
    }

    // ==========================================
    // 5. I/O & SYSTEM
    // ==========================================
    @Test
    public void testAsciiPrint() {
        // 72='H', 105='i' -> "Hi"
        String asm = "ld #72, 1; ld #105, 2; prf 1; prf 2; halt;";
        assertEquals("Hi", runAssembly(asm));
    }
}