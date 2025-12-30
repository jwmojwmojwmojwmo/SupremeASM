package main;

import exceptions.BadCodeException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to SupremeASM v0.1a");
        System.out.println("Write the SupremeASM Machine code to execute:");
        Scanner userInput = new Scanner(System.in);
        InputParser parser = new InputParser();
        String input = userInput.nextLine();
        /*
         primary instructions are four or eight bytes long
         instruction codes are given in hex
         all instructions return whether or not they were successful, in r0
         if ee is the second byte, it is an eight byte instruction
         0: load instruction
         1: store
         2: reg mov/arithmetic instructions
         a: control flow
         e: log instruction
         f: system instruction
         ASM: separated with ;
         vvvvvvvv -> r[r] = 0ree----vvvvvvvv = ld #v, r
         m[r[r] + o] -> r[s] = 00rsoooo = ld r+#o, s
         m[r[r] + r[o]] -> r[s] = 01ros--- = ld r+o, s
         r[r] -> m[r[s] + o] = 10rsoooo = st r, s+#o
         r[r] -> m[r[s] + r[o]] = 11rso--- = st r, s+o
         r[r] -> r[s] = 20rs---- = mov r, s
         r[r] + 1 -> r[r] = 210r---- = inc r
         r[r] - 1 -> r[r] = 220r---- = dec r
         r[r] + r[s] -> r[s] = 23rs---- = add r, s
         ~r[r] -> r[r] = 240r---- = not r
         r[r] & r[s] -> r[s] = 25rs---- = and r, s
         r[r] << v -> r[r] = 26r---vv = shf #v, r
         r[r] >> v -> r[r] = 26r---vv (when vv is negative)  = shf #v, r
         r[r] * r[s] -> r[s] = 27rs---- = mul r, s
         r[r] / r[s] -> r[s] = 28rs---- = div r, s
         r[r] % r[s] -> r[s] = 29rs---- = mod r, s
         pc + o -> pc = a00-oooo = jmp #o
         if r[r] == 0, then pc + o -> pc = a1r-oooo = ife r, #o
         if r[r] > r[s], then pc + o -> pc = a2rsoooo = igt r, s, #o
         pc + o -> r[r] = a3r-oooo = gpc r, #o
         vvvvvvvv -> pc = afee----vvvvvvvv = goto #v
         print(r[r]) = e00r---- prt r
         print(m[r[r] + o]) = e1r-oooo = prt r+#o
         print (m[r[r]+r[o]]) = e2ro---- = prt r+o
         printWithFormatting(r[r]) ie ascii = e30r---- prf r
         printWithFormatting(m[r[r] + o]) ie ascii = e4r-oooo = prf r+#o
         printWithFormatting (m[r[r]+r[o]]) = e5ro---- = prf r+o
         allocate x * 4 bytes of memory = f1ee----xxxxxxxx = moc #x
         deallocate memory block starting at address in register r = f20r---- = doc r
         defragment memory = f3------ = dfg
         get user input = f4------ = inp
         dump CPU = fd------ = dpc
         dump memory = fe------ = dpm
         nop = f0------ = nop
         halt = ffffffff = halt
         ASM only instructions:
         r[r] - r[s] -> r[s] = not s; inc s; add r, s; = sub r, s
        */
        CPU cpu = new CPU();
        try {
            long startTime = System.nanoTime();
            byte[] instructions = parser.parse(input);
            long endTime = System.nanoTime();
            long durationNs = (endTime - startTime);  // Duration in nanoseconds
            System.out.println("Code compiled in " + durationNs / 1000000 + " ms. Executing code...\n");
            cpu.load(instructions);
            cpu.run();
        } catch (BadCodeException e) {
            System.out.println("Code compilation failed. One or more instructions are malformed.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Code fatally failed or a sysfault was triggered (probably the first one though)");
        }
        userInput.close();
        System.out.println("\n\nExecution completed");
        System.exit(0);
    }
}
