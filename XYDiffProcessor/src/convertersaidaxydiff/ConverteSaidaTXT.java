package convertersaidaxydiff;

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
    private ArrayList<String> casamentoCorretoNaoAparece;
    //private ArrayList<String> casamentoCorretoDeleted; //Casou nas tags Deleted "move"
    //private ArrayList<String> casamentoCorretoInserted; //Casou nas tags Inserted "move"
    //private ArrayList<String> casamentoErradoDeleted; //Casou Errado nas tags Deleted "update"
    //private ArrayList<String> casamentoErradoInserted; //Casou Errado nas tags Inserted "update"
    //private ArrayList<String> outros; //Não casaram
    
    private ArrayList<NodeInfo> arrayDeletedMove;
    private ArrayList<NodeInfo> arrayInsertedMove;
    
    private EditaNovoResultado editaNovoResultado;
    private ArrayList<String> casamentoCorreto;
    private ArrayList<String> casamentoErrado;
    private ArrayList<String> naoCasou;
    
    private Document v2Document;
    private String graoGrossoPath;
    private String graoFinoPath;
    
    public ConverteSaidaTXT(Document doc, EditaNovoResultado editaNovoResultado, Document v2Doc, String graoGrosso, String graoFino){
        this.doc = doc;
        this.casamentoCorretoNaoAparece = new ArrayList<>();
//        this.casamentoCorretoDeleted = new ArrayList<>();
//        this.casamentoCorretoInserted = new ArrayList<>();
//        this.casamentoErradoDeleted = new ArrayList<>();
//        this.casamentoErradoInserted = new ArrayList<>();
//        this.outros = new ArrayList<>();
        
        this.arrayDeletedMove = new ArrayList<>();
        this.arrayInsertedMove = new ArrayList<>();
        
        this.editaNovoResultado = editaNovoResultado;
        this.casamentoCorreto = new ArrayList<>();
        this.casamentoErrado = new ArrayList<>();
        this.naoCasou = new ArrayList<>();
        
        this.v2Document = v2Doc;
        this.graoGrossoPath = graoGrosso;
        this.graoFinoPath = graoFino;
                
    }
    
    public void obtemUpdateOuMove(String chave, int posChave, int qtdTags, ArrayList<String> percenteV1){
        //Percorre nas Tags "Deleted"
        int contLine=0;
        NodeList geralDeleted = this.doc.getElementsByTagName("Deleted");
        for(int elem =0; elem < geralDeleted.getLength(); elem ++){
            Node node = geralDeleted.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(eElement.getAttributes().getLength()==2){ //Tem "move" ou "update"
                    String pos[] = eElement.getAttribute("pos").split(":");
                    if(pos.length>=4){
                        if(eElement.getAttribute("update").equalsIgnoreCase("yes")){ //Casou Certo
                            //arrayDeletedMove.add(new NodeInfo(eElement.getAttribute("pos"), eElement.getTextContent()));
                            int posLinhaMatriz = Integer.parseInt(pos[2])/2;
                            int posTag = Integer.parseInt(pos[3])/2;
                            String valorInserted = obtemInsertedUpdate(posLinhaMatriz, posTag);

                            //System.out.println(eElement.getTextContent());
                            editaNovoResultado.editNewResultado(posLinhaMatriz, posTag, eElement.getTextContent(), valorInserted);   
                        }else if(eElement.getAttribute("move").equalsIgnoreCase("yes") && String.valueOf(posChave).equals(pos[3])){
                            String moveChave = eElement.getTextContent();
                            int posLinhaDeleted = Integer.parseInt(pos[2]);
                            int posLinhaInserted = obtemMoveInserted(moveChave,posChave);
                            if(posLinhaInserted != -100000){ // se for igual a -100000 quer dizer que ele foi deletado e não existe mais em V2
                                CompararMove compararMove = new CompararMove(moveChave, contLine, posLinhaDeleted, posLinhaInserted, this.doc, editaNovoResultado, qtdTags, posChave, this.v2Document);                            
                                compararMove.insertMoveInNewResult(posLinhaInserted);
                            }
                        }
                    }
                }
            }
            contLine++;
            //System.out.println(node.getTextContent()+"     "+contLine);
        }
        editaNovoResultado.saveNewResultado();
        obtemCasamento(posChave, percenteV1);
        //obtemInsertedMove(chave, posChave);
    }
    
    public int obtemMoveInserted(String moveChave, int posChave){
        //Percorre nas Tags "Inserted"
        NodeList geralDeleted = this.doc.getElementsByTagName("Inserted");
        for(int elem =0; elem < geralDeleted.getLength(); elem ++){
            Node node = geralDeleted.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(eElement.getAttributes().getLength()==2){ //Tem "move" ou "update"
                    String pos[] = eElement.getAttribute("pos").split(":");
                    if(pos.length>=4){
                            if(eElement.getAttribute("move").equalsIgnoreCase("yes") && String.valueOf(posChave).equals(pos[3])){
                                if(eElement.getTextContent().equals(moveChave))
                                    return Integer.parseInt(pos[2]);
                            }
                    }
                }
            }
        }
        return -100000;
    }
    
    public String obtemInsertedUpdate(int posLinhaMatriz, int posTag){
        //Percorre nas Tags "Inserted"
        NodeList geralDeleted = this.doc.getElementsByTagName("Inserted");
        for(int elem =0; elem < geralDeleted.getLength(); elem ++){
            Node node = geralDeleted.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(eElement.getAttributes().getLength()==2){ //Tem "move" ou "update"
                    String pos[] = eElement.getAttribute("pos").split(":");
                    if(pos.length>=4){
                            if(eElement.getAttribute("update").equalsIgnoreCase("yes")){ //Casou Certo
                                if(Integer.parseInt(pos[2])/2 == posLinhaMatriz && Integer.parseInt(pos[3])/2 == posTag)
                                    return eElement.getTextContent();
                            }
                    }
                }
            }
        }
        return "";
    }
    
    public int contSomenteTagInserted(){
        int cont=0;
        //Percorre nas Tags "Inserted"
        NodeList geralDeleted = this.doc.getElementsByTagName("Inserted");
        for(int elem =0; elem < geralDeleted.getLength(); elem ++){
            Node node = geralDeleted.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(eElement.getAttributes().getLength()==1){ //Tem "inserted"
                    if(!eElement.getFirstChild().getTextContent().replaceAll("\n", "").replaceAll("\t", "").equals("") &&
                                    eElement.getFirstChild().getChildNodes().getLength()!=0){
                        for(int i=0; i<eElement.getFirstChild().getChildNodes().getLength();i++){
                            if(!eElement.getFirstChild().getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").equals(""))
                                cont++;
                                //System.out.println("++ "+eElement.getFirstChild().getChildNodes().item(i).getTextContent());
                        }
                    }
                }
            }
        }
        return cont;
    }
    
    private void obtemCasamento(int posChave, ArrayList<String> pertenceV1){
        NodeList geral = this.editaNovoResultado.getNewResultado().getDoc().getElementsByTagName(editaNovoResultado.getNewResultado().getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        for (int elem = 0; elem < geral.getLength(); elem++) {
            Node node = geral.item(elem);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                int i=0;
                while(i<eElement.getChildNodes().getLength()){
                    if(!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")
                        && eElement.getChildNodes().item(i).getChildNodes().getLength()>=2){
                        if(eElement.getChildNodes().item(i).getChildNodes().item(1).getNodeName().equals("UPDATEINCORRETO") ||
                                eElement.getChildNodes().item(i).getChildNodes().item(1).getNodeName().equals("MOVEFALTANDO") ||
                                eElement.getChildNodes().item(i).getChildNodes().item(1).getNodeName().equals("MOVEINCORRETO")){
                            
                            if(eElement.getChildNodes().item(posChave).getChildNodes().getLength() > 1) {
                                casamentoErrado.add(eElement.getChildNodes().item(posChave).getChildNodes().item(1).getAttributes().item(0).getTextContent());
                            } else
                                casamentoErrado.add(eElement.getChildNodes().item(posChave).getTextContent());
                            
                            break;
                        }
                    }
                    i++;
                    if(i==eElement.getChildNodes().getLength() && editaNovoResultado.existeChaveNaMatriz(posChave, eElement.getChildNodes().item(posChave).getTextContent()))
                        casamentoCorreto.add(eElement.getChildNodes().item(posChave).getTextContent());
                }
            }
        }
        saveGabaritoTXT(pertenceV1, posChave, contGraoFino());
    }
    
    private int contGraoFino(){
        NodeList geral = this.editaNovoResultado.getNewResultado().getDoc().getElementsByTagName(editaNovoResultado.getNewResultado().getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        int cont=0;
        for (int elem = 0; elem < geral.getLength(); elem++) {
            Node node = geral.item(elem);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                
                int i=0;
                
                while(i<eElement.getChildNodes().getLength()){
                    if(!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")
                        && eElement.getChildNodes().item(i).getChildNodes().getLength()< 2){
                        //System.out.println(eElement.getChildNodes().item(i).getTextContent());
                        cont++;
                    }
                    i++;
                }
            }
        }
        return cont;
    }
    

    
