package ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CodePanel extends JPanel {
    private final JTextArea labelArea;
    private final RSyntaxTextArea codeArea;

    public CodePanel() {
        setLayout(new BorderLayout());
        // main code area
        codeArea = new RSyntaxTextArea(20, 60);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
        codeArea.setCodeFoldingEnabled(false);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        // label area (left)
        labelArea = new JTextArea();
        labelArea.setColumns(5);
        labelArea.setBackground(new Color(225, 225, 225));
        labelArea.setForeground(new Color(0, 0, 0));
        labelArea.setFont(codeArea.getFont());
        // set up main scroll pane
        RTextScrollPane scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setFoldIndicatorEnabled(true);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(labelArea, BorderLayout.WEST);
        labelPanel.add(scrollPane.getGutter(), BorderLayout.CENTER);
        scrollPane.setRowHeaderView(labelPanel);
        add(scrollPane, BorderLayout.CENTER);
        codeArea.setText("test");
    }

    public Map<String, Integer> getLabelMap() throws BadLocationException {
        Map<String, Integer> labelMap = new HashMap<>();
        Element root = labelArea.getDocument().getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element lineElement = root.getElement(i);
            int start = lineElement.getStartOffset();
            int end = lineElement.getEndOffset();
            String text = labelArea.getText(start, end - start).trim();
            if (!text.isEmpty()) {
                labelMap.put(text, i + 1);
            }// i is line number
        }
        return labelMap;
    }

    public String getCode() {
        return codeArea.getText();
    }

    public void setCode(String code) {
        codeArea.setText(code);
        codeArea.setCaretPosition(0); // Scroll to top
    }
}
