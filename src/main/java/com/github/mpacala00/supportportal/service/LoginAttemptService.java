package com.github.mpacala00.supportportal.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    public static final int MAX_NUMBER_OF_ATTEMPTS = 15;
    //String - key, Integer - value
    private LoadingCache<String, Integer> loginAttemptCache;

    //initializing cache from guava lib
    public LoginAttemptService() {
        super();
        this.loginAttemptCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100) //100 entries at the cache at max
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username); //find the key and remove it from cache
    }

    public void addUserToLoginAttemptCache(String username) throws ExecutionException {
        int attempts = 0;

        attempts = loginAttemptCache.get(username) + 1; //get the value from map and add 1 to it
        loginAttemptCache.put(username, attempts);
    }

    public boolean maxAttemptsReached(String username) throws ExecutionException {
        return loginAttemptCache.get(username) >= MAX_NUMBER_OF_ATTEMPTS;
    }
}
