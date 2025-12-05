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
public class TransbankCommitResponseDTO {
    
    @JsonProperty("vci")
    private String vci;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("buy_order")
    private String buyOrder;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("card_detail")
    private CardDetail cardDetail;
    
    @JsonProperty("accounting_date")
    private String accountingDate;
    
    @JsonProperty("transaction_date")
    private String transactionDate;
    
    @JsonProperty("authorization_code")
    private String authorizationCode;
    
    @JsonProperty("payment_type_code")
    private String paymentTypeCode;
    
    @JsonProperty("response_code")
    private Integer responseCode;
    
    @JsonProperty("installments_number")
    private Integer installmentsNumber;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDetail {
        @JsonProperty("card_number")
        private String cardNumber;
    }
}
