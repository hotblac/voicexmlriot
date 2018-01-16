package org.vxmlriot.jvoicexml;

import org.jvoicexml.profile.TagStrategy;
import org.jvoicexml.profile.TagStrategyFactory;
import org.jvoicexml.xml.vxml.Vxml;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Tag strategy factory for non-Spring configuration of JVoiceXML
 */
public class EmbeddedTagStrategyFactory
    implements TagStrategyFactory {
    /**
     * Known strategies. The known strategies are templates for the strategy to
     * be executed by the <code>ForminterpreteationAlgorithm</code>.
     */
    private final Map<String, TagStrategy> strategies;

    /**
     * Creates a new object.
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    public EmbeddedTagStrategyFactory() 
        throws InstantiationException, IllegalAccessException,
        ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        strategies = new java.util.HashMap<String, TagStrategy>();
        strategies.put("assign",
         loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.AssignStrategy"));
        strategies.put("audio",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.AudioTagStrategy"));
        strategies.put("clear",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ClearStrategy"));
        strategies.put("data",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.DataStrategy"));
        strategies.put("disconnect",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.DisconnectStrategy"));
        strategies.put("exit",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ExitStrategy"));
        strategies.put("goto",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.GotoStrategy"));
        strategies.put("grammar",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.GrammarStrategy"));
        strategies.put("if",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.IfStrategy"));
        strategies.put("log",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.LogStrategy"));
        strategies.put("prompt",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.PromptStrategy"));
        strategies.put("reprompt",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.RepromptStrategy"));
        strategies.put("return",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ReturnStrategy"));
        strategies.put("script",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ScriptStrategy"));
        strategies.put("submit",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.SubmitStrategy"));
        strategies.put("#text",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.TextStrategy"));
        strategies.put("throw",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ThrowStrategy"));
        strategies.put("submit",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.SubmitStrategy"));
        strategies.put("value",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.ValueStrategy"));
        strategies.put("valvarue",
                loadStrategy("org.jvoicexml.profile.vxml21.tagstrategy.VarStrategy"));
    }

    /**
     * Loads the specified tag strategy.
     * @param name name of the class to load
     * @return loaded tag strategy
     * @throws InstantiationException
     *         unable to create the tag strategy
     * @throws IllegalAccessException
     *         unable to create the tag strategy
     * @throws ClassNotFoundException
     *         unable to create the tag strategy
     * @throws NoSuchMethodException 
     *         unable to create the tag strategy
     * @throws SecurityException 
     *         unable to create the tag strategy
     * @throws InvocationTargetException 
     *         unable to create the tag strategy
     * @throws IllegalArgumentException 
     *         unable to create the tag strategy
     */
    private TagStrategy loadStrategy(final String name)
        throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {
        final Class<?> clazz = Class.forName(name);
        @SuppressWarnings("rawtypes")
        final Constructor constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (TagStrategy) constructor.newInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagStrategy getTagStrategy(final Node node) {
        if (node == null) {
            return null;
        }

        final String tagName = node.getNodeName();
        return getTagStrategy(tagName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagStrategy getTagStrategy(final String tag) {
        if (tag == null) {
            return null;
        }
        final TagStrategy strategy = strategies.get(tag);
        if (strategy == null) {
            return null;
        }

        return strategy.newInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getTagNamespace() throws URISyntaxException {
        return new URI(Vxml.DEFAULT_XMLNS);
    }

}
