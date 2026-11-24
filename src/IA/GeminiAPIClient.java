package IA;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class GeminiAPIClient {
    private static final String MODELO = "gemini-2.5-flash"; 
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/"
        + "v1beta/models/"
        + MODELO
        + ":generateContent?key=";

    /**
     *
     * @param instruccionSistema 
     * @param historial          
     *                      
     * @param pregunta           
     * @return 
     */

    public String consultar(String instruccionSistema, List<String[]> historial, String pregunta) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "No se configuró la variable de entorno GEMINI_API_KEY en el servidor."
            );
        }

        String cuerpo = construirCuerpoJSON(instruccionSistema, historial, pregunta);

        URL url = new URL(API_URL + apiKey);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conexion.setConnectTimeout(15_000);
        conexion.setReadTimeout(60_000);
        conexion.setDoOutput(true);

        try (OutputStream salida = conexion.getOutputStream()) {
            salida.write(cuerpo.getBytes(StandardCharsets.UTF_8));
        }

        int codigo = conexion.getResponseCode();
        if (codigo != HttpURLConnection.HTTP_OK) {
            String error = leerRespuesta(conexion.getErrorStream());
            if (codigo == 429) {
                throw new RuntimeException("Límite de solicitudes excedido. Intenta en unos segundos.");
            } else if (codigo == 401 || codigo == 403) {
                throw new RuntimeException("API Key inválida o sin permisos.");
            }
            throw new RuntimeException("Gemini respondió HTTP " + codigo + ": " + error);
        }

        String respuesta = leerRespuesta(conexion.getInputStream());
        return extraerTexto(respuesta);
    }

    private String construirCuerpoJSON(String instruccion, List<String[]> historial, String pregunta) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"system_instruction\":{\"parts\":[{\"text\":\"")
            .append(escaparJson(instruccion))
            .append("\"}]},");

        json.append("\"contents\":[");

        for (int i = 0; i < historial.size(); i++) {
            String rol = historial.get(i)[0];
            String texto = historial.get(i)[1];
            json.append("{\"role\":\"").append(rol).append("\",");
            json.append("\"parts\":[{\"text\":\"").append(escaparJson(texto)).append("\"}]}");
            json.append(",");
        }

        json.append("{\"role\":\"user\",\"parts\":[{\"text\":\"")
            .append(escaparJson(pregunta))
            .append("\"}]}");

        json.append("]}");

        return json.toString();
    }

    private String leerRespuesta(java.io.InputStream entrada) throws Exception {
        if (entrada == null) {
            return "";
        }
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            StringBuilder resultado = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                resultado.append(linea);
            }
            return resultado.toString();
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extraerTexto(String json) {
        String marca = "\"text\":";
        int posicion = json.indexOf(marca);
        if (posicion == -1) {
            throw new RuntimeException("Gemini no devolvió texto.");
        }
        int inicio = json.indexOf("\"", posicion + marca.length());
        if (inicio == -1) {
            throw new RuntimeException("No se pudo interpretar la respuesta.");
        }

        StringBuilder texto = new StringBuilder();
        boolean escapado = false;

        for (int i = inicio + 1; i < json.length(); i++) {
            char caracter = json.charAt(i);
            if (escapado) {
                switch (caracter) {
                    case 'n':  texto.append('\n'); break;
                    case 'r':  texto.append('\r'); break;
                    case 't':  texto.append('\t'); break;
                    case '"':  texto.append('"');  break;
                    case '\\': texto.append('\\'); break;
                    default:   texto.append(caracter);
                }
                escapado = false;
            } else if (caracter == '\\') {
                escapado = true;
            } else if (caracter == '"') {
                return texto.toString();
            } else {
                texto.append(caracter);
            }
        }
        throw new RuntimeException("Respuesta JSON incompleta.");
    }
}
