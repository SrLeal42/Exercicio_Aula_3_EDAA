import java.util.Random;

public class D1OrdemDosLacos {

    // versao i, j, k -> "mais lenta" (percorre b por coluna, salto grande na memoria)
    static void multiplyIJK(double[][] a, double[][] b, double[][] c, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double soma = 0.0;
                for (int k = 0; k < n; k++) {
                    soma += a[i][k] * b[k][j];
                }
                c[i][j] = soma;
            }
        }
    }

    // versao i, k, j -> "mais rapida" (percorre b por linha, contiguo na memoria)
    static void multiplyIKJ(double[][] a, double[][] b, double[][] c, int n) {
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                double aik = a[i][k];
                for (int j = 0; j < n; j++) {
                    c[i][j] += aik * b[k][j];
                }
            }
        }
    }

    static double[][] matrizAleatoria(int n, Random rnd) {
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = rnd.nextDouble();
        return m;
    }

    // a versao i,k,j acumula com +=, entao c precisa comecar zerada a cada rodada
    static void zerar(double[][] m) {
        for (double[] linha : m) java.util.Arrays.fill(linha, 0.0);
    }

    static boolean iguais(double[][] a, double[][] b, double eps) {
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a.length; j++)
                if (Math.abs(a[i][j] - b[i][j]) > eps) return false;
        return true;
    }

    static double media(long[] valores) {
        long soma = 0;
        for (long v : valores) soma += v;
        return (double) soma / valores.length;
    }

    static String formatar(long[] valores) {
        StringBuilder sb = new StringBuilder();
        for (long v : valores) sb.append(String.format("%.1f ", v / 1e6));
        return sb.toString().trim() + " ms";
    }

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 512;
        int repeticoes = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        int aquecimento = 2; // rodadas de warm-up, descartadas da medicao (deixa o JIT compilar)

        Random rnd = new Random(42);
        double[][] a = matrizAleatoria(n, rnd);
        double[][] b = matrizAleatoria(n, rnd);
        double[][] cIJK = new double[n][n];
        double[][] cIKJ = new double[n][n];

        System.out.println("n = " + n + " | repeticoes medidas = " + repeticoes + " | aquecimento = " + aquecimento);

        for (int r = 0; r < aquecimento; r++) {
            multiplyIJK(a, b, cIJK, n);
            zerar(cIKJ);
            multiplyIKJ(a, b, cIKJ, n);
        }

        long[] temposIJK = new long[repeticoes];
        long[] temposIKJ = new long[repeticoes];

        for (int r = 0; r < repeticoes; r++) {
            long t0 = System.nanoTime();
            multiplyIJK(a, b, cIJK, n);
            long t1 = System.nanoTime();
            temposIJK[r] = t1 - t0;

            zerar(cIKJ);
            long t2 = System.nanoTime();
            multiplyIKJ(a, b, cIKJ, n);
            long t3 = System.nanoTime();
            temposIKJ[r] = t3 - t2;
        }

        System.out.println("Resultados iguais nas duas ordens? " + iguais(cIJK, cIKJ, 1e-9));

        double mediaIJK = media(temposIJK);
        double mediaIKJ = media(temposIKJ);

        System.out.printf("i,j,k -> media: %.2f ms  (rodadas: %s)%n", mediaIJK / 1e6, formatar(temposIJK));
        System.out.printf("i,k,j -> media: %.2f ms  (rodadas: %s)%n", mediaIKJ / 1e6, formatar(temposIKJ));
        System.out.printf("Fator observado (i,j,k / i,k,j) = %.2fx%n", mediaIJK / mediaIKJ);
    }
}

/*

n = 512 | repeticoes medidas = 5 | aquecimento = 2
Resultados iguais nas duas ordens? true
i,j,k -> media: 244,83 ms  (rodadas: 206,7 281,4 216,2 213,4 306,5 ms)
i,k,j -> media: 29,41 ms  (rodadas: 30,9 28,8 30,5 28,4 28,5 ms)
Fator observado (i,j,k / i,k,j) = 8,32x



*/