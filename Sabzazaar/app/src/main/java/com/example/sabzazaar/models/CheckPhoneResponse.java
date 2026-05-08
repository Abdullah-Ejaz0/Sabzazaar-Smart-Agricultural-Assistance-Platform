package com.example.sabzazaar.models;

public class CheckPhoneResponse {
    public int status;  // 1 for login (exists), 0 for signup (new)
    public String message;
    public String token;
    public UserData user;  // Only present if status == 1
}
