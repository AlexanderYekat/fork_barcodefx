package site.barsukov.barcodefx.model.dao;

import lombok.Data;
import site.barsukov.barcodefx.model.enums.SysPropType;

import javax.persistence.*;

@Data
@Entity
@Table(name = "sys_prop")
public class SysProp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sys_key")
    private String sysKey;
    @Enumerated(EnumType.STRING)
    private SysPropType type;
    @Lob
    private String value;
    @Column(name = "description")
    private String description;
}
