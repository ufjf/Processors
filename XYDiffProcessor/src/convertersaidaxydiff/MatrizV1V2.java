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
public class MatrizV1V2 {
    
    private DocumentoXML v1 = new DocumentoXML();
    private DocumentoXML v2 = new DocumentoXML();
    private DocumentoXML newResult = new DocumentoXML();
    private int posChave;
    private String convertPath;
    private ArrayList<ArrayList<ArrayList<InfoNode>>> matriz = new ArrayList<ArrayList<ArrayList<InfoNode>>>();
    private ArrayList<String> pertenceV1 = new ArrayList<String>();
    
    /**
     * Contrutor
     * Faz a leitura de v1 e v2
     */
    public MatrizV1V2(Document doc1, Document doc2, int posChave, String convertFile){    
        v1.setDoc(doc1);
        v2.setDoc(doc2);
        this.posChave = posChave;
        this.convertPath = convertFile;
    }
    
    public void insertLine(){
        matriz.add(new ArrayList<ArrayList<InfoNode>>());
    }
    
    public ArrayList getPercenteV1(){
        return pertenceV1;
    }
    
    public void prencheMatriz(){
        NodeList geral = v1.getDoc().getElementsByTagName(v1.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        int cont=1;
        int posicao=0;
        for(int elem =0; elem < geral.getLength(); elem ++){
            ArrayList<InfoNode> mat = new ArrayList<InfoNode>();
            Node node = geral.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                
                String empno = eElement.getChildNodes().item(posChave).getTextContent();
                pertenceV1.add(empno);
                if(this.buscaEmV2(empno,posicao)){
                    for(int i=0; i<eElement.getChildNodes().getLength();i++){
                        if(!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")){
                            InfoNode infoNode = new InfoNode(cont, eElement.getChildNodes().item(i).getTextContent());
                            cont++;
                            mat.add(infoNode);
                        }
                    }
                    this.matriz.get(posicao).set(0, mat);
                    posicao++;
                }else{
                    cont += eElement.getChildNodes().getLength()/2;
                }
            }
        }
        //imprimeMatriz();
        SalvarDocumentoXML.salvar(v2, this.convertPath);
        this.newResult.setDoc(new ReadXML(newResult.getDoc()).leXML(this.convertPath));
    }
    
    private boolean buscaEmV2(String empnoV1, int posicao){
        NodeList geral = v2.getDoc().getElementsByTagName(v2.getDoc().getDocumentElement().getChildNodes().item(1).getNodeName());
        int cont=1;
        ArrayList<InfoNode> mat = new ArrayList<InfoNode>();
        for(int elem =0; elem < geral.getLength(); elem ++){
            Node node = geral.item(elem);
                
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                
                String empno = eElement.getChildNodes().item(posChave).getTextContent();
                if(empno.equalsIgnoreCase(empnoV1)){
                    this.insertLine();
                    this.matriz.get(posicao).add(null);
                    for(int i=0; i<eElement.getChildNodes().getLength();i++){
                        if(!eElement.getChildNodes().item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "").equals("")){
                            InfoNode infoNode = new InfoNode(cont, eElement.getChildNodes().item(i).getTextContent());
                            cont++;
                            mat.add(infoNode);
                        }
                    }
                    this.matriz.get(posicao).add(mat);
                    return true;
                }else{
                    cont += eElement.getChildNodes().getLength()/2;
                }
            }
        }
        return false;
    }
    
    public DocumentoXML getNewResult(){
        return newResult;
    }

    public ArrayList<ArrayList<ArrayList<InfoNode>>> getMatriz() {
        return matriz;
    }
    
    private void imprimeMatriz(){    
        for(int v1=0; v1< matriz.size(); v1++){
            for(int j=0; j< matriz.get(v1).size(); j++){
                for(int k=0; k<matriz.get(v1).get(j).size();k++){
                    System.out.println(matriz.get(v1).get(j).get(k).getId() + " " + matriz.get(v1).get(j).get(k).getValueTag()+
                            " "+ matriz.get(v1).get(j).get(k).getIdCasamento());
                }
                System.out.println();
            }
            System.out.println("---------------------------------");
        }
    }
    
}
