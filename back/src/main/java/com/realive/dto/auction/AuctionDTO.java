package com.realive.dto.auction;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDTO {
    
    private Integer id;

  
    private Integer productId;
   

    private Integer startPrice;

    private Integer currentPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean isClosed;
    

}
