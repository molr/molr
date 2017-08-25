/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads source code for Java classes.
 * 
 * @author jepeders
 */
public class SourceCodeLoader {

    private final URL basePath;

    public SourceCodeLoader(URL basePath) {
        this.basePath = basePath;
    }

    public List<String> loadClassSourceCode(String fullName) throws IOException {
        try {
            URI uri = new URI(basePath.toString() + fullName);
            try (InputStream input = uri.toURL().openStream();
                    InputStreamReader inputReader = new InputStreamReader(input);
                    BufferedReader lineReader = new BufferedReader(inputReader)) {
                return lineReader.lines().collect(Collectors.toList());
            }
        } catch (URISyntaxException | MalformedURLException syntaxException) {
            throw new IllegalArgumentException("Given name of class not valid: " + fullName);
        }
    }
}
