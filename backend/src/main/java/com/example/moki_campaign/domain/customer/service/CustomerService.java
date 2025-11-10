package com.example.moki_campaign.domain.customer.service;

import com.example.moki_campaign.domain.store.entity.Store;

public interface CustomerService {

    void analyzeAllStores();

    void analyzeStore(Store store);
}