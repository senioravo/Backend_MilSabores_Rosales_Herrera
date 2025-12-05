package com.milsabores.ventas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransbankCreateResponseDTO {
    
    private String token;
    
    @JsonProperty("url")
    private String url;
}
