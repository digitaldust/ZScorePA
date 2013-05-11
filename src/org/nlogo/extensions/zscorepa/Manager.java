package org.nlogo.extensions.zscorepa;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.PrimitiveManager;

/**
 *
 * @author Simone Gabbriellini
 */
public class Manager extends DefaultClassManager {
    
    @Override
    public void load(PrimitiveManager pm) throws ExtensionException {
        
        // statistics about an already existing network
        pm.addPrimitive("empirical-stats", new Statistics());
        pm.addPrimitive("random-generate", new RandomGenerate());
        pm.addPrimitive("zpa-generate", new ZpaGenerate());
    }
    
}
