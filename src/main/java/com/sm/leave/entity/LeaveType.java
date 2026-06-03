package com.sm.leave.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ANNUAL   特休
     *  SICK     病假
     *  PERSONAL 事假
     *  MARRIAGE 婚假
     *  FUNERAL  喪假
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_days")
    private Integer maxDays;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;
}