//    private void obtemInsertedMove(String chave, int posChave){
//        //Percorre nas Tags "Inserted"
//        NodeList geralInserted = this.doc.getElementsByTagName("Inserted");
//        for(int elem =0; elem < geralInserted.getLength(); elem ++){
//            Node node = geralInserted.item(elem);
//                
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                Element eElement = (Element) node;
//                
//                if(eElement.getAttributes().getLength()==2){ //Tem "move" ou "update"
//                    String pos[] = eElement.getAttribute("pos").split(":");
//                    if(pos.length>=4){
//                        //if(String.valueOf(posChave).equals(pos[3])){
//                            if(eElement.getAttribute("move").equalsIgnoreCase("yes")) //Casou Certo
//                                arrayInsertedMove.add(new NodeInfo(eElement.getAttribute("pos"), eElement.getTextContent()));
//                        //}
//                    }
////                    else if(pos.length==3){
////                        try{
////                        if(!eElement.getFirstChild().getTextContent().replaceAll("\n", "").replaceAll("\t", "").equals(""))
////                            casamentoCorretoInserted.add(eElement.getElementsByTagName(chave).item(0).getTextContent());
////                        }catch(Exception e){
////                            System.out.println("Inserted: "+eElement.getAttribute("pos"));
////                        }
////                    }
//                }
//            }
//        }
//        this.setPossuiChaveNodeInfo(arrayDeletedMove, posChave);
//        this.setPossuiChaveNodeInfo(arrayInsertedMove, posChave);
//        
//        this.imprimeVetores();
//        this.compara();
//    }
    
    /**
     * Percorre o array tentando encontrar a chave
     *      Ao encontrar percorre o array novamente setando "Possui Chave" como "true"
     *      para os elementos que estão no mesmo nivel que a chave
     * 
     *                       pos     -    chave     - Possui chave 
     *                     0:0:1:1   -    10723     -     false
     *                     0:0:1:7   -   20050901   -     false
     *  Chave = 4ª posição é 1
     *                     0:0:1:1   -    10723     -     true (quarta posição é 1, sua terceira posição é 1)
     *  Percorre novamente o array olhando os outros atributos onde a terceira casa é igual a 1 (veio do mesmo elemento)
     *                      0:0:1:7   -   20050901   -     true
     * @param array
     * @param posChave 
     */
