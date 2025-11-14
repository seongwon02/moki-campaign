package com.example.moki_campaign.domain.customer.service;

import com.example.moki_campaign.domain.customer.dto.response.CustomerDetailResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.CustomerListResponseDto;
import com.example.moki_campaign.domain.customer.dto.response.DeclinedLoyalSummaryResponseDto;
import com.example.moki_campaign.domain.store.entity.Store;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    DeclinedLoyalSummaryResponseDto findDeclinedLoyalInfo(Store store);

    CustomerListResponseDto findCustomerList(Store store, String segment, Pageable pageable);

    CustomerDetailResponseDto findCustomerDetail(Store store, Long customerId);

    void analyzeAllStores();

    void analyzeStore(Store store);
}