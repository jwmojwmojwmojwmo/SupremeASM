package main;

import exceptions.BadCodeException;

public class InputParser {
    public InputParser() {
    }

    public byte[] parse(String input) throws BadCodeException {
        try {
            if (Character.isDigit(input.charAt(0))) {
                return parseMachine(input);
            } else {
                return parseMachine(parseAssembly(input));
            }
        } catch (Exception e) {
            throw new BadCodeException();
        }
    }

    // returns CPU readable instructions from machine code
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

    // returns machine code from assembly
    private String parseAssembly(String input) throws BadCodeException {
        String[] instructions = input.trim().replaceAll("%sp", "a").split("\\s*;\\s*");
        StringBuilder machineCode = new StringBuilder();
        for (String instruction : instructions) {
            machineCode.append(translate(instruction));
        }
        return machineCode.toString();
    }

    private String translate(String instruction) throws BadCodeException {
        String[] insParts = instruction.replace(",", " ").replace("%", " ").trim().split("\\s+");
        return switch (insParts[0]) {
            case "ld" -> translateLoad(insParts);
            case "st" -> translateStore(insParts);
            case "mov" -> "20" + insParts[1] + insParts[2] + "ffff";
            case "inc", "dec", "add", "not", "and", "shf", "mul", "div", "mod", "sub" -> translateArithmetic(insParts);
            case "jmp", "ife", "igt", "gpc", "goto" -> translateControl(insParts);
            case "prt", "prf" -> translatePrint(insParts);
            case "moc", "doc", "push", "pop" -> translateSystem(insParts);
            case "dfg" -> "f3ffffff";
            case "inp" -> "f4ffffff";
            case "dpc" -> "fdffffff";
            case "dpm" -> "feffffff";
            case "nop" -> "f0ffffff";
            case "halt" -> "ffffffff";
            default -> throw new BadCodeException();
        };
    }

    private String translateLoad(String[] insParts) {
        String instruction;
        if (insParts[1].contains("+")) {
            if (insParts[1].contains("#")) {
                instruction = "00" + insParts[1].charAt(0) + insParts[2] + String.format("%04x", Integer.decode(insParts[1].substring(3)) & 0xFFFF);
            } else {
                instruction = "01" + insParts[1].charAt(0) + insParts[1].charAt(2) + insParts[2] + "fff";
            }
        } else {
            instruction = "0" + insParts[2] + "eeffff" + String.format("%08x", Integer.decode(insParts[1].substring(1)));
        }
        return instruction;
    }

    private String translateStore(String[] insParts) {
        String instruction;
        if (insParts[2].contains("#")) {
            instruction = "10" + insParts[1] + insParts[2].charAt(0) + String.format("%04x", Integer.decode(insParts[2].substring(3)) & 0xFFFF);
        } else {
            instruction = "11" + insParts[1] + insParts[2].charAt(0) + insParts[2].charAt(2) + "fff";
        }
        return instruction;
    }

    private String translateArithmetic(String[] insParts) throws BadCodeException {
        String instruction;
        switch (insParts[0]) {
            case "inc":
                instruction = "210" + insParts[1] + "ffff";
                break;
            case "dec":
                instruction = "220" + insParts[1] + "ffff";
                break;
            case "add":
                instruction = "23" + insParts[1] + insParts[2] + "ffff";
                break;
            case "not":
                instruction = "240" + insParts[1] + "ffff";
                break;
            case "and":
                instruction = "25" + insParts[1] + insParts[2] + "ffff";
                break;
            case "shf":
                instruction = "26" + insParts[2] + "fff" + String.format("%02x", Integer.decode(insParts[1].substring(1)) & 0xFF);
                break;
            case "mul":
                instruction = "27" + insParts[1] + insParts[2] + "ffff";
                break;
            case "div":
                instruction = "28" + insParts[1] + insParts[2] + "ffff";
                break;
            case "mod":
                instruction = "29" + insParts[1] + insParts[2] + "ffff";
                break;
            case "sub":
                String f = insParts[1];
                String s = insParts[2];
                instruction = parseAssembly("not " + s + "; inc " + s + "; add " + f + ", " + s + ";");
                break;
            default:
                throw new BadCodeException();
        }
        return instruction;
    }

    private String translateControl(String[] insParts) throws BadCodeException {
        String instruction = switch (insParts[0]) {
            case "jmp" -> "a00f" + String.format("%04x", Integer.decode(insParts[1].substring(1)) & 0xFFFF);
            case "ife" -> "a1" + insParts[1] + "f" + String.format("%04x", Integer.decode(insParts[2].substring(1)) & 0xFFFF);
            case "igt" -> "a2" + insParts[1] + insParts[2] + String.format("%04x", Integer.decode(insParts[3].substring(1)) & 0xFFFF);
            case "gpc" -> "a3" + insParts[1] + "f" + String.format("%04x", Integer.decode(insParts[2].substring(1)) & 0xFFFF);
            case "goto" -> "a4" + insParts[1] + "fffff";
            default -> throw new BadCodeException();
        };
        return instruction;
    }

    private String translatePrint(String[] insParts) {
        String instruction;
        if (insParts[0].equals("prt")) {
            if (insParts[1].contains("#")) {
                instruction = "e1" + insParts[1].charAt(0) + "f" + String.format("%04x", Integer.decode(insParts[1].substring(3)) & 0xFFFF);
            } else if (insParts[1].contains("+")){
                instruction = "e2" + insParts[1].charAt(0) + insParts[1].charAt(2) + "ffff";
            } else {
                instruction = "e00" + insParts[1] + "ffff";
            }
        } else {
            if (insParts[1].contains("#")) {
                instruction = "e4" + insParts[1].charAt(0) + "f" + String.format("%04x", Integer.decode(insParts[1].substring(3)) & 0xFFFF);
            } else if (insParts[1].contains("+")) {
                instruction = "e5" + insParts[1].charAt(0) + insParts[1].charAt(2) + "ffff";
            } else {
                instruction = "e30" + insParts[1] + "ffff";
            }
        }
        return instruction;
    }

    private String translateSystem(String[] insParts) throws BadCodeException {
        String instruction = switch (insParts[0]) {
            case "moc" -> "f1eeffff" + String.format("%08x", Integer.decode(insParts[1].substring(1)));
            case "doc" -> "f20" + insParts[1] + "ffff";
            case "push" -> parseAssembly("inc a; st " + insParts[1] + ", a+#0");
            case "pop" -> parseAssembly("ld a+#0, " + insParts[1] + "; dec a");
            default -> throw new BadCodeException();
        };
        return instruction;
    }
}

