package com.nhnacademy.service.blacklist;

import org.springframework.stereotype.Service;

@Service
public interface BlacklistService {
    void addBlacklist(String token);
}
