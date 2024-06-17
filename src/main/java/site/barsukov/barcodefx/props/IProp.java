package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public interface IProp {
    String getDescr();

    String getDefaultValue();

    String name();

    SysPropType getSysPropType();

}
