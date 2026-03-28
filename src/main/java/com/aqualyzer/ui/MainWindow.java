package com.aqualyzer.ui;

import com.aqualyzer.core.FishService;
import com.aqualyzer.core.ImportService;
import com.aqualyzer.core.WaterMeasurementService;
import com.aqualyzer.core.CsvExportService;
import com.aqualyzer.core.enums.QualityRating;
import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.WaterMeasurement;
import com.aqualyzer.core.rule.PhRatingRule;
import com.aqualyzer.core.rule.SalinityRatingRule;
import com.aqualyzer.core.rule.TemperatureRatingRule;
import com.aqualyzer.core.rule.O2ConcentrationRatingRule;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainWindow extends JFrame {

    private final FishService fishService;
    private final WaterMeasurementService waterMeasurementService;
    private final ImportService importService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    private final List<WaterMeasurement> results = new ArrayList<>();
    private List<WaterMeasurement> filteredResults = new ArrayList<>();
    private String currentSearchText = "";

    private JPanel contentPane;
    private JTable resultTable;
    private JLabel resultsTitle;
    private JComboBox fishSelection;
    private JButton neuerFischButton,
            zeitreiheImportierenButton,
            messungErfassenButton;
    private JButton calculateScoreButton;
    private JLabel dataStatusLabel;
    private JButton editFishButton;
    private JButton refreshMeasurementsButton;
    private JLabel programStatusLabel;
    private JButton deleteFishButton;
    private JLabel fishTitle;
    private JScrollPane tableContainerPane;
    private JButton deleteResultButton;
    private JButton exportResultButton;
    private JTextField searchField;
    private JButton clearSearchButton;
    private JLabel searchLabel;
    private JCheckBox autoCalcCheckBox;

    private JMenuBar menuBar;
    private JMenu menuFile;

    // file
    private JMenuItem menuFileExit;
    private JMenuItem menuFileExport;

    private void makeMenus() {
        menuBar = new JMenuBar();
        menuFile = new JMenu("Datei");
        menuFileExport = new JMenuItem("Ergebnisse exportieren");
        menuFileExit = new JMenuItem("Beenden");
        menuFile.add(menuFileExport);
        menuFile.addSeparator();
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
    }

    private void makeTable() {
        applySearchFilter();
        var displayResults = filteredResults.isEmpty() && currentSearchText.isEmpty() ? results : filteredResults;

        var model = new DefaultTableModel(
                new Object[displayResults.size()][10],
                new String[]{
                        "Zeit", "Station", "Wassertemperatur", "Temperaturbewertung",
                        "pH-Wert", "pH-Bewertung", "Salzgehalt", "Salzgehalt-Bewertung",
                        "Sauerstoffgehalt", "Sauerstoffbewertung"
                }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                List<Integer> columns = List.of(2, 4, 6, 8);
                return columns.contains(column);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 2, 4, 6, 8 -> Double.class;
                    default -> String.class;
                };
            }
        };

        for (int i = 0; i < displayResults.size(); i++) {
            var m = displayResults.get(i);
            var sortableDate = m.getTimestamp().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DATE_FORMATTER);
            model.setValueAt(sortableDate, i, 0);
            model.setValueAt(m.getName(), i, 1);
            model.setValueAt(m.getTemperature(), i, 2);
            model.setValueAt(m.getTemperatureRating(), i, 3);
            model.setValueAt(m.getPhValue(), i, 4);
            model.setValueAt(m.getPhRating(), i, 5);
            model.setValueAt(m.getPsu(), i, 6);
            model.setValueAt(m.getSalinityRating(), i, 7);
            model.setValueAt(m.getO2concentration(), i, 8);
            model.setValueAt(m.getOxygenRating(), i, 9);
        }

        resultTable.setModel(model);
        resultTable.setFillsViewportHeight(true);
        resultTable.setAutoCreateRowSorter(true);

        var columnWidths = new int[]{100, 100, 50, 100, 50, 100, 50, 100};
        for (int i = 0; i < columnWidths.length; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }


        var renderer = new QualityRatingCellRenderer();
        resultTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
        resultTable.getColumnModel().getColumn(5).setCellRenderer(renderer);
        resultTable.getColumnModel().getColumn(7).setCellRenderer(renderer);
        resultTable.getColumnModel().getColumn(9).setCellRenderer(renderer);

        var rowCount = model.getRowCount();
        var totalCount = results.size();
        if (currentSearchText.isEmpty()) {
            dataStatusLabel.setText(rowCount == 1 ? rowCount + " Datensatz" : rowCount + " Datensätze");
        } else {
            dataStatusLabel.setText(rowCount + " von " + totalCount + " Datensätzen");
        }
        model.addTableModelListener(e -> {
            if (e.getType() != TableModelEvent.UPDATE) {
                return;
            }

            int row = e.getFirstRow();
            int column = e.getColumn();

            if (row < 0 || column < 0) {
                return;
            }

            var measurement = displayResults.get(row);
            Object value = model.getValueAt(row, column);

            try {
                switch (column) {
                    case 2 -> measurement.setTemperature(Double.parseDouble(value.toString()));
                    case 4 -> measurement.setPhValue(Double.parseDouble(value.toString()));
                    case 6 -> measurement.setPsu(Double.parseDouble(value.toString()));
                    case 8 -> measurement.setO2concentration(Double.parseDouble(value.toString()));
                }
                waterMeasurementService.updateMeasurement(measurement);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Ungültiger Zahlenwert: " + value);
            }

            var count = model.getRowCount();
            if (currentSearchText.isEmpty()) {
                dataStatusLabel.setText(count == 1 ? count + " Datensatz" : count + " Datensätze");
            } else {
                dataStatusLabel.setText(count + " von " + results.size() + " Datensätzen");
            }
        });

    }

    private void applySearchFilter() {
        if (currentSearchText.isEmpty()) {
            filteredResults = new ArrayList<>(results);
            return;
        }

        var searchLower = currentSearchText.toLowerCase();
        filteredResults = results.stream()
                .filter(m -> {
                    var dateStr = m.getTimestamp().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(DATE_FORMATTER);

                    return (m.getName() != null && m.getName().toLowerCase().contains(searchLower)) ||
                           dateStr.contains(searchLower) ||
                           String.valueOf(m.getTemperature()).contains(searchLower) ||
                           String.valueOf(m.getPhValue()).contains(searchLower) ||
                           String.valueOf(m.getPsu()).contains(searchLower) ||
                           String.valueOf(m.getO2concentration()).contains(searchLower);
                })
                .toList();
    }

    private void calculateRatings() {
        var phRule = new PhRatingRule();
        var tempRule = new TemperatureRatingRule();
        var o2Rule = new O2ConcentrationRatingRule();
        var salinityRule = new SalinityRatingRule();

        var selectedRows = resultTable.getSelectedRows();

        var fish = (Fish) fishSelection.getSelectedItem();

        if (selectedRows.length == 0) {
            for (var m : results) {
                applyRules(phRule, tempRule, salinityRule, o2Rule, m, fish);
                waterMeasurementService.updateMeasurement(m);
            }
        } else {
            var displayResults = filteredResults.isEmpty() && currentSearchText.isEmpty() ? results : filteredResults;
            for (var row : selectedRows) {
                var m = displayResults.get(resultTable.convertRowIndexToModel(row));
                applyRules(phRule, tempRule, salinityRule, o2Rule, m, fish);
                waterMeasurementService.updateMeasurement(m);
            }
        }


        makeTable();
    }

    private void applyRules(
            PhRatingRule phRule,
            TemperatureRatingRule tempRule,
            SalinityRatingRule salinityRule,
            O2ConcentrationRatingRule o2Rule,
            WaterMeasurement m,
            Fish fish) {
        m.setPhRating(phRule.apply(fish.getPhMin(), fish.getPhMax(), m.getPhValue()));
        m.setTemperatureRating(tempRule.apply(fish.getTempMin(), fish.getTempMax(), m.getTemperature()));
        m.setOxygenRating(o2Rule.apply(fish.getO2ConcentrationMin(), fish.getO2ConcentrationMax(), m.getO2concentration()));
        m.setSalinityRating(salinityRule.apply(fish.getPsuMin(), fish.getPsuMax(), m.getPsu()));
    }

    public MainWindow(
            FishService fishService,
            WaterMeasurementService waterMeasurementService,
            ImportService importService
    ) {
        this.fishService = fishService;
        this.waterMeasurementService = waterMeasurementService;
        this.importService = importService;

        setTitle("Aqualyzer");
        setContentPane(contentPane);
        setSize(1600, 1200);
        setJMenuBar(menuBar);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUIComponents();
        initList();
    }

    private void initList() {
        results.addAll(waterMeasurementService.getAll());
        makeTable();
    }

    private void getNewWaterMeasurement() {

        if (results.isEmpty()) {
            initList();
        }

        if (results.isEmpty()) return;

        var all = waterMeasurementService.getAll();

        var mostRecent = results.stream()
                .max(Comparator.comparing(WaterMeasurement::getTimestamp))
                .orElseThrow();

        var newWms = all.stream()
                .filter(wm -> !wm.getTimestamp().before(mostRecent.getTimestamp()))
                .toList();

        results.addAll(newWms);

        makeTable();
    }

    private void createUIComponents() {
        makeMenus();
        fillFish();
        makeTable();
        connectUi();
    }

    private void fillFish() {
        fishSelection.removeAllItems();
        var all = fishService.getAll();
        for (var f : all) {
            fishSelection.addItem(f);
        }
    }

    private void connectUi() {
        editFishButton.setEnabled(false);
        deleteFishButton.setEnabled(false);

        neuerFischButton.addActionListener(e -> onNeuerFischButtonClicked());
        zeitreiheImportierenButton.addActionListener(e -> onMessungenImportierenButtonClicked());
        messungErfassenButton.addActionListener(e -> onMessungErfassenButtonClicked());
        fishSelection.addActionListener(e -> onFishSelectionChanged());
        menuFileExport.addActionListener(e -> onExportResultsClicked());
        menuFileExit.addActionListener(e -> onMenuFileExitClicked());
        editFishButton.addActionListener(e -> onEditFishButtonClicked());
        deleteFishButton.addActionListener(e -> onDeleteFishButtonClicked());
        refreshMeasurementsButton.addActionListener(e -> getNewWaterMeasurement());
        calculateScoreButton.addActionListener(e -> onCalculateButtonClicked());
        deleteResultButton.addActionListener(e -> onDeleteResultButtonClicked());
        exportResultButton.addActionListener(e -> onExportResultsClicked());
        fishSelection.setSelectedIndex(-1);

        resultTable.getSelectionModel().addListSelectionListener(e -> {
            deleteResultButton.setEnabled(resultTable.getSelectedRowCount() > 0);
        });

        if (searchField != null) {
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearchTextChanged(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { onSearchTextChanged(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { onSearchTextChanged(); }
            });
        }

        if (clearSearchButton != null) {
            clearSearchButton.addActionListener(e -> onClearSearchClicked());
        }
    }

    private void onSearchTextChanged() {
        currentSearchText = searchField.getText();
        makeTable();
    }

    private void onClearSearchClicked() {
        searchField.setText("");
        currentSearchText = "";
        makeTable();
    }


    private void onDeleteFishButtonClicked() {
        var selectedFish = (Fish) fishSelection.getSelectedItem();
        if (selectedFish == null) {
            JOptionPane.showMessageDialog(this, "Es ist keine Fischart ausgewählt", "Fehler", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
                "Möchten Sie die Fischart '" + selectedFish.getName() + "' wirklich löschen?",
                "Fischart löschen",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            fishService.deleteFish(selectedFish);
            fillFish();
            fishSelection.setSelectedIndex(-1);
            makeTable();
            programStatusLabel.setText("Fischart gelöscht");
        }
    }

    private void onEditFishButtonClicked() {
        var editor = new FischEditor("Fischart bearbeiten");
        var fish = (Fish) fishSelection.getSelectedItem();
        editor.setFish(fish);
        editor.setVisible(true);
        editor.setModal(true);
        makeTable();
    }

    private void onNeuerFischButtonClicked() {
        var editor = new FischEditor("Fischart hinzufügen");
        editor.setVisible(true);
        editor.setModal(true);

        var fish = editor.getFish();

        if (fish != null) {
            fishService.addFish(fish);
            fillFish();
            var newFish = editor.getFish();
            fishSelection.setSelectedItem(newFish);
        }

    }

    private void onFishSelectionChanged() {
        var fish = (Fish) fishSelection.getSelectedItem();
        editFishButton.setEnabled(fish != null);
        deleteFishButton.setEnabled(fish != null);

        for (var m : results) {
            m.setPhRating(QualityRating.Unknown);
            m.setTemperatureRating(QualityRating.Unknown);
            m.setOxygenRating(QualityRating.Unknown);
            m.setSalinityRating(QualityRating.Unknown);
        }

        makeTable();
    }

    private void onDeleteResultButtonClicked() {
        var count = resultTable.getSelectedRowCount();
        if (count == 0) return;

        var confirmed = JOptionPane.showConfirmDialog(this,
                "Sie sind im Begriff " + count + " Datensätze zu löschen. Fortfahren?",
                "Auswertungen löschen",
                JOptionPane.YES_NO_OPTION);

        if (confirmed != JOptionPane.YES_OPTION) return;
        var selectedRows = resultTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int viewIndex = selectedRows[i];
            int modelIndex = resultTable.convertRowIndexToModel(viewIndex);
            var measurement = results.get(modelIndex);
            waterMeasurementService.deleteMeasurement(measurement);
            results.remove(modelIndex);
        }

        makeTable();
        programStatusLabel.setText(count + " Messung" + (count == 1 ? "" : "en") + " gelöscht");

    }

    private void onMessungErfassenButtonClicked() {

        var editor = new MessungEditor();
        editor.setVisible(true);
        editor.setModal(true);

        var measurement = editor.getWaterMeasurement();
        if (measurement != null) {
            waterMeasurementService.addMeasurement(measurement);
            results.add(measurement);
            makeTable();
            programStatusLabel.setText("Messung erfasst");
        } else {
            programStatusLabel.setText("Keine Messung erfasst");
        }


    }

    private void onCalculateButtonClicked() {
        if (fishSelection.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie eine Fischart aus.",
                    "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Es sind keine Datensätze vorhanden.",
                    "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        programStatusLabel.setText("Berechne...");
        calculateRatings();
        programStatusLabel.setText("Auswertung abgeschlossen");
    }


    private void onMessungenImportierenButtonClicked() {
        var picker = new ZeitreihenImporter();

        picker.setVisible(true);
        picker.setModal(true);

        var path = picker.getPath();
        var charset = picker.getCharset();
        var delim = picker.getDelimiter();
        if (path == null || path.isEmpty()) {
            programStatusLabel.setText("Import abgebrochen");
            return;
        }

        programStatusLabel.setText("Importiere Messungen...");

        try {
            var imported = importService.fromCsv(path, delim, charset);

            results.addAll(imported);
            programStatusLabel.setText("Import abgeschlossen, " + imported.size() + " Messungen importiert");
            makeTable();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Aus der Datei konnten keine Messungen importiert werden. Prüfen Sie Kodierung und Inhalt der Datei.",
                    "Import fehlgeschlagen", JOptionPane.ERROR_MESSAGE);

            programStatusLabel.setText("Import fehlgeschlagen.");
        }


    }

    private void onMenuFileExitClicked() {
        System.exit(0);
    }

    private void onExportResultsClicked() {
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Es sind keine Datensätze vorhanden zum Exportieren.",
                    "Keine Daten", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("CSV-Datei speichern");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV-Dateien (*.csv)", "csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            programStatusLabel.setText("Export abgebrochen");
            return;
        }

        var selectedFile = fileChooser.getSelectedFile();
        var filePath = selectedFile.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath = filePath + ".csv";
        }

        try {
            var exportService = new CsvExportService();
            exportService.exportToCsv(results, filePath);
            programStatusLabel.setText("Export erfolgreich abgeschlossen: " + filePath);
            JOptionPane.showMessageDialog(this, "Ergebnisse erfolgreich exportiert in:\n" + filePath,
                    "Export erfolgreich", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            programStatusLabel.setText("Export fehlgeschlagen");
            JOptionPane.showMessageDialog(this, "Der Export ist fehlgeschlagen:\n" + e.getMessage(),
                    "Export fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        programStatusLabel = new JLabel();
        programStatusLabel.setText("Bereit");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(programStatusLabel, gbc);
        dataStatusLabel = new JLabel();
        dataStatusLabel.setText("0 Datensätze");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(dataStatusLabel, gbc);
        resultsTitle = new JLabel();
        resultsTitle.setText("Messungen");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(resultsTitle, gbc);
        searchLabel = new JLabel();
        searchLabel.setText("Suche:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(searchLabel, gbc);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(searchField, gbc);
        clearSearchButton = new JButton();
        clearSearchButton.setText("✕");
        clearSearchButton.setToolTipText("Suche zurücksetzen");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(clearSearchButton, gbc);
        fishSelection = new JComboBox();
        fishSelection.setEditable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(fishSelection, gbc);
        neuerFischButton = new JButton();
        neuerFischButton.setIcon(new ImageIcon(getClass().getResource("/icons/asterisk_orange.png")));
        neuerFischButton.setText("Neu");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(neuerFischButton, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 7;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(separator1, gbc);
        fishTitle = new JLabel();
        fishTitle.setText("Fischart");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(fishTitle, gbc);
        editFishButton = new JButton();
        editFishButton.setIcon(new ImageIcon(getClass().getResource("/icons/pencil.png")));
        editFishButton.setText("Bearbeiten");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(editFishButton, gbc);
        deleteFishButton = new JButton();
        deleteFishButton.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
        deleteFishButton.setText("Löschen");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(deleteFishButton, gbc);
        zeitreiheImportierenButton = new JButton();
        zeitreiheImportierenButton.setIcon(new ImageIcon(getClass().getResource("/icons/lightning_add.png")));
        zeitreiheImportierenButton.setText("Importieren");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(zeitreiheImportierenButton, gbc);
        messungErfassenButton = new JButton();
        messungErfassenButton.setIcon(new ImageIcon(getClass().getResource("/icons/table_add.png")));
        messungErfassenButton.setText("Erfassen");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(messungErfassenButton, gbc);
        refreshMeasurementsButton = new JButton();
        refreshMeasurementsButton.setIcon(new ImageIcon(getClass().getResource("/icons/database_refresh.png")));
        refreshMeasurementsButton.setText("Aktualisieren");
        refreshMeasurementsButton.setToolTipText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(refreshMeasurementsButton, gbc);
        calculateScoreButton = new JButton();
        calculateScoreButton.setIcon(new ImageIcon(getClass().getResource("/icons/lightbulb.png")));
        calculateScoreButton.setText("Auswerten");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(calculateScoreButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(spacer1, gbc);
        tableContainerPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(tableContainerPane, gbc);
        resultTable = new JTable();
        tableContainerPane.setViewportView(resultTable);
        deleteResultButton = new JButton();
        deleteResultButton.setEnabled(false);
        deleteResultButton.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
        deleteResultButton.setText("Löschen");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(deleteResultButton, gbc);
        exportResultButton = new JButton();
        exportResultButton.setIcon(new ImageIcon(getClass().getResource("/icons/bullet_disk.png")));
        exportResultButton.setText("Exportieren");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(exportResultButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
