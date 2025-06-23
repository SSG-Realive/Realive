package com.realive.dto.logs;

import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.SalesWithCommissionDTO;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PayoutLogDetailDTO {
    private final PayoutLogDTO payoutInfo;
    private final List<SalesWithCommissionDTO> salesDetails;
}