import java.util.Scanner;
import java.util.ArrayList;

public class pe7 {

    static char[][] tauler = {
            { 'T', 'C', 'A', 'Q', 'K', 'A', 'C', 'T' },
            { 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' },
            { 't', 'c', 'a', 'q', 'k', 'a', 'c', 't' }
    };
    static boolean gameover = false;
    static boolean reiBlancMoviment = false, reiNegreMoviment = false;
    static boolean torreBlancaAMoviment = false, torreBlancaHMoviment = false;
    static boolean torreNegreAMoviment = false, torreNegreHMoviment = false;
    static ArrayList<String> historialMoviments = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Benvingut al joc d'escacs!");
        scanner.nextLine();
        System.out.println("Format de moviment: e2 e4 (lletra columna + número fila)");
        scanner.nextLine();
        System.out.println("Les peces blanques es representen amb majúscules i les negres amb minúscules.");
        scanner.nextLine();
        System.out.println("BLAQUES comencen primer.\n");
        int torn = 0;
        do {
            imprimirTauler();
            try {
                if (estaEnEscac(torn)) {
                    System.out.println("ESCAC al rei de les " + (torn == 0 ? "blanques" : "negres"));
                    if (esEscacIMat(torn)) {
                        System.out.println("JAQUE MATE!\n Guanya " + (torn == 0 ? "negres" : "blanques"));
                        gameover = true;
                        System.out.println("Historial de moviments:\n " + historialMoviments);
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al comprovar escac: " + e.getMessage());
            }

            if (!gameover) {
                System.out.println("Torn de les peces " + (torn == 0 ? "blanques" : "negres"));
                System.out.print("Moviment: ");
                String moviment = scanner.nextLine();
                if (moviment.length() != 5 || moviment.charAt(2) != ' ')
                    continue;

                try {
                    int filaOrigen = Character.getNumericValue(moviment.charAt(1)) - 1;
                    int columnaOrigen = moviment.charAt(0) - 'a';
                    int filaDestinacio = Character.getNumericValue(moviment.charAt(4)) - 1;
                    int columnaDestinacio = moviment.charAt(3) - 'a';

                    char origen = tauler[filaOrigen][columnaOrigen];

                    if ((torn == 0 && !esBlanca(origen)) || (torn == 1 && esBlanca(origen))) {
                        System.out.println("No es el torn de aquestes peces");
                        continue;
                    }

                    if (Character.toLowerCase(origen) == 'k' && Math.abs(columnaDestinacio - columnaOrigen) == 2) {
                        if (potEnrocar(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen)) {
                            realitzarEnroque(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen);
                            torn = 1 - torn;
                            continue;
                        } else {
                            System.out.println("Moviment invàlid (enroc)");
                            continue;
                        }
                    }

                    if (!potMoure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen) &&
                            !potCapturar(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen)) {
                        System.out.println("Moviment invàlid");
                        continue;
                    }

                    actualitzarFlags(origen, filaOrigen, columnaOrigen);

                    tauler[filaDestinacio][columnaDestinacio] = origen;
                    tauler[filaOrigen][columnaOrigen] = ' ';
                    torn = 1 - torn;
                    historialMoviments.add(moviment);

                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Moviment fora del tauler, prova de nou");
                } catch (NumberFormatException e) {
                    System.out.println("Format de moviment invàlid, prova de nou");
                } catch (NullPointerException e) {
                    System.out.println("Error intern amb la peça seleccionada");
                } catch (Exception e) {
                    System.out.println("Entrada invàlida, prova de nou: " + e.getMessage());
                }
            }
        } while (!gameover);
    }

    static void imprimirTauler() {
        try {
            for (int fila = 0; fila < 8; fila++) {
                System.out.print((fila + 1) + "  ");
                for (int columna = 0; columna < 8; columna++) {
                    String fons = ((fila + columna) % 2 == 0) ? "\u001B[47m" : "\u001B[40m";
                    char peça = tauler[fila][columna];
                    String colorPeça = (peça == ' ') ? "\u001B[30m"
                            : (Character.isUpperCase(peça) ? "\u001B[37m" : "\u001B[31m");
                    System.out.print(fons + colorPeça + " " + peça + " " + "\u001B[0m");
                }
                System.out.println();
            }
            System.out.println("   a  b  c  d  e  f  g  h\n");
        } catch (Exception e) {
            System.out.println("Error imprimint el tauler: " + e.getMessage());
        }
    }

    static boolean esBlanca(char peça) {
        try {
            return Character.isUpperCase(peça);
        } catch (Exception e) {
            System.out.println("Error comprovant color de peça");
            return false;
        }
    }

    static boolean potMoure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio, char peça) {
        try {
            if (peça == ' ')
                return false;
            boolean blanca = esBlanca(peça);
            char destinacio = tauler[filaDestinacio][columnaDestinacio];
            if (destinacio != ' ' && esBlanca(destinacio) == blanca)
                return false;

            switch (Character.toLowerCase(peça)) {
                case 'p':
                    int direccio = blanca ? 1 : -1;
                    if (columnaOrigen == columnaDestinacio && destinacio == ' '
                            && filaDestinacio == filaOrigen + direccio)
                        return true;
                    if (columnaOrigen == columnaDestinacio && destinacio == ' ' &&
                            ((blanca && filaOrigen == 1) || (!blanca && filaOrigen == 6)) &&
                            filaDestinacio == filaOrigen + 2 * direccio
                            && tauler[filaOrigen + direccio][columnaOrigen] == ' ')
                        return true;
                    if (Math.abs(columnaOrigen - columnaDestinacio) == 1 &&
                            filaDestinacio == filaOrigen + direccio &&
                            destinacio != ' ' && esBlanca(destinacio) != blanca)
                        return true;
                    return false;
                case 't':
                    return lineaLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
                case 'c':
                    return (Math.abs(filaOrigen - filaDestinacio) == 2
                            && Math.abs(columnaOrigen - columnaDestinacio) == 1) ||
                            (Math.abs(filaOrigen - filaDestinacio) == 1
                                    && Math.abs(columnaOrigen - columnaDestinacio) == 2);
                case 'a':
                    return diagonalLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
                case 'q':
                    return lineaLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio) ||
                            diagonalLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
                case 'k':
                    return Math.abs(filaOrigen - filaDestinacio) <= 1
                            && Math.abs(columnaOrigen - columnaDestinacio) <= 1;
            }
        } catch (Exception e) {
            System.out.println("Error comprovant moviment: " + e.getMessage());
        }
        return false;
    }

    static boolean potCapturar(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio,
            char peça) {
        try {
            char destinacio = tauler[filaDestinacio][columnaDestinacio];
            return destinacio != ' ' && esBlanca(destinacio) != esBlanca(peça) &&
                    potMoure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, peça);
        } catch (Exception e) {
            System.out.println("Error comprovant captura: " + e.getMessage());
            return false;
        }
    }

    static boolean lineaLliure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio) {
        try {
            if (filaOrigen != filaDestinacio && columnaOrigen != columnaDestinacio)
                return false;
            int incrementFila = Integer.compare(filaDestinacio, filaOrigen);
            int incrementColumna = Integer.compare(columnaDestinacio, columnaOrigen);
            int fila = filaOrigen + incrementFila;
            int columna = columnaOrigen + incrementColumna;
            while (fila != filaDestinacio || columna != columnaDestinacio) {
                if (tauler[fila][columna] != ' ')
                    return false;
                fila += incrementFila;
                columna += incrementColumna;
            }
        } catch (Exception e) {
            System.out.println("Error comprovant línia lliure: " + e.getMessage());
            return false;
        }
        return true;
    }

    static boolean diagonalLliure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio) {
        try {
            if (Math.abs(filaDestinacio - filaOrigen) != Math.abs(columnaDestinacio - columnaOrigen))
                return false;
            int incrementFila = (filaDestinacio - filaOrigen) / Math.abs(filaDestinacio - filaOrigen);
            int incrementColumna = (columnaDestinacio - columnaOrigen) / Math.abs(columnaDestinacio - columnaOrigen);
            int fila = filaOrigen + incrementFila;
            int columna = columnaOrigen + incrementColumna;
            while (fila != filaDestinacio && columna != columnaDestinacio) {
                if (tauler[fila][columna] != ' ')
                    return false;
                fila += incrementFila;
                columna += incrementColumna;
            }
        } catch (Exception e) {
            System.out.println("Error comprovant diagonal lliure: " + e.getMessage());
            return false;
        }
        return true;
    }

    static boolean estaEnEscac(int torn) {
        try {
            int reiFila = -1, reiColumna = -1;
            char reiChar = torn == 0 ? 'K' : 'k';
            for (int fila = 0; fila < 8; fila++)
                for (int columna = 0; columna < 8; columna++)
                    if (tauler[fila][columna] == reiChar) {
                        reiFila = fila;
                        reiColumna = columna;
                    }
            for (int fila = 0; fila < 8; fila++) {
                for (int columna = 0; columna < 8; columna++) {
                    char peça = tauler[fila][columna];
                    if (peça == ' ')
                        continue;
                    if (esBlanca(peça) == (torn == 0))
                        continue;
                    if (potCapturar(fila, columna, reiFila, reiColumna, peça))
                        return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error comprovant escac: " + e.getMessage());
        }
        return false;
    }

    static boolean esEscacIMat(int torn) {
        try {
            for (int fila = 0; fila < 8; fila++) {
                for (int columna = 0; columna < 8; columna++) {
                    char peça = tauler[fila][columna];
                    if (peça == ' ')
                        continue;
                    if (esBlanca(peça) != (torn == 0))
                        continue;
                    for (int filaDest = 0; filaDest < 8; filaDest++) {
                        for (int columnaDest = 0; columnaDest < 8; columnaDest++) {
                            if (!potMoure(fila, filaDest, columna, columnaDest, peça) &&
                                    !potCapturar(fila, filaDest, columna, columnaDest, peça))
                                continue;
                            char origen = tauler[fila][columna];
                            char destinacio = tauler[filaDest][columnaDest];
                            tauler[filaDest][columnaDest] = origen;
                            tauler[fila][columna] = ' ';
                            boolean escac = estaEnEscac(torn);
                            tauler[fila][columna] = origen;
                            tauler[filaDest][columnaDest] = destinacio;
                            if (!escac)
                                return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error comprovant escac i mat: " + e.getMessage());
        }
        return true;
    }

    static boolean potEnrocar(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio, char rei) {
        try {
            boolean blanca = esBlanca(rei);
            if (estaEnEscac(blanca ? 0 : 1))
                return false;

            if (columnaDestinacio - columnaOrigen == 2) {
                if (blanca && (reiBlancMoviment || torreBlancaHMoviment))
                    return false;
                if (!blanca && (reiNegreMoviment || torreNegreHMoviment))
                    return false;
                if (tauler[filaOrigen][columnaOrigen + 1] != ' ' || tauler[filaOrigen][columnaOrigen + 2] != ' ')
                    return false;
                if (estaEnEscacEnCasella(filaOrigen, columnaOrigen + 1, blanca) ||
                        estaEnEscacEnCasella(filaOrigen, columnaOrigen + 2, blanca))
                    return false;
                return true;
            }
            if (columnaDestinacio - columnaOrigen == -2) {
                if (blanca && (reiBlancMoviment || torreBlancaAMoviment))
                    return false;
                if (!blanca && (reiNegreMoviment || torreNegreAMoviment))
                    return false;
                if (tauler[filaOrigen][columnaOrigen - 1] != ' ' || tauler[filaOrigen][columnaOrigen - 2] != ' '
                        || tauler[filaOrigen][columnaOrigen - 3] != ' ')
                    return false;
                if (estaEnEscacEnCasella(filaOrigen, columnaOrigen - 1, blanca) ||
                        estaEnEscacEnCasella(filaOrigen, columnaOrigen - 2, blanca))
                    return false;
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error comprovant enroc: " + e.getMessage());
        }
        return false;
    }

    static void realitzarEnroque(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio,
            char rei) {
        try {
            boolean blanca = esBlanca(rei);
            tauler[filaDestinacio][columnaDestinacio] = rei;
            tauler[filaOrigen][columnaOrigen] = ' ';
            if (columnaDestinacio - columnaOrigen == 2) {
                char torre = tauler[filaOrigen][7];
                tauler[filaOrigen][columnaOrigen + 1] = torre;
                tauler[filaOrigen][7] = ' ';
            } else if (columnaDestinacio - columnaOrigen == -2) {
                char torre = tauler[filaOrigen][0];
                tauler[filaOrigen][columnaOrigen - 1] = torre;
                tauler[filaOrigen][0] = ' ';
            }
            if (blanca) {
                reiBlancMoviment = true;
                if (columnaDestinacio - columnaOrigen == 2)
                    torreBlancaHMoviment = true;
                else
                    torreBlancaAMoviment = true;
            } else {
                reiNegreMoviment = true;
                if (columnaDestinacio - columnaOrigen == 2)
                    torreNegreHMoviment = true;
                else
                    torreNegreAMoviment = true;
            }
        } catch (Exception e) {
            System.out.println("Error realitzant enroc: " + e.getMessage());
        }
    }

    static boolean estaEnEscacEnCasella(int fila, int columna, boolean blanca) {
        try {
            for (int filaP = 0; filaP < 8; filaP++) {
                for (int columnaP = 0; columnaP < 8; columnaP++) {
                    char peça = tauler[filaP][columnaP];
                    if (peça == ' ')
                        continue;
                    if (esBlanca(peça) == blanca)
                        continue;
                    if (potCapturar(filaP, columnaP, fila, columna, peça))
                        return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error comprovant escac en casella: " + e.getMessage());
        }
        return false;
    }

    static void actualitzarFlags(char peça, int filaOrigen, int columnaOrigen) {
        try {
            if (peça == 'K')
                reiBlancMoviment = true;
            else if (peça == 'k')
                reiNegreMoviment = true;
            else if (peça == 'T' && filaOrigen == 0 && columnaOrigen == 0)
                torreBlancaAMoviment = true;
            else if (peça == 'T' && filaOrigen == 0 && columnaOrigen == 7)
                torreBlancaHMoviment = true;
            else if (peça == 't' && filaOrigen == 7 && columnaOrigen == 0)
                torreNegreAMoviment = true;
            else if (peça == 't' && filaOrigen == 7 && columnaOrigen == 7)
                torreNegreHMoviment = true;
        } catch (Exception e) {
            System.out.println("Error actualitzant flags: " + e.getMessage());
        }
    }
}