//    private void setPossuiChaveNodeInfo(ArrayList<NodeInfo> array ,int posChave){
//        for(int i=0; i<array.size();i++){
//            String pos[] = array.get(i).getPos().split(":");
//            if(String.valueOf(posChave).equals(pos[3])){ //Verifica quarta casa - nivel de tags
//                array.get(i).setPossuiChave(true);
//                for(int j=0; j<array.size();j++){
//                    String pos2[] = array.get(j).getPos().split(":");
//                    if(pos[2].equals(pos2[2])) //Verifica terceira casa - nivel posicao no xml
//                        array.get(j).setPossuiChave(true);
//                }
//            }
//        }
//    }
    
//    private void compara(){
//        int cont=0;
//        for(int i=0; i<arrayDeletedMove.size(); i++){
//            for(int j=0; j<arrayInsertedMove.size(); j++){
//                if(!(arrayDeletedMove.get(i).getPos().equals(arrayInsertedMove.get(j).getPos()))&&
//                        arrayDeletedMove.get(i).getValue().equals(arrayInsertedMove.get(j).getValue())&&
//                        arrayDeletedMove.get(i).possuiChave() && arrayInsertedMove.get(i).possuiChave())
//                    cont++;
//            }
//        }
//        System.out.println("\nExistem "+cont+" tags que casaram corretamente");
//    }
    
    private void imprimeVetores(){
        System.out.println();
        for(int i=0; i< arrayDeletedMove.size();i++){
            System.out.println("Pos: "+arrayDeletedMove.get(i).getPos() + " -- Value: "+arrayDeletedMove.get(i).getValue()
                            + " -- Possui chave: "+ arrayDeletedMove.get(i).possuiChave());
        }
        System.out.println("-------------------------------------------------------");
        for(int i=0; i< arrayInsertedMove.size();i++){
            System.out.println("Pos: "+arrayInsertedMove.get(i).getPos() + " -- Value: "+arrayInsertedMove.get(i).getValue()
                            + " -- Possui chave: "+ arrayInsertedMove.get(i).possuiChave());
        }
    }
    
    private void obtemEmpnoNaoAparecem(int posChave){
        DocumentoXML documentoXML = new DocumentoXML();
        
        documentoXML.setDoc(v2Document);
                
        NodeList geral = documentoXML.getDoc().getElementsByTagName(documentoXML.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        for(int elem =0; elem < geral.getLength(); elem ++){
            Node node = geral.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                
                String empno = eElement.getChildNodes().item(posChave).getTextContent(); //1 é empno
                
                // Verifica se existe o empno em algum ArrayList já existente
                if((!casamentoCorreto.contains(empno))&&(!casamentoErrado.contains(empno)))
                            this.casamentoCorretoNaoAparece.add(empno); // Se não existe adiciona no ArrayCasamentoCorretoNaoAparece
                    
                
            }
        }
    }
    
    /**
     * Na inserção dos elementos nos vetores, pode acontecer de um elemento
     * estar em dois vetores (CasamentoCorretoDeleted e em CasamentoErradoDeleted caso i1 PaloAlto 5389)
     * Se o cara tiver no casamento errado tirar ele do casamento correto
     */
