package biz.jovido.seed.content;

import javax.persistence.*;

/**
 * @author Stephan Grundner
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "attribute_name"}))
public abstract class Payload {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    Item item;

    @Column(name = "attribute_name")
    String attributeName;

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public abstract Object getValue();
    public abstract void setValue(Object value);
}
