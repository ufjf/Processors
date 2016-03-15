package helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Carlos
 */
public class ConverteSaidaTXT {
    
    private Document doc;
    private ArrayList<String> casamentoCorreto;
    private ArrayList<String> casamentoErrado;
    private ArrayList<String> outros;
    private long tempoInicio;
    private long tempoFinal;
    private long tempoGraoGrosso;
    private long tempoGraoFino;
    
    public ConverteSaidaTXT(Document doc){
        this.doc = doc;
        this.casamentoCorreto = new ArrayList<>();
        this.casamentoErrado = new ArrayList<>();
        this.outros = new ArrayList<>();
    }
    
    public void obtemEMPNO(String chave, String nomeArquivo, String eachElementName, int qtdTags, long tempoCorrente) {
        this.tempoGraoFino = tempoCorrente;
        this.tempoGraoGrosso = tempoCorrente;
        
        this.tempoInicio = System.currentTimeMillis();
        NodeList geral = this.doc.getElementsByTagName(eachElementName);
        for (int elem = 0; elem < geral.getLength(); elem++) {
            Node node = geral.item(elem);
            if (node.getNodeType() == 1) {
                Element eElement = (Element) node;
                //System.out.println(eElement.getElementsByTagName(chave).item(0).getLastChild().getTextContent());
                if (("DELETE".equalsIgnoreCase(eElement.getFirstChild().getNodeName()))
                        || ("INSERT".equalsIgnoreCase(eElement.getFirstChild().getNodeName()))) {
                    this.outros.add(eElement.getElementsByTagName(chave).item(0).getTextContent());
                } else if (!"UPDATE".equals(eElement.getElementsByTagName(chave).item(0).getLastChild().getNodeName())) {
                    this.casamentoCorreto.add(eElement.getElementsByTagName(chave).item(0).getTextContent());
                } else if ("UPDATE".equals(eElement.getElementsByTagName(chave).item(0).getLastChild().getNodeName())) {
                    this.casamentoErrado.add(eElement.getElementsByTagName(chave).item(0).getTextContent());
                }
            }
        }
        
        tempoFinal = System.currentTimeMillis() - tempoInicio;
        tempoGraoFino += tempoFinal;
        tempoGraoGrosso += tempoFinal;
        
        saveGabaritoTXT(nomeArquivo, qtdTags);
        saveTXTCompleto(nomeArquivo);
    }
    
    private void saveGabaritoTXT(String nome, int qtdTags){
        tempoInicio = System.currentTimeMillis();
        File salvar = new File(nome+".txt");
        File salvarGraoFino = new File(nome+"_GraoFino"+".txt");
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(salvar));
            BufferedWriter bwGraoFino = new BufferedWriter(new FileWriter(salvarGraoFino));
                        
            if(casamentoCorreto.size() > 0){
                
                bwGraoFino.write(casamentoCorreto.size()*qtdTags+" Casamentos");
                tempoFinal = System.currentTimeMillis() - tempoInicio;
                tempoGraoFino += tempoFinal;
                
                for(int i=0; i < (casamentoCorreto.size());i++){ //Salva ate o penultimo elemento
                    bw.write("'"+casamentoCorreto.get(i)+"'");
                    bw.newLine();
                }
                tempoFinal = System.currentTimeMillis() - tempoInicio;
                tempoGraoGrosso += tempoFinal;
            }else{
                bwGraoFino.write("0 Casamentos");
                tempoFinal = System.currentTimeMillis() - tempoInicio;
                tempoGraoFino += tempoFinal;
            }
            tempoInicio = System.currentTimeMillis();
            bw.newLine();
            if(casamentoErrado.size() > 0){
                for(int i=0; i < (casamentoErrado.size()-1);i++){ //Salva ate o penultimo elemento
                    bw.write("'"+casamentoErrado.get(i)+"'");
                    bw.newLine();
                }
                bw.write("'"+casamentoErrado.get(casamentoErrado.size()-1)+"'");
            }
            bw.close();
            tempoFinal = System.currentTimeMillis() - tempoInicio;
            tempoGraoGrosso += tempoFinal;
            
            tempoInicio = System.currentTimeMillis();
            bwGraoFino.close();
            tempoFinal = System.currentTimeMillis() - tempoInicio;
            tempoGraoFino += tempoFinal;
            
            System.out.println("Arquivo Gabarito Convertido com Sucesso!");
            System.out.println("Arquivo Grão Fino Gerado com Sucesso!");
            System.out.println("\nTempo Grão Fino: " + 
                    String.format( "%02d:%02d:%02d", tempoGraoFino / 3600000, ( tempoGraoFino/ 60000 ) % 60 ,( tempoGraoFino / 1000 ) % 60 ) +
                    " - Tempo em Millisegundo: " + tempoGraoFino);
            System.out.println("Tempo Grão Grosso: " + 
                    String.format( "%02d:%02d:%02d", tempoGraoGrosso / 3600000, ( tempoGraoGrosso/ 60000 ) % 60 ,( tempoGraoGrosso / 1000 ) % 60 ) +
                    " - Tempo em Millisegundo: " + tempoGraoGrosso);
        } catch (IOException ex) {
            Logger.getLogger(ConverteSaidaTXT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveTXTCompleto(String nomeArquivo){
        File salvar = new File(nomeArquivo+"XDiff-COMPLETO.txt");
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(salvar));
            if(casamentoCorreto.size() > 0){
                for(int i=0; i < (casamentoCorreto.size());i++){ //Salva ate o penultimo elemento
                    bw.write("'"+casamentoCorreto.get(i)+"'");
                    bw.newLine();
                }
            }
            bw.newLine();
            if(casamentoErrado.size() > 0){
                for(int i=0; i < (casamentoErrado.size());i++){ //Salva ate o penultimo elemento
                    bw.write("'"+casamentoErrado.get(i)+"'");
                    bw.newLine();
                }
            }
            bw.newLine();
            if(outros.size() > 0){
                for(int i=0; i < (outros.size()-1);i++){ //Salva ate o penultimo elemento
                    bw.write("'"+outros.get(i)+"'");
                    bw.newLine();
                }
                bw.write("'"+outros.get(outros.size()-1)+"'");
            }
            bw.close();
            System.out.println("Arquivo Completo Convertido com Sucesso!");
        } catch (IOException ex) {
            Logger.getLogger(ConverteSaidaTXT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
