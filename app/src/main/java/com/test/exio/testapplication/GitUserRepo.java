package com.test.exio.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Создано: exio Дата: 25.10.2015.
 */
public class GitUserRepo implements Parcelable {
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeString(html_url);
        out.writeString(description);
        out.writeInt(fork ? 1 : 0);
        owner.writeToParcel(out,0);
        out.writeInt(has_pages?1:0);
    }

    public static final Parcelable.Creator<GitUserRepo> CREATOR
            = new Parcelable.Creator<GitUserRepo>() {
        public GitUserRepo createFromParcel(Parcel in) {
            return new GitUserRepo(in);
        }

        public GitUserRepo[] newArray(int size) {
            return new GitUserRepo[size];
        }
    };

    private GitUserRepo(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.html_url = in.readString();
        this.description = in.readString();
        this.fork = (in.readInt()==1);
        this.owner=new GitUser(in);
        this.has_pages=(in.readInt()==1);
    }
}
