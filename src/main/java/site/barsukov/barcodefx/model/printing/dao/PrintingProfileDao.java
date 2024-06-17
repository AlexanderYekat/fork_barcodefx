package site.barsukov.barcodefx.model.printing.dao;

import lombok.Data;
import site.barsukov.barcodefx.model.printing.ProfileType;

import javax.persistence.*;

@Data
@Entity
@Table(name = "printing_profile")
public class PrintingProfileDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private ProfileType type;
    @Lob
    private String profile;
}
