package com.tesis.file_manager.services;

import com.tesis.file_manager.entity.Fileentity;
import com.tesis.file_manager.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.*;

@Service
@RequiredArgsConstructor
public class ECGDataService {

    private final FileRepository fileRepository;

    public Map<String, Object> loadAndSplitFromDatabase() throws Exception {
        List<Fileentity> allFiles = fileRepository.findAll();

        List<double[]> healthy = new ArrayList<>();
        List<double[]> dns = new ArrayList<>();

        for (Fileentity file : allFiles) {
            if (file.getType().equalsIgnoreCase("text/csv")) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(file.getData())))) {

                    CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

                    // Convierte todo el archivo en un solo vector (una muestra)
                    List<Double> valores = new ArrayList<>();

                    for (CSVRecord record : parser) {
                        for (int i = 1; i < record.size(); i++) { // salta columna de tiempo o ID
                            try {
                                valores.add(Double.parseDouble(record.get(i)));
                            } catch (NumberFormatException e) {
                                valores.add(0.0);
                            }
                        }
                    }

                    double[] sample = valores.stream().mapToDouble(Double::doubleValue).toArray();

                    if (file.getDiagnostico().equalsIgnoreCase("DNS")) {
                        dns.add(sample);
                    } else {
                        healthy.add(sample);
                    }

                    System.out.println("📄 Archivo leído: " + file.getName() +
                            " | Diagnóstico: " + file.getDiagnostico() +
                            " | Puntos: " + sample.length);
                }
            }
        }

        if (healthy.isEmpty() && dns.isEmpty()) {
            throw new RuntimeException("❌ No se encontraron datos ECG válidos en la base de datos");
        }

        // Mezclar aleatoriamente los conjuntos
        Collections.shuffle(healthy);
        Collections.shuffle(dns);

        // División 70/30
        int trainHealthySize = (int) (healthy.size() * 0.7);
        int trainDnsSize = (int) (dns.size() * 0.7);

        List<double[]> trainHealthy = healthy.subList(0, trainHealthySize);
        List<double[]> testHealthy = healthy.subList(trainHealthySize, healthy.size());

        List<double[]> trainDns = dns.subList(0, Math.max(1, trainDnsSize));
        List<double[]> testDns = dns.subList(Math.max(1, trainDnsSize), dns.size());

        System.out.println("📊 División inicial de datos:");
        System.out.println("Sanos → Train: " + trainHealthy.size() + ", Test: " + testHealthy.size());
        System.out.println("DNS   → Train: " + trainDns.size() + ", Test: " + testDns.size());

        // ⚖️ Balanceo adaptativo (solo si hay desbalance fuerte)
        List<double[]> trainHealthyBalanced = new ArrayList<>(trainHealthy);
        List<double[]> trainDnsBalanced = new ArrayList<>(trainDns);

        if (trainDnsBalanced.size() < trainHealthyBalanced.size()) {
            System.out.println("⚠️ Pocos datos DNS detectados. Aplicando oversampling adaptativo...");

            while (trainDnsBalanced.size() < trainHealthyBalanced.size()) {
                trainDnsBalanced.addAll(new ArrayList<>(trainDns));
                if (trainDnsBalanced.size() > trainHealthyBalanced.size()) {
                    trainDnsBalanced = trainDnsBalanced.subList(0, trainHealthyBalanced.size());
                    break;
                }
            }

            System.out.println("✅ Oversampling completado: "
                    + trainHealthyBalanced.size() + " sanos / "
                    + trainDnsBalanced.size() + " DNS en entrenamiento.");
        } else {
            System.out.println("✅ Conjunto DNS balanceado o superior, sin oversampling aplicado.");
        }

        // Crear conjuntos finales
        List<double[]> trainData = Stream.concat(trainHealthyBalanced.stream(), trainDnsBalanced.stream())
                .collect(Collectors.toList());
        List<double[]> testData = Stream.concat(testHealthy.stream(), testDns.stream())
                .collect(Collectors.toList());

        // Etiquetas: 0 = sano, 1 = DNS
        int[] trainLabels = new int[trainData.size()];
        int[] testLabels = new int[testData.size()];

        for (int i = 0; i < trainHealthyBalanced.size(); i++) trainLabels[i] = 0;
        for (int i = 0; i < trainDnsBalanced.size(); i++) trainLabels[trainHealthyBalanced.size() + i] = 1;

        for (int i = 0; i < testHealthy.size(); i++) testLabels[i] = 0;
        for (int i = 0; i < testDns.size(); i++) testLabels[testHealthy.size() + i] = 1;

        Map<String, Object> result = new HashMap<>();
        result.put("trainData", trainData.toArray(new double[0][]));
        result.put("trainLabels", trainLabels);
        result.put("testData", testData.toArray(new double[0][]));
        result.put("testLabels", testLabels);

        System.out.println("División final completada:");
        System.out.println("Entrenamiento total: " + trainData.size() + " muestras.");
        System.out.println("Prueba total: " + testData.size() + " muestras.");

        return result;
    }
}
