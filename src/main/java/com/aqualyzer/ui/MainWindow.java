package com.aqualyzer.ui;

import com.aqualyzer.core.FishService;
import com.aqualyzer.core.ImportService;
import com.aqualyzer.core.ResultService;
import com.aqualyzer.core.WaterMeasurementService;
import com.aqualyzer.core.CsvExportService;
import com.aqualyzer.core.enums.QualityRating;
import com.aqualyzer.core.model.Fish;
import com.aqualyzer.core.model.Result;
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
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final FishService fishService;
    private final WaterMeasurementService waterMeasurementService;
    private final ResultService resultService;
    private final ImportService importService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    private final List<WaterMeasurement> measurements = new ArrayList<>();
    private List<WaterMeasurement> filteredMeasurements = new ArrayList<>();
    // Result-Lookup: measurementId -> Result (für den aktuell gewählten Fisch)
    private final Map<UUID, Result> resultsByMeasurement = new HashMap<>();
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
        menuFile = new JMenu(Messages.get("main.menu.file"));
        menuFileExport = new JMenuItem(Messages.get("main.menu.file.export"));
        menuFileExit = new JMenuItem(Messages.get("main.menu.file.exit"));
        menuFile.add(menuFileExport);
        menuFile.addSeparator();
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
    }

    private void loadResultsForSelectedFish() {
        resultsByMeasurement.clear();
        var fish = (Fish) fishSelection.getSelectedItem();
        if (fish == null) return;

        var results = resultService.getByFish(fish);
        for (var r : results) {
            resultsByMeasurement.put(r.getMeasurement().getId(), r);
        }
    }

    private QualityRating getRating(WaterMeasurement m, java.util.function.Function<Result, QualityRating> getter) {
        var result = resultsByMeasurement.get(m.getId());
        return result != null ? getter.apply(result) : QualityRating.Unknown;
    }

    private void makeTable() {
        applySearchFilter();
        var displayResults = filteredMeasurements.isEmpty() && currentSearchText.isEmpty() ? measurements : filteredMeasurements;

        var model = new DefaultTableModel(
                new Object[displayResults.size()][10],
                new String[]{
                        Messages.get("main.table.header.time"),
                        Messages.get("main.table.header.station"),
                        Messages.get("main.table.header.waterTemperature"),
                        Messages.get("main.table.header.temperatureRating"),
                        Messages.get("main.table.header.phValue"),
                        Messages.get("main.table.header.phRating"),
                        Messages.get("main.table.header.salinity"),
                        Messages.get("main.table.header.salinityRating"),
                        Messages.get("main.table.header.oxygenLevel"),
                        Messages.get("main.table.header.oxygenRating")
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
            model.setValueAt(getRating(m, Result::getTemperatureRating), i, 3);
            model.setValueAt(m.getPhValue(), i, 4);
            model.setValueAt(getRating(m, Result::getPhRating), i, 5);
            model.setValueAt(m.getPsu(), i, 6);
            model.setValueAt(getRating(m, Result::getSalinityRating), i, 7);
            model.setValueAt(m.getO2concentration(), i, 8);
            model.setValueAt(getRating(m, Result::getOxygenRating), i, 9);
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
        var totalCount = measurements.size();
        if (currentSearchText.isEmpty()) {
            dataStatusLabel.setText(Messages.get("main.status.recordCount", rowCount));
        } else {
            dataStatusLabel.setText(Messages.get("main.status.filteredRecordCount", rowCount, totalCount));
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
                JOptionPane.showMessageDialog(null, Messages.get("main.dialog.invalidNumber", value));
            }

            var count = model.getRowCount();
            if (currentSearchText.isEmpty()) {
                dataStatusLabel.setText(Messages.get("main.status.recordCount", count));
            } else {
                dataStatusLabel.setText(Messages.get("main.status.filteredRecordCount", count, measurements.size()));
            }
        });

    }

    private void applySearchFilter() {
        if (currentSearchText.isEmpty()) {
            filteredMeasurements = new ArrayList<>(measurements);
            return;
        }

        var searchLower = currentSearchText.toLowerCase();
        filteredMeasurements = measurements.stream()
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

        var fish = (Fish) fishSelection.getSelectedItem();
        var selectedRows = resultTable.getSelectedRows();

        List<WaterMeasurement> toEvaluate;
        if (selectedRows.length == 0) {
            toEvaluate = measurements;
        } else {
            var displayResults = filteredMeasurements.isEmpty() && currentSearchText.isEmpty() ? measurements : filteredMeasurements;
            toEvaluate = new ArrayList<>();
            for (var row : selectedRows) {
                toEvaluate.add(displayResults.get(resultTable.convertRowIndexToModel(row)));
            }
        }

        for (var m : toEvaluate) {
            var result = resultsByMeasurement.get(m.getId());
            if (result == null) {
                result = new Result(m, fish);
                resultsByMeasurement.put(m.getId(), result);
            }

            result.setPhRating(phRule.apply(fish.getPhMin(), fish.getPhMax(), m.getPhValue()));
            result.setTemperatureRating(tempRule.apply(fish.getTempMin(), fish.getTempMax(), m.getTemperature()));
            result.setOxygenRating(o2Rule.apply(fish.getO2ConcentrationMin(), fish.getO2ConcentrationMax(), m.getO2concentration()));
            result.setSalinityRating(salinityRule.apply(fish.getPsuMin(), fish.getPsuMax(), m.getPsu()));

            resultService.save(result);
        }

        makeTable();
    }

    public MainWindow(
            FishService fishService,
            WaterMeasurementService waterMeasurementService,
            ResultService resultService,
            ImportService importService
    ) {
        this.fishService = fishService;
        this.waterMeasurementService = waterMeasurementService;
        this.resultService = resultService;
        this.importService = importService;

        setTitle(Messages.get("app.title"));
        setContentPane(contentPane);
        setSize(1600, 1200);
        setJMenuBar(menuBar);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUIComponents();
        initList();
    }

    private void initList() {
        measurements.addAll(waterMeasurementService.getAll());
        makeTable();
    }

    private void getNewWaterMeasurement() {

        if (measurements.isEmpty()) {
            initList();
        }

        if (measurements.isEmpty()) return;

        var all = waterMeasurementService.getAll();

        var mostRecent = measurements.stream()
                .max(Comparator.comparing(WaterMeasurement::getTimestamp))
                .orElseThrow();

        var newWms = all.stream()
                .filter(wm -> !wm.getTimestamp().before(mostRecent.getTimestamp()))
                .toList();

        measurements.addAll(newWms);

        makeTable();
    }

    private void createUIComponents() {
        localizeComponents();
        makeMenus();
        fillFish();
        makeTable();
        connectUi();
    }

    private void localizeComponents() {
        programStatusLabel.setText(Messages.get("main.label.ready"));
        dataStatusLabel.setText(Messages.get("main.label.noData"));
        resultsTitle.setText(Messages.get("main.label.measurements"));
        searchLabel.setText(Messages.get("main.label.search"));
        fishTitle.setText(Messages.get("main.label.fishType"));
        neuerFischButton.setText(Messages.get("main.button.new"));
        editFishButton.setText(Messages.get("main.button.edit"));
        deleteFishButton.setText(Messages.get("main.button.delete"));
        zeitreiheImportierenButton.setText(Messages.get("main.button.import"));
        messungErfassenButton.setText(Messages.get("main.button.record"));
        refreshMeasurementsButton.setText(Messages.get("main.button.refresh"));
        calculateScoreButton.setText(Messages.get("main.button.evaluate"));
        deleteResultButton.setText(Messages.get("main.button.delete"));
        exportResultButton.setText(Messages.get("main.button.export"));
        clearSearchButton.setText(Messages.get("main.button.clearSearch"));
        clearSearchButton.setToolTipText(Messages.get("main.tooltip.clearSearch"));

        // Status-Labels aus dem GridBagLayout entkoppeln, damit sie Spalte 0 nicht aufblähen
        contentPane.remove(programStatusLabel);
        contentPane.remove(dataStatusLabel);

        var statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusBar.add(programStatusLabel, BorderLayout.WEST);
        statusBar.add(dataStatusLabel, BorderLayout.EAST);

        var wrapper = new JPanel(new BorderLayout());
        wrapper.add(contentPane, BorderLayout.CENTER);
        wrapper.add(statusBar, BorderLayout.SOUTH);
        setContentPane(wrapper);
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
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.noFishSelected"), Messages.get("main.dialog.noFishSelected.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
                Messages.get("main.dialog.confirmDeleteFish", selectedFish.getName()),
                Messages.get("main.dialog.confirmDeleteFish.title"),
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            resultService.deleteByFish(selectedFish);
            fishService.deleteFish(selectedFish);
            fillFish();
            fishSelection.setSelectedIndex(-1);
            makeTable();
            programStatusLabel.setText(Messages.get("main.status.fishDeleted"));
        }
    }

    private void onEditFishButtonClicked() {
        var editor = new FischEditor(Messages.get("editor.fish.title.edit"));
        var fish = (Fish) fishSelection.getSelectedItem();
        editor.setFish(fish);
        editor.setVisible(true);
        editor.setModal(true);
        makeTable();
    }

    private void onNeuerFischButtonClicked() {
        var editor = new FischEditor(Messages.get("editor.fish.title.add"));
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

        loadResultsForSelectedFish();
        makeTable();
    }

    private void onDeleteResultButtonClicked() {
        var count = resultTable.getSelectedRowCount();
        if (count == 0) return;

        var confirmed = JOptionPane.showConfirmDialog(this,
                Messages.get("main.dialog.confirmDeleteRecords", count),
                Messages.get("main.dialog.confirmDeleteRecords.title"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed != JOptionPane.YES_OPTION) return;
        var selectedRows = resultTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int viewIndex = selectedRows[i];
            int modelIndex = resultTable.convertRowIndexToModel(viewIndex);
            var measurement = measurements.get(modelIndex);
            resultService.deleteByMeasurement(measurement);
            waterMeasurementService.deleteMeasurement(measurement);
            resultsByMeasurement.remove(measurement.getId());
            measurements.remove(modelIndex);
        }

        makeTable();
        programStatusLabel.setText(Messages.get("main.status.deletedMeasurements", count));

    }

    private void onMessungErfassenButtonClicked() {

        var editor = new MessungEditor();
        editor.setVisible(true);
        editor.setModal(true);

        var measurement = editor.getWaterMeasurement();
        if (measurement != null) {
            waterMeasurementService.addMeasurement(measurement);
            measurements.add(measurement);
            makeTable();
            programStatusLabel.setText(Messages.get("main.status.measurementRecorded"));
        } else {
            programStatusLabel.setText(Messages.get("main.status.noMeasurementRecorded"));
        }


    }

    private void onCalculateButtonClicked() {
        if (fishSelection.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.selectFishFirst"),
                    "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (measurements.isEmpty()) {
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.noData"),
                    "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        programStatusLabel.setText(Messages.get("main.status.calculating"));
        calculateRatings();
        programStatusLabel.setText(Messages.get("main.status.evaluationComplete"));
    }


    private void onMessungenImportierenButtonClicked() {
        var picker = new ZeitreihenImporter();

        picker.setVisible(true);
        picker.setModal(true);

        var path = picker.getPath();
        var charset = picker.getCharset();
        var delim = picker.getDelimiter();
        if (path == null || path.isEmpty()) {
            programStatusLabel.setText(Messages.get("main.status.importCancelled"));
            return;
        }

        programStatusLabel.setText(Messages.get("main.status.importing"));

        try {
            var imported = importService.fromCsv(path, delim, charset);

            measurements.addAll(imported);
            programStatusLabel.setText(Messages.get("main.status.importComplete", imported.size()));
            makeTable();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.importError"),
                    Messages.get("main.dialog.importError.title"), JOptionPane.ERROR_MESSAGE);

            programStatusLabel.setText(Messages.get("main.status.importFailed"));
        }


    }

    private void onMenuFileExitClicked() {
        System.exit(0);
    }

    private void onExportResultsClicked() {
        if (measurements.isEmpty()) {
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.noDataToExport"),
                    Messages.get("main.dialog.noDataToExport.title"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Messages.get("main.dialog.saveAsCsv"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Messages.get("main.dialog.csvFilter"), "csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            programStatusLabel.setText(Messages.get("main.status.exportCancelled"));
            return;
        }

        var selectedFile = fileChooser.getSelectedFile();
        var filePath = selectedFile.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath = filePath + ".csv";
        }

        try {
            var exportService = new CsvExportService();
            exportService.exportToCsv(measurements, resultsByMeasurement, filePath);
            programStatusLabel.setText(Messages.get("main.status.exportComplete", filePath));
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.exportSuccess", filePath),
                    Messages.get("main.dialog.exportSuccess.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            programStatusLabel.setText(Messages.get("main.status.exportFailed"));
            JOptionPane.showMessageDialog(this, Messages.get("main.dialog.exportError", e.getMessage()),
                    Messages.get("main.dialog.exportError.title"), JOptionPane.ERROR_MESSAGE);
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
