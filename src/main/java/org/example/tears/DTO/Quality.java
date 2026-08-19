package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quality {

    private boolean accepted;

    private double score;

    private List<String> issues;

    private List<String> missing_fields;
}