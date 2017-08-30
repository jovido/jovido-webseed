package biz.jovido.seed.content.model;

import javax.persistence.*;

/**
 * @author Stephan Grundner
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"structure_id", "name"}))
public abstract class Attribute {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "structure_id")
    private Structure structure;

    private String name;

    private int ordinal;

    private int capacity = Integer.MAX_VALUE;
    private int required = 0;

    public Long getId() {
        return id;
    }

    /*default*/ void setId(Long id) {
        this.id = id;
    }

    public Structure getStructure() {
        return structure;
    }

    /*default*/ void setStructure(Structure structure) {
        this.structure = structure;
    }

    public String getName() {
        return name;
    }

    /*default*/ void setName(String name) {
        this.name = name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public abstract Payload createPayload();
}
