import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelClique extends Thread {

    private static final Object lock = new Object();

    private static int noOfVertices;
    private static int noOfEdges;
    private static int[] vertexDegree;
    private static boolean[] vertexMarked;

    private static Integer noOfVerticesChecked = 0;
    private static Integer noOfEdgesChecked = 0;
    private static Integer noOfThreadsCompleted = 0;

    private static byte[][] graph;
    private static List<Integer> vertexSet = new LinkedList<>();
    private static List<Integer> vertices = new LinkedList<>();
    private static Set<Integer> clique = new HashSet<>();

    private int start;
    private int end;

    ParallelClique(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static void main(String args[]) {
        String fileName = args[0];

        ParallelClique parallelClique = new ParallelClique(-1, -1);
        parallelClique.readInputGraphFromFile(fileName);
        parallelClique.formDualGraph();
        parallelClique.collectGraphVertices();
        parallelClique.collectGraphEdges();
        Set<Integer> maxClique = parallelClique.findMaxClique();

        for (int vertex : maxClique) {
            System.out.println(vertex + 1);
        }
    }

    public void run() {
        if(start == end) {
            int vertex = vertices.get(start);
            int degree = vertexDegree[vertex];
            if (degree == 0) {
                synchronized (lock) {
                    clique.add(vertex);
                }
            } else {
                vertexMarked[vertex] = new Random().nextInt(2 * degree) == 1;
            }
            synchronized (lock) {
                noOfVerticesChecked++;
            }

            while (noOfVerticesChecked < vertices.size()) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (int j = vertex + 1; j < noOfVertices; j++) {
                if (graph[vertex][j] == 1) {
                    synchronized (lock) {
                        if (vertexMarked[vertex] && vertexMarked[j]) {
                            if (vertexDegree[vertex] < vertexDegree[j]) {
                                vertexMarked[vertex] = false;
                            } else {
                                vertexMarked[j] = false;
                            }
                        }
                    }
                    synchronized (lock) {
                        noOfEdgesChecked++;
                    }
                }
            }

            while (noOfEdgesChecked < noOfEdges) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (vertexMarked[vertex]) {
                synchronized (lock) {
                    vertexSet.add(vertex);
                }
            }

            synchronized (lock) {
                noOfThreadsCompleted++;
            }
        } else {
            int mid = (start + end) / 2;
            ParallelClique thread1 = new ParallelClique(start, mid);
            ParallelClique thread2 = new ParallelClique(mid + 1, end);
            thread1.start();
            thread2.start();
        }
    }

    private void readInputGraphFromFile(String fileName) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)));

        String line;
        List<Byte> edges = new ArrayList<>();
        try {
            if ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(" ");
                for (String value : values) {
                    edges.add(Byte.valueOf(value));
                }
            }
            noOfVertices = edges.size();
            graph = new byte[noOfVertices][noOfVertices];
            for (int i = 0; i < noOfVertices; i++) {
                graph[0][i] = edges.get(i);
            }
            int vertex = 1;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(" ");
                for (int i = 0; i < values.length; i++) {
                    graph[vertex][i] = Byte.valueOf(values[i]);
                }
                vertex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void formDualGraph() {
        for (int i = 0; i < noOfVertices; i++) {
            for (int j = 0; j < noOfVertices; j++) {
                graph[i][j] = (byte) (1 - graph[i][j]);
            }
        }
    }

    private void collectGraphVertices() {
        for (int i = 0; i < noOfVertices; i++) {
            vertices.add(i);
        }
    }

    private void collectGraphEdges() {
        noOfEdges = 0;
        for (int i = 0; i < noOfVertices; i++) {
            for (int j = i + 1; j < noOfVertices; j++) {
                if (graph[i][j] == 1) {
                    noOfEdges++;
                }
            }
        }
    }

    private Set<Integer> findMaxClique() {
        while (vertices.size() > 0) {
            vertexSet = new LinkedList<>();
            vertexMarked = new boolean[noOfVertices];
            findVertexDegree();
            noOfVerticesChecked = 0;
            noOfEdgesChecked = 0;
            noOfThreadsCompleted = 0;

            start = 0;
            end = vertices.size() - 1;

            run();

            while (noOfThreadsCompleted < vertices.size()) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            clique.addAll(vertexSet);

            List<Integer> gammaSet = new LinkedList<>();
            for (int vertex : vertexSet) {
                for (int i = 0; i < noOfVertices; i++) {
                    if (graph[vertex][i] == 1) {
                        gammaSet.add(i);
                    }
                }
            }

            vertexSet.addAll(gammaSet);

            vertices.removeAll(vertexSet);
            for (int vertex : vertexSet) {
                for (int i = 0; i < noOfVertices; i++) {
                    graph[vertex][i] = 0;
                }
            }
            collectGraphEdges();
        }
        return clique;
    }

    private void findVertexDegree() {
        vertexDegree = new int[noOfVertices];
        for (int vertex : vertices) {
            for (int i = 0; i < noOfVertices; i++) {
                vertexDegree[vertex] += graph[vertex][i];
            }
        }
    }

}