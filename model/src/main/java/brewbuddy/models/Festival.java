package brewbuddy.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="Festivals")
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private Date eventDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="city_id", nullable=false)
    private City city;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ArrayList<Brewery> breweries;
}
