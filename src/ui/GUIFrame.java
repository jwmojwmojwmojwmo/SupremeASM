package ui;

import exceptions.BadCodeException;
import main.CPU;
import main.InputParser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class GUIFrame extends JFrame {
    private final CodePanel editor;
    private final ConsolePanel console;
    private final CPU cpu;
    private final InputParser parser;
    private Thread runThread;
    private boolean manuallyStopped = false;
    private Map<String, Integer> labels;
    private File currentFile = null;

    public GUIFrame(String name) {
        super(name);
        setSize(1200, 800);
        editor = new CodePanel();
        console = new ConsolePanel();
        createToolBar();
        add(editor, BorderLayout.CENTER);
        add(console, BorderLayout.SOUTH);
        parser = new InputParser();
        redirectSystemOut();
        cpu = new CPU();
    }

    private void createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        JButton saveBtn = new JButton("Save");
        saveBtn.setToolTipText("Save code to a file");
        saveBtn.addActionListener(e -> saveFile());
        JButton openBtn = new JButton("Open");
        openBtn.setToolTipText("Open a saved file");
        openBtn.addActionListener(e -> openFile());
        JButton runBtn = new JButton("Run");
        runBtn.setToolTipText("Compile and Run");
        runBtn.setBackground(new Color(0, 150, 0));
        runBtn.setForeground(Color.WHITE);
        runBtn.addActionListener(e -> run());
        JButton stopBtn = new JButton("Stop");
        stopBtn.setToolTipText("Halt Execution");
        stopBtn.setBackground(new Color(150, 0, 0));
        stopBtn.setForeground(Color.WHITE);
        stopBtn.addActionListener(e -> stop());
        JButton clearBtn = new JButton("Clear");
        clearBtn.setToolTipText("Clear Console Output");
        clearBtn.addActionListener(e -> console.clear());
        toolbar.add(saveBtn);
        toolbar.add(openBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(runBtn);
        toolbar.add(stopBtn);
        toolbar.addSeparator();
        toolbar.add(clearBtn);
        toolbar.addSeparator();
        add(toolbar, BorderLayout.NORTH);
    }

    private void run() {
        cpu.reset();
        manuallyStopped = false;
        redirectSystemIn();
        console.clear();
        try {
            labels = editor.getLabelMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (!labels.isEmpty()) {
//            console.append("Labels found: " + labels);
//        }
        runThread = new Thread(() -> {
            console.append("Compiling...");
            try {
                long startTime = System.nanoTime();
                String code = getCleanCode(editor);
                byte[] instructions = parser.parse(code);
                long endTime = System.nanoTime();
                long durationNs = (endTime - startTime);  // Duration in nanoseconds
                console.clear();
                console.append("Code compiled in " + (double) durationNs / 1000000 + " ms. Executing code...\n");
                cpu.load(instructions);
                cpu.run();
            } catch (BadCodeException e) {
                console.append("Code compilation failed. One or more instructions are malformed.");
            } catch (ArrayIndexOutOfBoundsException e) {
                console.append("Memory limit reached! Execution stopped.");
            } catch (Exception e) {
                console.append("Code fatally failed or a sysfault was triggered, with error code:");
                console.append(String.valueOf(e));
            } finally {
                if (manuallyStopped) {
                    console.append("Execution stopped manually.");
                } else {
                    console.append("Execution completed");
                }
            }
        });
        runThread.start();
    }

    private String getCleanCode(CodePanel editor) {
        StringBuilder cleanCode = new StringBuilder();
        String[] lines = editor.getCode().split("\n");
        for (String line : lines) {
            int i = line.indexOf("//");
            if (i != -1) {
                line = line.substring(0, i);
            }
            cleanCode.append(line).append("\n");
        }

        return cleanCode.toString();
    }

    private void stop() {
        cpu.halt();
        if (runThread != null && runThread.isAlive()) {
            runThread.interrupt();
        }
        manuallyStopped = true;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./scripts"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("SupremeASM Files", "sasm"));

        int result = fileChooser.showOpenDialog(editor);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                String code = java.nio.file.Files.readString(selected.toPath());
                editor.setCode(code);
                currentFile = selected;
                console.append("Loaded: " + selected.getName());
            } catch (Exception e) {
                console.append("Error: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./scripts")); // Start in default scripts folder
        // Filter for .sasm files
        fileChooser.setFileFilter(new FileNameExtensionFilter("SupremeASM Files", "sasm"));
        int result = fileChooser.showSaveDialog(editor);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            // Auto-add extension if missing
            if (!selected.getName().toLowerCase().endsWith(".sasm")) {
                selected = new File(selected.getAbsolutePath() + ".sasm");
            }
            try {
                java.nio.file.Files.writeString(selected.toPath(), editor.getCode());
                currentFile = selected; // Remember this file
                console.append("Saved: " + selected.getName());
            } catch (Exception e) {
                console.append("Error: " + e.getMessage());
            }
        }

    }

    private void redirectSystemOut() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                console.appendWithoutFormatting(String.valueOf((char) b));
            }
            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                console.appendWithoutFormatting(text);
            }
        };
        PrintStream newStream = new PrintStream(out, true); // true = auto-flush
        System.setOut(newStream); // Capture normal System.out.println
    }

    private void redirectSystemIn() {
        PipedOutputStream guiWriter = new PipedOutputStream();
        PipedInputStream systemReader = null;
        try {
            systemReader = new PipedInputStream(guiWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.setIn(systemReader);
        console.setInputPipe(guiWriter);
    }
}
