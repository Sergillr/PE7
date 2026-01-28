import java.util.Scanner;
import java.util.ArrayList;

public class pe7 {

    // ------------------------- Variables globals -------------------------
    // Tauler inicial (No ho faig amb for per facilitat de debugging)
    char[][] taulerInicial = {
            { 'T', 'C', 'A', 'Q', 'K', 'A', 'C', 'T' },
            { 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
            { 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' },
            { 't', 'c', 'a', 'q', 'k', 'a', 'c', 't' }
    };

    // Variables del joc
    char[][] tauler;
    boolean gameOver;
    boolean reiBlancHaMogut, reiNegreHaMogut;
    boolean torreBlancaAMogut, torreBlancaHMogut;
    boolean torreNegreAMogut, torreNegreHMogut;
    ArrayList<Character> pecesCapturades;
    ArrayList<String> historialMoviments;
    Scanner scanner = new Scanner(System.in);
    String jugadorBlanc;
    String jugadorNegre;

    // ------------------------- Main -------------------------
    public static void main(String[] args) {
        pe7 p = new pe7();
        p.iniciarJoc();
    }

    public void iniciarJoc() {
        donarBenvinguda();
        jugarPartides();
    }

    // ------------------------- Inicialització -------------------------
    public void donarBenvinguda() {
        System.out.println("Benvingut al joc d'escacs!");
        scanner.nextLine();
    }

    // Inicialitza el tauler i les variables del joc
    public void inicialitzarPartida() {
        tauler = new char[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(taulerInicial[i], 0, tauler[i], 0, 8);
        }
        gameOver = false;
        reiBlancHaMogut = false;
        reiNegreHaMogut = false;
        torreBlancaAMogut = false;
        torreBlancaHMogut = false;
        torreNegreAMogut = false;
        torreNegreHMogut = false;
        pecesCapturades = new ArrayList<>();
        historialMoviments = new ArrayList<>();
    }

    public String demanarNomJugador(String missatge) {
        String nom = "";
        while (nom.isEmpty()) {
            System.out.print(missatge);
            nom = scanner.nextLine().trim();
        }
        return nom;
    }

    // Solicita si els jugadors volen continuar jugant
    public boolean continuarJugant(String missatge) {
        while (true) {
            System.out.print(missatge);
            String resposta = scanner.nextLine().trim().toLowerCase();
            if (resposta.equals("s"))
                return true;
            if (resposta.equals("n"))
                return false;
        }
    }

    // ------------------------- Joc principal -------------------------
    // Bucle principal per jugar múltiples partides
    public void jugarPartides() {
        boolean seguirJugant = true;
        boolean mateixosJugadors = false;

        while (seguirJugant) {
            if (!mateixosJugadors) {
                jugadorBlanc = demanarNomJugador("Nom del jugador blanc: ");
                jugadorNegre = demanarNomJugador("Nom del jugador negre: ");
            }

            inicialitzarPartida();
            mostrarInstruccions();

            int torn = 0;
            while (!gameOver) {
                imprimirTauler();
                boolean tornConsumit = processarTorn(torn);
                if (tornConsumit)
                    torn = 1 - torn;
            }

            seguirJugant = continuarJugant("Voleu jugar una altra partida? (s/n): ");
            if (seguirJugant) {
                mateixosJugadors = continuarJugant("Són els mateixos jugadors? (s/n): ");
            }
        }
    }

    public void mostrarInstruccions() {
        System.out.println("En cas de voler abandonar el joc, escriu 'Abandonar'");
        scanner.nextLine();
        System.out.println("Format de moviment: AX BY (lletra columna + número fila)");
        System.out.println("BLAQUES comencen primer.\n");
    }

    // ------------------------- Torn i moviment -------------------------
    // Processa el torn d'un jugador i retorna si el torn ha estat consumit
    boolean processarTorn(int torn) {
        try {
            System.out.println("Torn de " + (torn == 0 ? jugadorBlanc : jugadorNegre));
            System.out.print("Moviment: ");
            String moviment = scanner.nextLine().toLowerCase();

            if (moviment.equalsIgnoreCase("Abandonar")) {
                gameOver = true;
                return true;
            }

            if (!validarFormatMoviment(moviment)) {
                System.out.println("Format incorrecte");
                return false;
            }

            int[] coords = obtenirCoords(moviment);
            int filaOrigen = coords[0], columnaOrigen = coords[1];
            int filaDest = coords[2], columnaDesti = coords[3];

            if (filaOrigen < 0 || filaOrigen > 7 || columnaOrigen < 0 || columnaOrigen > 7 ||
                    filaDest < 0 || filaDest > 7 || columnaDesti < 0 || columnaDesti > 7) {
                System.out.println("Coordenades fora del tauler");
                return false;
            }

            char origen = tauler[filaOrigen][columnaOrigen];
            char destinacio = tauler[filaDest][columnaDesti];

            if (origen == ' ') {
                System.out.println("No hi ha cap peça en l'origen");
                return false;
            }

            if (!movimentCorrecteTorn(origen, torn)) {
                return false;
            }

            if (reiEnroque(origen, filaOrigen, columnaOrigen, filaDest, columnaDesti)) {
                return true;
            }

            if (!potMoure(origen, filaOrigen, columnaOrigen, filaDest, columnaDesti) &&
                    !potCapturar(origen, filaOrigen, columnaOrigen, filaDest, columnaDesti)) {
                System.out.println("Moviment invàlid");
                return false;
            }

            tauler[filaDest][columnaDesti] = origen;
            tauler[filaOrigen][columnaOrigen] = ' ';
            boolean reiEnEscac = estaEnEscac(torn);

            tauler[filaOrigen][columnaOrigen] = origen;
            tauler[filaDest][columnaDesti] = destinacio;

            if (reiEnEscac) {
                System.out.println("Moviment invàlid: el teu rei quedaria en escac");
                return false;
            }

            realitzarMoviment(origen, filaOrigen, columnaOrigen, filaDest, columnaDesti, destinacio);

            if (esEscacIMat(torn == 0 ? 1 : 0)) {
                imprimirTauler();
                System.out.println("ESCAC I MAT!");
                System.out.println("Guanya " + (torn == 0 ? jugadorBlanc : jugadorNegre) + "!");
                System.out.println("Historial de moviments: " + historialMoviments);
                gameOver = true;
            }

            return true;

        } catch (Exception e) {
            System.out.println("S'ha produït un error al processar el torn. Torna a intentar-ho.");
            scanner.nextLine();
            return false;
        }
    }

    public boolean validarFormatMoviment(String moviment) {
        return moviment.length() == 5 && moviment.charAt(2) == ' ';
    }

    // Converteix el moviment en coordenades d'índexs
    public int[] obtenirCoords(String moviment) {
        int filaOrigen = Character.getNumericValue(moviment.charAt(1)) - 1;
        int columnaOrigen = moviment.charAt(0) - 'a';
        int filaDesti = Character.getNumericValue(moviment.charAt(4)) - 1;
        int columnaDesti = moviment.charAt(3) - 'a';
        return new int[] { filaOrigen, columnaOrigen, filaDesti, columnaDesti };
    }

    // Comprova si el moviment és correcte segons el torn
    public boolean movimentCorrecteTorn(char origen, int torn) {
        if ((torn == 0 && !esBlanca(origen)) || (torn == 1 && esBlanca(origen))) {
            System.out.println("No es el torn de aquestes peces");
            return false;
        }
        return true;
    }

    // -------------------- Funcions de tauler i visualització --------------------
    public boolean esBlanca(char peça) {
        return Character.isUpperCase(peça);
    }

    // Imprimir el tauler amb colors
    void imprimirTauler() {
        for (int fila = 0; fila < 8; fila++) {
            System.out.print((fila + 1) + "  ");
            for (int columna = 0; columna < 8; columna++) {
                char peça = tauler[fila][columna];
                String fons = (fila + columna) % 2 == 0 ? "\u001B[47m" : "\u001B[40m";
                String colorPeça = peça == ' ' ? "\u001B[30m"
                        : (Character.isUpperCase(peça) ? "\u001B[37m" : "\u001B[31m");
                System.out.print(fons + colorPeça + " " + peça + " " + "\u001B[0m");
            }
            System.out.println();
        }
        System.out.println("    a  b  c  d  e  f  g  h\n");
        mostrarPecesCapturades();
    }

    // Mostrar peces capturades amb colors
    public void mostrarPecesCapturades() {
        try {
            System.out.print("Peces capturades: ");
            for (char peça : pecesCapturades) {
                String color = Character.isUpperCase(peça) ? "\u001B[37m" : "\u001B[31m";
                System.out.print(color + peça + " " + "\u001B[0m");
            }
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println("Error mostrant les peces capturades.");
        }
    }

    // -------------------- Moviment i captura --------------------
    // Comprova si una peça pot moure's a una destinació
    public boolean potMoure(char peça, int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti) {
        if (peça == ' ')
            return false;
        boolean blanca = esBlanca(peça);
        char dest = tauler[filaDesti][columnaDesti];
        if (dest != ' ' && esBlanca(dest) == blanca)
            return false;

        switch (Character.toLowerCase(peça)) {
            case 'p':
                return potMourePeo(peça, filaOrigen, columnaOrigen, filaDesti, columnaDesti);
            case 't':
                return lineaLliure(filaOrigen, columnaOrigen, filaDesti, columnaDesti);
            case 'c':
                return Math.abs(filaOrigen - filaDesti) == 2 && Math.abs(columnaOrigen - columnaDesti) == 1
                        || Math.abs(filaOrigen - filaDesti) == 1 && Math.abs(columnaOrigen - columnaDesti) == 2;
            case 'a':
                return diagonalLliure(filaOrigen, columnaOrigen, filaDesti, columnaDesti);
            case 'q':
                return lineaLliure(filaOrigen, columnaOrigen, filaDesti, columnaDesti)
                        || diagonalLliure(filaOrigen, columnaOrigen, filaDesti, columnaDesti);
            case 'k':
                return Math.abs(filaOrigen - filaDesti) <= 1 && Math.abs(columnaOrigen - columnaDesti) <= 1;
        }
        return false;
    }

    // Comprova si una peça pot capturar en una destinació
    public boolean potCapturar(char peça, int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti) {
        char desti = tauler[filaDesti][columnaDesti];
        return desti != ' ' && esBlanca(desti) != esBlanca(peça)
                && potMoure(peça, filaOrigen, columnaOrigen, filaDesti, columnaDesti);
    }

    // Moviment específic del peó
    public boolean potMourePeo(char peça, int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti) {
        boolean blanca = esBlanca(peça);
        int direccio = blanca ? 1 : -1;
        char desti = tauler[filaDesti][columnaDesti];

        if (columnaOrigen == columnaDesti && desti == ' ' && filaDesti == filaOrigen + direccio)
            return true;
        if (columnaOrigen == columnaDesti && desti == ' ' &&
                ((blanca && filaOrigen == 1) || (!blanca && filaOrigen == 6)) &&
                filaDesti == filaOrigen + 2 * direccio && tauler[filaOrigen + direccio][columnaOrigen] == ' ')
            return true;
        if (Math.abs(columnaOrigen - columnaDesti) == 1 && filaDesti == filaOrigen + direccio && desti != ' '
                && esBlanca(desti) != blanca)
            return true;
        return false;
    }

    // Comprova si la línia entre origen i destinació està lliure
    public boolean lineaLliure(int filaOrigen, int colOrigen, int filaDesti, int columnaDesti) {
        if (filaOrigen != filaDesti && colOrigen != columnaDesti)
            return false;
        int incFila = Integer.compare(filaDesti, filaOrigen);
        int incColumna = Integer.compare(columnaDesti, colOrigen);
        int fila = filaOrigen + incFila, columna = colOrigen + incColumna;
        while (fila != filaDesti || columna != columnaDesti) {
            if (tauler[fila][columna] != ' ')
                return false;
            fila += incFila;
            columna += incColumna;
        }
        return true;
    }

    // Comprova si la diagonal entre origen i destinació està lliure
    public boolean diagonalLliure(int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti) {
        if (Math.abs(filaDesti - filaOrigen) != Math.abs(columnaDesti - columnaOrigen))
            return false;
        int incFila = Integer.compare(filaDesti, filaOrigen);
        int incColumna = Integer.compare(columnaDesti, columnaOrigen);
        int fila = filaOrigen + incFila, columna = columnaOrigen + incColumna;
        while (fila != filaDesti || columna != columnaDesti) {
            if (tauler[fila][columna] != ' ')
                return false;
            fila += incFila;
            columna += incColumna;
        }
        return true;
    }

    // -------------------- Escac i enrocs --------------------
    // Comprova si el jugador està en escac
    public boolean estaEnEscac(int torn) {
        int reiFila = -1, reiColumna = -1;
        char reiChar = torn == 0 ? 'K' : 'k';
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (tauler[i][j] == reiChar) {
                    reiFila = i;
                    reiColumna = j;
                }

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                char peça = tauler[i][j];
                if (peça == ' ' || esBlanca(peça) == (torn == 0))
                    continue;
                if (potCapturar(peça, i, j, reiFila, reiColumna))
                    return true;
            }
        return false;
    }

    // Comprova si el jugador està en escac i mat
    public boolean esEscacIMat(int torn) {
        if (!estaEnEscac(torn))
            return false;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                char peça = tauler[i][j];
                if (peça == ' ' || esBlanca(peça) != (torn == 0))
                    continue;
                for (int filaDesti = 0; filaDesti < 8; filaDesti++)
                    for (int columnaDesti = 0; columnaDesti < 8; columnaDesti++) {
                        if (!potMoure(peça, i, j, filaDesti, columnaDesti)
                                && !potCapturar(peça, i, j, filaDesti, columnaDesti))
                            continue;
                        char origen = tauler[i][j], desti = tauler[filaDesti][columnaDesti];
                        tauler[filaDesti][columnaDesti] = origen;
                        tauler[i][j] = ' ';
                        boolean escac = estaEnEscac(torn);
                        tauler[i][j] = origen;
                        tauler[filaDesti][columnaDesti] = desti;
                        if (!escac)
                            return false;
                    }
            }
        return true;
    }

