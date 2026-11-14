package red;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private final String s;
    private int i = 0;

    private Json(String s) { this.s = s; }

    public static Object parse(String texto) {
        if (texto == null) throw new IllegalArgumentException("JSON nulo");
        Json p = new Json(texto);
        p.saltarEspacios();
        Object valor = p.leerValor();
        p.saltarEspacios();
        if (p.i != p.s.length()) throw p.error("Texto sobrante despues del JSON");
        return valor;
    }

    public static String escape(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder(texto.length() + 8);
        for (int k = 0; k < texto.length(); k++) {
            char c = texto.charAt(k);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static Object get(Object raiz, Object... ruta) {
        Object actual = raiz;
        for (Object paso : ruta) {
            if (actual == null) return null;
            if (paso instanceof String && actual instanceof Map) {
                actual = ((Map<String, Object>) actual).get(paso);
            } else if (paso instanceof Integer && actual instanceof List) {
                List<Object> l = (List<Object>) actual;
                int idx = (Integer) paso;
                actual = (idx >= 0 && idx < l.size()) ? l.get(idx) : null;
            } else {
                return null;
            }
        }
        return actual;
    }

    private Object leerValor() {
        if (i >= s.length()) throw error("JSON incompleto");
        char c = s.charAt(i);
        switch (c) {
            case '{': return leerObjeto();
            case '[': return leerArreglo();
            case '"': return leerCadena();
            case 't': esperar("true");  return Boolean.TRUE;
            case 'f': esperar("false"); return Boolean.FALSE;
            case 'n': esperar("null");  return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) return leerNumero();
                throw error("Caracter inesperado '" + c + "'");
        }
    }

    private Map<String, Object> leerObjeto() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        i++; 
        saltarEspacios();
        if (peek() == '}') { i++; return mapa; }
        while (true) {
            saltarEspacios();
            if (peek() != '"') throw error("Se esperaba el nombre de un campo");
            String clave = leerCadena();
            saltarEspacios();
            if (peek() != ':') throw error("Se esperaba ':'");
            i++;
            saltarEspacios();
            mapa.put(clave, leerValor());
            saltarEspacios();
            char c = peek();
            i++;
            if (c == '}') return mapa;
            if (c != ',') throw error("Se esperaba ',' o '}'");
        }
    }

    private List<Object> leerArreglo() {
        List<Object> lista = new ArrayList<>();
        i++; 
        saltarEspacios();
        if (peek() == ']') { i++; return lista; }
        while (true) {
            saltarEspacios();
            lista.add(leerValor());
            saltarEspacios();
            char c = peek();
            i++;
            if (c == ']') return lista;
            if (c != ',') throw error("Se esperaba ',' o ']'");
        }
    }

    private String leerCadena() {
        i++; 
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (i >= s.length()) throw error("Cadena sin cerrar");
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            if (i >= s.length()) throw error("Escape incompleto");
            char e = s.charAt(i++);
            switch (e) {
                case '"':  sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/':  sb.append('/'); break;
                case 'b':  sb.append('\b'); break;
                case 'f':  sb.append('\f'); break;
                case 'n':  sb.append('\n'); break;
                case 'r':  sb.append('\r'); break;
                case 't':  sb.append('\t'); break;
                case 'u':
                    if (i + 4 > s.length()) throw error("Escape \\u incompleto");
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("Escape \\u invalido");
                    }
                    i += 4;
                    break;
                default: throw error("Escape invalido \\" + e);
            }
        }
    }

    private Object leerNumero() {
        int ini = i;
        if (peek() == '-') i++;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || ".eE+-".indexOf(s.charAt(i)) >= 0)) i++;
        String num = s.substring(ini, i);
        try {
            if (num.contains(".") || num.contains("e") || num.contains("E")) return Double.valueOf(num);
            return Long.valueOf(num);
        } catch (NumberFormatException ex) {
            throw error("Numero invalido '" + num + "'");
        }
    }

    private void esperar(String palabra) {
        if (!s.startsWith(palabra, i)) throw error("Se esperaba '" + palabra + "'");
        i += palabra.length();
    }

    private char peek() {
        if (i >= s.length()) throw error("JSON incompleto");
        return s.charAt(i);
    }

    private void saltarEspacios() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException(msg + " (posicion " + i + ")");
    }
}