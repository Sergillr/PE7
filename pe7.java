import java.util.Scanner;
import java.util.ArrayList;

public class pe7 {

    // Tauler inicial (No utilitzo for per el debugging més fàcil XD)
    static char[][] taulerInicial = {
            { 'T', ' ', ' ', ' ', 'K', ' ', ' ', 'T' },
            { 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' },
            { 't', 'c', 'a', 'q', 'k', 'a', 'c', 't' }
    };

    static char[][] tauler;

    // Inici de variables de control de joc i historial
    static boolean gameover;
    static boolean reiBlancMoviment, reiNegreMoviment;
    static boolean torreBlancaAMoviment, torreBlancaHMoviment;
    static boolean torreNegreAMoviment, torreNegreHMoviment;
    static ArrayList<Character> pecesCapturades;
    static ArrayList<String> historialMoviments;
    static Scanner scanner = new Scanner(System.in);
    static String jugadorBlanc;
    static String jugadorNegre;

    // Main
    public static void main(String[] args) {
        System.out.println("Benvingut al joc d'escacs!");
        scanner.nextLine();

        boolean seguirJugant = true;
        boolean mateixosJugadors = false;

        while (seguirJugant) {

            if (!mateixosJugadors) {
                jugadorBlanc = demanarNomJugador("Nom del jugador blanc: ");
                jugadorNegre = demanarNomJugador("Nom del jugador negre: ");
            }

            inicialitzarPartida();

            System.out.println("En cas de voler abandonar el joc, escriu 'Abandonar'");
            scanner.nextLine();

            System.out.println("Format de moviment: AX BY (lletra columna + número fila)");
            System.out.println("BLAQUES comencen primer.\n");

            int torn = 0;

            // Bucle principal del joc
            do {
                imprimirTauler();

                if (!gameover) {
                    System.out.println("Torn de " + (torn == 0 ? jugadorBlanc : jugadorNegre));
                    System.out.print("Moviment: ");
                    String moviment = scanner.nextLine().toLowerCase();

                    if (moviment.equalsIgnoreCase("Abandonar")) {
                        gameover = true;
                        break;
                    }

                    if (moviment.length() != 5 || moviment.charAt(2) != ' ')
                        continue;

                    try {
                        int filaOrigen = Character.getNumericValue(moviment.charAt(1)) - 1;
                        int columnaOrigen = moviment.charAt(0) - 'a';
                        int filaDestinacio = Character.getNumericValue(moviment.charAt(4)) - 1;
                        int columnaDestinacio = moviment.charAt(3) - 'a';

                        char origen = tauler[filaOrigen][columnaOrigen];
                        char destinacio = tauler[filaDestinacio][columnaDestinacio];

                        if ((torn == 0 && !esBlanca(origen)) || (torn == 1 && esBlanca(origen))) {
                            System.out.println("No es el torn de aquestes peces");
                            continue;
                        }

                        if (Character.toLowerCase(origen) == 'k'
                                && filaOrigen == filaDestinacio
                                && Math.abs(columnaDestinacio - columnaOrigen) == 2) {

                            if (potEnrocar(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen)) {
                                realitzarEnroque(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen);
                                historialMoviments.add(moviment);
                                torn = 1 - torn;
                                continue;
                            } else {
                                System.out.println("Moviment invàlid");
                                continue;
                            }
                        }

                        if (!potMoure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen) &&
                                !potCapturar(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, origen)) {
                            System.out.println("Moviment invàlid");
                            continue;
                        }

                        tauler[filaDestinacio][columnaDestinacio] = origen;
                        tauler[filaOrigen][columnaOrigen] = ' ';

                        if (estaEnEscac(torn)) {
                            tauler[filaOrigen][columnaOrigen] = origen;
                            tauler[filaDestinacio][columnaDestinacio] = destinacio;
                            System.out.println("No pots deixar el teu rei en escac");
                            continue;
                        }

                        if (destinacio != ' ') {
                            pecesCapturades.add(destinacio);
                        }

                        historialMoviments.add(moviment);
                        actualitzarFlags(origen, filaOrigen, columnaOrigen);
                        torn = 1 - torn;

                        if (esEscacIMat(torn)) {
                            imprimirTauler();
                            System.out.println("ESCAC I MAT!");
                            System.out.println("Guanya " + (torn == 0 ? jugadorNegre : jugadorBlanc) + "!");
                            System.out.println("Historial de moviments: " + historialMoviments);
                            gameover = true;
                        }

                        coronacio(filaDestinacio, columnaDestinacio);

                    } catch (Exception e) {
                        System.out.println("Entrada invàlida");
                    }
                }
            } while (!gameover);

            seguirJugant = preguntarSiNo("Voleu jugar una altra partida? (s/n): ");
            if (seguirJugant) {
                mateixosJugadors = preguntarSiNo("Són els mateixos jugadors? (s/n): ");
            }
        }
    }

