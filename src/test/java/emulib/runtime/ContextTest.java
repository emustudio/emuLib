/*
 * ContextTest.java
 * 
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2012, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package emulib.runtime;

import emulib.emustudio.APITest;
import emulib.plugins.compiler.ICompilerContext;
import emulib.plugins.cpu.ICPUContext;
import emulib.plugins.device.IDeviceContext;
import emulib.plugins.memory.IMemory.IMemListener;
import emulib.plugins.memory.IMemoryContext;
import emulib.runtime.interfaces.IConnections;
import junit.framework.TestCase;

/**
 *
 * @author vbmacher
 */
public class ContextTest extends TestCase {

    public class MockCPUContext implements C4664566E71E3C14D1732E34E2F66E8E31EE6951E {
        @Override
        public String getID() {
            return null;
        }
        @Override
        public boolean isInterruptSupported() {
            return false;
        }
        @Override
        public void setInterrupt(IDeviceContext device, int mask) {}
        @Override
        public void clearInterrupt(IDeviceContext device, int mask) {}
        @Override
        public void testMethod() {}
    }

    public class MockCompilerContext implements CD04C490FCBB878A5609DE8671C9FCC797D9702A0 {
        @Override
        public String getID() {
           return "MockCompilerContext";
        }
        @Override
        public void testCompilerMethod() {}
    }
    
    public class MockMemoryContext implements C4A08669110A42BC60DD75BD4EE7CDDC6E15EA091 {
        @Override
        public Object read(int from) {
            return null;
        }
        @Override
        public Object readWord(int from) {
            return null;
        }
        @Override
        public void write(int to, Object val) {}
        @Override
        public void writeWord(int to, Object val) {}
        @Override
        public Class getDataType() {
            return null;
        }
        @Override
        public void clearMemory() {}
        @Override
        public void addMemoryListener(IMemListener listener) {}
        @Override
        public void removeMemoryListener(IMemListener listener) {}
        @Override
        public String getID() {
            return "MockMemoryContext";
        }
        @Override
        public void testMemoryMethod() {}
    }
    
    public class MockDeviceContext implements CAC9BAC64A2ECE781605CE866F758692BB7F8FF36 {
        @Override
        public Object read() {
            return null;
        }
        @Override
        public void write(Object val) {}
        @Override
        public Class getDataType() {
            return null;
        }
        @Override
        public String getID() {
            return "MockDeviceContext";
        }
        @Override
        public void testDeviceMethod() {}
    }
        
    public class MockBadHashCPUContext implements IMockBadHashCPUContext {
        @Override
        public String getID() {
            return null;
        }
        @Override
        public boolean isInterruptSupported() {
            return false;
        }
        @Override
        public void setInterrupt(IDeviceContext device, int mask) {}
        @Override
        public void clearInterrupt(IDeviceContext device, int mask) {}
        @Override
        public void testMethod() {}
    }
    public class MockBadHashMemoryContext implements IMockBadHashMemoryContext {
        @Override
        public Object read(int from) {
            return null;
        }
        @Override
        public Object readWord(int from) {
            return null;
        }
        @Override
        public void write(int to, Object val) {}
        @Override
        public void writeWord(int to, Object val) {}
        @Override
        public Class getDataType() {
            return null;
        }
        @Override
        public void clearMemory() {}
        @Override
        public void addMemoryListener(IMemListener listener) {}
        @Override
        public void removeMemoryListener(IMemListener listener) {}
        @Override
        public String getID() {
            return "MockMemoryContext";
        }
        @Override
        public void testMemoryMethod() {}
    }
    public class MockBadHashCompilerContext implements IMockBadHashCompilerContext {
        @Override
        public String getID() {
           return "MockCompilerContext";
        }
        @Override
        public void testCompilerMethod() {}
    }
    public class MockBadHashDeviceContext implements IMockBadHashDeviceContext {
        @Override
        public Object read() {
            return null;
        }
        @Override
        public void write(Object val) {}
        @Override
        public Class getDataType() {
            return null;
        }
        @Override
        public String getID() {
            return "MockDeviceContext";
        }
        @Override
        public void testDeviceMethod() {}
    }

    
    /**
     * Test if context is singleton
     */
    public void testContextSingleton() {
        Context context1 = Context.getInstance();
        assertNotNull(context1);
        assertEquals(context1, Context.getInstance());
    }
    
