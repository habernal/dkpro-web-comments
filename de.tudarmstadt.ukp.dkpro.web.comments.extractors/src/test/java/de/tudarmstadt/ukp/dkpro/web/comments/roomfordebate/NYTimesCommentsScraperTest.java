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

package de.tudarmstadt.ukp.dkpro.web.comments.roomfordebate;

import de.tudarmstadt.ukp.dkpro.web.comments.Comment;
import de.tudarmstadt.ukp.dkpro.web.comments.roomfordebate.NYTimesCommentsScraper;
import org.junit.Test;

import java.util.List;

/**
 * (c) 2015 Ivan Habernal
 */
public class NYTimesCommentsScraperTest
{

    @Test public void testExtractComments()
            throws Exception
    {
        NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();
        List<Comment> comments = nyTimesCommentsScraper.extractComments(
                this.getClass().getClassLoader().getResourceAsStream("nytimes-step2.html"));

        for (Comment comment : comments) {
            System.out.println(comment);
        }
    }
}