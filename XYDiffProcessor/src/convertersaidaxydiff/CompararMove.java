package convertersaidaxydiff;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Admin
 */
public class CompararMove {
    
    private Document doc;
    private DocumentoXML v2 = new DocumentoXML();
    private EditaNovoResultado editaNovoResultado;
    
    /**
     * Posicao 0 -> Move que tava no Deleted
     * Posicao 1 -> Move que tava no Inserted (mesma chave do Deleted)
     * Posicao 2 -> Tags de v2 correspondente a chave
     */
    private ArrayList<ArrayList<ArrayList<String>>> matriz = new ArrayList<ArrayList<ArrayList<String>>>();

    public CompararMove(String moveChave, int contLine, int posLinhaDeleted, int posLinhaInserted, Document doc, EditaNovoResultado editaNovoResultado, int qtdTags, int posChave, Document v2Doc){
        this.doc = doc;
        this.editaNovoResultado = editaNovoResultado;
        v2.setDoc(v2Doc);
        this.montaDeleted(contLine, posLinhaDeleted, qtdTags, posChave);
        this.montaInserted(posLinhaInserted);
        this.encontraCorrespondenteV2(posLinhaInserted);
        //this.imprimeMatriz();
        //System.out.println("**************************************************************************");
    }
    
    private void montaDeleted(int contLine, int posLinhaDeleted, int qtdTags, int posChave){
        //Percorre nas Tags "Deleted"
        NodeList geralDeleted = this.doc.getElementsByTagName("Deleted");
        ArrayList<String> mat = new ArrayList<String>();
        
        try{
            for(int elem =(contLine+(posChave-1)); ((contLine+(posChave-1))-qtdTags) < elem; elem--){
                Node node = geralDeleted.item(elem);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;

                    if(eElement.getAttributes().getLength()==2){
                        String pos[] = eElement.getAttribute("pos").split(":");
                        if(pos.length>=4){
                            if(eElement.getAttribute("move").equalsIgnoreCase("yes") && String.valueOf(posLinhaDeleted).equals(pos[2])){
                                mat.add(eElement.getTextContent());
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            
        }finally{
            matriz.add(new ArrayList<ArrayList<String>>());
            //Collections.reverse(mat);
            matriz.get(0).add(mat);
        }
    }
    
    private void montaInserted(int posLinhaDeleted){
        //Percorre nas Tags "inserted"
        NodeList geralDeleted = this.doc.getElementsByTagName("Inserted");
        ArrayList<String> mat = new ArrayList<String>();
      
        for(int elem =0; elem < geralDeleted.getLength(); elem ++){
            Node node = geralDeleted.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if(eElement.getAttributes().getLength()==2){
                    String pos[] = eElement.getAttribute("pos").split(":");
                    if(pos.length>=4){
                        if(eElement.getAttribute("move").equalsIgnoreCase("yes") && String.valueOf(posLinhaDeleted).equals(pos[2])){
                            mat.add(eElement.getTextContent());
                        }
                    }
                }
            }
        }
        matriz.get(0).add(mat);
    }
    
    private void encontraCorrespondenteV2(int posLinhaInserted) {
        NodeList geral = v2.getDoc().getElementsByTagName(v2.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        Node node = geral.item(posLinhaInserted/2);

        ArrayList<String> mat = new ArrayList<String>();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) node;
            for(int i=0; i<eElement.getChildNodes().getLength();i++){
                if(!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")){
                    mat.add(eElement.getChildNodes().item(i).getTextContent());
                }
            }
        }
        matriz.get(0).add(mat);
    }
    
    public void insertMoveInNewResult(int posLinhaInserted){
        //Array Move Inserted ou Array Move Deleted não tem a mesma quantidade de tag que V2
        if((matriz.get(0).get(0).size() != matriz.get(0).get(2).size())||(matriz.get(0).get(1).size() != matriz.get(0).get(2).size())){
            //Procarar em tagv2 se tem v2
            if(matriz.get(0).get(0).size() < matriz.get(0).get(1).size()){
                for(int k=0; k<matriz.get(0).get(2).size();k++){
                        if(!(matriz.get(0).get(0).contains(matriz.get(0).get(2).get(k)))){
                            editaNovoResultado.escreveInNewResultado("MOVEFALTANDO", posLinhaInserted/2, k, "", true);
                        }
                }
            }else{
                for(int k=0; k<matriz.get(0).get(2).size();k++){
                        if(!(matriz.get(0).get(1).contains(matriz.get(0).get(2).get(k)))){
                            editaNovoResultado.escreveInNewResultado("MOVEFALTANDO", posLinhaInserted/2, k, "", true);
                        }
                }
            }
        }
        else{ //Array Move Inserted tem a mesma quantidade de tag que V2
            for(int k=0; k<matriz.get(0).get(2).size();k++){
                if(!(matriz.get(0).get(0).get(k).equals(matriz.get(0).get(1).get(k))) ||
                        !(matriz.get(0).get(1).get(k).equals(matriz.get(0).get(2).get(k)))){
                    //Move errado (pegou de outro funcionario por exemplo)
                    editaNovoResultado.escreveInNewResultado("MOVEINCORRETO", posLinhaInserted/2, k, "", true);
                }
            }
        }
    }
    
    private void imprimeMatriz(){
        for(int i=0; i< matriz.size(); i++){
            for(int j=0; j< matriz.get(i).size(); j++){
                for(int k=0; k<matriz.get(i).get(j).size();k++){
                    System.out.println(matriz.get(i).get(j).get(k));
                }
                System.out.println();
            }
        }
    }
    
}
