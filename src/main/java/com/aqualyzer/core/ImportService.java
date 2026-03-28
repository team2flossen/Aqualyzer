package com.aqualyzer.core;

import com.aqualyzer.core.model.WaterMeasurement;

import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.commons.csv.CSVFormat;

import static java.util.Locale.GERMAN;


public class ImportService {

    private final WaterMeasurementService waterMeasurementService;

    public ImportService(WaterMeasurementService waterMeasurementService) {
        this.waterMeasurementService = waterMeasurementService;
    }

    public List<WaterMeasurement> fromCsv(String path, char delim, Charset charset){

        int recordNumber = 0;

        try (var reader = new FileReader(path, charset)) {

            var numberFormat = java.text.NumberFormat.getInstance(GERMAN);

            var format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(delim)
                    .get();

            var parser = format.parse(reader);

            String station = null;
            var metricIndex = new HashMap<Integer, String>();
            var results = new ArrayList<WaterMeasurement>();

            boolean inHeader = true;

            for (var record : parser) {
                recordNumber++;

                // header-werte
                if (inHeader){
                    var key = record.get(0);
                    switch (key) {
                        case "Station":
                            station = record.get(1);
                            break;
                        case "Messgröße Kurzname":
                            for (int j = 1; j < record.size(); j++) {
                                var metric = record.get(j);
                                metricIndex.put(j, metric);
                                // 1 passt weil 0 immer zeitstempel
                            }
                            break;
                        case "Zeitstempel":
                            inHeader = false;
                            break;
                    }
                    continue ;
                }

                var timestamp = record.get(0);

                Double phValue = null;
                Double temperature = null;
                Double salinity = null;
                Double o2concentration = null;

                // body
                for (int j = 1; j < record.size(); j++){
                    var cell = record.get(j);
                    if (cell == null || cell.isEmpty()) {
                        continue;
                    }
                    var metric = metricIndex.get(j);
                    if (metric == null) continue;
                    switch (metric){
                        case "Tw":
                            temperature = numberFormat.parse(cell).doubleValue();
                            break;
                        case "pH":
                            phValue = numberFormat.parse(cell).doubleValue();
                            break;
                        case "O2":
                            o2concentration = numberFormat.parse(cell).doubleValue();
                            break;
                        case "S":
                        case "SP":
                            salinity = numberFormat.parse(cell).doubleValue();
                            break;
                        default:
                            break;
                    }
                }

                var waterMeasurement = new WaterMeasurement(
                        timestamp,
                        station,
                        salinity,
                        phValue,
                        temperature,
                        o2concentration);

                waterMeasurementService.addMeasurement(waterMeasurement);
                results.add(waterMeasurement);

            }


            return results;
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Lesen in Zeile " + recordNumber, e);
        }


    }


}
