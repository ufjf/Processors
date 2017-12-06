import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;
import br.uff.ic.gems.phoenix.PhoenixDiffCalculator;
import br.uff.ic.gems.phoenix.SettingsHelper;
import br.uff.ic.gems.phoenix.exception.ComparisonException;
import br.uff.ic.gems.phoenix.exception.PhoenixDiffException;
import br.ufrj.ppgi.parser.XMLParser;
import helpers.NaturalOrderComparator;
import helpers.SimilarityTranslate;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    /**
     * Execution Config
     */
//  private static Boolean useSimilarity = true;
    private static int startIndex = 1;
    private static int endIndex;
    private static double startThreshold = 0.0d;
    private static double endThreshold = 1.0d;
    
    private static String eachElementName = "employee";
    
    private static String documentsPath;
    private static String prologContextRulesPath = "prolog.txt";
    private static String prologSimilarityRulesPath = "prologSim.txt"; 
    private static String prologRulesPath;
    
    /**
     *  Phoenix Configs
     */
    private static boolean  ignoreTrivial = true,
                            automaticAllocation = true,
                            ignoreThresholdOnRoot = true,
                            allowDataTypeSimilarity = false;

    public static double NAME_WEIGHT_HARD_DEFAULT = 0.25;
    public static double ATTRIBUTE__WEIGHT_HARD_DEFAULT = 0.25;
    public static double VALUE_WEIGHT_HARD_DEFAULT = 0.25;
    public static double CHILDREN_WEIGHT_HARD_DEFAULT = 0.25;
    
    private static String dateFormat = "eng";

    public static NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();

    public static void main(String[] args) {

        if  (args.length < 1) {
            args = new String[]{
                "/Users/matheus/Desktop/Alessandreia/", // Path
                "true", //useSimilarity
                "1", // argIndex
                "0.90", //argThreshhold1
                "0.90", //argThreshhold2
                "0.24", //Name weight
                "0.26", //Attribute Weight
                "0.27", //Value weight
                "0.28", //Children Weight
                "true",  //ignoreTrivial
                "true" //automaticAllocation
            };
            
        }
        
        documentsPath = args[0];
        boolean useSimilarity = Boolean.parseBoolean(args[1]);
        int argIndex = Integer.parseInt(args[2]);
        double argThreshold1 = Double.parseDouble(args[3]);
        double argThreshold2;
        if (args[4].isEmpty()) {
            argThreshold2 = argThreshold1;
        } else {
            argThreshold2 = Double.parseDouble(args[4]);
        }
        NAME_WEIGHT_HARD_DEFAULT = Double.parseDouble(args[5]);
        ATTRIBUTE__WEIGHT_HARD_DEFAULT = Double.parseDouble(args[6]);
        VALUE_WEIGHT_HARD_DEFAULT = Double.parseDouble(args[7]);
        CHILDREN_WEIGHT_HARD_DEFAULT = Double.parseDouble(args[8]);
        ignoreTrivial = Boolean.parseBoolean(args[9]);
        automaticAllocation = Boolean.parseBoolean(args[10]);
                        
        if (useSimilarity) {
            prologRulesPath = documentsPath + prologSimilarityRulesPath;
        } else {
            prologRulesPath = documentsPath + prologContextRulesPath;
        }

        SettingsHelper.setIgnoreTrivialSimilarities(ignoreTrivial);
        SettingsHelper.setAutomaticWeightAllocation(automaticAllocation);
        SettingsHelper.setIgnoreThresholdOnRoot(ignoreThresholdOnRoot);
        SettingsHelper.setAllowDataTypeSimilarity(allowDataTypeSimilarity);
        SettingsHelper.setDateFormat(dateFormat);
        SettingsHelper.setNameSimilarityWeight(NAME_WEIGHT_HARD_DEFAULT);
        SettingsHelper.setAttributeSimilarityWeight(ATTRIBUTE__WEIGHT_HARD_DEFAULT);
        SettingsHelper.setValueSimilarityWeight(VALUE_WEIGHT_HARD_DEFAULT);
        SettingsHelper.setChildrenSimilarityWeight(CHILDREN_WEIGHT_HARD_DEFAULT);
        
        System.out.println("Name Weight: "+ SettingsHelper.getNameSimilarityWeight());
        System.out.println("Attribute Weight: "+ SettingsHelper.getAttributeSimilarityWeight());
        System.out.println("Value Weight: "+ SettingsHelper.getValueSimilarityWeight());
        System.out.println("Children Weight: "+ SettingsHelper.getChildrenSimilarityWeight());
        System.out.println("Ignore Trivial: "+ SettingsHelper.getIgnoreTrivialSimilarities());
        System.out.println("Automatic Allocation: "+ SettingsHelper.getAutomaticWeightAllocation());
        System.out.println("\n");

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

            // Reading Rules
            ArrayList<ArrayList<String>> rules = new ArrayList<ArrayList<String>>();
            System.out.println("\nLendo Regras...");

            String rulesFile = readFile(prologRulesPath, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("((.*?)\\((.*?)\\)):-");
            Matcher matcher = pattern.matcher(rulesFile);
            while (matcher.find()) {
                ArrayList<String> newRule = new ArrayList<String>();
                newRule.add(0, matcher.group(1));
                newRule.add(1, matcher.group(2));
                newRule.add(2, matcher.group(3));
                System.out.println("Regra encontrada: " + newRule.get(0));
                rules.add(newRule);
            }

            if (rules.size() == 0)
                throw new Exception("Nenhuma regra encontrada");

            // Executing
            System.out.println("\n==============================================");
            System.out.println("           Iniciando Execuções");
            System.out.println("==============================================\n\n");
            
            if (argIndex == 0 ) { // run all
                startIndex = 1;
                endIndex = documents.size();
                startThreshold = 0.0d;
                endThreshold = 1.0;
            } else { // run especific case
                startIndex= argIndex;
                endIndex=argIndex+1;
                startThreshold= argThreshold1;
                endThreshold= argThreshold2;
            }

            BigDecimal simValue = BigDecimal.valueOf(startThreshold);
            BigDecimal endValue = BigDecimal.valueOf(endThreshold);
            BigDecimal step = BigDecimal.valueOf(0.01);

            if(simValue.doubleValue() < 0.0)
                throw new Exception("Valor inválido de startThreshold");

            if(startIndex < 1 || startIndex >= documents.size())
                throw new Exception("Valor inválido de startIndex");

            for(int i = startIndex; i < endIndex; i++){
                File v1 = documents.get(i-1);
                File v2 = documents.get(i);

                String separator = System.getProperty("file.separator");
                File saveLocation = new File(docsDirectory.getAbsolutePath() + separator + getPathName(v1, v2));
                if(!saveLocation.isDirectory()) {
                    if(!saveLocation.mkdirs())
                        throw new Exception("Impossivel criar diretório de resultados");
                }

                if(simValue.doubleValue() > 1.0)
                    simValue = BigDecimal.valueOf(0);

                while(simValue.doubleValue() <= endValue.doubleValue()){
                    System.out.println("==============================================");
                    System.out.println("Executando: "+ v1.getName()+" - "+ v2.getName()+" (Index: "+i+")");
                    HashMap<String, String> output = new HashMap<String, String>();
                    File saveFile, saveTimeFile;
                    StringBuilder timeFile = new StringBuilder();
                    Long starTime = System.currentTimeMillis();
                    
                    if (useSimilarity) {
                        SettingsHelper.setSimilarityThreshold(simValue.doubleValue());
                        saveFile = new File(saveLocation.getAbsolutePath() + separator + String.format("%.3f", SettingsHelper.getSimilarityThreshold()).replaceAll(",", ".")+".txt");
                        saveTimeFile = new File(saveLocation.getAbsolutePath() + separator + String.format("%.3f", SettingsHelper.getSimilarityThreshold()).replaceAll(",", ".")+"-time.txt");

                        // Phoenix Similarity
                        System.out.println("Calculando Similaridade(Threshold: "+SettingsHelper.getSimilarityThreshold()+")...");
                        String simDiff = doSimilarity(v1.getAbsolutePath(), v2.getAbsolutePath());
                        System.out.println(simDiff);
                        timeFile.append((System.currentTimeMillis() - starTime)).append("\n");
                        
                        // XCHange 2.0 Phoenix Translate Module (Output: document 1: output.get("left") & document 2: output.get("right")
                        System.out.println("Interpretando Similaridade...");
                        SimilarityTranslate similarityTranslate = new SimilarityTranslate();
                        output = similarityTranslate.documentWithIDsTEST(simDiff);

                        simValue = simValue.add(step);
                    } else {
                        // Chave de Contexto
                        saveFile = new File(saveLocation.getAbsolutePath() + separator + "Gabarito.txt");
                        saveTimeFile = new File(saveLocation.getAbsolutePath() + separator + "Gabarito-time.txt");

                        System.out.println("Lendo Documentos XML(Context-Key)...");

                        output.put("left", readFile(v1.getAbsolutePath(), StandardCharsets.UTF_8));
                        output.put("right", readFile(v2.getAbsolutePath(), StandardCharsets.UTF_8));

                        simValue = simValue.add(BigDecimal.valueOf(1.1)); //Só para sair do loop
                    }

                    //Theory String
                    StringBuilder theoryBuilding = new StringBuilder();

                    // XML Parser to Prolog Facts
                    System.out.println("Traduzindo documentos para fatos Prolog...");
                    XMLParser parser = new XMLParser();
                    String replacement;
                    HashMap<String, String> parseResult;

                    // Parsing v1
                    replacement = eachElementName + "\\(1";
                    parseResult = parser.executeParseSax(output.get("left"), 0);
                    theoryBuilding.append(parseResult.get("facts").replaceAll(replacement, eachElementName + "(before"));

                    // Parsing v2
                    Integer lastID = Integer.parseInt(parseResult.get("lasId"));
                    replacement = eachElementName + "\\(" + (lastID + 1);
                    parseResult = parser.executeParseSax(output.get("right"), lastID);
                    theoryBuilding.append(parseResult.get("facts").replaceAll(replacement, eachElementName + "(after")).append("\n");

                    // Appending Prolog Rules
                    theoryBuilding.append(rulesFile);

                    System.out.println("Inferindo Regras...");

                    // Generating Result
                    Prolog engine = new Prolog();
                    engine.setTheory(new Theory(theoryBuilding.toString()));

                    StringBuilder resultString = new StringBuilder();
                    resultString.append("diff(").append(getCode(v1)).append(", ").append(getCode(v2)).append(")\n\n");

                    for(ArrayList<String> rule: rules) {
                        resultString.append(rule.get(1).toUpperCase()).append(":\n");
                        SolveInfo result = engine.solve(rule.get(0)+".");
                        while (true) {
                            if (result.isSuccess())
                                resultString.append(result.getVarValue(rule.get(2))).append("\n");

                            if (engine.hasOpenAlternatives())
                                result = engine.solveNext();
                            else
                                break;
                        }
                        resultString.append("\n");
                        engine.solveEnd();
                    }

                    long totalTime = System.currentTimeMillis() - starTime;
                    timeFile.append(totalTime);
                    
                    BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                    bw.write(resultString.toString());
                    bw.close();
                    
                    bw = new BufferedWriter(new FileWriter(saveTimeFile));
                    bw.write(timeFile.toString());
                    bw.close();

                    System.out.println("Tempo de execução: "+ totalTime/1000.0 +" segundos\n");
                    System.out.println("Resultado Salvo em: "+ saveFile.getAbsolutePath() +"\n");
                    System.gc();
                }
            }

            System.out.println("\n==============================================");
            System.out.println("                     Fim");
            System.out.println("==============================================\n\n");

        } catch (Exception e) {
            System.out.println("Erro encontrado! Encerrando...");
            e.printStackTrace();
        }
    }

    public static String doSimilarity(String xmlfilepath1, String xmlfilepath2) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PhoenixDiffCalculator cmp = new PhoenixDiffCalculator(xmlfilepath1,xmlfilepath2);
            cmp.setOutputStream(baos);
            cmp.executeComparison();
            cmp = null;
        } catch (PhoenixDiffException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ComparisonException ex) {

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return baos.toString();
    }

    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String getCode(File file) {
        Date date = new Date(file.lastModified());
        return file.getName().substring(0, file.getName().lastIndexOf(".")) + " - " + date.toString();
    }

    public static String getPathName(File file1, File file2) {
        return file1.getName().substring(0, file1.getName().lastIndexOf(".")) + "_-_-_" + file2.getName().substring(0, file2.getName().lastIndexOf("."));
    }

}