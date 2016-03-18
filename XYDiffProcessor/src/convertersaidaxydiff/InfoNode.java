package convertersaidaxydiff;

/**
 *
 * @author Admin
 */
public class InfoNode {

    private int id;
    private String valueTag;
    private int idCasamento;
    
    public InfoNode(int id, String nameTSag){
        this.id = id;
        this.valueTag = nameTSag;
        this.idCasamento=-1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValueTag() {
        return valueTag;
    }

    public void setValueTag(String valueTag) {
        this.valueTag = valueTag;
    }

    public int getIdCasamento() {
        return idCasamento;
    }

    public void setIdCasamento(int idCasamento) {
        this.idCasamento = idCasamento;
    }
    
}
