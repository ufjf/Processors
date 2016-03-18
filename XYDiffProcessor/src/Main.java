import convertersaidaxydiff.ConverteSaidaTXT;
import convertersaidaxydiff.EditaNovoResultado;
import convertersaidaxydiff.MatrizV1V2;
import fr.loria.ecoo.so6.xml.xydiff.DeltaConstructor;
import fr.loria.ecoo.so6.xml.xydiff.XyDiff;
import helpers.NaturalOrderComparator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

    /**
     * Execution Config
     */
    private static int startIndex = 1;
    private static int endIndex;
    private static int parIndex;

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
                
                File xyDiffOutput = new File(saveLocation.getAbsolutePath() + separator + "XYDiffOutput.xml");
                File xyDiffTime = new File(saveLocation.getAbsolutePath() + separator + "XYDiff-time.txt");
                StringBuilder timeFile = new StringBuilder();
                  
                System.out.println("Executando XYDiff");
                Long starTime = System.currentTimeMillis();

                //XYDiff
                XyDiff xydiff = new XyDiff(v1.getAbsolutePath(), v2.getAbsolutePath());
            
                DeltaConstructor c = xydiff.diff();
                fr.loria.ecoo.so6.xml.node.Document delta = c.getDeltaDocument();
                delta.save(xyDiffOutput.getAbsolutePath(), false);
                
                //XYDiffTime
                timeFile.append((System.currentTimeMillis() - starTime)).append("\n");

                //Conversor
                
                String convertXY = saveLocation.getAbsolutePath() + separator + "resultadoModificadoXyDiff.xml";
                String resultadoNovo = saveLocation.getAbsolutePath() + separator + "resultNovo.xml";
                String graoGrosso = saveLocation.getAbsolutePath() + separator + "CasamentoGraoGrossoXyDiff.txt";
                String graoFino = saveLocation.getAbsolutePath() + separator + "CasamentoGraoFinoXyDiff.txt";

                Document v1Doc = readXML(v1);
                Document v2Doc = readXML(v2);
                int posChave = obterPosicaoChaveSelecionada(v1Doc);
                // ---------- Parte do Matriz
                MatrizV1V2 matrizV1V2 = new MatrizV1V2(v1Doc, v2Doc,posChave, convertXY);
                matrizV1V2.prencheMatriz();
                EditaNovoResultado editaNovoResultado = new EditaNovoResultado(matrizV1V2.getNewResult(), matrizV1V2.getMatriz(), resultadoNovo);

                // ---------- Parte do Conversor
                ConverteSaidaTXT modifyXML = new ConverteSaidaTXT(readXML(xyDiffOutput), editaNovoResultado, v2Doc, graoGrosso, graoFino);
                modifyXML.obtemUpdateOuMove(contextKey, posChave, tagsQuant, matrizV1V2.getPercenteV1());


                //TotalTime
                long totalTime = System.currentTimeMillis() - starTime;
                timeFile.append(totalTime);

                BufferedWriter bw = new BufferedWriter(new FileWriter(xyDiffTime));
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
            doc = (Document) dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return doc; 
    }
    
    public static int obterPosicaoChaveSelecionada(Document doc){
        int posChaveSelecionada = 0;
        NodeList geral = doc.getElementsByTagName(doc.getDocumentElement().getChildNodes().item(1).getNodeName());
        Node node = geral.item(0);
        int pos=1;
        for(int i=0; i< node.getChildNodes().getLength(); i++){
            if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node.getChildNodes();
                if(eElement.getChildNodes().item(i).getNodeName().equals(contextKey))
                    posChaveSelecionada = pos;
                pos += 2;
            }
        }
        
        return posChaveSelecionada;
    }
    
    public static String getPathName(File file1, File file2) {
        return file1.getName().substring(0, file1.getName().lastIndexOf(".")) + "__-__" + file2.getName().substring(0, file2.getName().lastIndexOf(".")) + "-XYDiff";
    }

}
