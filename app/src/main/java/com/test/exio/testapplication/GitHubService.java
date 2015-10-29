package com.test.exio.testapplication;

/**
 * Создано: exio1989 Дата: 27.10.2015.
 */
public class GitHubService{
    private static GitHub mGithub;

    public static GitHub getService() {
        if (mGithub == null) {
            mGithub =  GitHubServiceGenerator.createService(GitHub.class, "exio1989","a34245814");
        }
        return mGithub;
    }
}