package biz.jovido.seed.content;

/**
 * @author Stephan Grundner
 */
public abstract class StructureConfigurer {

    public abstract TextAttributeConfigurer addTextAttribute(String fieldName);
    public abstract FragmentAttributeConfigurer addFragmentAttribute(String fieldName);

    public abstract Structure getStructure();
}