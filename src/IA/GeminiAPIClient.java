package IA;

import red.Json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class GeminiAPIClient {
    // Modelos vigentes en orden de preferencia: si uno devuelve 404/503 o timeout, se intenta el siguiente.
    private static final String[] MODELOS = {
            "gemini-3.1-flash-lite",
            "gemini-flash-lite-latest",
            "gemini-3.8-flash",
            "gemini-flash-latest"
    };
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    /**
     * Envía una consulta a Gemini con instrucción de sistema, historial y pregunta actual.
     *
     * @param instruccionSistema Contexto de la tienda y catálogo vigente
     * @param historial          Mensajes previos de la sesión {rol, texto}
     * @param pregunta           Pregunta actual del cliente
     * @return Texto de respuesta generado por Gemini
     */
    public String consultar(String instruccionSistema, List<String[]> historial, String pregunta) throws Exception {
        String apiKey = obtenerApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || "PEGA_TU_API_KEY_AQUI".equals(apiKey.trim())) {
            throw new IllegalStateException(
                    "No se configuró GEMINI_API_KEY en el servidor (.vscode/launch.json, variable de entorno o config.properties)."
            );
        }

        String cuerpo = construirCuerpoJSON(instruccionSistema, historial, pregunta);
        Exception ultimoErrorRecuperable = null;

        for (String modelo : MODELOS) {
            try {
                return consultarModelo(modelo, apiKey.trim(), cuerpo);
            } catch (RecuperableException ex) {
                ultimoErrorRecuperable = ex;
                System.err.println("[IA] Modelo " + modelo + " no disponible (" + ex.getMessage() + "), probando respaldo...");
            }
        }

        if (ultimoErrorRecuperable != null) {
            throw new RuntimeException(ultimoErrorRecuperable.getMessage());
        }
        throw new RuntimeException("No hay modelos de Gemini disponibles en este momento.");
    }

    private String consultarModelo(String modelo, String apiKey, String cuerpo) throws Exception {
        URL url = URI.create(BASE_URL + modelo + ":generateContent?key=" + apiKey).toURL();
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conexion.setConnectTimeout(10_000);
        conexion.setReadTimeout(20_000);
        conexion.setDoOutput(true);

        try (OutputStream salida = conexion.getOutputStream()) {
            salida.write(cuerpo.getBytes(StandardCharsets.UTF_8));
        } catch (UnknownHostException e) {
            throw new RuntimeException("Sin conexión a internet para contactar a Gemini.");
        } catch (SocketTimeoutException e) {
            throw new RecuperableException("Tiempo de espera agotado al conectar con " + modelo + ".");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo conectar con la API de Gemini: " + e.getMessage());
        }

        int codigo;
        try {
            codigo = conexion.getResponseCode();
        } catch (SocketTimeoutException e) {
            throw new RecuperableException("Tiempo de espera agotado esperando respuesta de " + modelo + ".");
        } catch (IOException e) {
            throw new RuntimeException("Error de conexión al comunicarse con Gemini: " + e.getMessage());
        }

        if (codigo != HttpURLConnection.HTTP_OK) {
            String error = leerRespuesta(conexion.getErrorStream());
            String detalle = extraerMensajeError(error);
            if (codigo == 429) {
                throw new RuntimeException("Límite de solicitudes excedido en la API de Gemini. Espera unos segundos e intenta de nuevo.");
            } else if (codigo == 400 || codigo == 401 || codigo == 403) {
                throw new RuntimeException("API Key inválida o sin permisos (" + codigo + ")" + (detalle.isEmpty() ? "." : ": " + detalle));
            } else if (codigo == 404 || codigo == 500 || codigo == 503) {
                throw new RecuperableException("El modelo " + modelo + " respondió HTTP " + codigo + (detalle.isEmpty() ? "" : ": " + detalle));
            }
            throw new RuntimeException("Error de Gemini (HTTP " + codigo + ")" + (detalle.isEmpty() ? "" : ": " + detalle));
        }

        String respuesta = leerRespuesta(conexion.getInputStream());
        return extraerTexto(respuesta);
    }

    private static class RecuperableException extends Exception {
        RecuperableException(String message) {
            super(message);
        }
    }

    //Obtiene la API para que funcione
    private String obtenerApiKey() {
        String env = System.getenv("GEMINI_API_KEY");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }

        String prop = System.getProperty("GEMINI_API_KEY");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }

        File configFile = new File("config.properties");
        if (configFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                String val = props.getProperty("GEMINI_API_KEY");
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            } catch (IOException ignored) {}
        }

        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(envFile), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.startsWith("#") || !linea.contains("=")) continue;
                    String[] partes = linea.split("=", 2);
                    if ("GEMINI_API_KEY".equals(partes[0].trim())) {
                        String val = partes[1].trim().replaceAll("^[\"']|[\"']$", "");
                        if (!val.isEmpty()) return val;
                    }
                }
            } catch (IOException ignored) {}
        }

        return null;
    }

    private String construirCuerpoJSON(String instruccion, List<String[]> historial, String pregunta) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"system_instruction\":{\"parts\":[{\"text\":\"")
            .append(Json.escape(instruccion))
            .append("\"}]},");

        json.append("\"contents\":[");

        if (historial != null) {
            for (String[] turno : historial) {
                if (turno == null || turno.length < 2) continue;
                json.append("{\"role\":\"").append(Json.escape(turno[0])).append("\",");
                json.append("\"parts\":[{\"text\":\"").append(Json.escape(turno[1])).append("\"}]},");
            }
        }

        json.append("{\"role\":\"user\",\"parts\":[{\"text\":\"")
            .append(Json.escape(pregunta))
            .append("\"}]}");

        json.append("]}");
        return json.toString();
    }

    private String leerRespuesta(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            StringBuilder resultado = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                resultado.append(linea);
            }
            return resultado.toString();
        }
    }

    private String extraerTexto(String json) {
        try {
            Object raiz = Json.parse(json);
            Object texto = Json.get(raiz, "candidates", 0, "content", "parts", 0, "text");
            if (texto != null) {
                return String.valueOf(texto);
            }
        } catch (RuntimeException ignored) {}
        throw new RuntimeException("Gemini no devolvió una respuesta de texto válida.");
    }

    private String extraerMensajeError(String errorJson) {
        if (errorJson == null || errorJson.trim().isEmpty()) return "";
        try {
            Object raiz = Json.parse(errorJson);
            Object msg = Json.get(raiz, "error", "message");
            if (msg != null) return String.valueOf(msg);
        } catch (RuntimeException ignored) {}
        return errorJson;
    }

    public static void main(String[] args) {
        try {
            GeminiAPIClient cliente = new GeminiAPIClient();
            String respuesta = cliente.consultar(
                    "Eres el asistente de SuperCuricó. Responde muy breve.",
                    java.util.Collections.emptyList(),
                    "Hola, ¿estás conectado?"
            );
            System.out.println("Respuesta de Gemini: " + respuesta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
