package com.test.exio.testapplication;

/**
 * Created by exio on 25.10.2015.
 */
public class GitUser {
    public int id;
    public String login;
    public String avatar_url;
    public String html_url;

    GitUser(int id,String login,String avatar_url,String html_url) {
        this.id = id;
        this.login = login;
        this.avatar_url = avatar_url;
        this.html_url = html_url;
    }
}
