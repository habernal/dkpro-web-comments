package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ivan Habernal
 */
public class CorpusPreparator
{
    public static void extractAllDebates(File inFolder, File outFolder)
            throws IOException
    {
        File[] files = inFolder.listFiles();
        if (files == null) {
            throw new IOException("No such dir: " + inFolder);
        }

        for (File f : files) {
            InputStream inputStream = new FileInputStream(f);
            try {
                Debate debate = CreateDebateHTMLParser.parseDebate(inputStream);

                if (debate != null) {
                    System.out.println(f.getName());
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

    }

    public static void main(String[] args)
    {
        File inFolder = new File("/home/user-ukp/data2/createdebate-exported-2014-raw-html");
        File outFolder = new File("/home/user-ukp/data2/createdebate-exported-2014");
        outFolder.mkdir();

        try {
            extractAllDebates(inFolder, outFolder);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
