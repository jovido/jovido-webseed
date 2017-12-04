package biz.jovido.seed.content;

import biz.jovido.seed.content.ValuePayload;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Stephan Grundner
 */
@Entity
@DiscriminatorValue("text")
public class TextPayload extends ValuePayload<String> {

    @Column(name = "text_value")
    private String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
