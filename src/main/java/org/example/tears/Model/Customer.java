package org.example.tears.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Table(name = "customers")
public class Customer{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past
    private LocalDate dateOfBirth;

    @OneToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}



