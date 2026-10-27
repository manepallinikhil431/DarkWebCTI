
package darkwebcti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Main extends JFrame {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel statusLabel;
    private final JTextArea fullTextArea;
    private final JTextField searchField;
    private final JButton clearSearchButton;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JButton importButton;
    private final JButton analyzeButton;
    private final JButton analyzeTextButton;
    private final JButton preprocessButton;
    private final JButton threatButton;
    private final JButton riskButton;
    private final JButton temporalButton;
    private final JButton chartButton;
    private final JButton exportButton;

    private final Map<Integer, String> textByRow = new HashMap<>();
    private final Map<Integer, String> dateByRow = new HashMap<>();

    private static final List<String> STOP_WORDS = Arrays.asList(
        "the", "a", "an", "and", "or", "but", "if", "then",
        "is", "are", "was", "were", "be", "been", "being",
        "to", "of", "in", "on", "at", "for", "from", "with",
        "by", "as", "it", "this", "that", "these", "those",
        "i", "you", "he", "she", "we", "they", "my", "your",
        "our", "their", "me", "him", "her", "us", "them",
        "have", "has", "had", "do", "does", "did", "can",
        "could", "would", "should", "will", "just", "not",
        "no", "so", "than", "too", "very", "about", "what",
        "when", "where", "who", "how", "which", "there",
        "here", "all", "also", "more", "some", "any"
    );

    private static final List<String> THREAT_KEYWORDS = Arrays.asList(
        "phishing", "malware", "ransomware", "data breach",
        "credential theft", "sql injection", "denial of service",
        "ddos", "botnet", "vulnerability", "exploit", "spyware",
        "trojan", "keylogger", "brute force", "zero day",
        "account takeover", "data leak", "credential stuffing",
        "suspicious login"
    );

    public Main() {
        setTitle("Dark Web Cyber Threat Intelligence Analysis");
        setSize(1450, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        model = new DefaultTableModel(
            new String[]{"ID", "Title", "Category", "Created At"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        fullTextArea = new JTextArea();
        fullTextArea.setEditable(false);
        fullTextArea.setLineWrap(true);
        fullTextArea.setWrapStyleWord(true);
        fullTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(table),
            new JScrollPane(fullTextArea)
        );
        splitPane.setResizeWeight(0.60);

        importButton = new JButton("Import Dataset");
        analyzeButton = new JButton("Analyze Data");
        analyzeTextButton = new JButton("Analyze Selected Text");
        preprocessButton = new JButton("Preprocess Selected Text");
        threatButton = new JButton("Detect Threat Keywords");
        riskButton = new JButton("Classify Threat Risk");
        temporalButton = new JButton("Temporal Threat Analysis");
        chartButton = new JButton("View Threat Trend Chart");
        exportButton = new JButton("Export Analysis Report");

        analyzeButton.setEnabled(false);
        setAnalysisButtons(false);
        temporalButton.setEnabled(false);
        chartButton.setEnabled(false);
        exportButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(importButton);
        buttonPanel.add(analyzeButton);
        buttonPanel.add(analyzeTextButton);
        buttonPanel.add(preprocessButton);
        buttonPanel.add(threatButton);
        buttonPanel.add(riskButton);
        buttonPanel.add(temporalButton);
        buttonPanel.add(chartButton);
        buttonPanel.add(exportButton);

        searchField = new JTextField(28);
        searchField.setToolTipText("Search ID, title, category, date, or full post text");
        clearSearchButton = new JButton("Clear Search");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search records:"));
        searchPanel.add(searchField);
        searchPanel.add(clearSearchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Please import a CSV dataset.");

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        importButton.addActionListener(e -> importDataset());
        analyzeButton.addActionListener(e -> analyzeData());
        analyzeTextButton.addActionListener(e -> analyzeSelectedText());
        preprocessButton.addActionListener(e -> preprocessSelectedText());
        threatButton.addActionListener(e -> detectThreatKeywords());
        riskButton.addActionListener(e -> classifyThreatRisk());
        temporalButton.addActionListener(e -> analyzeTemporalTrends());
        chartButton.addActionListener(e -> showThreatTrendChart());
        exportButton.addActionListener(e -> exportAnalysisReport());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterRecords();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterRecords();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterRecords();
            }
        });

        clearSearchButton.addActionListener(e -> searchField.setText(""));

        table.getSelectionModel().addListSelectionListener(
            (ListSelectionEvent e) -> {
                if (!e.getValueIsAdjusting()) {
                    showSelectedPost();
                }
            }
        );
    }

    private void filterRecords() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    int modelRow = entry.getIdentifier();

                    // Search the visible columns: ID, title, category, and date.
                    for (int column = 0; column < entry.getValueCount(); column++) {
                        String value = entry.getStringValue(column);
                        if (value != null && value.toLowerCase().contains(query)) {
                            return true;
                        }
                    }

                    // Also search the full post text, although it is not a table column.
                    String fullText = textByRow.getOrDefault(modelRow, "");
                    return fullText != null && fullText.toLowerCase().contains(query);
                }
            });
        }

        statusLabel.setText("Showing " + table.getRowCount() + " of "
            + model.getRowCount() + " records");

        if (table.getRowCount() == 0) {
            fullTextArea.setText("No records match your search.");
            setAnalysisButtons(false);
        } else if (table.getSelectedRow() == -1) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void importDataset() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV Dataset");
        chooser.setFileFilter(
            new FileNameExtensionFilter("CSV Files (*.csv)", "csv")
        );

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        importButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        setAnalysisButtons(false);
        temporalButton.setEnabled(false);
        chartButton.setEnabled(false);
        exportButton.setEnabled(false);
        statusLabel.setText("Loading dataset...");

        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                List<String[]> rows = new ArrayList<>();

                try (java.io.Reader reader = Files.newBufferedReader(
                        file.toPath(), StandardCharsets.UTF_8);
                     CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)) {

                    Map<String, Integer> headers = parser.getHeaderMap();

                    for (CSVRecord record : parser) {
                        rows.add(new String[]{
                            getField(record, headers, "id"),
                            getField(record, headers, "title"),
                            getField(record, headers, "category"),
                            getField(record, headers, "created_at"),
                            getField(record, headers, "full_text")
                        });
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<String[]> rows = get();

                    searchField.setText("");
                    model.setRowCount(0);
                    textByRow.clear();
                    dateByRow.clear();
                    fullTextArea.setText("");

                    for (String[] row : rows) {
                        int rowIndex = model.getRowCount();

                        model.addRow(new Object[]{
                            row[0], row[1], row[2], row[3]
                        });

                        textByRow.put(rowIndex, row[4]);
                        dateByRow.put(rowIndex, row[3]);
                    }

                    analyzeButton.setEnabled(!rows.isEmpty());
                    temporalButton.setEnabled(!rows.isEmpty());
                    chartButton.setEnabled(!rows.isEmpty());
                    exportButton.setEnabled(!rows.isEmpty());

                    statusLabel.setText(
                        "Loaded " + rows.size() + " records from "
                        + file.getName()
                    );

                    if (!rows.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                    }
                    if (!searchField.getText().trim().isEmpty()) {
                        filterRecords();
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        Main.this,
                        "Could not load the CSV file:\n" + ex.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    statusLabel.setText("Dataset import failed.");
                } finally {
                    importButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private String getField(
        CSVRecord record,
        Map<String, Integer> headers,
        String fieldName
    ) {
        for (String header : headers.keySet()) {
            if (header.trim().equalsIgnoreCase(fieldName)) {
                String value = record.get(header);
                return value == null ? "" : value.trim();
            }
        }
        return "";
    }

    private void showSelectedPost() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            setAnalysisButtons(false);
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        String text = textByRow.getOrDefault(row, "");

        fullTextArea.setText(
            "ID: " + model.getValueAt(row, 0)
            + "\nTitle: " + model.getValueAt(row, 1)
            + "\nCategory: " + model.getValueAt(row, 2)
            + "\nCreated At: " + model.getValueAt(row, 3)
            + "\n\nFull Text:\n" + text
        );

        fullTextArea.setCaretPosition(0);
        setAnalysisButtons(text != null && !text.trim().isEmpty());
    }

    private void setAnalysisButtons(boolean enabled) {
        analyzeTextButton.setEnabled(enabled);
        preprocessButton.setEnabled(enabled);
        threatButton.setEnabled(enabled);
        riskButton.setEnabled(enabled);
    }

    private void analyzeData() {
        int total = model.getRowCount();

        if (total == 0) {
            JOptionPane.showMessageDialog(this, "No dataset loaded.");
            return;
        }

        Map<String, Integer> categoryCounts = new TreeMap<>();
        int missingCategories = 0;
        LocalDate earliest = null;
        LocalDate latest = null;

        for (int i = 0; i < total; i++) {
            String category = String.valueOf(model.getValueAt(i, 2)).trim();
            LocalDate date = parseDate(dateByRow.getOrDefault(i, ""));

            if (category.isEmpty() || category.equalsIgnoreCase("null")) {
                missingCategories++;
            } else {
                categoryCounts.put(
                    category, categoryCounts.getOrDefault(category, 0) + 1
                );
            }

            if (date != null) {
                if (earliest == null || date.isBefore(earliest)) {
                    earliest = date;
                }
                if (latest == null || date.isAfter(latest)) {
                    latest = date;
                }
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("DATASET ANALYSIS REPORT\n");
        report.append("=======================\n\n");
        report.append("Total records: ").append(total).append("\n");
        report.append("Records with category: ")
              .append(total - missingCategories).append("\n");
        report.append("Records missing category: ")
              .append(missingCategories).append("\n");
        report.append("Unique non-empty categories: ")
              .append(categoryCounts.size()).append("\n");
        report.append("Earliest date: ")
              .append(earliest == null ? "Not available" : earliest).append("\n");
        report.append("Latest date: ")
              .append(latest == null ? "Not available" : latest).append("\n\n");
        report.append("CATEGORY COUNTS\n---------------\n");

        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            report.append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append("\n");
        }

        showReport("Dataset Analysis", report.toString());
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                   .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    private List<String> removeStopWords(String text) {
        List<String> result = new ArrayList<>();

        if (text.isEmpty()) {
            return result;
        }

        for (String word : text.split("\\s+")) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
                result.add(word);
            }
        }

        return result;
    }

    private void analyzeSelectedText() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        String original = textByRow.getOrDefault(row, "");
        String cleaned = cleanText(original);
        List<String> words = removeStopWords(cleaned);

        Map<String, Integer> frequencies = new HashMap<>();
        for (String word : words) {
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted =
            new ArrayList<>(frequencies.entrySet());

        sorted.sort(
            Map.Entry.<String, Integer>comparingByValue(
                Comparator.reverseOrder()
            )
        );

        StringBuilder report = new StringBuilder();
        report.append("SELECTED TEXT ANALYSIS\n======================\n\n");
        report.append("Total characters: ").append(original.length()).append("\n");
        report.append("Words after cleaning: ")
              .append(cleaned.isEmpty() ? 0 : cleaned.split("\\s+").length)
              .append("\n");
        report.append("Words after stop-word removal: ").append(words.size());
        report.append("\n\nTOP 15 FREQUENT TERMS\n---------------------\n");

        int limit = Math.min(15, sorted.size());
        for (int i = 0; i < limit; i++) {
            report.append(i + 1).append(". ")
                  .append(sorted.get(i).getKey()).append(" — ")
                  .append(sorted.get(i).getValue()).append(" occurrence(s)\n");
        }

        if (sorted.isEmpty()) {
            report.append("No terms found after processing.\n");
        }

        showReport("Selected Text Analysis", report.toString());
    }

    private void preprocessSelectedText() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        String original = textByRow.getOrDefault(row, "");
        String cleaned = cleanText(original);
        List<String> filtered = removeStopWords(cleaned);

        StringBuilder report = new StringBuilder();
        report.append("TEXT PREPROCESSING REPORT\n=========================\n\n");
        report.append("Record ID: ").append(model.getValueAt(row, 0)).append("\n");
        report.append("Title: ").append(model.getValueAt(row, 1)).append("\n\n");
        report.append("ORIGINAL TEXT\n-------------\n").append(original).append("\n\n");
        report.append("CLEANED TEXT\n------------\n").append(cleaned).append("\n\n");
        report.append("AFTER STOP-WORD REMOVAL\n-----------------------\n")
              .append(String.join(" ", filtered)).append("\n\n");
        report.append("Original characters: ").append(original.length()).append("\n");
        report.append("Cleaned characters: ").append(cleaned.length()).append("\n");
        report.append("Words after stop-word removal: ").append(filtered.size());

        showReport("Preprocess Selected Text", report.toString());
    }

    private void detectThreatKeywords() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        String text = cleanText(textByRow.getOrDefault(row, ""));

        StringBuilder report = new StringBuilder();
        report.append("CYBERSECURITY KEYWORD ANALYSIS\n==============================\n\n");
        report.append("Record ID: ").append(model.getValueAt(row, 0)).append("\n");
        report.append("Title: ").append(model.getValueAt(row, 1)).append("\n\n");
        report.append("MATCHED KEYWORDS\n----------------\n");

        int totalMatches = 0;
        int matchedTypes = 0;

        for (String keyword : THREAT_KEYWORDS) {
            int count = countPhrase(text, cleanText(keyword));

            if (count > 0) {
                report.append("- ").append(keyword).append(": ")
                      .append(count).append(" match(es)\n");
                totalMatches += count;
                matchedTypes++;
            }
        }

        if (matchedTypes == 0) {
            report.append("No predefined keywords were found.\n");
        }

        report.append("\nKeyword types matched: ").append(matchedTypes).append("\n");
        report.append("Total keyword matches: ").append(totalMatches).append("\n\n");
        report.append("Keyword matches alone do not prove an actual cyberattack.");

        showReport("Detect Threat Keywords", report.toString());
    }

    private void classifyThreatRisk() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        String text = cleanText(textByRow.getOrDefault(row, ""));

        int score = 0;
        int matchedTypes = 0;
        StringBuilder matched = new StringBuilder();

        for (String keyword : THREAT_KEYWORDS) {
            int count = countPhrase(text, cleanText(keyword));

            if (count > 0) {
                matchedTypes++;
                score += 2;
                matched.append("- ").append(keyword)
                       .append(" (").append(count).append(" occurrence(s))\n");
            }
        }

        score = Math.min(score, 10);

        String riskLevel;
        if (matchedTypes == 0) {
            riskLevel = "No predefined keywords detected";
        } else if (score <= 2) {
            riskLevel = "Low";
        } else if (score <= 6) {
            riskLevel = "Medium";
        } else {
            riskLevel = "High";
        }

        StringBuilder report = new StringBuilder();
        report.append("THREAT RISK CLASSIFICATION\n==========================\n\n");
        report.append("Record ID: ").append(model.getValueAt(row, 0)).append("\n");
        report.append("Title: ").append(model.getValueAt(row, 1)).append("\n\n");
        report.append("Matched keyword types: ").append(matchedTypes).append("\n");
        report.append("Illustrative score: ").append(score).append(" / 10\n");
        report.append("Rule-based risk level: ").append(riskLevel).append("\n\n");
        report.append("MATCHED KEYWORDS\n----------------\n");
        report.append(matchedTypes == 0
            ? "No predefined keywords matched.\n" : matched.toString());
        report.append("\nThis is a demonstration score, not a validated risk rating.");

        showReport("Classify Threat Risk", report.toString());
    }

    private Map<YearMonth, Integer> getMonthlyMatchedCounts() {
        Map<YearMonth, Integer> matchedByMonth = new TreeMap<>();

        for (int row = 0; row < model.getRowCount(); row++) {
            LocalDate date = parseDate(dateByRow.getOrDefault(row, ""));
            if (date == null) {
                continue;
            }

            String text = cleanText(textByRow.getOrDefault(row, ""));
            if (containsThreatKeyword(text)) {
                YearMonth month = YearMonth.from(date);
                matchedByMonth.put(
                    month, matchedByMonth.getOrDefault(month, 0) + 1
                );
            }
        }

        return matchedByMonth;
    }

    private void analyzeTemporalTrends() {
        Map<YearMonth, Integer> totalByMonth = new TreeMap<>();
        Map<YearMonth, Integer> matchedByMonth = getMonthlyMatchedCounts();
        int invalidDates = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            LocalDate date = parseDate(dateByRow.getOrDefault(row, ""));

            if (date == null) {
                invalidDates++;
                continue;
            }

            YearMonth month = YearMonth.from(date);
            totalByMonth.put(month, totalByMonth.getOrDefault(month, 0) + 1);
        }

        int totalMatchedPosts = 0;
        for (int count : matchedByMonth.values()) {
            totalMatchedPosts += count;
        }

        StringBuilder report = new StringBuilder();
        report.append("TEMPORAL THREAT ANALYSIS\n========================\n\n");
        report.append("Total dataset records: ").append(model.getRowCount()).append("\n");
        report.append("Records with valid dates: ")
              .append(model.getRowCount() - invalidDates).append("\n");
        report.append("Records with invalid or missing dates: ")
              .append(invalidDates).append("\n");
        report.append("Records with keyword matches: ")
              .append(totalMatchedPosts).append("\n\n");
        report.append(String.format("%-12s | %-12s | %-20s%n",
            "Month", "Total Posts", "Keyword-Matched Posts"));
        report.append("------------------------------------------------------\n");

        for (Map.Entry<YearMonth, Integer> entry : totalByMonth.entrySet()) {
            report.append(String.format("%-12s | %-12d | %-20d%n",
                entry.getKey(), entry.getValue(),
                matchedByMonth.getOrDefault(entry.getKey(), 0)));
        }

        report.append("\nKeyword matches are indicators only, not confirmed attacks.");
        showReport("Temporal Threat Analysis", report.toString());
    }

    private void showThreatTrendChart() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Import a dataset first.");
            return;
        }

        Map<YearMonth, Integer> data = getMonthlyMatchedCounts();

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "No monthly keyword matches were found to chart."
            );
            return;
        }

        JFrame chartFrame = new JFrame("Monthly Threat Keyword Trend");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(1000, 650);
        chartFrame.setLocationRelativeTo(this);
        chartFrame.add(new TrendChartPanel(data));
        chartFrame.setVisible(true);
    }

    private boolean containsThreatKeyword(String text) {
        for (String keyword : THREAT_KEYWORDS) {
            if (countPhrase(text, cleanText(keyword)) > 0) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String dateText = value.trim();

        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            // Try another format.
        }

        try {
            return LocalDateTime.parse(
                dateText, DateTimeFormatter.ISO_LOCAL_DATE_TIME
            ).toLocalDate();
        } catch (DateTimeParseException ex) {
            // Try alternatives.
        }

        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "yyyy-MM"
        };

        for (String pattern : patterns) {
            try {
                TemporalAccessor parsed =
                    DateTimeFormatter.ofPattern(pattern).parse(dateText);

                try {
                    return LocalDate.from(parsed);
                } catch (DateTimeException ex) {
                    return YearMonth.from(parsed).atDay(1);
                }
            } catch (DateTimeParseException ex) {
                // Try next pattern.
            }
        }

        return null;
    }

    private int countPhrase(String text, String phrase) {
        if (text == null || text.isEmpty()
                || phrase == null || phrase.isEmpty()) {
            return 0;
        }

        String pattern = "(?<!\\S)" + Pattern.quote(phrase) + "(?!\\S)";
        Matcher matcher = Pattern.compile(pattern).matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private String buildExportReport() {
        int total = model.getRowCount();
        int missingCategories = 0;
        int invalidDates = 0;
        int matchedPosts = 0;

        Map<String, Integer> categories = new TreeMap<>();
        Map<YearMonth, Integer> totalByMonth = new TreeMap<>();
        Map<YearMonth, Integer> matchedByMonth = getMonthlyMatchedCounts();

        StringBuilder report = new StringBuilder();
        report.append("DARK WEB CYBER THREAT INTELLIGENCE ANALYSIS\n");
        report.append("===========================================\n\n");
        report.append("Report type: Dataset and keyword-based analysis\n");
        report.append("Total records: ").append(total).append("\n\n");

        report.append("1. DATASET SUMMARY\n------------------\n");

        LocalDate earliest = null;
        LocalDate latest = null;

        for (int row = 0; row < total; row++) {
            String category = String.valueOf(model.getValueAt(row, 2)).trim();
            LocalDate date = parseDate(dateByRow.getOrDefault(row, ""));

            if (category.isEmpty() || category.equalsIgnoreCase("null")) {
                missingCategories++;
            } else {
                categories.put(category, categories.getOrDefault(category, 0) + 1);
            }

            if (date == null) {
                invalidDates++;
            } else {
                if (earliest == null || date.isBefore(earliest)) {
                    earliest = date;
                }
                if (latest == null || date.isAfter(latest)) {
                    latest = date;
                }

                YearMonth month = YearMonth.from(date);
                totalByMonth.put(month, totalByMonth.getOrDefault(month, 0) + 1);
            }

            if (containsThreatKeyword(
                    cleanText(textByRow.getOrDefault(row, "")))) {
                matchedPosts++;
            }
        }

        report.append("Records with category: ")
              .append(total - missingCategories).append("\n");
        report.append("Records missing category: ")
              .append(missingCategories).append("\n");
        report.append("Records with valid dates: ")
              .append(total - invalidDates).append("\n");
        report.append("Earliest date: ")
              .append(earliest == null ? "Not available" : earliest).append("\n");
        report.append("Latest date: ")
              .append(latest == null ? "Not available" : latest).append("\n\n");

        report.append("CATEGORY COUNTS\n---------------\n");
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            report.append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append("\n");
        }

        report.append("\n2. KEYWORD ANALYSIS\n-------------------\n");
        report.append("Posts with at least one predefined keyword: ")
              .append(matchedPosts).append("\n");
        report.append("Keywords searched: ")
              .append(String.join(", ", THREAT_KEYWORDS)).append("\n");

        report.append("\n3. MONTHLY TEMPORAL ANALYSIS\n---------------------------\n");
        report.append(String.format("%-12s | %-12s | %-20s%n",
            "Month", "Total Posts", "Matched Posts"));

        for (Map.Entry<YearMonth, Integer> entry : totalByMonth.entrySet()) {
            report.append(String.format("%-12s | %-12d | %-20d%n",
                entry.getKey(), entry.getValue(),
                matchedByMonth.getOrDefault(entry.getKey(), 0)));
        }

        report.append("\n4. RISK CLASSIFICATION METHOD\n-----------------------------\n");
        report.append("Each distinct matched keyword type contributes 2 points, ")
              .append("capped at 10 points per post.\n");
        report.append("0 matches: no predefined keywords detected; ")
              .append("1 type: Low; 2-3 types: Medium; 4+ types: High.\n");

        report.append("\n5. LIMITATIONS\n--------------\n");
        report.append("Keyword matching does not confirm a cyberattack.\n");
        report.append("The risk score is a demonstration rule, not a validated security rating.\n");
        report.append("Results depend on the predefined keyword list and dataset quality.\n");

        return report.toString();
    }

    // New feature: save a report as a UTF-8 text file.
    private void exportAnalysisReport() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Import a dataset first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Analysis Report");
        chooser.setSelectedFile(new File("DarkWebCTI_Analysis_Report.txt"));
        chooser.setFileFilter(
            new FileNameExtensionFilter("Text Report (*.txt)", "txt")
        );

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }

        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(
                this,
                "The file already exists. Do you want to replace it?",
                "Confirm Overwrite",
                JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Files.write(
                file.toPath(),
                buildExportReport().getBytes(StandardCharsets.UTF_8)
            );

            JOptionPane.showMessageDialog(
                this,
                "Analysis report saved successfully:\n" + file.getAbsolutePath(),
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE
            );

            statusLabel.setText("Report exported to " + file.getName());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not save report:\n" + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showReport(String title, String content) {
        JTextArea reportArea = new JTextArea(content, 28, 80);
        reportArea.setEditable(false);
        reportArea.setLineWrap(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(reportArea),
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static class TrendChartPanel extends JPanel {
        private final List<YearMonth> months;
        private final List<Integer> counts;

        TrendChartPanel(Map<YearMonth, Integer> data) {
            months = new ArrayList<>(data.keySet());
            counts = new ArrayList<>();

            for (YearMonth month : months) {
                counts.add(data.get(month));
            }

            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(1000, 650));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int left = 80;
            int right = getWidth() - 40;
            int top = 70;
            int bottom = getHeight() - 100;
            int width = right - left;
            int height = bottom - top;

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("Monthly Threat Keyword Trend", left, 35);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Month", getWidth() / 2 - 20, getHeight() - 25);
            g2.drawString("Keyword-matched posts", 10, 58);

            int max = 1;
            for (int value : counts) {
                max = Math.max(max, value);
            }

            int yMax = Math.max(5, ((max + 4) / 5) * 5);

            for (int i = 0; i <= 5; i++) {
                int y = bottom - (i * height / 5);
                int value = i * yMax / 5;

                g2.setColor(new Color(220, 220, 220));
                g2.drawLine(left, y, right, y);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(value), left - 35, y + 4);
            }

            g2.setColor(Color.BLACK);
            g2.drawLine(left, top, left, bottom);
            g2.drawLine(left, bottom, right, bottom);

            if (months.size() == 1) {
                int x = left + width / 2;
                int y = bottom - counts.get(0) * height / yMax;
                g2.setColor(new Color(35, 100, 200));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.drawString(months.get(0).toString(), x - 25, bottom + 20);
                g2.dispose();
                return;
            }

            int previousX = -1;
            int previousY = -1;
            int size = months.size();

            g2.setColor(new Color(35, 100, 200));
            g2.setStroke(new java.awt.BasicStroke(2.0f));

            for (int i = 0; i < size; i++) {
                int x = left + (int) ((long) i * width / (size - 1));
                int y = bottom - (int) ((long) counts.get(i) * height / yMax);

                if (previousX >= 0) {
                    g2.drawLine(previousX, previousY, x, y);
                }

                g2.fillOval(x - 4, y - 4, 8, 8);

                if (i % Math.max(1, size / 12) == 0 || i == size - 1) {
                    g2.setColor(Color.BLACK);
                    g2.drawString(months.get(i).toString(), x - 22, bottom + 18);
                    g2.setColor(new Color(35, 100, 200));
                }

                previousX = x;
                previousY = y;
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                // Use default appearance if unavailable.
            }

            new Main().setVisible(true);
        });
    }
}
