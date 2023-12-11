package calculadoraparalela;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase Runnable que representa la tarea de sumar elementos en un subarreglo del arreglo principal.
 */
class Suma implements Runnable {
    private final int[] arreglo;
    private final int indexInicio;
    private final int indexFinal;
    private final CountDownLatch cerrojo;
    private int sumaParcial;

    /**
     * Constructor de la clase Suma.
     * @param arreglo Arreglo de enteros sobre el cual se realizará la suma parcial.
     * @param indexInicio Índice de inicio del subarreglo.
     * @param indexFinal Índice de fin del subarreglo (no inclusivo).
     * @param cerrojo CountDownLatch para sincronización entre hilos.
     */
    public Suma(int[] arreglo, int indexInicio, int indexFinal, CountDownLatch cerrojo) {
        this.arreglo = arreglo;
        this.indexInicio = indexInicio;
        this.indexFinal = indexFinal;
        this.cerrojo = cerrojo;
        this.sumaParcial = 0;
    }

    /**
     * Método run() que realiza la suma de elementos en el subarreglo asignado al hilo.
     * Se ejecuta cuando se inicia el hilo.
     */
    @Override
    public void run() {
        for (int i = indexInicio; i < indexFinal; i++) {
            sumaParcial += arreglo[i];
        }
        cerrojo.countDown();
    }

    /**
     * Método que devuelve la suma parcial calculada por el hilo.
     * @return Suma parcial del subarreglo.
     */
    public int obtenerSumaParcial() {
        return sumaParcial;
    }
}

/**
 * Clase principal que realiza la suma de elementos de un arreglo en paralelo utilizando múltiples hilos.
 */
public class CalculadoraParalela {

    /**
     * Método principal que inicia la ejecución del programa.
     * @param args Argumentos de la línea de comandos (no se utilizan en este programa).
     */
    public static void main(String[] args) {
        
        // Configuración de parámetros
        int tamañoArreglo = 100;
        int numeroHilos = 10; 

        // Generación de arreglo aleatorio
        int[] arreglo = generarArregloAleatorio(tamañoArreglo);

        // Configuración del ejecutor y el cerrojo
        ExecutorService ejecutor = Executors.newFixedThreadPool(numeroHilos);
        CountDownLatch cerrojo = new CountDownLatch(numeroHilos);

        // Cálculo del tamaño de cada subarreglo
        int tamañoSubArreglo = tamañoArreglo / numeroHilos;
        Suma[] calculadoras = new Suma[numeroHilos];

        // Creación y ejecución de hilos
        for (int i = 0; i < numeroHilos; i++) {
            int indexInicio = i * tamañoSubArreglo;
            int indexFinal = (i == numeroHilos - 1) ? tamañoArreglo : (i + 1) * tamañoSubArreglo;

            calculadoras[i] = new Suma(arreglo, indexInicio, indexFinal, cerrojo);
            ejecutor.execute(calculadoras[i]);
        }

        // Espera hasta que todos los hilos hayan terminado
        try {
            cerrojo.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Apagar el ejecutor
        ejecutor.shutdown();

        // Impresión del arreglo generado
        System.out.println("Arreglo generado:");
        imprimirArreglo(arreglo);

        // Impresión de los resultados de cada hilo y la suma total
        System.out.println("\nResultados de cada hilo:");
        int sumaTotal = 0;
        for (int i = 0; i < numeroHilos; i++) {
            System.out.println("Hilo " + i + ": " + calculadoras[i].obtenerSumaParcial());
            sumaTotal += calculadoras[i].obtenerSumaParcial();
        }
        System.out.println("\nSuma total: " + sumaTotal);
    }

    /**
     * Método que genera un arreglo de enteros con valores aleatorios.
     * @param tamaño Tamaño del arreglo a generar.
     * @return Arreglo de enteros con valores aleatorios.
     */
    private static int[] generarArregloAleatorio(int tamaño) {
        int[] arreglo = new int[tamaño];
        Random random = new Random();
        for (int i = 0; i < tamaño; i++) {
            arreglo[i] = random.nextInt(100);
        }
        return arreglo;
    }

    /**
     * Método que imprime los elementos de un arreglo.
     * @param arreglo Arreglo de enteros a imprimir.
     */
    private static void imprimirArreglo(int[] arreglo) {
        for (int valor : arreglo) {
            System.out.print(valor + " ");
        }
        System.out.println();
    }
}