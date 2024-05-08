package brewbuddy.model.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="Users")
public class User {

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "birth_date",nullable = false)
    private Date birthDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


}