    // Comprova i realitza l'enroc si és possible
    boolean reiEnroque(char rei, int filaOrigen, int colOrigen, int filaDest, int columnaDesti) {
        if (Character.toLowerCase(rei) != 'k')
            return false;

        int dist = columnaDesti - colOrigen;
        if (filaOrigen != filaDest || Math.abs(dist) != 2)
            return false;

        boolean blanca = esBlanca(rei);
        int colTorre = dist > 0 ? 7 : 0;
        char torre = tauler[filaOrigen][colTorre];
        if (Character.toLowerCase(torre) != 't') {
            System.out.println("Enroque invàlid: no hi ha torre a la posició correcta");
            return true;
        }

        // Comprobar que no haya piezas intermedias
        int paso = dist > 0 ? 1 : -1;
        for (int c = colOrigen + paso; c != colTorre; c += paso) {
            if (tauler[filaOrigen][c] != ' ') {
                System.out.println("Enroque invàlid: hi ha peces entre el rei i la torre");
                return true;
            }
        }

        // Comprobar que las casillas por donde pasa el rey no estén bajo ataque
        for (int c = colOrigen; c != columnaDesti + paso; c += paso) {
            if (estaEnEscacEnCasella(filaOrigen, c, !blanca)) {
                System.out.println("Enroque invàlid: el rei passaria per casella atacada");
                return true;
            }
        }

        // Realizar enroque
        realitzarEnroque(filaOrigen, colOrigen, filaDest, columnaDesti, rei);
        historialMoviments.add(coordsAString(filaOrigen, colOrigen, filaDest, columnaDesti));
        return true;
    }

