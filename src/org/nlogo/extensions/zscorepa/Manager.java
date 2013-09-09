package org.nlogo.extensions.zscorepa;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.PrimitiveManager;

/**
 *
 * @author Simone Gabbriellini
 */
public class Manager extends DefaultClassManager {
    
    /**
     * Execute one of the custom primitives of the extension.
     * @param pm
     * @throws ExtensionException
     */
    @Override
    public void load(PrimitiveManager pm) throws ExtensionException {
        
        // statistics about an already existing network
        pm.addPrimitive("empirical-stats", new Statistics());
        // generate a random network
        pm.addPrimitive("random-generate", new RandomGenerate());
        // generate a ZPA network
        pm.addPrimitive("zpa-generate", new ZpaGenerate());
    }
    
}
