package com.holiday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "country")
public class Country {

    @Id
    @Column(name = "country_code", length = 3, nullable = false, unique = true)
    private String countryCode;

    @Column(name = "country_name", nullable = false)
    private String countryName;

}