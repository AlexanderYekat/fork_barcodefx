package site.barsukov.barcodefx.controllers.elements;

import javafx.scene.control.Label;

import java.util.Objects;

public class MyLabel extends Label {
    private boolean errorCode = false;

    public MyLabel(String text) {
        super(text);
    }

    public boolean isErrorCode() {
        return errorCode;
    }

    public void setErrorCode(boolean errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyLabel that = (MyLabel) o;
        return Objects.equals(this.getText(), that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getText());
    }
}
