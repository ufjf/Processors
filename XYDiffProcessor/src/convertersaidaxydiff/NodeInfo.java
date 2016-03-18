package convertersaidaxydiff;

/**
 *
 * @author Admin
 */
public class NodeInfo {

    private String pos;
    private String value;
    private boolean possuiChave;

    public NodeInfo(String pos, String value) {
        this.pos = pos;
        this.value = value;
        this.possuiChave = false;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean possuiChave() {
        return possuiChave;
    }

    public void setPossuiChave(boolean possuiChave) {
        this.possuiChave = possuiChave;
    }
    
}