    // Comprova si l'enroc és possible
    public boolean potEnrocar(int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti, char rei) {
        boolean blanca = esBlanca(rei);
        if (estaEnEscac(blanca ? 0 : 1))
            return false;
        if (columnaDesti - columnaOrigen == 2) {
            if ((blanca && (reiBlancHaMogut || torreBlancaHMogut)) ||
                    (!blanca && (reiNegreHaMogut || torreNegreHMogut)))
                return false;
            if (tauler[filaOrigen][columnaOrigen + 1] != ' ' || tauler[filaOrigen][columnaOrigen + 2] != ' ')
                return false;
            if (estaEnEscacEnCasella(filaOrigen, columnaOrigen + 1, blanca)
                    || estaEnEscacEnCasella(filaOrigen, columnaOrigen + 2, blanca))
                return false;
            return true;
        }
        if (columnaDesti - columnaOrigen == -2) {
            if ((blanca && (reiBlancHaMogut || torreBlancaAMogut)) ||
                    (!blanca && (reiNegreHaMogut || torreNegreAMogut)))
                return false;
            if (tauler[filaOrigen][columnaOrigen - 1] != ' ' || tauler[filaOrigen][columnaOrigen - 2] != ' '
                    || tauler[filaOrigen][columnaOrigen - 3] != ' ')
                return false;
            if (estaEnEscacEnCasella(filaOrigen, columnaOrigen - 1, blanca)
                    || estaEnEscacEnCasella(filaOrigen, columnaOrigen - 2, blanca))
                return false;
            return true;
        }
        return false;
    }

