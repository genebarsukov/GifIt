package codewrencher.gifit.objects.simple.item;

/**
 * Created by Gene on 12/19/2015.
 */
public class Item {
    protected int id;
    protected int index;
    protected String state;     // "on" or "off"

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    public int getIndex() {
        return this.index;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String getState() {
        return this.state;
    }
    public void switchState() {
        switch (this.state) {
            case "on":
                this.state = "off";
                break;
            case "off":
                this.state = "on";
                break;
            default:
                this.state = "on";
                break;
        }
    }
}
