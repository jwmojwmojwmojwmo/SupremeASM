package main;

import exceptions.BadCodeException;

import java.util.Arrays;

public class InputParser {
    public InputParser() {
    }

    public byte[] parse(String input) throws BadCodeException {
        try {
            if (Character.isDigit(input.charAt(0))) {
                return parseMachine(input);
            } else {
                return parseAssembly(input);
            }
        } catch (Exception e) {
            throw new BadCodeException();
        }
    }

    private byte[] parseMachine(String input) throws BadCodeException {
        input = input.replaceAll("\\s+", "");
        if (input.length() % 8 != 0) {
            throw new BadCodeException();
        }
        byte[] instructions = new byte[input.length() / 2];
        for (int i = 0; i < input.length(); i += 2) {
            instructions[i / 2] = (byte) Integer.parseInt(input.substring(i, i + 2), 16);
        }
        return instructions;
    }

    private byte[] parseAssembly(String input) throws BadCodeException {
        String[] instructions = input.trim().split("\\s*;\\s*");
        StringBuilder machineCode = new StringBuilder();
        for (String instruction: instructions) {
            machineCode.append(translate(instruction));
        }
        return parseMachine(machineCode.toString());
    }

    private String translate(String instruction) throws BadCodeException {
        String[] insParts = instruction.replace(",", " ").trim().split("\\s+");
        return switch (insParts[0]) {
            case "ld" -> translateLoad(insParts);
            case "st" -> translateStore(insParts);
            case "mov" -> translateMov(insParts);
            case "inc", "dec", "add", "not", "and", "shf", "mul", "div", "mod", "sub" -> translateArithmetic(insParts);
            case "jmp", "ife", "igt", "gpc", "goto" -> translateControl(insParts);
            case "prt", "prf" -> translatePrint(insParts);
            case "moc", "doc" -> translateSystem(insParts);
            case "dfg" -> {
                yield "f3ffffff";
            }
            case "inp" -> {
                yield "f4ffffff";
            }
            case "dpc" -> {
                yield "fdffffff";
            }
            case "dpm" -> {
                yield "feffffff";
            }
            case "nop" -> {
                yield "f0ffffff";
            }
            case "halt" -> {
                yield "ffffffff";
            }

            default -> throw new BadCodeException();
        };
    }

    //TODO
    private String translateLoad(String[] insParts) {
        return null;
    }

    private String translateStore(String[] insParts) {
        return null;
    }

    private String translateMov(String[] insParts) {
        return null;
    }

    private String translateArithmetic(String[] insParts) {
        return null;
    }

    private String translateControl(String[] insParts) {
        return null;
    }

    private String translatePrint(String[] insParts) {
        return null;
    }

    private String translateSystem(String[] insParts) {
        return null;
    }
}
