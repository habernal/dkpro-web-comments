/*
 * Copyright 2015 XXX
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

package xxx.web.comments.roomfordebate;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import xxx.web.comments.Comment;

import java.io.StringWriter;
import java.util.List;

/**
 * (c) 2015 XXX
 */
public class NYTimesCommentsScraperTest
{

    @Test public void testExtractComments()
            throws Exception
    {
        NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();
        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("nytimes-step2.html"),
                writer, "utf-8");
        String html = writer.toString();

        List<Comment> comments = nyTimesCommentsScraper.extractComments(html);

        for (Comment comment : comments) {
            System.out.println(comment);
        }
    }
}