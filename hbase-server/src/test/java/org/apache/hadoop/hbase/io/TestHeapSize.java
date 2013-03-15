begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|RuntimeMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArraySet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantReadWriteLock
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|BlockCacheKey
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|CachedBlock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|LruBlockCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|HStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|MemStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ClassSize
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Testing the sizing that HeapSize offers and compares to the size given by  * ClassSize.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHeapSize
extends|extends
name|TestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHeapSize
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// List of classes implementing HeapSize
comment|// BatchOperation, BatchUpdate, BlockIndex, Entry, Entry<K,V>, HStoreKey
comment|// KeyValue, LruBlockCache, LruHashMap<K,V>, Put, HLogKey
annotation|@
name|BeforeClass
specifier|public
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Print detail on jvm so we know what is different should below test fail.
name|RuntimeMXBean
name|b
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"name="
operator|+
name|b
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"specname="
operator|+
name|b
operator|.
name|getSpecName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"specvendor="
operator|+
name|b
operator|.
name|getSpecVendor
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmname="
operator|+
name|b
operator|.
name|getVmName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmversion="
operator|+
name|b
operator|.
name|getVmVersion
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmvendor="
operator|+
name|b
operator|.
name|getVmVendor
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|p
init|=
name|b
operator|.
name|getSystemProperties
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"properties="
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test our hard-coded sizing of native java objects    */
specifier|public
name|void
name|testNativeSizes
parameter_list|()
throws|throws
name|IOException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Class
name|cl
init|=
literal|null
decl_stmt|;
name|long
name|expected
init|=
literal|0L
decl_stmt|;
name|long
name|actual
init|=
literal|0L
decl_stmt|;
comment|// ArrayList
name|cl
operator|=
name|ArrayList
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|ARRAYLIST
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// ByteBuffer
name|cl
operator|=
name|ByteBuffer
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|BYTE_BUFFER
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Integer
name|cl
operator|=
name|Integer
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|INTEGER
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Map.Entry
comment|// Interface is public, all others are not.  Hard to size via ClassSize
comment|//    cl = Map.Entry.class;
comment|//    expected = ClassSize.estimateBase(cl, false);
comment|//    actual = ClassSize.MAP_ENTRY;
comment|//    if(expected != actual) {
comment|//      ClassSize.estimateBase(cl, true);
comment|//      assertEquals(expected, actual);
comment|//    }
comment|// Object
name|cl
operator|=
name|Object
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|OBJECT
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// TreeMap
name|cl
operator|=
name|TreeMap
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|TREEMAP
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// String
name|cl
operator|=
name|String
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|STRING
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// ConcurrentHashMap
name|cl
operator|=
name|ConcurrentHashMap
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|CONCURRENT_HASHMAP
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// ConcurrentSkipListMap
name|cl
operator|=
name|ConcurrentSkipListMap
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|CONCURRENT_SKIPLISTMAP
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// ReentrantReadWriteLock
name|cl
operator|=
name|ReentrantReadWriteLock
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|REENTRANT_LOCK
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// AtomicLong
name|cl
operator|=
name|AtomicLong
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|ATOMIC_LONG
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// AtomicInteger
name|cl
operator|=
name|AtomicInteger
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|ATOMIC_INTEGER
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// AtomicBoolean
name|cl
operator|=
name|AtomicBoolean
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|ATOMIC_BOOLEAN
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// CopyOnWriteArraySet
name|cl
operator|=
name|CopyOnWriteArraySet
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|COPYONWRITE_ARRAYSET
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// CopyOnWriteArrayList
name|cl
operator|=
name|CopyOnWriteArrayList
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ClassSize
operator|.
name|COPYONWRITE_ARRAYLIST
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Testing the classes that implements HeapSize and are a part of 0.20.    * Some are not tested here for example BlockIndex which is tested in    * TestHFile since it is a non public class    * @throws IOException    */
specifier|public
name|void
name|testSizes
parameter_list|()
throws|throws
name|IOException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Class
name|cl
init|=
literal|null
decl_stmt|;
name|long
name|expected
init|=
literal|0L
decl_stmt|;
name|long
name|actual
init|=
literal|0L
decl_stmt|;
comment|//KeyValue
name|cl
operator|=
name|KeyValue
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|()
decl_stmt|;
name|actual
operator|=
name|kv
operator|.
name|heapSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|//Put
name|cl
operator|=
name|Put
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|//The actual TreeMap is not included in the above calculation
name|expected
operator|+=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|TREEMAP
operator|+
name|ClassSize
operator|.
name|REFERENCE
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|'0'
block|}
argument_list|)
decl_stmt|;
name|actual
operator|=
name|put
operator|.
name|heapSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|//LruBlockCache Overhead
name|cl
operator|=
name|LruBlockCache
operator|.
name|class
expr_stmt|;
name|actual
operator|=
name|LruBlockCache
operator|.
name|CACHE_FIXED_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// CachedBlock Fixed Overhead
comment|// We really need "deep" sizing but ClassSize does not do this.
comment|// Perhaps we should do all these more in this style....
name|cl
operator|=
name|CachedBlock
operator|.
name|class
expr_stmt|;
name|actual
operator|=
name|CachedBlock
operator|.
name|PER_BLOCK_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ByteBuffer
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ByteBuffer
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// MemStore Overhead
name|cl
operator|=
name|MemStore
operator|.
name|class
expr_stmt|;
name|actual
operator|=
name|MemStore
operator|.
name|FIXED_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// MemStore Deep Overhead
name|actual
operator|=
name|MemStore
operator|.
name|DEEP_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ReentrantReadWriteLock
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|AtomicLong
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ConcurrentSkipListMap
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ConcurrentSkipListMap
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|CopyOnWriteArraySet
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|expected
operator|+=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|CopyOnWriteArrayList
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ReentrantReadWriteLock
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|AtomicLong
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|ConcurrentSkipListMap
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|CopyOnWriteArraySet
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|CopyOnWriteArrayList
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Store Overhead
name|cl
operator|=
name|HStore
operator|.
name|class
expr_stmt|;
name|actual
operator|=
name|HStore
operator|.
name|FIXED_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Region Overhead
name|cl
operator|=
name|HRegion
operator|.
name|class
expr_stmt|;
name|actual
operator|=
name|HRegion
operator|.
name|FIXED_OVERHEAD
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Block cache key overhead
name|cl
operator|=
name|BlockCacheKey
operator|.
name|class
expr_stmt|;
comment|// Passing zero length file name, because estimateBase does not handle
comment|// deep overhead.
name|actual
operator|=
operator|new
name|BlockCacheKey
argument_list|(
literal|""
argument_list|,
literal|0
argument_list|)
operator|.
name|heapSize
argument_list|()
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|// Currently NOT testing Deep Overheads of many of these classes.
comment|// Deep overheads cover a vast majority of stuff, but will not be 100%
comment|// accurate because it's unclear when we're referencing stuff that's already
comment|// accounted for.  But we have satisfied our two core requirements.
comment|// Sizing is quite accurate now, and our tests will throw errors if
comment|// any of these classes are modified without updating overhead sizes.
block|}
block|}
end_class

end_unit

