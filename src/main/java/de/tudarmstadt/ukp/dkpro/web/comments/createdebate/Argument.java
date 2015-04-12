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

/**
 * (c) 2015 Ivan Habernal
 */
public class Argument
{
    private String author;
    private Integer argPoints;
    private String stance;
    private String text;
    private String parentId;
    private String id;

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public void setArgPoints(Integer argPoints)
    {
        this.argPoints = argPoints;
    }

    public void setStance(String stance)
    {
        this.stance = stance;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public Integer getArgPoints()
    {
        return this.argPoints;
    }

    public String getStance()
    {
        return this.stance;
    }

    public String getText()
    {
        return this.text;

    }

    public String getParentId()
    {
        return this.parentId;

    }

    public String getId()
    {
        return this.id;
    }
}
