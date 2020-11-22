package uk.ac.man.cs.comp38211.util;

import java.util.ListIterator;

import org.apache.commons.cli.GnuParser;

/*
 * A wrapper around the GnuParser to ignore any unrecognised options
 * 
 * @author Kristian Epps
 */
public class XParser extends GnuParser
{
    private boolean ignoreUnrecognizedOption;

    public XParser(final boolean ignoreUnrecognizedOption)
    {
        this.ignoreUnrecognizedOption = ignoreUnrecognizedOption;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void processOption(final String arg, final ListIterator iter)
    {
        boolean hasOption = getOptions().hasOption(arg);

        if (hasOption || !ignoreUnrecognizedOption)
        {
            try
            {
                super.processOption(arg, iter);
            }
            catch (org.apache.commons.cli.ParseException e)
            {
                e.printStackTrace();
            }
        }
    }
}
