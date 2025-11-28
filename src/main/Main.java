package main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Write the SupremeASM Machine code to execute:");
        Scanner userInput = new Scanner(System.in);
        String input = userInput.nextLine();
        // primary instructions are four or eight bytes long
        // instruction codes are given in hex
        // all instructions return whether or not they were successful, in the Status
        // register
        // if ee is the second byte, it is an eight byte instruction
        // 0: load instruction
        // 1: store
        // 2: reg mov/arithmetic instructions
        // e: log instruction
        // f: system instruction
        // vvvvvvvv -> r[r] = 0ree----vvvvvvvv
        // m[r[r] + o] -> r[s] = 0ros----
        // r[r] -> m[r[s] + o] = 1rso----
        // r[r] -> r[s] = 20rs----
        // r[r] + 1 -> r[r] = 210r----
        // r[r] - 1 -> r[r] = 220r----
        // r[r] + r[s] -> r[s] = 23rs----
        // ~r[r] -> r[r] = 240r----
        // r[r] & r[s] -> r[s] = 25rs----
        //
        // print(r[r]) = e00r----
        // print(m[r[r] + o]) = e0ro----
        // printWithFormatting(r[r]) ie ascii = e10r----
        // printWithFormatting(m[r[r] + o]) ie ascii = e1ro----
        // allocate x * 4 bytes of memory = f1ee----xxxxxxxx
        // deallocate memory block starting at address in register r = f20r----
        // defragment memory = f3------
        // dump CPU = fd------
        // dump memory = fe------
        // nop = f0------
        // halt = ffffffff
        System.out.println("Executing your code...\n");
        userInput.close();
        input = input.replaceAll("\\s+", "");
        try {
            if (input.length() % 8 != 0) {
                System.out.println("bad code compilation...one or more instructions is malformed");
                System.exit(0);
            }
            byte[] instructions = new byte[input.length() / 2];
            for (int i = 0; i < input.length(); i += 2) {
                instructions[i / 2] = (byte) Integer.parseInt(input.substring(i, i + 2), 16);
            }
            CPU cpu = new CPU();
            cpu.load(instructions);
            cpu.run();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("invalid code or a sysfault was triggered (probably the first one though)");
        }
        System.out.println("\nExecution completed");
        System.exit(0);
    }
}
