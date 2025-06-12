package com.example.demo.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag_names")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "tagName")
    private Set<ArticleTag> articleTags = new HashSet<>();

    // constructor using name
    public TagName(String name) {
        this.name = name;
    }
}

