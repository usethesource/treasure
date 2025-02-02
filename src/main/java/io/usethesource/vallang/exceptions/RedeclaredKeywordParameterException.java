package io.usethesource.vallang.exceptions;

import io.usethesource.vallang.type.Type;

public class RedeclaredKeywordParameterException extends FactTypeDeclarationException {
    private static final long serialVersionUID = -2118606173620347920L;
    private String label;
    private Type earlier;

    public RedeclaredKeywordParameterException(String label, Type earlier) {
        super("Keyword parameter " + label + " was declared earlier as " + earlier);
        this.label = label;
        this.earlier = earlier;
    }

    public String getLabel() {
        return label;
    }

    public Type getEarlierType() {
        return earlier;
    }
}
