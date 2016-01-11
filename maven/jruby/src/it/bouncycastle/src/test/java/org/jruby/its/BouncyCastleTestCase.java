package org.jruby.its;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jruby.embed.ScriptingContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BouncyCastleTestCase {

    @Before
    public void setupProvider() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void java() {
        assertEquals("BouncyCastle Security Provider v1.47", new BouncyCastleProvider().getInfo());
    }

    @Test
    public void ruby(){
        ScriptingContainer container = new ScriptingContainer();
        container.setClassloaderDelegate(false);
        Object result = container.parse( "require 'openssl'; Java::OrgBouncycastleJceProvider::BouncyCastleProvider.new.info").run();
        Object version = container.parse( "require 'jopenssl/version.rb'; Jopenssl::BOUNCY_CASTLE_VERSION").run();
        assertEquals( "BouncyCastle Security Provider v" + version, result.toString() );

        result = container.parse("JRuby.runtime.jruby_class_loader").run();
        assertEquals("org.jruby.util.SelfFirstJRubyClassLoader", result.toString().replaceFirst("@.*$", ""));
    }

}
