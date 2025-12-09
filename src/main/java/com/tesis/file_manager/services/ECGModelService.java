package com.tesis.file_manager.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.IntVector;
import smile.validation.metric.Accuracy;

import java.io.*;
import java.util.Properties;

@Slf4j
@Service
public class ECGModelService {

    private static final String MODEL_FILE = "ecg_randomforest.model";
    private RandomForest modelo;

    /**
     * Entrena y evalúa un RandomForest usando la API de SMILE 2.6.0 basada en DataFrame + Formula.
     * - trainData: matriz [n_train x n_features]
     * - trainLabels: vector de etiquetas [n_train]
     * - testData: matriz [n_test x n_features]
     * - testLabels: vector de etiquetas [n_test]
     */
    public void entrenarYEvaluarModelo(double[][] trainData, int[] trainLabels,
                                       double[][] testData, int[] testLabels) {

        try {
            // VALIDACIONES básicas
            log.info("Validando tamaños de los datos...");
            if (trainData == null || trainLabels == null || testData == null || testLabels == null) {
                log.error("Uno de los arreglos de datos es null.");
                return;
            }

            log.info("Train samples: {}, Train labels: {}", trainData.length, trainLabels.length);
            log.info("Test samples: {}, Test labels: {}", testData.length, testLabels.length);

            if (trainData.length == 0 || trainLabels.length == 0) {
                log.error("Conjunto de entrenamiento vacío. Abortando.");
                return;
            }
            if (testData.length == 0 || testLabels.length == 0) {
                log.error("Conjunto de prueba vacío. Abortando.");
                return;
            }
            if (trainData.length != trainLabels.length) {
                log.error("Desajuste: trainData.length != trainLabels.length");
                return;
            }
            if (testData.length != testLabels.length) {
                log.error("Desajuste: testData.length != testLabels.length");
                return;
            }

            // Crear nombres de columnas simples: F1, F2, ..., Fn
            int nFeatures = trainData[0].length;
            if (nFeatures == 0) {
                log.error("Número de características es 0. Revisar la carga de CSV.");
                return;
            }
            String[] columnNames = new String[nFeatures];
            for (int i = 0; i < nFeatures; i++) columnNames[i] = "F" + (i + 1);

            // Construir DataFrame de entrenamiento y prueba + columna de clase
            DataFrame trainDf = DataFrame.of(trainData, columnNames)
                    .merge(IntVector.of("clase", trainLabels));

            DataFrame testDf = DataFrame.of(testData, columnNames)
                    .merge(IntVector.of("clase", testLabels));

            log.info("DataFrames creados: train filas={}, test filas={}", trainDf.size(), testDf.size());

            // Parámetros de RandomForest
            Properties props = new Properties();
            props.setProperty("nTrees", "100");
            props.setProperty("maxNodes", "200");
            props.setProperty("nodeSize", "5");

            // Entrenamiento
            log.info("Entrenando RandomForest...");
            modelo = RandomForest.fit(Formula.lhs("clase"), trainDf, props);
            log.info("Entrenamiento finalizado.");

            // Predicciones
            int[] trainPreds = modelo.predict(trainDf);
            int[] testPreds = modelo.predict(testDf);

            double accTrain = Accuracy.of(trainLabels, trainPreds);
            double accTest = Accuracy.of(testLabels, testPreds);

            log.info("🎯 Precisión en entrenamiento: {}%", String.format("%.2f", accTrain * 100));
            log.info("🎯 Precisión en prueba: {}%", String.format("%.2f", accTest * 100));

            // Métricas adicionales
            calcularYLogearMetricas(testPreds, testLabels);

            // Guardar modelo
            guardarModeloPorSerializacion();

        } catch (Exception e) {
            log.error("Error en entrenarYEvaluarModelo: {}", e.getMessage(), e);
        }
    }

    private void calcularYLogearMetricas(int[] preds, int[] reales) {
        int vp = 0, fp = 0, vn = 0, fn = 0;
        for (int i = 0; i < preds.length; i++) {
            if (preds[i] == 1 && reales[i] == 1) vp++;
            else if (preds[i] == 1 && reales[i] == 0) fp++;
            else if (preds[i] == 0 && reales[i] == 0) vn++;
            else if (preds[i] == 0 && reales[i] == 1) fn++;
        }

        double precision = (vp + fp) > 0 ? (double) vp / (vp + fp) : 0;
        double recall = (vp + fn) > 0 ? (double) vp / (vp + fn) : 0;
        double f1 = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0;

        log.info("Matriz de confusión - VP: {}, FP: {}, FN: {}, VN: {}", vp, fp, fn, vn);
        log.info("Precision: {}%, Recall: {}%, F1: {}%",
                String.format("%.2f", precision * 100),
                String.format("%.2f", recall * 100),
                String.format("%.2f", f1 * 100));
    }

    private void guardarModeloPorSerializacion() {
        if (modelo == null) {
            log.warn("No hay modelo entrenado para guardar.");
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MODEL_FILE))) {
            oos.writeObject(modelo);
            log.info("Modelo serializado guardado en '{}'", MODEL_FILE);
        } catch (IOException e) {
            log.error("Error al guardar modelo: {}", e.getMessage(), e);
        }
    }

    /**
     * Carga el modelo serializado desde disco (ObjectInputStream).
     */
    public void cargarModeloSerializado() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MODEL_FILE))) {
            modelo = (RandomForest) ois.readObject();
            log.info("Modelo cargado desde '{}'", MODEL_FILE);
        } catch (FileNotFoundException fnf) {
            log.warn("Archivo de modelo no encontrado: '{}'. Debes entrenar primero.", MODEL_FILE);
        } catch (Exception e) {
            log.error("Error al cargar modelo: {}", e.getMessage(), e);
        }
    }

    /**
     * Predice una sola muestra (vector features). Construye un DataFrame de 1 fila y retorna la etiqueta predicha.
     */
    public int predecir(double[] features) {
        if (features == null || features.length == 0) {
            throw new IllegalArgumentException("Las features no pueden ser null/empty");
        }
        if (modelo == null) {
            throw new IllegalStateException("Modelo no cargado. Llama a cargarModeloSerializado() o entrena primero.");
        }

        // Nombres de columnas F1...Fn
        String[] cols = new String[features.length];
        for (int i = 0; i < features.length; i++) cols[i] = "F" + (i + 1);

        DataFrame input = DataFrame.of(new double[][]{features}, cols);
        int[] pred = modelo.predict(input);
        return pred[0];
    }
}













