import java.util.*;

public class BetweenleConsole {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BetweenleAPI juego = new BetweenleAPI();

        try {
            System.out.println("--- BIENVENIDO A BETWEENLE ---");

            // Menú de selección de idioma
            int idioma = 0;
            while (idioma != 1 && idioma != 2) {
                System.out.println("Selecciona el idioma:");
                System.out.println("1) Español");
                System.out.println("2) English");

                if (sc.hasNextInt()) {
                    idioma = sc.nextInt();
                    if (idioma != 1 && idioma != 2) {
                        System.out.println("Opción no válida.\n");
                    }
                } else {
                    System.out.println("Por favor, introduce un número válido.\n");
                    sc.next();
                }
            }
            // Limpiar el buffer después de leer el número
            sc.nextLine();

            // Asignar la ruta dependiendo de la elección ya sea espanol o ingles
            String rutaDiccionario = (idioma == 1) ? "palabrasSpanol.txt" : "palabrasIngles.txt";

            juego.cargarDiccionario(rutaDiccionario);

            if (idioma == 1) {
                System.out.println("¡Diccionario cargado automáticamente!\n");
            }

            System.out.println("--- JUEGO BETWEENLE ---");

            int len = 0;
            while (len < 5 || len > 7) {
                System.out.print("Dificultad (Elige 5, 6 o 7 letras): ");
                if (sc.hasNextInt()) {
                    len = sc.nextInt();
                    if (len < 5 || len > 7) System.out.println("Opción no válida. Inténtalo de nuevo.");
                } else {
                    System.out.println("Por favor, introduce un número válido.");
                    sc.next();
                }
            }

            System.out.print("Oportunidades (10, 12, 14): ");
            int tries = sc.nextInt();
            sc.nextLine();

            juego.iniciarJuego(len, tries);

            while (juego.getIntentos() > 0) {
                String txtTop = (juego.getPalabraTop() == null) ? "----" : juego.getPalabraTop().toUpperCase();
                String pctTop = (juego.getPorcentajeTop() == -1) ? "?" : String.format("%.2f%%", juego.getPorcentajeTop());

                String txtBottom = (juego.getPalabraBottom() == null) ? "----" : juego.getPalabraBottom().toUpperCase();
                String pctBottom = (juego.getPorcentajeBottom() == -1) ? "?" : String.format("%.2f%%", juego.getPorcentajeBottom());

                System.out.println("\n=============================================");
                System.out.printf(" [Top Limit]    →   %s (Distancia: %s)\n", txtTop, pctTop);
                System.out.println("                   [ Palabra Secreta Oculta ]");
                System.out.printf(" [Bottom Limit] →   %s (Distancia: %s)\n", txtBottom, pctBottom);
                System.out.println("=============================================");

                System.out.println("Intentos restantes: " + juego.getIntentos());
                System.out.println("Letras usadas: " + juego.getLetrasUsadas());
                System.out.print("Tu palabra (o escribe 'pista'): ");
                String input = sc.nextLine().toLowerCase();

                if (input.equals("pista") || input.equals("hint")) {
                    System.out.println("\n--- MENÚ DE PISTAS ---");
                    System.out.println("a) Recorrer un 1% la palabra de arriba (Top Limit)");
                    System.out.println("b) Recorrer un 1% la palabra de abajo (Bottom Limit)");
                    System.out.println("c) Letra con que empieza la palabra");
                    System.out.print("Elige una opción (a/b/c): ");

                    String opcionPista = sc.nextLine().toLowerCase();

                    switch (opcionPista) {
                        case "a":
                            System.out.println("-> " + juego.darPistaTop1Porciento());
                            break;
                        case "b":
                            System.out.println("-> " + juego.darPistaBottom1Porciento());
                            break;
                        case "c":
                            System.out.println("-> " + juego.obtenerPistaComienzo());
                            break;
                        default:
                            System.out.println("-> Opción no válida. Menú de pistas cancelado.");
                            break;
                    }
                    continue;
                }

                if (!juego.esValida(input)) {
                    System.out.print("No existe en el diccionario. ¿Es real? (s/n): ");
                    if (sc.nextLine().equalsIgnoreCase("s")) {
                        System.out.print("Longitud: ");
                        juego.agregarPalabra(input, sc.nextLine());
                        System.out.println("¡Palabra guardada! Inténtalo de nuevo.");
                    }
                    continue;
                }

                String resultado = juego.procesarIntento(input);

                if (resultado.equals("FUERA_DE_RANGO")) {
                    System.out.println("Tu palabra debe estar alfabéticamente ENTRE los límites actuales.");
                } else if (resultado.equals("ERROR_LONGITUD")) {
                    System.out.println("Tu palabra debe tener exactamente " + len + " letras.");
                } else if (resultado.equals("GANASTE")) {
                    System.out.println("\n¡¡FELICIDADES!! Adivinaste la palabra exacta.");
                    break;
                } else {
                    System.out.println("-> " + resultado);
                }
            }

            if (juego.getIntentos() == 0) {
                System.out.println("\nTe quedaste sin intentos. La palabra era: " + juego.getSecreta().toUpperCase());
            }

            System.out.print("\nLetras intentadas en total: ");
            juego.getLetrasUsadas().forEach(letra -> System.out.print("[" + letra + "] "));

            System.out.println("\n\n--- Verificación del Iterator ---");
            Iterator<Character> it = juego.getLetrasUsadas().iterator();
            if (it.hasNext()) System.out.println("Primera letra registrada en el Set: " + it.next());

        } catch (Exception e) {
            System.out.println("Error del sistema al cargar el diccionario: " + e.getMessage());
            System.out.println("Asegúrate de que los archivos 'palabrasSpanol.txt' y 'palabrasIngles.txt' existan en la ruta correcta.");
        }
    }
}