    // Inicialitzar estat de partida
    static void inicialitzarPartida() {
        tauler = new char[8][8];
        for (int i = 0; i < 8; i++)
            System.arraycopy(taulerInicial[i], 0, tauler[i], 0, 8);

        gameover = false;
        reiBlancMoviment = reiNegreMoviment = false;
        torreBlancaAMoviment = torreBlancaHMoviment = false;
        torreNegreAMoviment = torreNegreHMoviment = false;
        pecesCapturades = new ArrayList<>();
        historialMoviments = new ArrayList<>();
    }

    // Pregunta sí/no
    static boolean preguntarSiNo(String missatge) {
        while (true) {
            System.out.print(missatge);
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s")) return true;
            if (r.equals("n")) return false;
        }
    }

    // Demanar nom jugador
    static String demanarNomJugador(String missatge) {
        String nom;
        do {
            System.out.print(missatge);
            nom = scanner.nextLine().trim();
        } while (nom.isEmpty());
        return nom;
    }


    // Comprovar si el rei està en escac
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

    // Comprovar si és escac i mat
    static boolean esEscacIMat(int torn) {
        try {
            // Si el rei no està en escac, no pot ser mat
            if (!estaEnEscac(torn))
                return false;

            for (int fila = 0; fila < 8; fila++) {
                for (int columna = 0; columna < 8; columna++) {
                    char peça = tauler[fila][columna];
                    if (peça == ' ')
                        continue;
                    if (esBlanca(peça) != (torn == 0))
                        continue;

                    for (int filaDest = 0; filaDest < 8; filaDest++) {
                        for (int columnaDest = 0; columnaDest < 8; columnaDest++) {
                            if (!potMoure(fila, columna, filaDest, columnaDest, peça) &&
                                    !potCapturar(fila, columna, filaDest, columnaDest, peça))
                                continue;

                            char origen = tauler[fila][columna];
                            char destinacio = tauler[filaDest][columnaDest];

                            // Simular moviment
                            tauler[filaDest][columnaDest] = origen;
                            tauler[fila][columna] = ' ';

                            boolean escac = estaEnEscac(torn);

                            // Desfer moviment
                            tauler[fila][columna] = origen;
                            tauler[filaDest][columnaDest] = destinacio;

                            // Si hi ha algun moviment que treu al rei de l'escac, no és mat
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


    // Funcions auxiliars
    // Imprimir el tauler amb colors
    static void imprimirTauler() {
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
        System.out.println("    a  b  c  d  e  f  g  h\n");

        mostrarPecesCapturades();
    }

    // Mostrar peces capturades amb colors
    static void mostrarPecesCapturades() {
        System.out.print("Peces capturades: ");
        for (char peça : pecesCapturades) {
            String color = Character.isUpperCase(peça) ? "\u001B[37m" : "\u001B[31m";
            System.out.print(color + peça + " " + "\u001B[0m");
        }
        System.out.println("\n");
    }

    // Comprovar si una peça és blanca
    static boolean esBlanca(char peça) {
        return Character.isUpperCase(peça);
    }

    // Comprovar si una peça pot moure's a una destinació
    static boolean potMoure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio, char peça) {
        if (peça == ' ')
            return false;

        boolean blanca = esBlanca(peça);
        char destinacio = tauler[filaDestinacio][columnaDestinacio];
        if (destinacio != ' ' && esBlanca(destinacio) == blanca)
            return false;

        switch (Character.toLowerCase(peça)) {
            case 'p':
                int direccio = blanca ? 1 : -1;
                if (columnaOrigen == columnaDestinacio && destinacio == ' ' &&
                        filaDestinacio == filaOrigen + direccio)
                    return true;
                if (columnaOrigen == columnaDestinacio && destinacio == ' ' &&
                        ((blanca && filaOrigen == 1) || (!blanca && filaOrigen == 6)) &&
                        filaDestinacio == filaOrigen + 2 * direccio &&
                        tauler[filaOrigen + direccio][columnaOrigen] == ' ')
                    return true;
                if (Math.abs(columnaOrigen - columnaDestinacio) == 1 &&
                        filaDestinacio == filaOrigen + direccio &&
                        destinacio != ' ' && esBlanca(destinacio) != blanca)
                    return true;
                return false;
            case 't':
                return lineaLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
            case 'c':
                return (Math.abs(filaOrigen - filaDestinacio) == 2 && Math.abs(columnaOrigen - columnaDestinacio) == 1)
                        ||
                        (Math.abs(filaOrigen - filaDestinacio) == 1
                                && Math.abs(columnaOrigen - columnaDestinacio) == 2);
            case 'a':
                return diagonalLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
            case 'q':
                return lineaLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio) ||
                        diagonalLliure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio);
            case 'k':
                return Math.abs(filaOrigen - filaDestinacio) <= 1 && Math.abs(columnaOrigen - columnaDestinacio) <= 1;
        }
        return false;
    }

    // Comprovar si una peça pot capturar a una destinació
    static boolean potCapturar(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio,
            char peça) {
        char destinacio = tauler[filaDestinacio][columnaDestinacio];
        return destinacio != ' ' && esBlanca(destinacio) != esBlanca(peça) &&
                potMoure(filaOrigen, columnaOrigen, filaDestinacio, columnaDestinacio, peça);
    }

    // Comprovar si la línia entre origen i destinació està lliure
    static boolean lineaLliure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio) {
        if (filaOrigen != filaDestinacio && columnaOrigen != columnaDestinacio)
            return false;

        int incFila = Integer.compare(filaDestinacio, filaOrigen);
        int incCol = Integer.compare(columnaDestinacio, columnaOrigen);

        int fila = filaOrigen + incFila;
        int col = columnaOrigen + incCol;

        while (fila != filaDestinacio || col != columnaDestinacio) {
            if (tauler[fila][col] != ' ')
                return false;
            fila += incFila;
            col += incCol;
        }
        return true;
    }

    // Comprovar si la diagonal entre origen i destinació està lliure
    static boolean diagonalLliure(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio) {
        if (Math.abs(filaDestinacio - filaOrigen) != Math.abs(columnaDestinacio - columnaOrigen))
            return false;

        int incFila = Integer.compare(filaDestinacio, filaOrigen);
        int incCol = Integer.compare(columnaDestinacio, columnaOrigen);

        int fila = filaOrigen + incFila;
        int col = columnaOrigen + incCol;

        while (fila != filaDestinacio || col != columnaDestinacio) {
            if (tauler[fila][col] != ' ')
                return false;
            fila += incFila;
            col += incCol;
        }
        return true;
    }

    // Comprovar si es pot enrocar
    static boolean potEnrocar(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio, char rei) {
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
            if (tauler[filaOrigen][columnaOrigen - 1] != ' ' || tauler[filaOrigen][columnaOrigen - 2] != ' ' ||
                    tauler[filaOrigen][columnaOrigen - 3] != ' ')
                return false;
            if (estaEnEscacEnCasella(filaOrigen, columnaOrigen - 1, blanca) ||
                    estaEnEscacEnCasella(filaOrigen, columnaOrigen - 2, blanca))
                return false;
            return true;
        }
        return false;
    }

    // Realitzar l'enroc
    static void realitzarEnroque(int filaOrigen, int columnaOrigen, int filaDestinacio, int columnaDestinacio,
            char rei) {
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
    }

    // Comprovar si una casella està en escac
    static boolean estaEnEscacEnCasella(int fila, int columna, boolean blanca) {
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
        return false;
    }

    // Actualitzar les flags de moviment de rei i torres
    static void actualitzarFlags(char peça, int filaOrigen, int columnaOrigen) {
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
    }

    // Funció de coronació de peó
    static void coronacio(int fila, int columna) {
        char peça = tauler[fila][columna];
        if (Character.toLowerCase(peça) != 'p')
            return;

        if (peça == 'P' && fila == 7)
            tauler[fila][columna] = escollirPeçaCoronacio(true);
        if (peça == 'p' && fila == 0)
            tauler[fila][columna] = escollirPeçaCoronacio(false);
    }

    // Funció per escollir peça en la coronació
    static char escollirPeçaCoronacio(boolean blanca) {
        while (true) {
            System.out.print("Coronació! Tria (Q) Reina, (T) Torre, (A) Alfil, (C) Cavall: ");
            String entrada = scanner.nextLine().toUpperCase();
            if (entrada.length() != 1)
                continue;
            char opcio = entrada.charAt(0);
            switch (opcio) {
                case 'Q':
                case 'T':
                case 'A':
                case 'C':
                    return blanca ? opcio : Character.toLowerCase(opcio);
            }
        }
    }
}
