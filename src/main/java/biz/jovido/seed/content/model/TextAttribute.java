package biz.jovido.seed.content.model;

/**
 * @author Stephan Grundner
 */
public class TextAttribute extends Attribute {

    private int lines = 1;

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public boolean isMultiline() {
        return lines > 1;
    }

    @Override
    public Payload createPayload() {
        return new TextPayload();
    }
}