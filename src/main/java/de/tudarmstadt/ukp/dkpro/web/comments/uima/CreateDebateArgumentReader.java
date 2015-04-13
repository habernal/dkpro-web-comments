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

package de.tudarmstadt.ukp.dkpro.web.comments.uima;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.Argument;
import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.Debate;
import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.DebateSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * (c) 2015 Ivan Habernal
 */
public class CreateDebateArgumentReader
        extends JCasCollectionReader_ImplBase
{
    /**
     * Folder containing serialized XML files with {@link Debate} object in each file
     */
    public static final String PARAM_SOURCE_LOCATION = "sourceLocation";
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true) File sourceLocation;

    Queue<File> files = new ArrayDeque<>();

    Queue<Argument> currentArguments = new ArrayDeque<>();

    @Override public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        File[] fileArray = sourceLocation.listFiles(new FilenameFilter()
        {
            @Override public boolean accept(File dir, String name)
            {
                return name.endsWith(".xml");
            }
        });

        if (fileArray != null) {
            this.files.addAll(Arrays.asList(fileArray));
        }
    }

    protected void loadArgumentsFromNextFile()
            throws IOException
    {
        // there might be debates without arguments

        File file = files.poll();

        Debate debate = DebateSerializer
                .deserializeFromXML(FileUtils.readFileToString(file, "utf-8"));
        currentArguments = new ArrayDeque<>(debate.getArgumentList());
    }

    @Override public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        if (currentArguments.peek() == null) {
            loadArgumentsFromNextFile();
        }

        Argument argument = currentArguments.poll();

        jCas.setDocumentLanguage("en");
        jCas.setDocumentText(argument.getText());

        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.addToIndexes();
        metaData.setDocumentId(argument.getId());
        metaData.setDocumentTitle(argument.getStance());
    }

    @Override public boolean hasNext()
            throws IOException, CollectionException
    {
        return !currentArguments.isEmpty() || !files.isEmpty();
    }

    @Override public Progress[] getProgress()
    {
        return new Progress[0];
    }
}
