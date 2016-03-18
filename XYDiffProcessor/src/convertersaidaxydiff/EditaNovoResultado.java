package convertersaidaxydiff;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Admin
 */
public class EditaNovoResultado {

    private DocumentoXML newResultado;
    private ArrayList<ArrayList<ArrayList<InfoNode>>> matriz;
    private String saveResultPath;

    public EditaNovoResultado(DocumentoXML newResultado, ArrayList<ArrayList<ArrayList<InfoNode>>> matriz, String savePath) {
        this.newResultado = newResultado;
        this.matriz = matriz;
        this.saveResultPath = savePath;
    }

    public void editNewResultado(int posLinhaMatriz, int posTag, String valorV1, String valorInserted) {
        
        try { 
            //for (int k = 0; k < matriz.get(posLinhaMatriz).get(0).size(); k++) {
                //if (matriz.get(posLinhaMatriz).get(0).get(k).getValueTag().equals(valorV1)) {
                    //if (matriz.get(posLinhaMatriz).get(0).get(posTag).getValueTag().equals(matriz.get(posLinhaMatriz).get(1).get(posTag).getValueTag())) {
                        //Valor de v1 e v2 sao os mesmos na matriz
                        
                    if(matriz.get(posLinhaMatriz).get(1).get(posTag).getValueTag().equals(valorInserted)){
                        //Valor de v1 e v2 sao diferentes na matriz
                        this.escreveInNewResultado("UPDATE",posLinhaMatriz, posTag, valorV1, false);
                    }else{
                        this.escreveInNewResultado("UPDATEINCORRETO",posLinhaMatriz, posTag, valorV1, false);
                    }
                //}
            //}
        } catch (Exception ex) {
            //Update que não está na matriz - casou errado
            escreveInNewResultado("UPDATEINCORRETO", posLinhaMatriz, posTag, valorV1, false);
        }
    }

//    private void escreveInNewResultado(String situacao, String v1, String v2) {
//        NodeList geral = newResultado.getDoc().getElementsByTagName(newResultado.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
//        for (int elem = 0; elem < geral.getLength(); elem++) {
//            Node node = geral.item(elem);
//
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                Element eElement = (Element) node;                
//                for (int i = 0; i < eElement.getChildNodes().getLength(); i++) {
//                    if (!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")) {
//                        if(eElement.getChildNodes().item(i).getTextContent().equals(v2)){
//                            
//                            Element novo = newResultado.getDoc().createElement(situacao);
//                            novo.setAttribute("FROM",v1);
//                            eElement.getChildNodes().item(i).appendChild(novo);
//                        }
//                    }
//                }
//
//            }
//
//        }
//    }
    
    public void escreveInNewResultado(String situacao, int posLinhaMatriz, int posTag , String v1, boolean move) {
        NodeList geral = newResultado.getDoc().getElementsByTagName(newResultado.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        //for (int elem = 0; elem < geral.getLength(); elem++) {
            Node node = geral.item(posLinhaMatriz);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;                
                //for (int i = 0; i < eElement.getChildNodes().getLength(); i++) {
                    //if (!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")) {
                        //if(eElement.getChildNodes().item(i).getTextContent().equals(v2)){
                            //System.out.println(posLinhaMatriz+" "+eElement.getTextContent());
                            Element novo = newResultado.getDoc().createElement(situacao);
                            if(!move)
                                novo.setAttribute("FROM",v1);
                            eElement.getChildNodes().item((posTag*2)+1).appendChild(novo);
                        //}
                    //}
                //}

            }

        //}
    }
    
    public boolean existeChaveNaMatriz(int posChave, String chave){
        for(int i=0; i< matriz.size();i++){
            for(int k=0; k < matriz.get(i).get(0).size(); k++){
                if(matriz.get(i).get(0).get(k).getValueTag().equals(chave)) //&&
                        //matriz.get(i).get(0).get(k).getId() == matriz.get(i).get(1).get(k).getId())
                    return true;
            }
        }
        return false;
    }
    
    public void saveNewResultado(){
        SalvarDocumentoXML.salvar(newResultado, this.saveResultPath);
        System.out.println("Novo Resultado XML salvo com Sucesso!");
    }

    public DocumentoXML getNewResultado() {
        return newResultado;
    }
    
    

}
