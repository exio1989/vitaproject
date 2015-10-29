package com.test.exio.testapplication;

/**
 * Created by exio1989 on 28.10.2015.
 */
public class GitUserDetailed {
        public int id;
        public String login;
        public String avatar_url;
        public String html_url;
        public String name;

    GitUserDetailed(int id,String login,String avatar_url,String html_url,String name) {
            this.id = id;
            this.login = login;
            this.avatar_url = avatar_url;
            this.html_url = html_url;
            this.name = name;
        }
}
