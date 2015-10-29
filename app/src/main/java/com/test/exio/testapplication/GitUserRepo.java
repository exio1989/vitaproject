package com.test.exio.testapplication;

/**
 * Created by exio on 25.10.2015.
 */
public class GitUserRepo {
   public int id;
    public String name;
    public String html_url;
    public String description;
    public boolean fork;
    public GitUser owner;
    public boolean has_pages;

    GitUserRepo(int id,String name,String html_url,String description,boolean fork,GitUser owner,boolean has_pages) {
        this.id = id;
        this.name = name;
        this.html_url = html_url;
        this.description = description;
        this.fork = fork;
        this.owner=owner;
        this.has_pages=has_pages;
    }
}
