package com.stardew.gui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Set;

@Getter
@Setter
public class PasteInputDialog {

    private final BasicWindow window;
    private final TextBox textBox;

    @Getter(AccessLevel.NONE)
    private DialogListener dialogListener;

    public PasteInputDialog(String title) {
        window = new BasicWindow(title);
        window.setFixedSize(new TerminalSize(40, 2));
        window.setHints(Set.of(Window.Hint.CENTERED));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new AbsoluteLayout());
        mainPanel.setPosition(new TerminalPosition(0, 0));
        mainPanel.setSize(new TerminalSize(40, 3));
        window.setComponent(mainPanel);

        Panel textPanel = new Panel();
        textPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        textPanel.setPosition(new TerminalPosition(0, 0));
        textPanel.setSize(new TerminalSize(40, 1));

        textBox = new TextBox(new TerminalSize(40, 1))
                .addTo(textPanel);
        textPanel.addTo(mainPanel);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.setPosition(new TerminalPosition(0, 1));
        buttonPanel.setSize(new TerminalSize(40, 1));
        new Button("Ok").addTo(buttonPanel).addListener(button -> {
            if (dialogListener != null) {
                dialogListener.onAccept(textBox.getText());
            } else {
                window.close();
            }
        });
        new Button("Cancel").addTo(buttonPanel).addListener(button -> {
            if (dialogListener != null) {
                dialogListener.onCancel();
            } else {
                window.close();
            }
        });
        new Button("From Clipboard") {
            @Override
            protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                super.afterEnterFocus(direction, previouslyInFocus);
                textBox.setText(getClipboardContents());
            }

            @Override
            protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
                textBox.setText("");
            }
        }.addTo(buttonPanel).addListener(button -> {
            textBox.setText(getClipboardContents());
            window.close();
        });
        buttonPanel.addTo(mainPanel);
        textBox.takeFocus();
    }

    private String getClipboardContents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            // Get the clipboard contents
            String data = (String) clipboard.getData(DataFlavor.stringFlavor);
            System.out.println("Clipboard contents: " + data);
            return data;
        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Clipboard does not contain text data: " + e.getMessage());
        }
        return "";
    }

    public String show(WindowBasedTextGUI gui) {
        gui.addWindowAndWait(window);
        return textBox.getText();
    }

    public interface DialogListener {
        void onAccept(String input);

        void onCancel();
    }
}