//    private void atualizaVetores(){        
//        for(int i=0; i<casamentoCorretoDeleted.size();i++){
//            if(!casamentoCorretoInserted.contains(casamentoCorretoDeleted.get(i))){
//                casamentoCorretoDeleted.remove(i);
//                i--;
//            }
//        }
//        
//        for(int i=0; i<casamentoCorretoInserted.size(); i++){
//            if(!casamentoCorretoDeleted.contains(casamentoCorretoInserted.get(i))){
//                casamentoCorretoInserted.remove(i);
//                i--;
//            }
//        }
//        
//        for(int i=0; i<casamentoErradoDeleted.size() ;i++){
//            for(int j=0; j<casamentoCorretoDeleted.size() ;j++){
//                if(casamentoCorretoDeleted.get(j).equals(casamentoErradoDeleted.get(i))){
//                    casamentoCorretoDeleted.remove(j);
//                    j--;
//                }
//            }
//        }
//        
//        for(int i=0; i<casamentoErradoInserted.size() ;i++){
//            for(int j=0; j<casamentoCorretoInserted.size() ;j++){
//                if(casamentoCorretoInserted.get(j).equals(casamentoErradoInserted.get(i))){
//                    casamentoCorretoInserted.remove(j);
//                    j--;
//                }
//            }
//        }
//        
//        for(int i=0; i<casamentoCorretoDeleted.size() ;i++){
//            for(int j=0; j<casamentoCorretoInserted.size() ;j++){
//                if(casamentoCorretoInserted.get(j).equals(casamentoCorretoDeleted.get(i))){
//                    casamentoCorretoInserted.remove(j);
//                    j--;
//                }
//            }
//        }
//        
//        for(int i=0; i<outros.size() ;i++){
//            for(int j=0; j<casamentoCorretoDeleted.size() ;j++){
//                if(casamentoCorretoDeleted.get(j).equals(outros.get(i))){
//                    casamentoCorretoDeleted.remove(j);
//                    j--;
//                }
//            }
//        }
//        
//        for(int i=0; i<outros.size() ;i++){
//            for(int j=0; j<casamentoCorretoInserted.size() ;j++){
//                if(casamentoCorretoInserted.get(j).equals(outros.get(i))){
//                    casamentoCorretoInserted.remove(j);
//                    j--;
//                }
//            }
//        }
//        
//    }
    
    private void presentesSoEmV2(ArrayList<String> pertencenteV1){
        for(int i=0; i<casamentoCorreto.size(); i++){
            if(!(pertencenteV1.contains(casamentoCorreto.get(i)))){
                naoCasou.add(casamentoCorreto.get(i));
                casamentoCorreto.remove(i);
                i--;
            }
        }
        for(int i=0; i<casamentoCorretoNaoAparece.size(); i++){
            if(!(pertencenteV1.contains(casamentoCorretoNaoAparece.get(i)))){
                naoCasou.add(casamentoCorretoNaoAparece.get(i));
                casamentoCorretoNaoAparece.remove(i);
                i--;
            }
        }
    }
    
    private void saveGabaritoTXT(ArrayList<String> pertenceV1, int posChave, int graoFino){
        File salvar = new File(this.graoGrossoPath);
        File salvarGraoFino = new File(this.graoFinoPath);
        obtemEmpnoNaoAparecem(posChave);
        presentesSoEmV2(pertenceV1);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(salvar));
            BufferedWriter bwGraoFino = new BufferedWriter(new FileWriter(salvarGraoFino));
            
            bwGraoFino.write((graoFino-this.contSomenteTagInserted()) + " Casamentos");
            bwGraoFino.close();
            
            bw.write("UPDATE / Casamento Correto");
            bw.newLine();
            if(casamentoCorreto.size() > 0){
                for(int i=0; i < casamentoCorreto.size();i++){
                    bw.write(casamentoCorreto.get(i));
                    bw.newLine();
                }
            }
            if(casamentoCorretoNaoAparece.size() >0){
                for(int i=0; i < casamentoCorretoNaoAparece.size();i++){
                    bw.write(casamentoCorretoNaoAparece.get(i));
                    bw.newLine();
                }
            }
            
            bw.newLine();
            bw.write("UPDATE ERRADO / Casamento Errado");
            bw.newLine();
            if(casamentoErrado.size() > 0){
                for(int i=0; i < casamentoErrado.size();i++){
                    bw.write(casamentoErrado.get(i));
                    bw.newLine();
                }
            }
            
            /*
            REMOVIDO POIS NAO EH UTILIZADO, DEU RESULTADO ERRADO APOS COLOCAR AS CHAVES DE V1 NO CASAMENTO ERRADO (ANTES USAVA V2)
            bw.newLine();
            bw.write("Não Casou");
            bw.newLine();
            if(naoCasou.size() > 0){
                for(int i=0; i < naoCasou.size();i++){
                    bw.write(naoCasou.get(i));
                    bw.newLine();
                }
            }
            */
            bw.close();
            System.out.println("CasamentoGraoFino.txt gerado com Sucesso!");
            System.out.println("CasamentoGraoGrosso.txt gerado com Sucesso!");
        } catch (IOException ex) {
            Logger.getLogger(ConverteSaidaTXT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    private void saveTXTCompleto(){
//        File salvar = new File("XyDiff-COMPLETO.txt");
//        
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(salvar));
//            bw.write("Casamento Correto Deleted:");
//            bw.newLine();
//            if(casamentoCorretoDeleted.size() > 0){
//                for(int i=0; i < (casamentoCorretoDeleted.size());i++){ //Salva ate o penultimo elemento
//                    bw.write(casamentoCorretoDeleted.get(i));
//                    bw.newLine();
//                }
//            }
//            bw.newLine();
//            bw.write("Casamento Correto Inserted:");
//            bw.newLine();
//            if(casamentoCorretoInserted.size() > 0){
//                for(int i=0; i < casamentoCorretoInserted.size(); i++){
//                    bw.write(casamentoCorretoInserted.get(i));
//                    bw.newLine();
//                }
//            }
//            bw.newLine();
//            bw.write("Casamento Correto que não aparece:");
//            bw.newLine();
//            if(casamentoCorretoNaoAparece.size() > 0){
//                for(int i=0; i < casamentoCorretoNaoAparece.size(); i++){
//                    bw.write(casamentoCorretoNaoAparece.get(i));
//                    bw.newLine();
//                }
//            }
//            
//            bw.newLine();
//            bw.write("---------------------------------------");
//            bw.newLine();
//            bw.write("Casamento Errado Deleted:");
//            bw.newLine();
//            if(casamentoErradoDeleted.size() > 0){
//                for(int i=0; i < (casamentoErradoDeleted.size());i++){ //Salva ate o penultimo elemento
//                    bw.write(casamentoErradoDeleted.get(i));
//                    bw.newLine();
//                }
//            }
//            bw.newLine();
//            bw.write("Casamento Errado Inserted:");
//            bw.newLine();
//            if(casamentoErradoInserted.size() > 0){
//                for(int i=0; i < (casamentoErradoInserted.size());i++){ //Salva ate o penultimo elemento
//                    bw.write(casamentoErradoInserted.get(i));
//                    bw.newLine();
//                }
//            }
//            
//            bw.newLine();
//            bw.write("Outros - Não Casaram:");
//            bw.newLine();
//            if(outros.size() > 0){
//                for(int i=0; i < (outros.size()-1);i++){ //Salva ate o penultimo elemento
//                    bw.write(outros.get(i));
//                    bw.newLine();
//                }
//                bw.write(outros.get(outros.size()-1));
//            }
//            bw.close();
//            System.out.println("Arquivo Completo Convertido com Sucesso!");
//        } catch (IOException ex) {
//            Logger.getLogger(ConverteSaidaTXT.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
