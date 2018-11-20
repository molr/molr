package org.molr.commons.domain.dto;

import java.util.Objects;

public class TestValueDto {

    private String text;

    public TestValueDto() {
        this.text = null;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getText(){
        return this.text;
    }

    @Override
    public String toString() {
        return "TestValueDto{" +
                "text='" + text + '\'' +
                '}';
    }

}
