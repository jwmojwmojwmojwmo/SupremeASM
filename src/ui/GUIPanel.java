package ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class GUIPanel extends JPanel {

        private final RSyntaxTextArea textArea;
        public GUIPanel() {
            setLayout(new BorderLayout());

            textArea = new RSyntaxTextArea(20, 60);
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
            textArea.setCodeFoldingEnabled(true);
            textArea.setAntiAliasingEnabled(true);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            RTextScrollPane scrollPane = new RTextScrollPane(textArea);
            scrollPane.setFoldIndicatorEnabled(true);
            add(scrollPane, BorderLayout.CENTER);
        }

        public String getCode() {
            return textArea.getText();
        }

        public void setCode(String code) {
            textArea.setText(code);
            textArea.setCaretPosition(0); // Scroll to top
        }
}
