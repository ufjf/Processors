package testeprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TesteProcessor {

    public static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Rodando...");
        try{
            int incremento = 25;
            StringBuilder sb = new StringBuilder();
            sb.append("w1\tw2\tw3\tw4\tignore trivial similarities\taaron_pat casou certo?\tantczak_ashley casou errado?\t\n");

            for (int w1 = 0; w1 <= 100; w1 += incremento) {
                for (int w2 = 0; w2 <= 100 - w1; w2 += incremento) {
                    for (int w3 = 0; w3 <= 100 - w1 - w2; w3 += incremento) {
                        int w4 = 100 - w1 - w2 - w3;

                        boolean[] options = {true, false};
                        for (boolean ignoreTrivialSimilarities : options) {
                            // Call to Phoenix with the following parameters, generating the results
                            // a_v1 x a_v2, a_v1 x b_v2
                            // where a_v1 x a_v2 is the comparison of the same element "a" in each version "v1" and "v2"
                            // and a_v1 x b_v2 is the comparisons of different elements "a" and "b" in each version "v1" and "v2"
                            // both results are booleans, where true indicates a match and false otherwise
                            sb.append(w1).append("\t").append(w2).append("\t");
                            sb.append(w3).append("\t").append(w4).append("\t");
                            sb.append(ignoreTrivialSimilarities).append("\t");
                            
                            String[] command = new String[]{
                                "java","-jar",
                                "/Users/matheus/Dropbox/Desenvolvimento/NetBeans Workspace/Processors/XChangeProcessor/dist/XChangeProcessor.jar", // XChange Processor jar
                                "/Users/matheus/Desktop/Alessandreia/", // Path
                                "true", //useSimilarity
                                "1", // argIndex
                                "0.74", //argThreshhold1
                                "0.74", //argThreshhold2
                                Double.toString(w1/(double)100.0), //Name weight
                                Double.toString(w2/(double)100.0), //Attribute Weight
                                Double.toString(w3/(double)100.0), //Value weight
                                Double.toString(w4/(double)100.0), //Children Weight
                                Boolean.toString(ignoreTrivialSimilarities),  //ignoreTrivial
                                "false" //automaticAllocation
                            };
                            Process ps = Runtime.getRuntime().exec(command);
                            ps.waitFor();

                            java.io.InputStream is = ps.getInputStream();
                            byte b[]=new byte[is.available()];
                            is.read(b,0,b.length);
                            System.out.println(new String(b));

                            String txt = readFile("/Users/matheus/Desktop/Alessandreia/v1_-_-_v2/0.740.txt");
                            if(txt.contains("aaron_pat")) {
                                sb.append("true\t");
                            } else {
                                sb.append("false\t");
                            }
                            
                            if(txt.contains("antczak_ashley g")) {
                                sb.append("true\t");
                            } else {
                                sb.append("false\t");
                            }
                            
                            sb.append("\n");
                        
                        }
                    }
                }
            }
            
            System.out.println(sb.toString());
            
            System.out.println("Fim!");
        } catch(IOException | InterruptedException e) {
            System.out.println("ERRO!");
        }
    }
    
}
