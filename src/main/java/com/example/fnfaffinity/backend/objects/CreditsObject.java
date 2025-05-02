package com.example.fnfaffinity.backend.objects;

public class CreditsObject {
    public String name = "";
    public String desc = "";
    public String url = "";
    public String icon = "";
    public int size = 130;
    public boolean skipMe = false;

    public CreditsObject(String name, String desc, String url, String icon, Object size, Object skipMe) {
        if (name != null)
            this.name = name;
        if (desc != null)
            this.desc = desc;
        if (url != null)
            this.url = url;
        if (icon != null)
            this.icon = icon;
        if (size != null)
            this.size = (int) size;
        if (skipMe != null)
            this.skipMe = (boolean) skipMe;

    }
}
