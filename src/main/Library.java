package main;

import exceptions.BadCodeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// represents a library in ASM
// libraries may only have def functions, and can be imported with the import instruction
public class Library {
    private Path path; // path to library
    private Map<String, Integer> functions; // defined functions in library, with name of function and line that function is at in library
    private String cleanedCode; // library's code fully expanded

    public Library(Path path) throws IOException {
        this.path = path;
        this.functions = getDefinedFunctions();
    }

    // returns assembly version of defined func
    // since inputParser calls parseAssembly on this String, it is ok if this library depends on another
    public String call(String[] insParts) throws BadCodeException {
        String[] instructions = cleanedCode.trim().replaceAll("%sp", "a").split("\\s*;\\s*");
        String instruction = instructions[functions.get(insParts[0])];
        String[] funcDef = instruction.split("\\s*,\\s*");
        if (funcDef.length != insParts.length) {
            throw new BadCodeException();
        }
        HashMap<String, String> paramConversion = new HashMap<>();
        for (int i = 1; i < funcDef.length; i++) {
            paramConversion.put(funcDef[i], insParts[i]);
        }
        String currentLine = "";
        StringBuilder defBody = new StringBuilder();
        for (int j = (functions.get(insParts[0]) + 1); j < instructions.length; j++) {
            currentLine = instructions[j];
            if (currentLine.contains("enddef")) {
                break;
            }
            for (String param : paramConversion.keySet()) {
                if (currentLine.contains(param)) {
                    currentLine = currentLine.replace(param, paramConversion.get(param));
                }
            }
            defBody.append(currentLine).append("; ");
        }
        return defBody.toString();
    }

    public boolean containsFunc(String funcName) {
        return functions.containsKey(funcName);
    }

    private HashMap<String, Integer> getDefinedFunctions() throws IOException {
        // defined functions need to be:
        // def function, 1, 2, 3, 4
        // body;
        // body;
        // enddef;
        String code = Files.readString(path);
        code = getCleanCode(code);
        this.cleanedCode = code;
        HashMap<String, Integer> funcs = new HashMap<String, Integer>();
        String[] instructions = code.trim().replaceAll("%sp", "a").split("\\s*;\\s*");
        for (int i = 0; i < instructions.length; i++) {
            String instruction = instructions[i];
            if (instruction.contains("def") && !instruction.contains("enddef")) {
                String func = instruction.substring(4, instruction.indexOf(","));
                funcs.put(func, i);
            }
        }
        return funcs;
    }

    private String getCleanCode(String code) {
        StringBuilder cleanCode = new StringBuilder();
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("//")) {
                line = line.substring(0, line.indexOf("//"));
            }
            cleanCode.append(line).append("\n");
        }
        code = cleanCode.toString();
        return code;
    }
}
