/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * (c) 2015 Ivan Habernal
 */
public class CreateDebateExtractor
{
    private static final String URLS_CREATEDEBATE_TXT = "bootstraping/URLs-createdebate.txt";

    private Map<String, Set<Debate>> debates;

    private Map<String, Set<String>> loadURLs()
            throws IOException
    {
        Map<String, Set<String>> result = new HashMap<>();

        InputStream stream = this.getClass().getClassLoader()
                .getResourceAsStream(URLS_CREATEDEBATE_TXT);
        List<String> strings = IOUtils.readLines(stream);

        for (String line : strings) {
            if (!line.startsWith("#")) {
                String[] split = line.split("\\s+");
                String domain = split[0].trim();
                String url = split[1].trim();

                if (!result.containsKey(domain)) {
                    result.put(domain, new HashSet<String>());
                }

                result.get(domain).add(url);
            }
        }

        return result;
    }

    private Map<String, Set<Debate>> loadDebates()
            throws IOException
    {
        Map<String, Set<Debate>> result = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : loadURLs().entrySet()) {
            for (String url : entry.getValue()) {
                InputStream inputStream = new URL(url).openStream();

                Debate debate = CreateDebateHTMLParser.parseDebate(inputStream);
                IOUtils.closeQuietly(inputStream);

                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new HashSet<Debate>());
                }

                result.get(entry.getKey()).add(debate);
            }
        }

        return result;
    }

    /**
     * Returns a map where key is the debate topic and value is a set of extracted debates
     *
     * @return debates
     * @throws IOException
     */
    public Map<String, Set<Debate>> getDebates()
            throws IOException
    {
        if (this.debates == null) {
            this.debates = loadDebates();
        }

        return this.debates;
    }

    public static void main(String[] args)
            throws Exception
    {
        CreateDebateExtractor createDebateExtractor = new CreateDebateExtractor();
        Map<String, Set<Debate>> debates1 = createDebateExtractor.getDebates();

        System.out.println(debates1);
    }
}
