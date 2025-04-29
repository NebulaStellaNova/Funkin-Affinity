package com.example.fnfaffinity.backend.objects;

import org.w3c.dom.Document;

import java.io.File;

public class FileCache {
    public String path = "";
    public File fileData = null;
    public Document document = null;

    public FileCache(String daPath, File daFile, Document daDocument) {
        path = daPath;
        fileData = daFile;
        document = daDocument;
    }
}
