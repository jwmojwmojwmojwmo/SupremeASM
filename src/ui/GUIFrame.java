package ui;

import exceptions.BadCodeException;
import main.CPU;
import main.InputParser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;

public class GUIFrame extends JFrame {
    private final CodePanel editor;
    private final ConsolePanel console;
    private final CPU cpu;
    private final InputParser parser;
    private Thread runThread;
    private boolean manuallyStopped = false;
    private final String name = "SupremeIDE";
    private final String helpText = "<html>" +
            "<style>" +
            "  body { font-family: 'Segoe UI', sans-serif; font-size: 12px; margin: 10px; }" +
            "  h1 { color: #2c3e50; border-bottom: 2px solid #2c3e50; }" +
            "  h2 { background-color: #ecf0f1; padding: 5px; color: #2980b9; margin-top: 20px; }" +
            "  h3 { color: #e67e22; margin-bottom: 2px; }" +
            "  p { margin-top: 5px; line-height: 1.4; }" +
            "  code { font-family: 'Consolas', monospace; color: #c0392b; background-color: #fce4ec; padding: 2px; }" +
            "  pre { font-family: 'Consolas', monospace; background-color: #f8f9fa; padding: 10px; border: 1px solid #ddd; overflow: auto; }" +
            "  ul { margin-top: 5px; }" +
            "  li { margin-bottom: 5px; }" +
            "  table { border-collapse: collapse; width: 100%; margin-top: 10px; }" +
            "  th { background-color: #2c3e50; color: white; text-align: left; padding: 6px; border: 1px solid #ddd; }" +
            "  td { border: 1px solid #ddd; padding: 5px; vertical-align: top; font-size: 11px; }" +
            "  tr:nth-child(even) { background-color: #f2f2f2; }" +
            "  .size-col { text-align: center; font-weight: bold; color: #555; }" +
            "</style>" +
            "<body>" +
            "<h1>SupremeASM Reference</h1>" +
            "<h2>Language Rules</h2>" +
            "<p>Instructions are 4 or 8 bytes. Memory is stored as 4-byte ints (signed), ie one memory slot is 4 bytes. This also means when pc increments, it jumps one memory slot, or four bytes. Therefore instructions take up either one or two memory slots. </p>" +
            "<ul>" +
            "  <li><b>ASM Code:</b> Separated by <code>;</code>. Immediate values in Base 10 (e.g., <code>ld #1, 1</code>). Use <code>//</code> for comments. </li>" +
            "  <li><b>Registers in ASM:</b> The <code>%</code> symbol is optional (<code>%1</code>) except when using a register's special name (<code>%sp</code>).</li>" +
            "</ul>" +
            "<h2>Register Rules</h2>" +
            "<ul>" +
            "  <li><b>r0 - r9:</b> General Purpose.</li>" +
            "  <li><b>r0:</b> Receives return values (1=success, -1=fail, or data from inputs/mallocs).</li>" +
            "  <li><b>PC:</b> Program Counter (Inaccessible directly, use <code>gpc</code>).</li>" +
            "  <li><b>rA (r10 / sp):</b> Stack Pointer. Reference as <code>a</code> or <code>%sp</code> in ASM.</li>" +
            "</ul>" +
            "<h2>Instruction Set Architecture (ISA)</h2>" +
            "<table>" +
            "  <tr><th>Operation</th><th>ASM Code</th><th>Format/Semantics</th><th>Size</th></tr>" +
            "  <tr><td>Load Immediate</td><td><code>ld #v, r</code></td><td>v -> r[r]</td><td class='size-col'>8</td></tr>" +
            "  <tr><td>Load Base+Off</td><td><code>ld r+#o, s</code></td><td>m[r[r] + o] -> r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Load Indexed</td><td><code>ld r+o, s</code></td><td>m[r[r] + r[o]] -> r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Store Base+Off</td><td><code>st r, s+#o</code></td><td>r[r] -> m[r[s] + o]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Store Indexed</td><td><code>st r, s+o</code></td><td>r[r] -> m[r[s] + r[o]]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Move Register</td><td><code>mov r, s</code></td><td>r[r] -> r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Increment</td><td><code>inc r</code></td><td>r[r] + 1</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Decrement</td><td><code>dec r</code></td><td>r[r] - 1</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Add</td><td><code>add r, s</code></td><td>r[r] + r[s] -> r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Subtract (Macro)</td><td><code>sub r, s</code></td><td>(Macro: not, inc, add)</td><td class='size-col'>12</td></tr>" +
            "  <tr><td>Not (Bitwise)</td><td><code>not r</code></td><td>~r[r]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>And (Bitwise)</td><td><code>and r, s</code></td><td>r[r] & r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Bitshift</td><td><code>shf #v, r</code></td><td>Right if v is negative, Left otherwise</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Multiply</td><td><code>mul r, s</code></td><td>r[r] * r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Divide</td><td><code>div r, s</code></td><td>r[r] / r[s] (truncated)</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Modulus</td><td><code>mod r, s</code></td><td>r[r] % r[s]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Jump Indirect</td><td><code>jmp #o</code></td><td>pc + o -> pc</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>If Zero</td><td><code>ife r, #o</code></td><td>if r[r]==0 jump</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>If Greater</td><td><code>igt r, s, #o</code></td><td>if r[r] > r[s] jump</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Get PC</td><td><code>gpc r, #o</code></td><td>pc + o -> r[r]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Goto Address</td><td><code>goto r</code></td><td>r[r] -> pc</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Print (Int)</td><td><code>prt r</code></td><td>Print Register (Base 10)</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Print Mem (Off)</td><td><code>prt r+#o</code></td><td>Print m[r[r]+o]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Print Mem (Idx)</td><td><code>prt r+o</code></td><td>Print m[r[r]+r[o]]</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Print ASCII</td><td><code>prf r</code></td><td>Print as Char (formatting)</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Print ASCII Mem</td><td><code>prf r+#o</code></td><td>Print Mem as Char</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Input</td><td><code>inp</code></td><td>User Input -> r0</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Dump CPU</td><td><code>dpc</code></td><td>Print all Registers</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Dump Mem</td><td><code>dpm</code></td><td>Print non-zero Mem</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Malloc</td><td><code>moc #x</code></td><td>Allocate x slots (x*4 bytes)</td><td class='size-col'>8</td></tr>" +
            "  <tr><td>Free</td><td><code>free r</code></td><td>Deallocate block at r</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Defrag</td><td><code>dfg</code></td><td>Coalesce memory blocks</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Push</td><td><code>push r</code></td><td>Macro: inc sp, st r, sp</td><td class='size-col'>8</td></tr>" +
            "  <tr><td>Pop</td><td><code>pop r</code></td><td>Macro: ld sp, r, dec sp</td><td class='size-col'>8</td></tr>" +
            "  <tr><td>Call</td><td><code>call file.sasm</code></td><td>Include file inline</td><td class='size-col'>Var</td></tr>" +
            "  <tr><td>No Op</td><td><code>nop</code></td><td>Do nothing</td><td class='size-col'>4</td></tr>" +
            "  <tr><td>Halt</td><td><code>halt</code></td><td>Stop Execution (returns 0)</td><td class='size-col'>4</td></tr>" +
            "</table>" +

