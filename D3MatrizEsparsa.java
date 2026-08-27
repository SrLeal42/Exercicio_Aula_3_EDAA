import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Matriz esparsa representada como "lista de tripas" (linha, coluna, valor).
 * So guarda os elementos nao nulos - e o formato do slide 29 da Aula 2.
 *
 * main() roda:
 *   1) testesDeCorretude()   -> Parte B: leitura, escrita e percurso dos nao nulos
 *   2) experimentoDensidade() -> Parte C: memoria e tempo, densa vs esparsa, por densidade
 */
public class D3MatrizEsparsa {

    // uma tripa: um elemento nao nulo da matriz
    static class Tripla {
        int linha;
        int coluna;
        double valor;

        Tripla(int linha, int coluna, double valor) {
            this.linha = linha;
            this.coluna = coluna;
            this.valor = valor;
        }
    }

    private final int linhas;
    private final int colunas;
    private final List<Tripla> tripas; // so os nao nulos ficam aqui

    public D3MatrizEsparsa(int linhas, int colunas) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.tripas = new ArrayList<>();
    }

    private void validarPosicao(int i, int j) {
        if (i < 0 || i >= linhas || j < 0 || j >= colunas) {
            throw new IndexOutOfBoundsException(
                "posicao (" + i + "," + j + ") fora da matriz " + linhas + "x" + colunas);
        }
    }

    // procura a tripa na posicao (i,j); retorna null se nao existir (celula ainda zero)
    private Tripla buscar(int i, int j) {
        for (Tripla t : tripas) {
            if (t.linha == i && t.coluna == j) {
                return t;
            }
        }
        return null;
    }

    /** Leitura: O(k), k = numero de nao nulos. Celula ausente vale 0.0. */
    public double ler(int i, int j) {
        validarPosicao(i, j);
        Tripla t = buscar(i, j);
        return (t == null) ? 0.0 : t.valor;
    }

    /**
     * Escrita: O(k) para achar a posicao.
     * valor == 0  -> remove a tripa se ela existir (nao guardamos zero)
     * valor != 0  -> atualiza a tripa existente, ou cria uma nova
     */
    public void escrever(int i, int j, double valor) {
        validarPosicao(i, j);
        Tripla existente = buscar(i, j);

        if (valor == 0.0) {
            if (existente != null) {
                tripas.remove(existente);
            }
            return;
        }

        if (existente != null) {
            existente.valor = valor;
        } else {
            tripas.add(new Tripla(i, j, valor));
        }
    }

    /**
     * Carga rapida, so para montar os dados dos testes/benchmarks: adiciona a tripa direto,
     * sem checar se a posicao ja existe. Nao e a API normal de escrita (essa e a escrever()) -
     * so faz sentido quando quem chama ja garante que as posicoes sao unicas, como ao carregar
     * dados em massa de um arquivo.
     */
    void adicionarBruto(int i, int j, double valor) {
        tripas.add(new Tripla(i, j, valor));
    }

    /** Percurso dos nao nulos (imprime): O(k), nao toca nas celulas vazias. */
    public void percorrerNaoNulos() {
        for (Tripla t : tripas) {
            System.out.printf("(%d, %d) = %.1f%n", t.linha, t.coluna, t.valor);
        }
    }

    /** Percurso dos nao nulos (silencioso, para medir tempo): mesma ideia, sem imprimir. */
    public double somarNaoNulos() {
        double soma = 0.0;
        for (Tripla t : tripas) {
            soma += t.valor;
        }
        return soma;
    }

    public int quantidadeNaoNulos() {
        return tripas.size();
    }

    // ---- Testes de corretude (leitura, escrita, percurso) ----
    static void testesDeCorretude() {
        System.out.println("===== Leitura, escrita e percurso =====");
        D3MatrizEsparsa m = new D3MatrizEsparsa(6, 6);

        m.escrever(0, 3, 7.0);
        m.escrever(2, 1, 4.0);
        m.escrever(4, 4, 9.0);
        System.out.println("nao nulos apos 3 escritas: " + m.quantidadeNaoNulos());

        System.out.println("m[0][3] = " + m.ler(0, 3) + "   (esperado 7.0)");
        System.out.println("m[1][1] = " + m.ler(1, 1) + "   (esperado 0.0, celula vazia)");

        m.escrever(0, 3, 12.0); // atualiza, nao duplica
        System.out.println("m[0][3] apos atualizar = " + m.ler(0, 3) + "   (esperado 12.0)");
        System.out.println("nao nulos continua: " + m.quantidadeNaoNulos() + "   (esperado 3)");

        m.escrever(2, 1, 0.0); // escrever zero remove a tripa
        System.out.println("nao nulos apos remover: " + m.quantidadeNaoNulos() + "   (esperado 2)");

        System.out.println("percurso dos nao nulos:");
        m.percorrerNaoNulos();
    }

    // ---- Memoria e tempo, densa vs esparsa, por densidade ----

    // percorre TODAS as celulas da matriz densa somando as que nao sao zero -> O(n^2)
    static double somarNaoNulosDenso(double[][] m) {
        double soma = 0.0;
        for (double[] linha : m) {
            for (double v : linha) {
                if (v != 0.0) soma += v;
            }
        }
        return soma;
    }

    static void experimentoDensidade() {
        System.out.println();
        System.out.println("===== Memoria e tempo por densidade (n = " + N + ") =====");
        System.out.printf("%-9s %8s | %10s %10s %-8s | %10s %10s %-8s | %10s %10s %-8s%n",
            "dens.", "k", "mem densa", "mem espar", "mem", "leit densa", "leit espar", "leitura", "perc densa", "perc espar", "percurso");
        System.out.println("(memoria em KB | tempo de leitura em ns/leitura, media de " + LEITURAS_POR_TESTE
            + " leituras | tempo de percurso em ms, media de " + REPETICOES_PERCURSO + " repeticoes)");

        double[] densidades = {0.001, 0.005, 0.01, 0.02, 0.05, 0.10, 0.20, 0.30, 0.40, 0.50, 0.70, 1.00};
        Random rnd = new Random(7);

        // aquecimento: roda uma vez em densidade media antes de medir, para o JIT compilar
        rodarUmaDensidade(0.15, rnd, false);

        double[] memVenceuDensaEm = new double[densidades.length]; // 1.0 = densa venceu memoria
        boolean[] leituraVenceuEsparsa = new boolean[densidades.length];
        int n = 0;

        for (double d : densidades) {
            Resultado r = rodarUmaDensidade(d, rnd, true);

            String vencMem = r.memEsparsaBytes < r.memDensaBytes ? "esparsa" : "densa";
            String vencLeitura = r.tempoLeituraEsparsaNs < r.tempoLeituraDensaNs ? "esparsa" : "densa";
            String vencPercurso = r.tempoPercursoEsparsaMs < r.tempoPercursoDensaMs ? "esparsa" : "densa";

            memVenceuDensaEm[n] = vencMem.equals("densa") ? 1.0 : 0.0;
            leituraVenceuEsparsa[n] = vencLeitura.equals("esparsa");
            n++;

            System.out.printf("%-9s %8d | %10d %10d %-8s | %10.1f %10.1f %-8s | %10.2f %10.2f %-8s%n",
                String.format("%.1f%%", d * 100),
                r.k,
                r.memDensaBytes / 1024,
                r.memEsparsaBytes / 1024,
                vencMem,
                r.tempoLeituraDensaNs,
                r.tempoLeituraEsparsaNs,
                vencLeitura,
                r.tempoPercursoDensaMs,
                r.tempoPercursoEsparsaMs,
                vencPercurso);
        }

        // memoria: cruzamento e deterministico (formula fixa), primeira densidade com "densa" ja resolve
        String memCross = ">100%";
        for (int i = 0; i < densidades.length; i++) {
            if (memVenceuDensaEm[i] == 1.0) { memCross = String.format("%.1f%%", densidades[i] * 100); break; }
        }

        // leitura: tempo de nanossegundos oscila (ruido) nas densidades muito baixas -
        // por isso procuramos a partir de qual densidade a densa vence DAI EM DIANTE, sem mais voltar
        String leituraCross = "abaixo de " + String.format("%.1f%%", densidades[0] * 100);
        int ultimoIndiceEsparsaVenceu = -1;
        for (int i = 0; i < densidades.length; i++) {
            if (leituraVenceuEsparsa[i]) ultimoIndiceEsparsaVenceu = i;
        }
        if (ultimoIndiceEsparsaVenceu == densidades.length - 1) {
            leituraCross = ">100% (esparsa venceu ate na maior densidade testada)";
        } else if (ultimoIndiceEsparsaVenceu >= 0) {
            leituraCross = String.format("%.1f%%", densidades[ultimoIndiceEsparsaVenceu + 1] * 100);
        }

    }

    static final int N = 200;
    static final int LEITURAS_POR_TESTE = 20_000;
    static final int REPETICOES_PERCURSO = 200;

    static class Resultado {
        int k;
        long memDensaBytes;
        long memEsparsaBytes;
        double tempoLeituraDensaNs;
        double tempoLeituraEsparsaNs;
        double tempoPercursoDensaMs;
        double tempoPercursoEsparsaMs;
    }

    static Resultado rodarUmaDensidade(double densidade, Random rnd, boolean medir) {
        int k = (int) Math.round(densidade * N * N);

        // gera k posicoes unicas
        Set<Integer> usadas = new HashSet<>();
        int[] posLinha = new int[k];
        int[] posColuna = new int[k];
        int gerados = 0;
        while (gerados < k) {
            int i = rnd.nextInt(N);
            int j = rnd.nextInt(N);
            int chave = i * N + j;
            if (usadas.add(chave)) {
                posLinha[gerados] = i;
                posColuna[gerados] = j;
                gerados++;
            }
        }

        double[][] densa = new double[N][N];
        D3MatrizEsparsa esparsa = new D3MatrizEsparsa(N, N);
        for (int idx = 0; idx < k; idx++) {
            double valor = 0.1 + rnd.nextDouble(); // nunca zero
            densa[posLinha[idx]][posColuna[idx]] = valor;
            esparsa.adicionarBruto(posLinha[idx], posColuna[idx], valor);
        }

        Resultado r = new Resultado();
        r.k = k;

        // memoria: mesma conta do slide 30 -> densa 8 bytes/celula, tripa 2 int + 1 double = 16 bytes
        r.memDensaBytes = (long) N * N * 8;
        r.memEsparsaBytes = (long) k * 16;

        if (!medir) return r;

        // ---- tempo de leitura de uma posicao qualquer ----
        int[] leiturasI = new int[LEITURAS_POR_TESTE];
        int[] leiturasJ = new int[LEITURAS_POR_TESTE];
        for (int idx = 0; idx < LEITURAS_POR_TESTE; idx++) {
            leiturasI[idx] = rnd.nextInt(N);
            leiturasJ[idx] = rnd.nextInt(N);
        }

        double somaControle = 0.0; // impede o JIT de eliminar as leituras como codigo morto

        long t0 = System.nanoTime();
        for (int idx = 0; idx < LEITURAS_POR_TESTE; idx++) {
            somaControle += densa[leiturasI[idx]][leiturasJ[idx]];
        }
        long t1 = System.nanoTime();
        r.tempoLeituraDensaNs = (double) (t1 - t0) / LEITURAS_POR_TESTE;

        long t2 = System.nanoTime();
        for (int idx = 0; idx < LEITURAS_POR_TESTE; idx++) {
            somaControle += esparsa.ler(leiturasI[idx], leiturasJ[idx]);
        }
        long t3 = System.nanoTime();
        r.tempoLeituraEsparsaNs = (double) (t3 - t2) / LEITURAS_POR_TESTE;

        // ---- tempo de percorrer todos os nao nulos ----
        long t4 = System.nanoTime();
        for (int rep = 0; rep < REPETICOES_PERCURSO; rep++) {
            somaControle += somarNaoNulosDenso(densa);
        }
        long t5 = System.nanoTime();
        r.tempoPercursoDensaMs = (double) (t5 - t4) / REPETICOES_PERCURSO / 1e6;

        long t6 = System.nanoTime();
        for (int rep = 0; rep < REPETICOES_PERCURSO; rep++) {
            somaControle += esparsa.somarNaoNulos();
        }
        long t7 = System.nanoTime();
        r.tempoPercursoEsparsaMs = (double) (t7 - t6) / REPETICOES_PERCURSO / 1e6;

        if (somaControle == Double.NEGATIVE_INFINITY) {
            System.out.println("nunca acontece, so existe para o JIT nao eliminar as leituras: " + somaControle);
        }

        return r;
    }

    public static void main(String[] args) {
        testesDeCorretude();
        experimentoDensidade();
    }
}



