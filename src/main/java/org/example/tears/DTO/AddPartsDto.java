package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class AddPartsDto {
    private List<RequestNoteDTO> notes;
    private List<PartDto> parts;

}
