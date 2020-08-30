package editor;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    private int targetLength;   // the target search text's length
    private final ArrayList<Integer> foundIndex = new ArrayList<>(); // all matched text's indexes
    private int currentIndex = -1;  // current match text's index

    public TextEditor() {
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Text Editor");
        setLayout(new FlowLayout(FlowLayout.CENTER));

        JTextArea textArea = new JTextArea(20, 30);
        textArea.setName("TextArea");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);

        JTextField searchField = new JTextField(20);
        searchField.setName("SearchField");
        add(searchField);

        JCheckBox checkBox = new JCheckBox("Use regex");
        checkBox.setName("UseRegExCheckbox");
        add(checkBox);

        JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jFileChooser.setName("FileChooser");
        jFileChooser.setVisible(false);
        add(jFileChooser);

        // Image size 24 * 24
        JButton saveButton = new JButton(new ImageIcon("Icons/save.png"));
        saveButton.setName("SaveButton");
        saveButton.addActionListener(actionEvent -> save(jFileChooser, textArea));
        add(saveButton);

        JButton openButton = new JButton(new ImageIcon("Icons/open.png"));
        openButton.setName("OpenButton");
        openButton.addActionListener(actionEvent -> open(jFileChooser, textArea));
        add(openButton);

        JButton searchButton = new JButton(new ImageIcon("Icons/search.png"));
        searchButton.setName("StartSearchButton");
        searchButton.addActionListener(actionEvent -> search(searchField, textArea, checkBox));
        add(searchButton);

        JButton previousSearchButton = new JButton(new ImageIcon("Icons/previous.png"));
        previousSearchButton.setName("PreviousMatchButton");
        previousSearchButton.addActionListener(actionEvent -> previousSearch(textArea));
        add(previousSearchButton);

        JButton nextSearchButton = new JButton(new ImageIcon("Icons/next.png"));
        nextSearchButton.setName("NextMatchButton");
        nextSearchButton.addActionListener(actionEvent -> nextSearch(textArea));
        add(nextSearchButton);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Load");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        openMenuItem.setName("MenuOpen");
        saveMenuItem.setName("MenuSave");
        exitMenuItem.setName("MenuExit");

        saveMenuItem.addActionListener(actionEvent -> save(jFileChooser, textArea));
        openMenuItem.addActionListener(actionEvent -> open(jFileChooser, textArea));
        exitMenuItem.addActionListener(actionEvent -> System.exit(0));

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // Search menu
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(searchMenu);

        JMenuItem startSearchMenuItem = new JMenuItem("Start Search");
        JMenuItem previousMatchMenuItem = new JMenuItem("Previous Match");
        JMenuItem nextMatchMenuItem = new JMenuItem("Next Match");
        JMenuItem useRegexMenuItem = new JMenuItem("Use Regex");

        startSearchMenuItem.setName("MenuStartSearch");
        previousMatchMenuItem.setName("MenuPreviousMatch");
        nextMatchMenuItem.setName("MenuNextMatch");
        useRegexMenuItem.setName("MenuUseRegExp");

        startSearchMenuItem.addActionListener(actionEvent -> search(searchField, textArea, checkBox));
        previousMatchMenuItem.addActionListener(actionEvent -> previousSearch(textArea));
        nextMatchMenuItem.addActionListener(actionEvent -> nextSearch(textArea));
        useRegexMenuItem.addActionListener(actionEvent -> checkBox.setSelected(true));

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(previousMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(useRegexMenuItem);

        setVisible(true);
    }

    private void save(JFileChooser jFileChooser, JTextArea textArea) {
        jFileChooser.setVisible(true);
        int returnValue = jFileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileChooser.getSelectedFile();
            try {
                Files.write(Paths.get(selectedFile.getAbsolutePath()), textArea.getText().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void open(JFileChooser jFileChooser, JTextArea textArea) {
        jFileChooser.setVisible(true);
        int returnValue = jFileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileChooser.getSelectedFile();
            try {
                textArea.setText(new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath()))));
            } catch (IOException e) {
                textArea.setText("");
                e.printStackTrace();
            }
        }
    }

    private void search(JTextField searchField, JTextArea textArea, JCheckBox checkBox) {
        foundIndex.clear();
        currentIndex = -1;
        (new SwingWorker<Void, Object>() {
            @Override
            public Void doInBackground() {
                String searchPattern = searchField.getText();
                Pattern pattern = Pattern.compile(searchPattern, checkBox.isSelected() ? 0 : Pattern.LITERAL);
                Matcher matcher = pattern.matcher(textArea.getText());
                while (matcher.find()) {
                    targetLength = matcher.group().length();
                    foundIndex.add(matcher.start());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    nextSearch(textArea);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }

    private void previousSearch(JTextArea textArea) {
        currentIndex = (currentIndex - 1 + foundIndex.size()) % foundIndex.size();
        int index = foundIndex.get(currentIndex);
        textArea.setCaretPosition(index + targetLength);
        textArea.select(index, index + targetLength);
        textArea.grabFocus();
    }

    private void nextSearch(JTextArea textArea) {
        currentIndex = (currentIndex + 1) % foundIndex.size();
        int index = foundIndex.get(currentIndex);
        textArea.setCaretPosition(index + targetLength);
        textArea.select(index, index + targetLength);
        textArea.grabFocus();
    }
}