    public void realitzarEnroque(int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti, char rei) {
        boolean blanca = esBlanca(rei);
        tauler[filaDesti][columnaDesti] = rei;
        tauler[filaOrigen][columnaOrigen] = ' ';
        if (columnaDesti - columnaOrigen == 2) {
            tauler[filaOrigen][columnaOrigen + 1] = tauler[filaOrigen][7];
            tauler[filaOrigen][7] = ' ';
        } else {
            tauler[filaOrigen][columnaOrigen - 1] = tauler[filaOrigen][0];
            tauler[filaOrigen][0] = ' ';
        }
        if (blanca) {
            reiBlancHaMogut = true;
            if (columnaDesti - columnaOrigen == 2)
                torreBlancaHMogut = true;
            else
                torreBlancaAMogut = true;
        } else {
            reiNegreHaMogut = true;
            if (columnaDesti - columnaOrigen == 2)
                torreNegreHMogut = true;
            else
                torreNegreAMogut = true;
        }
    }

    // Comprova si la casella del rei està en escac
    boolean estaEnEscacEnCasella(int fila, int col, boolean blanca) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                char peça = tauler[i][j];
                if (peça == ' ' || esBlanca(peça) == blanca)
                    continue;
                if (potCapturar(peça, i, j, fila, col))
                    return true;
            }
        return false;
    }

    // -------------------- Realitzar moviment --------------------
    public void realitzarMoviment(char peça, int filaOrigen, int columnaOrigen, int filaDesti, int columnaDesti,
            char destinacio) {
        tauler[filaDesti][columnaDesti] = peça;
        tauler[filaOrigen][columnaOrigen] = ' ';
        if (destinacio != ' ')
            pecesCapturades.add(destinacio);
        historialMoviments.add(coordsAString(filaOrigen, columnaOrigen, filaDesti, columnaDesti));
        actualitzarFlags(peça, filaOrigen, columnaOrigen);
        coronacio(filaDesti, columnaDesti);
    }

    // Actualitza les banderes d'enroc i moviment del rei
    public void actualitzarFlags(char peça, int filaOrigen, int columnaOrigen) {
        if (peça == 'K')
            reiBlancHaMogut = true;
        else if (peça == 'k')
            reiNegreHaMogut = true;
        else if (peça == 'T' && filaOrigen == 0 && columnaOrigen == 0)
            torreBlancaAMogut = true;
        else if (peça == 'T' && filaOrigen == 0 && columnaOrigen == 7)
            torreBlancaHMogut = true;
        else if (peça == 't' && filaOrigen == 7 && columnaOrigen == 0)
            torreNegreAMogut = true;
        else if (peça == 't' && filaOrigen == 7 && columnaOrigen == 7)
            torreNegreHMogut = true;
    }

    // Converteix les coordenades a format de cadena
    public String coordsAString(int filaOrigen, int colimnaOrigen, int filaDesti, int columnaDesti) {
        return "" + (char) ('a' + colimnaOrigen) + (filaOrigen + 1) + " " + (char) ('a' + columnaDesti)
                + (filaDesti + 1);
    }

    // -------------------- Coronació --------------------
    public void coronacio(int fila, int columna) {
        char peça = tauler[fila][columna];
        if (Character.toLowerCase(peça) != 'p')
            return;
        if (peça == 'P' && fila == 7)
            tauler[fila][columna] = escollirPeçaCoronacio(true);
        if (peça == 'p' && fila == 0)
            tauler[fila][columna] = escollirPeçaCoronacio(false);
    }

    public char escollirPeçaCoronacio(boolean blanca) {
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