    /**
     * Test successful registration of plug-in contexts
     */
    public void testContextOperations() throws AlreadyRegisteredException,
            InvalidImplementationException, InvalidHashException {
        MockCPUContext cpuContext = new MockCPUContext();
        MockCompilerContext compilerContext = new MockCompilerContext();
        MockMemoryContext memoryContext = new MockMemoryContext();
        MockDeviceContext deviceContext = new MockDeviceContext();
        
        Context cInstance = Context.getInstance();
        APITest.assignEmuStudioPassword();
        
        assertTrue(cInstance.assignComputer(APITest.getEmuStudioPassword(), new IConnections() {

            @Override
            public int getPluginType(long pluginID) {
                switch ((int)pluginID) {
                    case 0:
                        return TYPE_CPU;
                    case 1:
                        return TYPE_COMPILER;
                    case 2:
                        return TYPE_MEMORY;
                    case 3:
                        return TYPE_DEVICE;
                    default:
                        return TYPE_UNKNOWN;
                }
            }

            @Override
            public boolean isConnected(long pluginID, long toPluginID) {
                return true;
            }
        }));
        
        // register CPU
        assertTrue(cInstance.register(0, cpuContext, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class));
        ICPUContext getCPUContext = cInstance.getCPUContext(0, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class);
        assertNotNull(getCPUContext);
        assertEquals(cpuContext, getCPUContext);
        
        // register compiler
        assertTrue(cInstance.register(1, compilerContext, ICompilerContext.class));
        ICompilerContext getCompilerContext = cInstance.getCompilerContext(1, ICompilerContext.class);
        assertNotNull(getCompilerContext);
        assertEquals(compilerContext, getCompilerContext);
        
        // register memory
        assertTrue(cInstance.register(2, memoryContext, IMemoryContext.class));
        IMemoryContext getMemoryContext = cInstance.getMemoryContext(2, IMemoryContext.class);
        assertNotNull(getMemoryContext);
        assertEquals(memoryContext, getMemoryContext);
        
        // register device
        assertTrue(cInstance.register(3, deviceContext, IDeviceContext.class));
        IDeviceContext getDeviceContext = cInstance.getDeviceContext(3, IDeviceContext.class);
        assertNotNull(getDeviceContext);
        assertEquals(deviceContext, getDeviceContext);
        
        // unregister all contexts
        assertTrue(cInstance.unregister(0, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class));
        assertTrue(cInstance.unregister(1, ICompilerContext.class));
        assertTrue(cInstance.unregister(2, IMemoryContext.class));
        assertTrue(cInstance.unregister(3, IDeviceContext.class));
    }

    /**
     * Tests invalid implementation of contexts and emuLib responds.
     * 
     * @throws AlreadyRegisteredException shouldn't throw
     * @throws InvalidHashException shouldn't throw
     */
    public void testInvalidImplementation() throws AlreadyRegisteredException, InvalidHashException {
        Context cInstance = Context.getInstance();
        APITest.assignEmuStudioPassword();
        try {
            // test different context registration
            MockMemoryContext memoryContext = new MockMemoryContext();
            cInstance.register(1, memoryContext, ICPUContext.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, ICPUContext.class));

        try {
            // test different context classes with same content
            ICPUContext cpuContext = new MockBadHashCPUContext();
            // Correct hash is set as parameter, but class has wrong name.
            cInstance.register(1, cpuContext, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class));
        
        
        // ------- Memory -------
        try {
            MockCPUContext cpuContext = new MockCPUContext();
            cInstance.register(1, cpuContext, IMemoryContext.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, IMemoryContext.class));

        try {
            // test different context classes with same content
            IMemoryContext memoryContext = new MockBadHashMemoryContext();
            // Correct hash is set as parameter, but class has wrong name.
            cInstance.register(1, memoryContext, C4A08669110A42BC60DD75BD4EE7CDDC6E15EA091.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, C4A08669110A42BC60DD75BD4EE7CDDC6E15EA091.class));
        
        
        // ------- Compiler ------
        
        try {
            MockCPUContext cpuContext = new MockCPUContext();
            cInstance.register(1, cpuContext, ICompilerContext.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, ICompilerContext.class));

        try {
            // test different context classes with same content
            ICompilerContext compilerContext = new MockBadHashCompilerContext();
            // Correct hash is set as parameter, but class has wrong name.
            cInstance.register(1, compilerContext, CD04C490FCBB878A5609DE8671C9FCC797D9702A0.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, CD04C490FCBB878A5609DE8671C9FCC797D9702A0.class));
        
        // ------- Device --------
        try {
            MockCPUContext cpuContext = new MockCPUContext();
            cInstance.register(1, cpuContext, IDeviceContext.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, IDeviceContext.class));

        try {
            // test different context classes with same content
            IDeviceContext deviceContext = new MockBadHashDeviceContext();
            // Correct hash is set as parameter, but class has wrong name.
            cInstance.register(1, deviceContext, CAC9BAC64A2ECE781605CE866F758692BB7F8FF36.class);
            fail("Context.register() method didn't throw InvalidImplementationException!");
        } catch (InvalidImplementationException e) {}
        assertFalse(cInstance.unregister(1, CAC9BAC64A2ECE781605CE866F758692BB7F8FF36.class));
    }

