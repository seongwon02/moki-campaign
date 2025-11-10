package com.example.moki_campaign.domain.admin.service;

import com.example.moki_campaign.domain.admin.dto.CustomerSeedRequestDto;
import com.example.moki_campaign.domain.admin.dto.DailyVisitSeedRequestDto;
import com.example.moki_campaign.domain.customer.entity.Customer;
import com.example.moki_campaign.domain.visit.entity.DailyVisit;

public interface DataSeedingService {

    Customer createSpecificCustomer(CustomerSeedRequestDto dto);
    DailyVisit createSpecificVisit(DailyVisitSeedRequestDto dto);
}
