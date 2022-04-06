package org.example.movieapi.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import java.util.Objects;

public class EmptyStringConvertor implements AttributeConverter<String,String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!Objects.isNull(attribute) && attribute.isEmpty()){
            return null;
        }
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
