package be.suyo.toasdatabase.gui.news;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import be.suyo.toasdatabase.news.*;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import be.suyo.toasdatabase.gui.LoggingExecutingDialog;
import be.suyo.toasdatabase.logging.Logger;

public class NewsWindow extends BasicWindow {
    private ActionListBox actions;
    private Button exit;

    public NewsWindow() {
        super("Manage News");
        setHints(Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.MODAL));

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        actions = new ActionListBox();
        actions.addItem("1 Redownload News Post (New Version)", () -> {
            BigInteger nid = TextInputDialog
                    .showNumberDialog(getTextGUI(), "Redownload News Post (New Version)", "Enter News Post ID", "");
            if (nid != null) {
                getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Redownload News Post (New Version)", () -> {
                    NewsPost post = NewsPost.getLatestByPageId(nid.longValue());
                    if (post == null) {
                        Logger.println("No news post with this ID.");
                        return;
                    }
                    NewsPost newPost = NewsPost.createCloneWithNewVersion(post);
                    Logger.println("Downloading...");
                    Set<Long> downloadedSet = new HashSet<>();
                    downloadedSet.add(nid.longValue());
                    NewsUpdateCallback cb = new DatabaseStoreCallback();
                    NewsManager.downloadSubpages(newPost.downloadAndParseContent(), downloadedSet, cb);
                    cb.onUpdatedPost(newPost);
                }));
            }
        });
        actions.addItem("2 Redownload News Post (No New Version)", () -> {
            BigInteger nid = TextInputDialog
                    .showNumberDialog(getTextGUI(), "Redownload News Post (No New Version)", "Enter News Post ID", "");
            if (nid != null) {
                getTextGUI()
                        .addWindowAndWait(new LoggingExecutingDialog("Redownload News Post (No New Version)", () -> {
                            NewsPost np = NewsPost.getLatestByPageId(nid.longValue());
                            if (np == null) {
                                Logger.println("No news post with this ID.");
                                return;
                            }
                            Logger.println("Downloading...");
                            Set<Long> downloadedSet = new HashSet<>();
                            downloadedSet.add(nid.longValue());
                            NewsManager.downloadSubpages(np.downloadAndParseContent(), downloadedSet,
                                    new DatabaseStoreCallback());
                        }));
            }
        });
        actions.addItem("3 Reparse News Post", () -> {
            BigInteger nid =
                    TextInputDialog.showNumberDialog(getTextGUI(), "Reparse News Post", "Enter News Post ID", "");
            if (nid != null) {
                getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Reparse News Post", () -> {
                    NewsPost np = NewsPost.getLatestByPageId(nid.intValue());
                    if (np == null) {
                        Logger.println("No news post with this ID.");
                        return;
                    }
                    Logger.println("Parsing...");
                    np.parseContent();
                }));
            }
        });
        actions.addItem("4 Reparse All News Posts",
                () -> getTextGUI().addWindowAndWait(new LoggingExecutingDialog("Reparse All News Posts", () -> {
                    Logger.println("Parsing...");
                    NewsManager.reparseAllContentFiles();
                })));
        actions.addItem("5 Resend Discord Notification For Single News Post", () -> {
            BigInteger nid = TextInputDialog
                    .showNumberDialog(getTextGUI(), "Resend Discord Notification For Single News Post",
                            "Enter News Post ID", "");
            if (nid != null) {
                getTextGUI().addWindowAndWait(
                        new LoggingExecutingDialog("Resend Discord Notification For Single News Post", () -> {
                            NewsPost np = NewsPost.getLatestByPageId(nid.longValue());
                            if (np == null) {
                                Logger.println("No news post with this ID.");
                                return;
                            }
                            Logger.println("Sending...");
                            new DiscordWebhookCallback().onNewPost(np);
                        }));
            }
        });
        mainPanel.addComponent(actions, BorderLayout.Location.CENTER);

        exit = new Button("Return", this::close);
        mainPanel.addComponent(exit, BorderLayout.Location.BOTTOM);

        setComponent(mainPanel);
    }

    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'r' || key.getCharacter() == 'R') {
                exit.handleKeyStroke(new KeyStroke(KeyType.Enter));
                return true;
            } else if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
                int taskIndex = (int) key.getCharacter() - (int) '1';
                if (taskIndex < 0) {
                    taskIndex = 9;
                }
                if (taskIndex < actions.getItemCount()) {
                    actions.setSelectedIndex(taskIndex);
                    actions.getItemAt(taskIndex).run();
                }
            }
        }
        return super.handleInput(key);
    }
}
