package com.aqualyzer.core;

import com.aqualyzer.ui.viewmodels.ResultListViewModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportToCsv(List<ResultListViewModel> results, String filePath) throws IOException {
        var headers = new String[]{
                "Zeit", "Station", "Wassertemperatur", "Temperaturbewertung",
                "pH-Wert", "pH-Bewertung", "Salzgehalt", "Salzgehalt-Bewertung",
                "Sauerstoffgehalt", "Sauerstoffbewertung"
        };

        var csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build();

        try (var fileWriter = new FileWriter(filePath, StandardCharsets.UTF_8);
             var csvPrinter = new CSVPrinter(fileWriter, csvFormat)) {

            for (var result : results) {
                var measurement = result.getMeasurement();
                var sortableDate = measurement.getTimestamp().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DATE_FORMATTER);

                csvPrinter.printRecord(
                        sortableDate,
                        measurement.getName(),
                        measurement.getTemperature(),
                        result.getTemperatureRating(),
                        measurement.getPhValue(),
                        result.getPhRating(),
                        measurement.getPsu(),
                        result.getSalinityRating(),
                        measurement.getO2concentration(),
                        result.getOxygenRating()
                );
            }

            csvPrinter.flush();
        }
    }
}
