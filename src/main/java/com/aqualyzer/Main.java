package com.aqualyzer;

import com.aqualyzer.core.FishService;
import com.aqualyzer.core.ImportService;
import com.aqualyzer.core.ResultService;
import com.aqualyzer.core.WaterMeasurementService;
import com.aqualyzer.ui.MainWindow;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import javax.swing.*;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Inject
    FishService fishService;

    @Inject
    WaterMeasurementService waterMeasurementService;

    @Inject
    ResultService resultService;

    @Override
    public int run(String... args) {
        var importService = new ImportService(waterMeasurementService);

        SwingUtilities.invokeLater(() -> {
            new MainWindow(fishService, waterMeasurementService, resultService, importService).setVisible(true);
        });

        Quarkus.waitForExit();
        return 0;
    }

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }
}
