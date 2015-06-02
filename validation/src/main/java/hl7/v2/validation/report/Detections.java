package hl7.v2.validation.report;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import gov.nist.validation.report.impl.EntryImpl;
import hl7.v2.instance.Element;
import hl7.v2.instance.Location;
import hl7.v2.profile.BindingStrength;
import hl7.v2.profile.Range;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.content.Constraint;
import hl7.v2.validation.vs.ValueSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility for creating report entries for various detections
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class Detections {

    final static Config conf = ConfigFactory.load();

    /* ========================================================================
     *  Structure related entries
     * =======================================================================*/
     /**
     * @return A report entry for the R usage detection
     */
    public static Entry rusage(Location l) {
        String category       = conf.getString("report.r-usage.category");
        String classification = conf.getString("report.r-usage.classification");
        String template       = conf.getString("report.r-usage.template");
        String description    = String.format(template, l.prettyString());
        return entry(l, description, category, classification);
    }

    /**
     * @return A report entry for the X usage detection
     */
    public static Entry xusage(Location l) {
        String category       = conf.getString("report.x-usage.category");
        String classification = conf.getString("report.x-usage.classification");
        String template       = conf.getString("report.x-usage.template");
        String description    = String.format(template, l.prettyString());
        return entry(l, description, category, classification);
    }

    /**
     * @return A report entry for the W usage detection
     */
    public static Entry wusage(Location l) {
        String category       = conf.getString("report.w-usage.category");
        String classification = conf.getString("report.w-usage.classification");
        String template       = conf.getString("report.w-usage.template");
        String description    = String.format(template, l.prettyString());
        return entry(l, description, category, classification);
    }

    /**
     * @return A report entry for the RE usage detection
     */
    public static Entry reusage(Location l) {
        String category       = conf.getString("report.re-usage.category");
        String classification = conf.getString("report.re-usage.classification");
        String template       = conf.getString("report.re-usage.template");
        String description    = String.format(template, l.prettyString());
        return entry(l, description, category, classification);
    }

    /**
     * @return A report entry for the W usage detection
     */
    public static Entry cardinality(Location l, Range r, int count) {
        String category       = conf.getString("report.cardinality.category");
        String classification = conf.getString("report.cardinality.classification");
        String template       = conf.getString("report.cardinality.template");
        String description    = String.format(template, l.prettyString(), r.min(), r.max(), count);
        return entry(l, description, category, classification);
    }

    /**
     * @return A report entry for the length detection
     */
    public static Entry length(Location l, Range r, String value) {
        String category       = conf.getString("report.length.category");
        String classification = conf.getString("report.length.classification");
        String template       = conf.getString("report.length.template");
        String desc = String.format(template, l.prettyString(), r.min(), r.max(), value);
        return entry(l, desc, category, classification);
    }

    /**
     * @return A report entry for the format detection
     */
    public static Entry format(Location l, String msg) {
        String category       = conf.getString("report.format.category");
        String classification = conf.getString("report.format.classification");
        String template       = conf.getString("report.format.template");
        String desc = String.format(template, msg);
        return entry(l, desc, category, classification);
    }

    /**
     * @return A report entry for the extra detection
     */
    public static Entry extra(Location l) {
        String category       = conf.getString("report.extra.category");
        String classification = conf.getString("report.extra.classification");
        String template       = conf.getString("report.extra.template");
        String desc = String.format(template, l.prettyString());
        return entry(l, desc, category, classification);
    }

    /**
     * @return A report entry for unescaped separators detection
     */
    public static Entry unescaped(Location l) {
        String category       = conf.getString("report.unescaped.category");
        String classification = conf.getString("report.unescaped.classification");
        String template       = conf.getString("report.unescaped.template");
        String desc           = String.format(template, l.prettyString());
        return entry(l, desc, category, classification);
    }

    /**
     * @return A report entry for the unexpected segment detection
     */
    public static Entry unexpected(int line, String content) {
        String path = content.length() >=3 ? content.substring(0, 3) : "";
        String category       = conf.getString("report.unexpected.category");
        String classification = conf.getString("report.unexpected.classification");
        String template       = conf.getString("report.unexpected.template");
        String desc           = String.format(template, content);
        return entry(line, 1, path, desc, category, classification);
    }

    /**
     * @return A report entry for the invalid content detection
     */
    public static Entry invalid(int line, String content) {
        String category       = conf.getString("report.invalid.category");
        String classification = conf.getString("report.invalid.classification");
        String template       = conf.getString("report.invalid.template");
        String desc           = String.format(template, content);
        return entry(line, 1, "", desc, category, classification);
    }

    /* ========================================================================
     *  Content related entries
     * =======================================================================*/

    /**
     * @return A report entry for a constraint failure detection
     */
    public static Entry csFailure(Element context, Constraint c,List<Trace> stack) {
        String category       = conf.getString("report.constraint-failure.category");
        String classification = conf.getString("report.constraint-failure.classification");
        String template       = conf.getString("report.constraint-failure.template");
        String desc = String.format(template, c.id(), c.description());
        Map<String, Object> metaData = new HashMap<String, Object>();
        if( c.reference().isDefined() )
            metaData.put("reference", c.reference().get());
        return entry(context.location(), desc, category, classification, stack, metaData);
    }

    /**
     * @return A report entry for a constraint failure detection
     */
    public static Entry csSuccess(Element context, Constraint c) {
        String category       = conf.getString("report.constraint-success.category");
        String classification = conf.getString("report.constraint-success.classification");
        String template       = conf.getString("report.constraint-success.template");
        String desc = String.format(template, c.id(), c.description());
        Map<String, Object> metaData = new HashMap<String, Object>();
        if( c.reference().isDefined() )
            metaData.put("reference", c.reference().get());
        return entry(context.location(), desc, category, classification, null, metaData);
    }

    /**
     * @return A report entry for a constraint failure detection
     */
    public static Entry csSpecError(Element context, Constraint c,List<Trace> stack) {
        String category       = conf.getString("report.constraint-spec-error.category");
        String classification = conf.getString("report.constraint-spec-error.classification");
        String template       = conf.getString("report.constraint-spec-error.template");
        String desc = String.format(template, c.id(), c.description());
        Map<String, Object> metaData = new HashMap<String, Object>();
        if( c.reference().isDefined() )
            metaData.put("reference", c.reference().get());
        return entry(context.location(), desc, category, classification, stack, metaData);
    }

    /**
     * @return A report entry for a predicate failure detection
     */
    public static Entry predicateFailure(Location l, String usageErr,
                                         String expectedUsage, String predicateDesc) {
        String category       = conf.getString("report.predicate-failure.category");
        String classification = conf.getString("report.predicate-failure.classification");
        String template       = conf.getString("report.predicate-failure.template");
        String desc = String.format(template, usageErr, expectedUsage, predicateDesc);
        return entry(l, desc, category, classification);
    }

    /* ========================================================================
     *  Value Set related entries
     * =======================================================================*/
    /**
     * @return A report entry for an excluded VS code detection
     */
    public static Entry evs(Location l, String value, ValueSet vs,
                            BindingStrength bs) {
        return vsEntry("evs", l, value, vs, bs);
    }

    /**
     * @return A report entry for a permitted VS code detection
     */
    public static Entry pvs(Location l, String value, ValueSet vs,
                            BindingStrength bs) {
        return vsEntry("pvs", l, value, vs, bs);
    }

    /**
     * @return A report entry for a VS code not found detection
     */
    public static Entry codeNotFound(Location l, String value, ValueSet vs,
                                     BindingStrength bs) {
        return vsEntry("code-not-found", l, value, vs, bs);
    }

    /**
     * @return A report entry for a VS not found detection
     */
    public static Entry vsNotFound(Location l, String value, String vsID,
                                   BindingStrength bs) {
        String category       = conf.getString("report.vs-not-found.category");
        String classification = conf.getString("report.vs-not-found.classification");
        String template       = conf.getString("report.vs-not-found.template");
        String desc = String.format(template, value, l.prettyString(), vsID);
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("bindingStrength", bs);
        return entry(l, desc, category, classification, null, metaData);
    }

    /**
     * @return A report entry for a VS code not found detection
     */
    public static Entry emptyVS(Location l, String value, ValueSet vs,
                                BindingStrength bs) {
        return vsEntry("empty-vs", l, value, vs, bs);
    }

    /**
     * @return A report entry for a VS code not found detection
     */
    public static Entry vsError(Location l, String msg, ValueSet vs,
                                BindingStrength bs) {
        String category       = conf.getString("report.vs-error.category");
        String classification = conf.getString("report.vs-error.classification");
        String template       = conf.getString("report.vs-error.template");
        String desc = String.format(template, msg);
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("valueSet", vs);
        metaData.put("bindingStrength", bs);
        return entry(l, desc, category, classification, null, metaData);
    }

    /**
     * @return A report entry for an excluded VS detection
     */
    public static Entry vsNoVal(Location l, ValueSet vs) {
        String category       = conf.getString("report.vs-no-validation.category");
        String classification = conf.getString("report.vs-no-validation.classification");
        String template       = conf.getString("report.vs-no-validation.template");
        String desc = String.format(template, vs.id());
        return entry(l, desc, category, classification);
    }

    /**
     * @return A report entry for a coded Elem detection
     */
    public static Entry codedElem(Location l, String msg, ValueSet vs,
                                  ValueSetSpec vsSpec) {
        String category       = conf.getString("report.coded-element.category");
        String classification = conf.getString("report.coded-element.classification");
        String template       = conf.getString("report.coded-element.template");
        String desc = String.format(template, msg);
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("valueSet", vs);
        metaData.put("valueSetSpec", vsSpec);
        return entry(l, desc, category, classification, null, metaData);
    }

    /* ========================================================================
     *  Helpers
     * =======================================================================*/
    private static Entry entry(
            int line,
            int column,
            String path,
            String description,
            String category,
            String classification,
            List<Trace> stackTrace,
            Map<String, Object> metaData
    ) {
        return new EntryImpl(line, column, path, description, category,
                classification, stackTrace, metaData);
    }

    private static Entry entry(
            int line,
            int column,
            String path,
            String description,
            String category,
            String classification
    ) {
        return entry(line, column, path, description, category,
                classification, null, null);
    }

    private static Entry entry(Location l, String description, String category,
                               String classification) {
        int line    = l.line();
        int column  = l.column();
        String path = l.path();
        return entry(line, column, path, description, category, classification);
    }

    private static Entry entry(Location l, String description, String category,
                               String classification, List<Trace> stackTrace,
                               Map<String, Object> metaData) {
        int line    = l.line();
        int column  = l.column();
        String path = l.path();
        return entry(line, column, path, description, category,
                classification, stackTrace, metaData);
    }

    private static Entry vsEntry(String configKey, Location l, String value,
                                 ValueSet vs, BindingStrength bs) {
        String category       = conf.getString("report."+configKey+".category");
        String classification = conf.getString("report."+configKey+".classification");
        String template       = conf.getString("report."+configKey+".template");
        String desc = String.format(template, value, l.prettyString(), vs.id());
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("valueSet", vs);
        metaData.put("bindingStrength", bs);
        return entry(l, desc, category, classification, null, metaData);
    }
}
