/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package convertersaidaxydiff;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Admin
 */
public class SalvarDocumentoXML {
    
    public static String salvar(DocumentoXML documentoXML, String nome){
        try
            {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                tr.transform(new DOMSource(documentoXML.getDoc()), 
                                     new StreamResult(new FileOutputStream(nome)));
            }
            catch (TransformerException te)
            {
                System.out.println(te.getMessage());
            }catch (FileNotFoundException ex) {
                Logger.getLogger(MatrizV1V2.class.getName()).log(Level.SEVERE, null, ex);
            }
        return "";
    }
    
}
