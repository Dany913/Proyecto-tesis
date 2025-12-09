package com.tesis.file_manager.controller;

import com.tesis.file_manager.entity.Fileentity;
import com.tesis.file_manager.repository.FileRepository;
import com.tesis.file_manager.services.ECGDataService;
import com.tesis.file_manager.services.ECGModelService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ecg-model")
@RequiredArgsConstructor
public class ECGModelController {

    private final ECGDataService ecgDataService;
    private final ECGModelService ecgModelService;
    private final FileRepository fileRepository;

    /**
     * Entrena y evalúa el modelo usando los datos de la base de datos.
     */
    @PostMapping("/train")
    public String trainModel() {
        try {
            Map<String, Object> data = ecgDataService.loadAndSplitFromDatabase();

            double[][] trainData = (double[][]) data.get("trainData");
            int[] trainLabels = (int[]) data.get("trainLabels");
            double[][] testData = (double[][]) data.get("testData");
            int[] testLabels = (int[]) data.get("testLabels");

            ecgModelService.entrenarYEvaluarModelo(trainData, trainLabels, testData, testLabels);
            return "   Entrenamiento completado. Revisa la consola para métricas y división de datos.";
        } catch (Exception e) {
            e.printStackTrace();
            return " Error durante el entrenamiento: " + e.getMessage();
        }
    }

    /**
     * Predice usando un archivo ya guardado en la base de datos.
     * Ejemplo: POST /api/ecg-model/predict?fileName=registro_1.csv
     */
    @PostMapping("/predict")
    public String predict(@RequestParam String fileName) {
        try {
            Optional<Fileentity> optionalFile = fileRepository.findByName(fileName);
            if (optionalFile.isEmpty()) {
                return " No se encontró el archivo con nombre: " + fileName;
            }

            Fileentity file = optionalFile.get();
            if (!file.getType().equalsIgnoreCase("text/csv")) {
                return " El archivo no es de tipo CSV.";
            }

            // Leer los datos del archivo CSV desde la base de datos
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getData())));
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            List<Double> valores = new ArrayList<>();
            for (CSVRecord record : parser) {
                for (int i = 1; i < record.size(); i++) { // omite la columna de tiempo o ID
                    try {
                        valores.add(Double.parseDouble(record.get(i)));
                    } catch (NumberFormatException e) {
                        valores.add(0.0);
                    }
                }
            }
            parser.close();

            double[] features = valores.stream().mapToDouble(Double::doubleValue).toArray();

            // Cargar modelo entrenado y hacer predicción
            ecgModelService.cargarModeloSerializado();
            int resultado = ecgModelService.predecir(features);

            String diagnostico = (resultado == 1)
                    ? "Arritmia asociada a disfunción nodal sinusal (DNS)"
                    : "Ritmo sinusal normal";

            return "🧠 Archivo: " + fileName + "\n📊 Diagnóstico: " + diagnostico;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al procesar la predicción: " + e.getMessage();
        }
    }
}



