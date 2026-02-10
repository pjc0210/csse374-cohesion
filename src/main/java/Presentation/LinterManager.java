package Presentation;

import Domain.Category;
import Domain.Interfaces.ICheck;
import Domain.Interfaces.IPatternCheck;
import Domain.Interfaces.IPrincipleCheck;
import Domain.Interfaces.IStyleCheck;
import Domain.LintResult;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

/**
 * GUI-based Linter Manager
 * Allows users to:
 * 1. Select which linter checks to run
 * 2. Choose files or packages to lint
 * 3. View results in a formatted output
 */
public class LinterManager extends JFrame {

    private JPanel checkBoxPanel;
    private List<JCheckBox> checkBoxes;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JButton selectFilesButton;
    private JButton selectPackageButton;
    private JButton runLinterButton;
    private List<File> selectedFiles;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }

            LinterManager manager = new LinterManager();
            manager.setVisible(true);
        });
    }

    public LinterManager() {
        selectedFiles = new ArrayList<>();
        checkBoxes = new ArrayList<>();
        initializeUI();
        loadAvailableChecks();
    }

    private void initializeUI() {
        setTitle("Java Linter Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - File selection
        JPanel topPanel = createFileSelectionPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Split between checks and output
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);

        // Left side - Check selection
        JPanel checkPanel = createCheckSelectionPanel();
        splitPane.setLeftComponent(checkPanel);

        // Right side - Output
        JPanel outputPanel = createOutputPanel();
        splitPane.setRightComponent(outputPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Status and run button
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("File/Package Selection"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        selectFilesButton = new JButton("Select .class Files");
        selectFilesButton.addActionListener(e -> selectClassFiles());

        selectPackageButton = new JButton("Select Package Directory");
        selectPackageButton.addActionListener(e -> selectPackageDirectory());

        JButton clearButton = new JButton("Clear Selection");
        clearButton.addActionListener(e -> clearSelection());

        buttonPanel.add(selectFilesButton);
        buttonPanel.add(selectPackageButton);
        buttonPanel.add(clearButton);

        statusLabel = new JLabel("No files selected");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCheckSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Available Checks"));

        // Use a panel with checkboxes
        checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons for select all/none
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(true)));

        JButton selectNoneButton = new JButton("Select None");
        selectNoneButton.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(false)));

        JButton selectByCategoryButton = new JButton("Select by Category...");
        selectByCategoryButton.addActionListener(e -> selectByCategory());

        buttonPanel.add(selectAllButton);
        buttonPanel.add(selectNoneButton);
        buttonPanel.add(selectByCategoryButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Linter Output"));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearOutputButton = new JButton("Clear Output");
        clearOutputButton.addActionListener(e -> outputArea.setText(""));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearOutputButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        runLinterButton = new JButton("Run Linter");
        runLinterButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        runLinterButton.addActionListener(e -> runLinter());

        panel.add(runLinterButton);

        return panel;
    }

    private void loadAvailableChecks() {
        List<CheckWrapper> checks = new ArrayList<>();

        // Load Pattern Checks
        checks.addAll(loadChecksFromPackage("Domain.PatternCheck", IPatternCheck.class, Category.PATTERN));

        // Load Style Checks
        checks.addAll(loadChecksFromPackage("Domain.StyleCheck", IStyleCheck.class, Category.STYLE));

        // Load Principle Checks
        checks.addAll(loadChecksFromPackage("Domain.PrincipleCheck", IPrincipleCheck.class, Category.PRINCIPLE));

        // Sort checks by category and name
        checks.sort(Comparator.comparing((CheckWrapper c) -> c.category).thenComparing(c -> c.check.getName()));

        // Add checkboxes for each check
        for (CheckWrapper check : checks) {
            String categoryIcon = "";
            switch (check.category) {
                case PATTERN:
                    categoryIcon = "🔷";
                    break;
                case STYLE:
                    categoryIcon = "✏️";
                    break;
                case PRINCIPLE:
                    categoryIcon = "📐";
                    break;
            }

            JCheckBox checkBox = new JCheckBox(categoryIcon + " " + check.check.getName() + " (" + check.category + ")");
            checkBox.setSelected(true);
            checkBox.putClientProperty("checkWrapper", check);

            checkBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        checkBoxPanel.revalidate();
        checkBoxPanel.repaint();

        if (checks.isEmpty()) {
            outputArea.append("Warning: No checks found. Make sure check classes are compiled.\n");
            outputArea.append("Looking in:\n");
            outputArea.append("  - Domain.PatternCheck\n");
            outputArea.append("  - Domain.StyleCheck\n");
            outputArea.append("  - Domain.PrincipleCheck\n");
        } else {
            outputArea.append("Loaded " + checks.size() + " check(s)\n");
        }
    }

    private List<CheckWrapper> loadChecksFromPackage(String packageName, Class<?> interfaceType, Category category) {
        List<CheckWrapper> checks = new ArrayList<>();

        try {
            String path = packageName.replace('.', '/');
            File packageDir = new File("target/classes/" + path);

            if (!packageDir.exists()) {
                packageDir = new File("out/production/classes/" + path);
            }

            if (!packageDir.exists()) {
                return checks;
            }

            File[] files = packageDir.listFiles((dir, name) -> name.endsWith(".class"));

            if (files != null) {
                for (File file : files) {
                    String className = file.getName().replace(".class", "");
                    String fullClassName = packageName + "." + className;

                    try {
                        Class<?> clazz = Class.forName(fullClassName);

                        // Check if it implements ICheck and is not an interface
                        if (ICheck.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            Constructor<?> constructor = clazz.getDeclaredConstructor();
                            ICheck instance = (ICheck) constructor.newInstance();
                            checks.add(new CheckWrapper(instance, category));
                        }
                    } catch (Exception e) {
                        // Skip classes that can't be instantiated
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading checks from " + packageName + ": " + e.getMessage());
        }

        return checks;
    }

    private void selectClassFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("target/test-classes"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java Class Files (*.class)", "class"));
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            selectedFiles = new ArrayList<>(Arrays.asList(files));
            updateFileSelectionStatus();
        }
    }

    private void selectPackageDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("target/test-classes"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            selectedFiles = new ArrayList<>();
            collectClassFiles(directory, selectedFiles);
            updateFileSelectionStatus();
        }
    }

    private void collectClassFiles(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectClassFiles(file, fileList);
                } else if (file.getName().endsWith(".class")) {
                    fileList.add(file);
                }
            }
        }
    }

    private void clearSelection() {
        selectedFiles.clear();
        updateFileSelectionStatus();
    }

    private void updateFileSelectionStatus() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("No files selected");
        } else {
            statusLabel.setText(selectedFiles.size() + " file(s) selected");
        }
    }

    private void selectByCategory() {
        Category[] categories = {Category.PATTERN, Category.STYLE, Category.PRINCIPLE};

        Category selected = (Category) JOptionPane.showInputDialog(
                this,
                "Select a category to enable (others will be disabled):",
                "Select by Category",
                JOptionPane.PLAIN_MESSAGE,
                null,
                categories,
                categories[0]
        );

        if (selected != null) {
            for (JCheckBox checkBox : checkBoxes) {
                CheckWrapper wrapper = (CheckWrapper) checkBox.getClientProperty("checkWrapper");
                checkBox.setSelected(wrapper.category == selected);
            }
        }
    }

    private void runLinter() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select files or a package to lint first.",
                    "No Files Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CheckWrapper> selectedChecks = getSelectedChecks();

        if (selectedChecks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one check to run.",
                    "No Checks Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        outputArea.setText("");
        outputArea.append("=".repeat(80) + "\n");
        outputArea.append("LINTER EXECUTION STARTED\n");
        outputArea.append("=".repeat(80) + "\n\n");
        outputArea.append("Running " + selectedChecks.size() + " check(s) on " +
                selectedFiles.size() + " file(s)\n\n");

        int totalViolations = 0;

        for (File file : selectedFiles) {
            try {
                ClassNode classNode = loadClassNode(file);

                outputArea.append("-".repeat(80) + "\n");
                outputArea.append("Analyzing: " + classNode.name + "\n");
                outputArea.append("-".repeat(80) + "\n");

                boolean hasViolations = false;

                for (CheckWrapper checkWrapper : selectedChecks) {
                    List<LintResult> results = checkWrapper.check.execute(classNode);

                    if (!results.isEmpty()) {
                        hasViolations = true;
                        outputArea.append("\n[" + checkWrapper.category + "] " +
                                checkWrapper.check.getName() + ":\n");

                        for (LintResult result : results) {
                            totalViolations++;
                            outputArea.append("  ⚠ " + result.getMessage() + "\n");
                            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                                outputArea.append("    → " + result.getMessage() + "\n");
                            }
                        }
                    }
                }

                if (!hasViolations) {
                    outputArea.append("  ✓ No violations found\n");
                }

                outputArea.append("\n");

            } catch (IOException e) {
                outputArea.append("  ✗ Error loading file: " + e.getMessage() + "\n\n");
            }
        }

        outputArea.append("=".repeat(80) + "\n");
        outputArea.append("LINTER EXECUTION COMPLETED\n");
        outputArea.append("Total violations found: " + totalViolations + "\n");
        outputArea.append("=".repeat(80) + "\n");

        // Scroll to top
        outputArea.setCaretPosition(0);
    }

    private List<CheckWrapper> getSelectedChecks() {
        List<CheckWrapper> selected = new ArrayList<>();

        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                CheckWrapper wrapper = (CheckWrapper) checkBox.getClientProperty("checkWrapper");
                selected.add(wrapper);
            }
        }

        return selected;
    }

    private ClassNode loadClassNode(File classFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(classFile)) {
            ClassReader reader = new ClassReader(fis);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            return classNode;
        }
    }

    /**
     * Wrapper class for checks with category information
     */
    private static class CheckWrapper {
        ICheck check;
        Category category;

        CheckWrapper(ICheck check, Category category) {
            this.check = check;
            this.category = category;
        }

        @Override
        public String toString() {
            return check.getName();
        }
    }
}