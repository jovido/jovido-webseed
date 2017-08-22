package biz.jovido.seed.content.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @author Stephan Grundner
 */
@Entity
public class RelationPayload extends Payload<Relation> {

    @OneToOne(cascade = CascadeType.ALL)
    private Relation relation;

    @Override
    public Relation getValue() {
        return relation;
    }

    @Override
    public void setValue(Relation value) {
        relation = value;
        relation.source = this;
    }
}