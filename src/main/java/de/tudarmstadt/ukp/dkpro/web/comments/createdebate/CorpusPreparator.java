package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main class for extracting raw HTML debates from createdebate.com and storing them using the
 * internal format
 *
 * @author Ivan Habernal
 * @see Debate
 * @see DebateSerializer
 */
public class CorpusPreparator
{
    /**
     * Extracts all debates from raw HTML files in inFolder and stores them into outFolder
     * as serialized {@link Debate} objects (see {@link DebateSerializer}.
     *
     * @param inFolder  in folder
     * @param outFolder out folder (must exist)
     * @throws IOException exception
     */
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
                    // serialize to xml
                    String xml = DebateSerializer.serializeToXML(debate);

                    // same name with .xml
                    File outputFile = new File(outFolder, f.getName() + ".xml");

                    FileUtils.writeStringToFile(outputFile, xml, "utf-8");
                    System.out.println("Saved to " + outputFile.getAbsolutePath());

                    // ensure we can read it again
                    DebateSerializer.deserializeFromXML(FileUtils.readFileToString(outputFile));
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
