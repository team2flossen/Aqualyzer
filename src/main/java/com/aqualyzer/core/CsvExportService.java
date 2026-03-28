package com.aqualyzer.core;

import com.aqualyzer.core.enums.QualityRating;
import com.aqualyzer.core.model.Result;
import com.aqualyzer.core.model.WaterMeasurement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportToCsv(List<WaterMeasurement> measurements, Map<UUID, Result> resultsByMeasurement, String filePath) throws IOException {
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

            for (var m : measurements) {
                var sortableDate = m.getTimestamp().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DATE_FORMATTER);

                var result = resultsByMeasurement.get(m.getId());

                csvPrinter.printRecord(
                        sortableDate,
                        m.getName(),
                        m.getTemperature(),
                        result != null ? result.getTemperatureRating() : QualityRating.Unknown,
                        m.getPhValue(),
                        result != null ? result.getPhRating() : QualityRating.Unknown,
                        m.getPsu(),
                        result != null ? result.getSalinityRating() : QualityRating.Unknown,
                        m.getO2concentration(),
                        result != null ? result.getOxygenRating() : QualityRating.Unknown
                );
            }

            csvPrinter.flush();
        }
    }
}
