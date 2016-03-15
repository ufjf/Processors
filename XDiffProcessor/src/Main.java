import helpers.ConverteSaidaTXT;
import helpers.NaturalOrderComparator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class Main {

    /**
     * Execution Config
     */
    private static int startIndex = 1;
    private static int endIndex;
    private static int parIndex;

    private static String eachElementName = "employee";
    private static String contextKey = "name";
    private static int tagsQuant = 7;
    
    private static String documentsPath = "c:/Users/alessandreia/Documents/Baltimore/";

    
    public static NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();

    public static void main(String[] args) {
        
        if  (args.length < 1) {
            args = new String[]{
                "f0", // fragment
                "0", //index
            };
            
        } 
        documentsPath += args[0] + "/"; 
        parIndex = Integer.parseInt(args[1]);
        
        XDiff.setNO_MATCH_THRESHOLD(0.3);
             
        try {
            // Reading Docs
            System.out.println("Procurando Documentos XML...");

            File docsDirectory = new File(documentsPath);
            if(!docsDirectory.isDirectory())
                throw new Exception("Diretório de documentos não encontrado!");

            ArrayList<File> documents = new ArrayList<File>();

            for(File doc: docsDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            })){
                System.out.println("Documento encontrado: " + doc.getName());
                documents.add(doc);
            }

            if(documents.size() < 2)
                throw new Exception("Deve haver ao menos 2 documentos XML no diretório");

            //Sorting Documents
            Collections.sort(documents, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return naturalOrderComparator.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());
                }
            });

            System.out.println("\nOrdem de comparação: ");
            for(File doc: documents) {
                System.out.print(" -> "+ doc.getName());
            }
            System.out.print("\n");
            
            if (parIndex == 0) { // run all
                startIndex=1;
                endIndex=documents.size();
            } else { // run only index
                startIndex=parIndex;
                endIndex=parIndex+1;
            }

            // Executing
            System.out.println("\n==============================================");
            System.out.println("           Iniciando Execuções");
            System.out.println("==============================================\n\n");

            for(int i = startIndex; i < endIndex; i++){
                File v1 = documents.get(i-1);
                File v2 = documents.get(i);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                System.out.println(sdf.format(new Date()));
                System.out.println(v1.getName() + " -> " + v2.getName());
                
                String separator = System.getProperty("file.separator");
                File saveLocation = new File(docsDirectory.getAbsolutePath() + separator + getPathName(v1, v2));
                if(!saveLocation.isDirectory()) {
                    if(!saveLocation.mkdir())
                        throw new Exception("Impossivel criar diretório de resultados");
                }
                
                File xDiffOutput = new File(saveLocation.getAbsolutePath() + separator + "XDiffOutput.xml");
                File xDiffTime = new File(saveLocation.getAbsolutePath() + separator + "XDiff-time.txt");
                StringBuilder timeFile = new StringBuilder();

                String resultOutputSuffix = saveLocation.getAbsolutePath() + separator + "resultadoConvertido";
                  
                Long starTime = System.currentTimeMillis();

                XDiff xDiff = new XDiff(v1.getAbsolutePath(), v2.getAbsolutePath(), xDiffOutput.getAbsolutePath());
                
                //XDiffTime
                timeFile.append((System.currentTimeMillis() - starTime)).append("\n");
                
                ConverteSaidaTXT modifyXML = new ConverteSaidaTXT(readXML(xDiffOutput));
                modifyXML.obtemEMPNO(contextKey, resultOutputSuffix, eachElementName, tagsQuant, 0);
                
                //TotalTime
                long totalTime = System.currentTimeMillis() - starTime;
                timeFile.append(totalTime);

                BufferedWriter bw = new BufferedWriter(new FileWriter(xDiffTime));
                bw.write(timeFile.toString());
                bw.close();
                    
                System.out.println("Tempo de execução: "+ totalTime/1000.0 +" segundos\n");
                System.gc();
            }

            System.out.println("\n==============================================");
            System.out.println("                     Fim");
            System.out.println("==============================================\n\n");

        } catch (Exception e) {
            System.out.println("Erro encontrado! Encerrando...");
            e.printStackTrace();
        }
    }

    public static Document readXML(File fXmlFile){
        Document doc = null;
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return doc;
    }
    
    public static String getPathName(File file1, File file2) {
        return file1.getName().substring(0, file1.getName().lastIndexOf(".")) + "_--_" + file2.getName().substring(0, file2.getName().lastIndexOf(".")) + "-XDiff";
    }

}