/*


== atualizar uma posicao existente ==
m[0][3] agora = 12.0   (esperado 12.0)
nao nulos continua: 3   (nao deve subir, so atualizou)

== escrever zero remove a tripa ==
m[2][1] agora = 0.0   (esperado 0.0)
nao nulos apos remocao: 2   (esperado 2)

== percurso dos nao nulos ==
(0, 3) = 12,0
(4, 4) = 9,0

== posicao fora dos limites ==
excecao capturada como esperado: posicao (10,10) fora da matriz 6x6




===== Leitura, escrita e percurso =====
nao nulos apos 3 escritas: 3
m[0][3] = 7.0   (esperado 7.0)
m[1][1] = 0.0   (esperado 0.0, celula vazia)
m[0][3] apos atualizar = 12.0   (esperado 12.0)
nao nulos continua: 3   (esperado 3)
nao nulos apos remover: 2   (esperado 2)
percurso dos nao nulos:
(0, 3) = 12,0
(4, 4) = 9,0

===== Memoria e tempo por densidade (n = 200) =====
dens.            k |  mem densa  mem espar mem      | leit densa leit espar leitura  | perc densa perc espar percurso
(memoria em KB | tempo de leitura em ns/leitura, media de 20000 leituras | tempo de percurso em ms, media de 200 repeticoes)
0,1%            40 |        312          0 esparsa  |       37,5      940,6 densa    |       0,07       0,00 esparsa 
0,5%           200 |        312          3 esparsa  |        6,5      144,9 densa    |       0,03       0,00 esparsa 
1,0%           400 |        312          6 esparsa  |       19,6      282,1 densa    |       0,04       0,02 esparsa 
2,0%           800 |        312         12 esparsa  |        7,6     1142,2 densa    |       0,04       0,00 esparsa 
5,0%          2000 |        312         31 esparsa  |       13,2     2887,2 densa    |       0,04       0,00 esparsa 
10,0%         4000 |        312         62 esparsa  |       10,0     3820,7 densa    |       0,05       0,00 esparsa 
20,0%         8000 |        312        125 esparsa  |        4,4     7962,0 densa    |       0,09       0,01 esparsa 
30,0%        12000 |        312        187 esparsa  |        1,7    10025,5 densa    |       0,12       0,01 esparsa 
40,0%        16000 |        312        250 esparsa  |        1,9    13119,1 densa    |       0,14       0,02 esparsa 
50,0%        20000 |        312        312 densa    |        1,5    16036,2 densa    |       0,18       0,03 esparsa 
70,0%        28000 |        312        437 densa    |        3,7    19144,1 densa    |       0,13       0,03 esparsa 
100,0%       40000 |        312        625 densa    |        1,6    22543,4 densa    |       0,04       0,05 densa   


*/
