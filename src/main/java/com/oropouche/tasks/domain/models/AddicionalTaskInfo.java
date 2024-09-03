package com.oropouche.tasks.domain.models;


import lombok.Data;


@Data
public class AddicionalTaskInfo {
    private final Long userId ;
    private final String userName;
    private final String userEmail;

    public AddicionalTaskInfo(Long userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }



}
