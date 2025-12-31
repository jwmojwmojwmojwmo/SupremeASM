package ui;

import exceptions.BadCodeException;
import main.CPU;
import main.InputParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class GUIFrame extends JFrame {
    private final CodePanel editor;
    private final ConsolePanel console;
    private final CPU cpu;
    private final InputParser parser;
    private Thread runThread;

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
        String code = editor.getCode();
        cpu.reset();
        redirectSystemIn();
        console.clear();
        runThread = new Thread(() -> {
            console.append("Compiling...");
            try {
                long startTime = System.nanoTime();
                byte[] instructions = parser.parse(code);
                long endTime = System.nanoTime();
                long durationNs = (endTime - startTime);  // Duration in nanoseconds
                console.clear();
                console.append("Code compiled in " + (double) durationNs / 1000000 + " ms. Executing code...\n");
                cpu.load(instructions);
                cpu.run();
            } catch (BadCodeException e) {
                console.append("Code compilation failed. One or more instructions are malformed.");
            } catch (Exception e) {
                console.append("Code fatally failed or a sysfault was triggered, with error code:");
                console.append(String.valueOf(e));
            } finally {
                console.append("Execution completed.");
            }
        });
        runThread.start();
    }

    private void stop() {
        cpu.isRunning = false;
        console.append("Execution stopped manually.");
    }

    private void openFile() {

    }

    private void saveFile() {

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
