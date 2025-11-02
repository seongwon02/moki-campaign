package com.example.moki_campaign.domain.store.service;

import com.example.moki_campaign.domain.store.dto.response.WeeklySummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;

public interface StoreService {

    WeeklySummaryResponseDto findWeeklySummary(Store store);
}
