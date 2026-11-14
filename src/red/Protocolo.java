package red;

import java.util.Map;

public class Protocolo {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_DATA = "data_exchange";

    public static final String STOCK = "STOCK:";                 // servidor -> cliente: stock absoluto
    public static final String PRECIO = "PRECIO:";               // servidor -> cliente: precio absoluto
    public static final String RECHAZADO = "RECHAZADO:";         // servidor -> cliente: "<cantidad>:<motivo>"
    public static final String AGREGAR = "AGREGAR:";             // cliente -> servidor: +n reserva, -n devuelve
    public static final String COMPRAR = "COMPRAR";              // cliente -> servidor: pago confirmado
    public static final String AGREGAR_DESEO = "AGREGAR_DESEO:"; // cliente -> servidor: aviso de reposicion
    public static String createJsonMessage(String content) {
        return "{\"type\": \"message\", \"content\": \"" + escapeJson(content) + "\"}";
    }

    public static String createJsonData(int numero, String texto) {
        return "{\"type\": \"data_exchange\", \"numero\": " + numero + ", \"texto\": \"" + escapeJson(texto) + "\"}";
    }

    public static String escapeJson(String text) {
        return Json.escape(text);
    }

    public static String extractJsonField(String json, String fieldName) {
        try {
            Object raiz = Json.parse(json);
            if (!(raiz instanceof Map)) return null;
            Object v = ((Map<?, ?>) raiz).get(fieldName);
            if (v == null) return null;
            if (v instanceof Double) {
                double d = (Double) v;
                return d == Math.rint(d) && !Double.isInfinite(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            return String.valueOf(v);
        } catch (RuntimeException e) {
            return null;
        }
    }
}