    /**
     * Test invalid hash of contexts and emuLib responds.
     * 
     * @throws AlreadyRegisteredException shouldn't throw
     * @throws InvalidImplementationException shouldn't throw
     */
    public void testInvalidHash() throws AlreadyRegisteredException, InvalidImplementationException {
        Context cInstance = Context.getInstance();
        APITest.assignEmuStudioPassword();
               
        // CPU
        try {
            ICPUContext cpuContext = new MockBadHashCPUContext();
            cInstance.register(1, cpuContext, IMockBadHashCPUContext.class);
            fail("Context.register() method didn't throw InvalidHashException!");
        } catch (InvalidHashException e) {}
        assertFalse(cInstance.unregister(1, IMockBadHashCPUContext.class));

        // Memory
        try {
            IMemoryContext memoryContext = new MockBadHashMemoryContext();
            cInstance.register(1, memoryContext, IMockBadHashMemoryContext.class);
            fail("Context.register() method didn't throw InvalidHashException!");
        } catch (InvalidHashException e) {}
        assertFalse(cInstance.unregister(1, IMockBadHashMemoryContext.class));
        
        // Compiler
        try {
            ICompilerContext compilerContext = new MockBadHashCompilerContext();
            cInstance.register(1, compilerContext, IMockBadHashCompilerContext.class);
            fail("Context.register() method didn't throw InvalidHashException!");
        } catch (InvalidHashException e) {}
        assertFalse(cInstance.unregister(1, IMockBadHashCompilerContext.class));
        
        // Device
        try {
            IDeviceContext deviceContext = new MockBadHashDeviceContext();
            cInstance.register(1, deviceContext, IMockBadHashDeviceContext.class);
            fail("Context.register() method didn't throw InvalidHashException!");
        } catch (InvalidHashException e) {}
        assertFalse(cInstance.unregister(1, IMockBadHashDeviceContext.class));
    }

    /**
     * Tests multi-registration of contexts and emuLib responses.
     * 
     * @throws InvalidImplementationException shouldn't throw
     * @throws InvalidHashException shouldn't throw
     */
    public void testAlreadyRegistered() throws InvalidImplementationException, InvalidHashException {
        Context cInstance = Context.getInstance();
        APITest.assignEmuStudioPassword();

        // CPU
        try {
            ICPUContext cpuContext = new MockBadHashCPUContext();
            // Should pass
            assertTrue(cInstance.register(1, cpuContext, ICPUContext.class));
            assertFalse(cInstance.register(1, cpuContext, ICPUContext.class));
            fail("Context.register() method didn't throw AlreadyRegisteredException!");
        } catch (AlreadyRegisteredException e) {}
        assertTrue(cInstance.unregister(1, ICPUContext.class));
        assertFalse(cInstance.unregister(1, ICPUContext.class));

        // Memory
        try {
            MockMemoryContext memoryContext = new MockMemoryContext();
            // Should pass
            assertTrue(cInstance.register(1, memoryContext, IMemoryContext.class));
            assertFalse(cInstance.register(1, memoryContext, IMemoryContext.class));
            fail("Context.register() method didn't throw AlreadyRegisteredException!");
        } catch (AlreadyRegisteredException e) {}
        assertTrue(cInstance.unregister(1, IMemoryContext.class));
        assertFalse(cInstance.unregister(1, IMemoryContext.class));
        
        // Compiler
        try {
            MockCompilerContext compilerContext = new MockCompilerContext();
            // Should pass
            assertTrue(cInstance.register(1, compilerContext, ICompilerContext.class));
            assertFalse(cInstance.register(1, compilerContext, ICompilerContext.class));
            fail("Context.register() method didn't throw AlreadyRegisteredException!");
        } catch (AlreadyRegisteredException e) {}
        assertTrue(cInstance.unregister(1, ICompilerContext.class));
        assertFalse(cInstance.unregister(1, ICompilerContext.class));
        
        // Device
        try {
            MockDeviceContext deviceContext = new MockDeviceContext();
            assertTrue(cInstance.register(1, deviceContext, IDeviceContext.class));
            assertFalse(cInstance.register(1, deviceContext, IDeviceContext.class));
            fail("Context.register() method didn't throw AlreadyRegisteredException!");
        } catch (AlreadyRegisteredException e) {}
        assertTrue(cInstance.unregister(1, IDeviceContext.class));
        assertFalse(cInstance.unregister(1, IDeviceContext.class));
    }
        
}
