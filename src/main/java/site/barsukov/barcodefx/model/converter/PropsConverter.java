package site.barsukov.barcodefx.model.converter;

import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.PropDto;
import site.barsukov.barcodefx.model.dao.SysProp;
import site.barsukov.barcodefx.props.IProp;

@Component
public class PropsConverter {

    public SysProp toEntity(IProp prop, String value) {
        var entity = new SysProp();
        entity.setDescription(prop.getDescr());
        entity.setType(prop.getSysPropType());
        entity.setValue(value);
        entity.setSysKey(prop.name());
        return entity;
    }

    public PropDto toDto(SysProp prop) {
        return PropDto.builder()
                .name(prop.getSysKey())
                .id(prop.getId())
                .descr(prop.getDescription())
                .type(prop.getType())
                .value(prop.getValue())
                .build();
    }

    public SysProp toEntity(PropDto dto) {
        var entity = new SysProp();
        entity.setType(dto.getType());
        entity.setValue(dto.getValue());
        entity.setSysKey(dto.getName());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescr());
        return entity;
    }
}
