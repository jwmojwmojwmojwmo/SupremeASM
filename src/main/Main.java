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
        */
        CPU cpu = new CPU();
        try {
            long startTime = System.nanoTime();
            byte[] instructions = parser.parse(input);
            long endTime = System.nanoTime();
            long durationNs = (endTime - startTime);  // Duration in nanoseconds
            System.out.println("Code compiled in " + (double) durationNs / 1000000 + " ms. Executing code...\n");
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
