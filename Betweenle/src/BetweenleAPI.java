import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.text.Normalizer;

public class BetweenleAPI {
    private HashMap<String, String> diccionario;
    private List<String> palabrasFiltradas;
    private HashSet<Character> letrasUsadas;
    private String palabraSecreta;

    private String palabraTop;
    private String palabraBottom;
    private int intentosRestantes;

    public BetweenleAPI() {
        this.diccionario = new HashMap<>();
        this.letrasUsadas = new HashSet<>();
    }

    // limpia acentos y poder comparar el abecedario matemáticamente
    private String quitarAcentos(String texto) {
        if (texto == null) return null;
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    // carga el diccionario con formato "palabra; longitud"
    public void cargarDiccionario(String ruta) throws IOException {
        File archivo = new File(ruta);
        Scanner lector = new Scanner(archivo);

        while (lector.hasNextLine()) {
            String linea = lector.nextLine().trim();

            if (!linea.isEmpty()) {
                // Separamos la línea usando el punto y coma como delimitador
                String[] partes = linea.split(";");

                // Verificamos que la línea tenga al menos la palabra y la longitud
                if (partes.length >= 2) {
                    String palabra = partes[0].trim().toLowerCase();
                    String longitud = partes[1].trim();

                    // HashMap: La clave es la palabra, el valor es su longitud
                    diccionario.put(palabra, longitud);
                } else if (partes.length == 1) {
                    // Por si alguna línea viene rota y solo trae la palabra
                    String palabra = partes[0].trim().toLowerCase();
                    diccionario.put(palabra, "longitud no disponible.");
                }
            }
        }
        lector.close();
    }

    // Te permite consultar la longitud de cualquier palabra
    public String obtenerLongitud(String palabra) {
        return diccionario.getOrDefault(palabra.toLowerCase(), "Longitud no encontrada.");
    }

    public void iniciarJuego(int longitud, int intentos) {
        // Filtramos por longitud y ordenamos ignorando los acentos
        palabrasFiltradas = diccionario.keySet().stream()
                .filter(p -> p.length() == longitud)
                .sorted((p1, p2) -> quitarAcentos(p1).compareTo(quitarAcentos(p2)))
                .collect(Collectors.toList());

        if (palabrasFiltradas.isEmpty()) throw new RuntimeException("No hay palabras de esa longitud.");

        this.palabraSecreta = palabrasFiltradas.get(new Random().nextInt(palabrasFiltradas.size()));
        this.intentosRestantes = intentos;
        this.letrasUsadas.clear();

        // Los límites arrancan vacíos
        this.palabraTop = null;
        this.palabraBottom = null;
    }

    public String procesarIntento(String intento) {
        intento = intento.toLowerCase();

        // Validación de longitud
        if (intento.length() != palabraSecreta.length()) {
            return "ERROR_LONGITUD";
        }

        String intentoNorm = quitarAcentos(intento);
        String topNorm = quitarAcentos(palabraTop);
        String bottomNorm = quitarAcentos(palabraBottom);
        String secretaNorm = quitarAcentos(palabraSecreta);

        if (topNorm != null && intentoNorm.compareTo(topNorm) <= 0) return "FUERA_DE_RANGO";
        if (bottomNorm != null && intentoNorm.compareTo(bottomNorm) >= 0) return "FUERA_DE_RANGO";

        for (char c : intento.toCharArray()) letrasUsadas.add(c);
        intentosRestantes--;

        if (intento.equals(palabraSecreta)) return "GANASTE";

        if (intentoNorm.compareTo(secretaNorm) < 0) {
            palabraTop = intento;
            return "La palabra secreta está DESPUÉS (Se mueve el límite superior)";
        } else {
            palabraBottom = intento;
            return "La palabra secreta está ANTES (Se mueve el límite inferior)";
        }
    }

    public double getPorcentajeTop() {
        if (palabraTop == null) return -1;
        int total = palabrasFiltradas.size();
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxTop = palabrasFiltradas.indexOf(palabraTop);
        return ((double) (idxSecreta - idxTop) / total) * 100;
    }

    public double getPorcentajeBottom() {
        if (palabraBottom == null) return -1;
        int total = palabrasFiltradas.size();
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxBottom = palabrasFiltradas.indexOf(palabraBottom);
        return ((double) (idxBottom - idxSecreta) / total) * 100;
    }

    // nos arroja el primer caracter de la palabra secreta

    public String obtenerPistaComienzo() {
        return "La palabra empieza con: " + palabraSecreta.charAt(0);
    }

    //conseguimos que recorra la palabra un porciento

    public String darPistaTop1Porciento() {
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxActual = (palabraTop == null) ? 0 : palabrasFiltradas.indexOf(palabraTop);
        int unoPorciento = Math.max(1, palabrasFiltradas.size() / 100);

        // Validar si la distancia actual es menor o igual al 1%
        if ((idxSecreta - idxActual) <= unoPorciento) {
            return "La distancia al límite superior ya es del 1% o menor. Estás demasiado cerca.";
        }

        int nuevoIdx = idxActual + unoPorciento;
        palabraTop = palabrasFiltradas.get(nuevoIdx);
        return "El [Top Limit] se ha recorrido un 1% alfabéticamente a: " + palabraTop.toUpperCase();
    }

    public String darPistaBottom1Porciento() {
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxActual = (palabraBottom == null) ? palabrasFiltradas.size() - 1 : palabrasFiltradas.indexOf(palabraBottom);
        int unoPorciento = Math.max(1, palabrasFiltradas.size() / 100);

        // Validar si la distancia actual es menor o igual al 1%
        if ((idxActual - idxSecreta) <= unoPorciento) {
            return "La distancia al límite inferior ya es del 1% o menor. Estás demasiado cerca.";
        }

        int nuevoIdx = idxActual - unoPorciento;
        palabraBottom = palabrasFiltradas.get(nuevoIdx);
        return "El [Bottom Limit] se ha recorrido un 1% alfabéticamente a: " + palabraBottom.toUpperCase();
    }

    public boolean esValida(String p) {
        return diccionario.containsKey(p.toLowerCase()); }
    public void agregarPalabra(String p, String l) {
        diccionario.put(p.toLowerCase(), l); }
    public int getIntentos() {
        return intentosRestantes; }
    public HashSet<Character> getLetrasUsadas() {
        return letrasUsadas; }
    public String getSecreta() {
        return palabraSecreta; }
    public String getPalabraTop() {
        return palabraTop; }
    public String getPalabraBottom() {
        return palabraBottom; }
}
