package ui;

import javax.swing.*;
import java.awt.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

public class MainGUI extends JPanel {

    private RSyntaxTextArea textArea;

    public MainGUI() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setFoldIndicatorEnabled(true);
        add(sp, BorderLayout.CENTER);
    }

    public String getCode() {
        return textArea.getText();
    }

    public void setCode(String code) {
        textArea.setText(code);
        textArea.setCaretPosition(0); // Scroll to top
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SupremeIDE Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MainGUI());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}