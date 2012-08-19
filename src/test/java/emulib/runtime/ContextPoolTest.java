/*
 * ContextPoolTest.java
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
import emulib.plugins.Context;
import emulib.plugins.compiler.CompilerContext;
import emulib.plugins.cpu.CPUContext;
import emulib.plugins.device.DeviceContext;
import emulib.plugins.memory.Memory.MemoryListener;
import emulib.plugins.memory.MemoryContext;
import emulib.runtime.interfaces.PluginConnections;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vbmacher
 */
public class ContextPoolTest {

    public class CPUContextStub implements CPUContextInterface {
        @Override
        public boolean isInterruptSupported() {
            return false;
        }
        @Override
        public void setInterrupt(DeviceContext device, int mask) {}
        @Override
        public void clearInterrupt(DeviceContext device, int mask) {}
        @Override
        public void testMethod() {}
    }

    public class CompilerContextStub implements CompilerContextInterface {
        @Override
        public void testCompilerMethod() {}
    }
    
    public class MemoryContextStub implements MemoryContextInterface {
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
        public void clear() {}
        @Override
        public void addMemoryListener(MemoryListener listener) {}
        @Override
        public void removeMemoryListener(MemoryListener listener) {}
        @Override
        public void testMemoryMethod() {}
    }
    
    public class DeviceContextStub implements DeviceContextInterface {
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
        public void testDeviceMethod() {}
    }
    
    public class UnannotatedContextStub implements UnannotatedContextInterface {

        @Override
        public void testMethod() {
            
        }
        
    }
        
    /**
     * Test if context is singleton
     */
    @Test
    public void testContextSingleton() {
        ContextPool context1 = ContextPool.getInstance();
        Assert.assertNotNull(context1);
        Assert.assertEquals(context1, ContextPool.getInstance());
    }
    
    /**
     * Test successful registration of plug-in contexts
     */
    @Test
    public void testContextOperations() throws AlreadyRegisteredException, InvalidContextException {
        CPUContextStub cpuContext = new CPUContextStub();
        CompilerContextStub compilerContext = new CompilerContextStub();
        MemoryContextStub memoryContext = new MemoryContextStub();
        DeviceContextStub deviceContext = new DeviceContextStub();
        
        ContextPool cInstance = ContextPool.getInstance();
        APITest.assignEmuStudioPassword();
        
        Assert.assertTrue(cInstance.setComputer(APITest.getEmuStudioPassword(), new PluginConnections() {

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
        cInstance.register(0, cpuContext, CPUContextInterface.class);
        CPUContext getCPUContext = cInstance.getCPUContext(0, CPUContextInterface.class);
        Assert.assertNotNull(getCPUContext);
        Assert.assertEquals(cpuContext, getCPUContext);
        
        // register compiler
        cInstance.register(1, compilerContext, CompilerContext.class);
        CompilerContext getCompilerContext = cInstance.getCompilerContext(1, CompilerContext.class);
        Assert.assertNotNull(getCompilerContext);
        Assert.assertEquals(compilerContext, getCompilerContext);
        
        // register memory
        cInstance.register(2, memoryContext, MemoryContext.class);
        MemoryContext getMemoryContext = cInstance.getMemoryContext(2, MemoryContext.class);
        Assert.assertNotNull(getMemoryContext);
        Assert.assertEquals(memoryContext, getMemoryContext);
        
        // register device
        cInstance.register(3, deviceContext, DeviceContext.class);
        DeviceContext getDeviceContext = cInstance.getDeviceContext(3, DeviceContext.class);
        Assert.assertNotNull(getDeviceContext);
        Assert.assertEquals(deviceContext, getDeviceContext);
        
        // unregister all contexts
        Assert.assertTrue(cInstance.unregister(0, CPUContextInterface.class));
        Assert.assertTrue(cInstance.unregister(1, CompilerContext.class));
        Assert.assertTrue(cInstance.unregister(2, MemoryContext.class));
        Assert.assertTrue(cInstance.unregister(3, DeviceContext.class));
    }

    /**
     * Tests invalid implementation of contexts and emuLib responds.
     * 
     * @throws AlreadyRegisteredException shouldn't throw
     */
    @Test
    public void testInvalidContext() throws AlreadyRegisteredException {
        ContextPool cInstance = ContextPool.getInstance();
        APITest.assignEmuStudioPassword();
        try {
            // test different context registration
            MemoryContextStub memoryContext = new MemoryContextStub();
            cInstance.register(1, memoryContext, CPUContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (InvalidContextException e) {}
        Assert.assertFalse(cInstance.unregister(1, CPUContext.class));

        try {
            // test unannotated context class
            Context context = new UnannotatedContextStub();
            cInstance.register(1, context, UnannotatedContextInterface.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (InvalidContextException e) {}
        
        try {
            CPUContextStub cpuContext = new CPUContextStub();
            cInstance.register(1, cpuContext, MemoryContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (InvalidContextException e) {}
        Assert.assertFalse(cInstance.unregister(1, MemoryContext.class));

        try {
            CPUContextStub cpuContext = new CPUContextStub();
            cInstance.register(1, cpuContext, CompilerContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (InvalidContextException e) {}
        Assert.assertFalse(cInstance.unregister(1, CompilerContext.class));

        try {
            CPUContextStub cpuContext = new CPUContextStub();
            cInstance.register(1, cpuContext, DeviceContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (InvalidContextException e) {}
        Assert.assertFalse(cInstance.unregister(1, DeviceContext.class));
    }

    /**
     * Tests multi-registration of contexts and emuLib responses.
     * 
     * @throws InvalidContextException shouldn't throw
     * @throws InvalidHashException shouldn't throw
     */
    @Test
    public void testAlreadyRegistered() throws InvalidContextException {
        ContextPool cInstance = ContextPool.getInstance();
        APITest.assignEmuStudioPassword();

        // CPU
        try {
            CPUContext cpuContext = new CPUContextStub();
            // Should pass
            cInstance.register(1, cpuContext, CPUContext.class);
            cInstance.register(2, cpuContext, CPUContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (AlreadyRegisteredException e) {}
        Assert.assertTrue(cInstance.unregister(1, CPUContext.class));
        Assert.assertFalse(cInstance.unregister(1, CPUContext.class));

        // Memory
        try {
            MemoryContextStub memoryContext = new MemoryContextStub();
            // Should pass
            cInstance.register(1, memoryContext, MemoryContext.class);
            cInstance.register(2, memoryContext, MemoryContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (AlreadyRegisteredException e) {}
        Assert.assertTrue(cInstance.unregister(1, MemoryContext.class));
        Assert.assertFalse(cInstance.unregister(1, MemoryContext.class));
        
        // Compiler
        try {
            CompilerContextStub compilerContext = new CompilerContextStub();
            // Should pass
            cInstance.register(1, compilerContext, CompilerContext.class);
            cInstance.register(2, compilerContext, CompilerContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (AlreadyRegisteredException e) {}
        Assert.assertTrue(cInstance.unregister(1, CompilerContext.class));
        Assert.assertFalse(cInstance.unregister(1, CompilerContext.class));
        
        // Device
        try {
            DeviceContextStub deviceContext = new DeviceContextStub();
            cInstance.register(1, deviceContext, DeviceContext.class);
            cInstance.register(2, deviceContext, DeviceContext.class);
            Assert.fail("Context.register() method didn't throw!");
        } catch (AlreadyRegisteredException e) {}
        Assert.assertTrue(cInstance.unregister(1, DeviceContext.class));
        Assert.assertFalse(cInstance.unregister(1, DeviceContext.class));
    }
        
}
