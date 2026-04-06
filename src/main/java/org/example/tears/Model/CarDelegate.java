package org.example.tears.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CarDelegate {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Car car;

    @ManyToOne
    private User delegate;           // الشخص المفوض

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String permissions;      // نص أو JSON يصف الصلاحيات
}