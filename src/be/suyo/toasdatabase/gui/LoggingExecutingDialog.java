package be.suyo.toasdatabase.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AnimatedLabel;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import be.suyo.toasdatabase.logging.Logger;
import be.suyo.toasdatabase.logging.LoggerIFace;
import be.suyo.toasdatabase.utils.Multithread;

public class LoggingExecutingDialog extends BasicWindow implements LoggerIFace {
    private TextBox output;
    private Button close, save;
    volatile boolean finished = false;

    public LoggingExecutingDialog(String title, Runnable task) {
        super(title);
        setHints(Arrays.asList(Window.Hint.MODAL, Window.Hint.CENTERED));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        Panel spinner = Panels.horizontal(new Label("Executing..."), AnimatedLabel.createClassicSpinningLine());
        mainPanel.addComponent(spinner, BorderLayout.Location.TOP);

        output = new TextBox(new TerminalSize(60, 5), TextBox.Style.MULTI_LINE);
        for (int i = 0; i < 4; i++) {
            output.addLine("");
        }
        output.setReadOnly(true);
        mainPanel.addComponent(output, BorderLayout.Location.CENTER);

        close = new Button("Close", () -> {
            if (finished) {
                close();
            }
        });
        save = new Button("Save Output", () -> {
            if (finished) {
                String filename =
                        TextInputDialog.showDialog(getTextGUI(), "Save Output", "Enter File Name (no /)", "out");
                if (filename != null && !filename.contains("/")) {
                    File file = new File(filename);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(output.getText().getBytes());
                        MessageDialog.showMessageDialog(getTextGUI(), "Save Output", "Successfully saved",
                                MessageDialogButton.OK);
                    } catch (IOException e) {
                        MessageDialog.showMessageDialog(getTextGUI(), "Error", "Unable to save output",
                                MessageDialogButton.OK);
                    }
                }
            }
        });
        mainPanel.addComponent(Panels.horizontal(close, save), BorderLayout.Location.BOTTOM);

        Multithread.run(() -> {
            Logger.currentLogger = LoggingExecutingDialog.this;
            try {
                task.run();
            } catch (Throwable e) {
                doPrintln("Thrown: " + e.toString());
                for (StackTraceElement st : e.getStackTrace()) {
                    doPrintln(st.toString());
                }
            }
            doPrintln("");
            Logger.currentLogger = null;
            mainPanel.removeComponent(spinner);
            finished = true;
        });

        setComponent(mainPanel);
        close.takeFocus();
    }

    public void doPrint(String s) {
        try {
            getTextGUI().getGUIThread().invokeAndWait(() -> {
                output.setText(output.getText() + s);
                down(s, 0);
            });
        } catch (Exception ignored) {
        }
    }

    public void doPrintln(String s) {
        try {
            getTextGUI().getGUIThread().invokeAndWait(() -> {
                output.addLine(s);
                down(s, 1);
            });
        } catch (Exception ignored) {
        }
    }

    public void doNotify(String s) {
        doPrintln("[!!!] " + s);
    }

    public void down(String s, int extra) {
        for (int i = 0; i < s.length() - s.replace("\n", "").length() + extra; i++) {
            output.handleKeyStroke(new KeyStroke(KeyType.ArrowDown));
        }
    }

    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'c' || key.getCharacter() == 'C') {
                close.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            } else if (key.getCharacter() == 's' || key.getCharacter() == 'S') {
                save.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            }
        }
        return super.handleInput(key);
    }
}
