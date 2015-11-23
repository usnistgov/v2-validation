/*
 * NIST Healthcare Core
 * SkeletonURIResolver.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.erx.xml;


import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;



public class SkeletonURIResolver implements URIResolver {

    public static final String XSLT_SKELETON_1_5 = "skeleton1-5.xsl";

    public Source resolve(String href, String base) throws TransformerException {
        if ("skeleton1-5.xsl".equals(href)) {
            StreamSource skeleton = new StreamSource(
                    SkeletonURIResolver.class.getClassLoader().getResourceAsStream(
                            XSLT_SKELETON_1_5));
            return skeleton;
        }
        return null;
    }

}
