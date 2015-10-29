package com.test.exio.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Создано: exio Дата: 25.10.2015.
 */
public class GitUser implements Parcelable {
    public int id;
    public String login;
    public String avatar_url;
    public String html_url;

    public GitUser(int id,String login,String avatar_url,String html_url) {
        this.id = id;
        this.login = login;
        this.avatar_url = avatar_url;
        this.html_url = html_url;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(login);
        out.writeString(avatar_url);
        out.writeString(html_url);
    }

    public static final Parcelable.Creator<GitUser> CREATOR
            = new Parcelable.Creator<GitUser>() {
        public GitUser createFromParcel(Parcel in) {
            return new GitUser(in);
        }

        public GitUser[] newArray(int size) {
            return new GitUser[size];
        }
    };

    public GitUser(Parcel in) {
        this.id = in.readInt();
        this.login = in.readString();
        this.avatar_url = in.readString();
        this.html_url = in.readString();
    }
}
