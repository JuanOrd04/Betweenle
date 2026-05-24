import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.text.Normalizer;

public class BetweenleAPI {
    private HashMap<String, String> diccionario;
    private List<String> palabrasFiltradas;
    private HashSet<Character> letrasUsadas;
    private List<String> historialPalabras;
    private String palabraSecreta;
    private String rutaActual;
    private String palabraTop;
    private String palabraBottom;
    private int intentosRestantes;

    public BetweenleAPI() {
        this.diccionario = new HashMap<>();
        this.letrasUsadas = new HashSet<>();
        this.historialPalabras = new ArrayList<>();
    }

    private String quitarAcentos(String texto) {
        if (texto == null) return null;
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public void cargarDiccionario(String ruta) throws IOException {
        this.rutaActual = ruta;
        File archivo = new File(ruta);
        Scanner lector = new Scanner(archivo);

        while (lector.hasNextLine()) {
            String linea = lector.nextLine().trim();
            if (!linea.isEmpty()) {
                String[] partes = linea.split(";");
                if (partes.length >= 2) {
                    diccionario.put(partes[0].trim().toLowerCase(), partes[1].trim());
                } else if (partes.length == 1) {
                    diccionario.put(partes[0].trim().toLowerCase(), "longitud no disponible.");
                }
            }
        }
        lector.close();
    }

    public void iniciarJuego(int longitud, int intentos) {
        palabrasFiltradas = diccionario.keySet().stream()
                .filter(p -> p.length() == longitud)
                .sorted((p1, p2) -> quitarAcentos(p1).compareTo(quitarAcentos(p2)))
                .collect(Collectors.toList());

        if (palabrasFiltradas.isEmpty()) throw new RuntimeException("No hay palabras de esa longitud.");

        this.palabraSecreta = palabrasFiltradas.get(new Random().nextInt(palabrasFiltradas.size()));
        this.intentosRestantes = intentos;
        this.letrasUsadas.clear();
        this.historialPalabras.clear();

        String extremoA = "";
        String extremoZ = "";
        for(int i = 0; i < longitud; i++) {
            extremoA += "a";
            extremoZ += "z";
        }

        this.palabraTop = extremoA;
        this.palabraBottom = extremoZ;
    }

    public String procesarIntento(String intento) {
        intento = intento.toLowerCase();

        if (intento.length() != palabraSecreta.length()) {
            return "ERROR_LONGITUD";
        }

        String intentoNorm = quitarAcentos(intento);
        String topNorm = quitarAcentos(palabraTop);
        String bottomNorm = quitarAcentos(palabraBottom);
        String secretaNorm = quitarAcentos(palabraSecreta);

        if (topNorm != null && intentoNorm.compareTo(topNorm) <= 0) return "FUERA_DE_RANGO_TOP";
        if (bottomNorm != null && intentoNorm.compareTo(bottomNorm) >= 0) return "FUERA_DE_RANGO_BOTTOM";

        for (char c : intento.toCharArray()) letrasUsadas.add(c);
        intentosRestantes--;

        if (intento.equals(palabraSecreta)) {
            historialPalabras.add(intento);
            return "GANASTE";
        }

        if (intentoNorm.compareTo(secretaNorm) < 0) {
            palabraTop = intento;
            historialPalabras.add(intento);
            return "La palabra secreta está DESPUÉS (Se mueve el límite superior)";
        } else {
            palabraBottom = intento;
            historialPalabras.add(intento);
            return "La palabra secreta está ANTES (Se mueve el límite inferior)";
        }
    }
    public double getPorcentajeTop() {
        if (!esValida(palabraTop)) return -1;

        int total = palabrasFiltradas.size();
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxTop = palabrasFiltradas.indexOf(palabraTop);
        return ((double) (idxSecreta - idxTop) / total) * 100;
    }

    public double getPorcentajeBottom() {
        if (!esValida(palabraBottom)) return -1;

        int total = palabrasFiltradas.size();
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxBottom = palabrasFiltradas.indexOf(palabraBottom);
        return ((double) (idxBottom - idxSecreta) / total) * 100;
    }

    public String obtenerPistaComienzo() {
        return "La palabra empieza con: " + palabraSecreta.charAt(0);
    }

    public String darPistaTop1Porciento() {
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxActual = palabrasFiltradas.indexOf(palabraTop);
        if (idxActual == -1) idxActual = 0;

        int unoPorciento = Math.max(1, palabrasFiltradas.size() / 100);

        if ((idxSecreta - idxActual) <= unoPorciento) {
            return "La distancia al límite superior ya es del 1% o menor. Estás demasiado cerca.";
        }

        int nuevoIdx = idxActual + unoPorciento;
        palabraTop = palabrasFiltradas.get(nuevoIdx);
        return "El [Top Limit] se ha recorrido un 1% alfabéticamente a: " + palabraTop.toUpperCase();
    }

    public String darPistaBottom1Porciento() {
        int idxSecreta = palabrasFiltradas.indexOf(palabraSecreta);
        int idxActual = palabrasFiltradas.indexOf(palabraBottom);
        if (idxActual == -1) idxActual = palabrasFiltradas.size() - 1;

        int unoPorciento = Math.max(1, palabrasFiltradas.size() / 100);

        if ((idxActual - idxSecreta) <= unoPorciento) {
            return "La distancia al límite inferior ya es del 1% o menor. Estás demasiado cerca.";
        }

        int nuevoIdx = idxActual - unoPorciento;
        palabraBottom = palabrasFiltradas.get(nuevoIdx);
        return "El [Bottom Limit] se ha recorrido un 1% alfabéticamente a: " + palabraBottom.toUpperCase();
    }

    public void agregarPalabra(String p, String l) {
        String palabraLimpia = p.toLowerCase();
        diccionario.put(palabraLimpia, l);

        if (palabrasFiltradas != null && palabraSecreta != null && palabraLimpia.length() == palabraSecreta.length()) {
            if (!palabrasFiltradas.contains(palabraLimpia)) {
                palabrasFiltradas.add(palabraLimpia);
                palabrasFiltradas.sort((p1, p2) -> quitarAcentos(p1).compareTo(quitarAcentos(p2)));
            }
        }

        if (rutaActual != null) {
            try {
                List<String> todasLasPalabras = new ArrayList<>(diccionario.keySet());
                todasLasPalabras.sort((p1, p2) -> quitarAcentos(p1).compareTo(quitarAcentos(p2)));

                try (FileWriter fw = new FileWriter(rutaActual, false);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter out = new PrintWriter(bw)) {

                    for (String palabra : todasLasPalabras) {
                        out.println(palabra + ";" + diccionario.get(palabra));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al guardar en el archivo: " + e.getMessage());
            }
        }
    }

    public boolean esValida(String p) {
        return diccionario.containsKey(p.toLowerCase()); }
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
    public List<String> getHistorialPalabras() {
        return historialPalabras; }
}
