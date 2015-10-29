package com.test.exio.testapplication;

import java.util.List;

/**
 * Created by exio1989 on 28.10.2015.
 */
public class GitSearchUsersResponse {
    public int total_count;
    public List<GitUser> items;

    GitSearchUsersResponse(int total_count,List<GitUser> items) {
        this.total_count = total_count;
        this.items = items;
    }
}