            "</body></html>";

    public GUIFrame() {
        super();
        setTitle(name);
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
        JButton helpBtn = new JButton("Help");
        helpBtn.setToolTipText("Open the documentation");
        helpBtn.addActionListener(e -> showHelp());
        JButton calcBtn = new JButton("Offset Calculator");
        calcBtn.setToolTipText("Calculates offset between two lines of code");
        calcBtn.addActionListener(e -> calculate());
        JButton byteBtn = new JButton("See Bytecode");
        byteBtn.setToolTipText("Displays bytecode of current ASM program");
        byteBtn.addActionListener(e -> showByte());
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
        toolbar.add(helpBtn);
        toolbar.add(calcBtn);
        toolbar.add(byteBtn);
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
                console.append("Code compilation failed starting at line @" + e.getErrLine());
            } catch (ArrayIndexOutOfBoundsException e) {
                console.append("Memory limit reached! Execution stopped.");
            } catch (Exception e) {
                console.append("Code fatally failed or a sysfault was triggered, with error code:");
                console.append(String.valueOf(e));
            } finally {
                if (manuallyStopped) {
                    console.append("Execution stopped manually.");
                } else {
                    console.append("Execution completed.");
                }
                console.append(""); 
            }
        });
        runThread.start();
    }

    private String getCleanCode(CodePanel editor) {
        StringBuilder cleanCode = new StringBuilder();
        String[] lines = editor.getCode().split("\n");
        for (String line : lines) {
            if (line.contains("//")) {
                line = line.substring(0, line.indexOf("//"));
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
                String code = Files.readString(selected.toPath());
                editor.setCode(code);
                console.append("Loaded: " + selected.getName());
                this.setTitle(name + ": " + selected.getName());
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
                Files.writeString(selected.toPath(), editor.getCode());
                console.append("Saved: " + selected.getName());
                this.setTitle(name + ": " + selected.getName());
            } catch (Exception e) {
                console.append("Error: " + e.getMessage());
            }
        }
    }

    private void showHelp() {
        JDialog helpDialog = new JDialog(this, "SupremeASM Help", false);
        JEditorPane helpPane = new JEditorPane("text/html", helpText);
        helpPane.setEditable(false);
        helpPane.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(helpPane);
        helpDialog.add(scrollPane);
        helpDialog.pack();
        helpDialog.setSize(500, 1000);
        helpDialog.setVisible(true);
    }

    private void calculate() {
        JDialog dialog = new JDialog(this, "Jump Calculator");
        dialog.setLayout(new FlowLayout());
        dialog.setSize(300, 150);
        JTextField fromField = new JTextField(5);
        JTextField toField = new JTextField(5);
        JLabel resultLabel = new JLabel("Offset: ?");
        JButton calcBtn = new JButton("Calculate");
        calcBtn.addActionListener(e -> {
            try {
                int from = Integer.parseInt(fromField.getText());
                int to = Integer.parseInt(toField.getText());
                int offset = calculateOffset(from, to);
                resultLabel.setText("Offset: " + (offset / 4) + " memory slots (" + offset + " bytes)");
                resultLabel.setForeground(Color.BLUE);
            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid Numbers");
                resultLabel.setForeground(Color.RED);
            } catch (BadCodeException ex) {
                resultLabel.setText("Invalid code: compilation failed.");
                resultLabel.setForeground(Color.RED);
            }
        });
        dialog.add(new JLabel("From Line:"));
        dialog.add(fromField);
        dialog.add(new JLabel("To Line:"));
        dialog.add(toField);
        dialog.add(calcBtn);
        dialog.add(resultLabel);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int calculateOffset(int fromLine, int toLine) throws BadCodeException {
        String[] lines = editor.getCode().split("\n");
        String code = getCleanCode(editor);
        parser.parse(code);
        int offset = 0;
        if (fromLine < 1 || toLine < 1 || fromLine > lines.length || toLine > lines.length) {
            return 0;
        }
        int start = Math.min(fromLine, toLine) - 1;
        int end = Math.max(fromLine, toLine) - 1;
        for (int i = start; i < end; i++) {
            offset += getInstructionSize(lines[i]);
        }
        if (toLine < fromLine) {
            offset = -offset; // Jumping backwards
        }
        offset = offset - getInstructionSize(lines[fromLine - 1]);
        return offset;
    }

    private int getInstructionSize(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//") || line.startsWith(";")) {
            return 0;
        }
        if (line.startsWith("sub")) {
            return 12;
        }
        if (line.startsWith("ld #") || line.startsWith("moc #") || line.startsWith("push") || line.startsWith("pop")) {
            return 8;
        }
        return 4;
    }

    private void showByte() {
        String code = getCleanCode(editor);
        byte[] bytecode;
        JDialog dialog = new JDialog(this, "Machine Code Viewer", false);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 300);
        JTextArea hexArea = new JTextArea();
        hexArea.setEditable(false);
        hexArea.setFont(new Font("Monospaced", java.awt.Font.PLAIN, 14));
        hexArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(hexArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        try {
            bytecode = parser.parse(code);
            String formattedHex = formatBytesToHex(bytecode);
            hexArea.setText(formattedHex);
        } catch (BadCodeException e) {
            hexArea.setText("Code compilation failed starting at line @" + e.getErrLine());
        }
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String formatBytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X", data[i]));
            sb.append(" ");
            if ((i + 1) % 16 == 0) { // new line every 16 bytes
                sb.append("\n");
            }
        }
        return sb.toString();
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
