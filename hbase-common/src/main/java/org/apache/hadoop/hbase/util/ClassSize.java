begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Class for determining the "size" of a class, an attempt to calculate the  * actual bytes that an object of this class will occupy in memory  *  * The core of this class is taken from the Derby project  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClassSize
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ClassSize
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Array overhead */
specifier|public
specifier|static
specifier|final
name|int
name|ARRAY
decl_stmt|;
comment|/** Overhead for ArrayList(0) */
specifier|public
specifier|static
specifier|final
name|int
name|ARRAYLIST
decl_stmt|;
comment|/** Overhead for LinkedList(0) */
specifier|public
specifier|static
specifier|final
name|int
name|LINKEDLIST
decl_stmt|;
comment|/** Overhead for a single entry in LinkedList */
specifier|public
specifier|static
specifier|final
name|int
name|LINKEDLIST_ENTRY
decl_stmt|;
comment|/** Overhead for ByteBuffer */
specifier|public
specifier|static
specifier|final
name|int
name|BYTE_BUFFER
decl_stmt|;
comment|/** Overhead for an Integer */
specifier|public
specifier|static
specifier|final
name|int
name|INTEGER
decl_stmt|;
comment|/** Overhead for entry in map */
specifier|public
specifier|static
specifier|final
name|int
name|MAP_ENTRY
decl_stmt|;
comment|/** Object overhead is minimum 2 * reference size (8 bytes on 64-bit) */
specifier|public
specifier|static
specifier|final
name|int
name|OBJECT
decl_stmt|;
comment|/** Reference size is 8 bytes on 64-bit, 4 bytes on 32-bit */
specifier|public
specifier|static
specifier|final
name|int
name|REFERENCE
decl_stmt|;
comment|/** String overhead */
specifier|public
specifier|static
specifier|final
name|int
name|STRING
decl_stmt|;
comment|/** Overhead for TreeMap */
specifier|public
specifier|static
specifier|final
name|int
name|TREEMAP
decl_stmt|;
comment|/** Overhead for ConcurrentHashMap */
specifier|public
specifier|static
specifier|final
name|int
name|CONCURRENT_HASHMAP
decl_stmt|;
comment|/** Overhead for ConcurrentHashMap.Entry */
specifier|public
specifier|static
specifier|final
name|int
name|CONCURRENT_HASHMAP_ENTRY
decl_stmt|;
comment|/** Overhead for ConcurrentHashMap.Segment */
specifier|public
specifier|static
specifier|final
name|int
name|CONCURRENT_HASHMAP_SEGMENT
decl_stmt|;
comment|/** Overhead for ConcurrentSkipListMap */
specifier|public
specifier|static
specifier|final
name|int
name|CONCURRENT_SKIPLISTMAP
decl_stmt|;
comment|/** Overhead for ConcurrentSkipListMap Entry */
specifier|public
specifier|static
specifier|final
name|int
name|CONCURRENT_SKIPLISTMAP_ENTRY
decl_stmt|;
comment|/** Overhead for CellFlatMap */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_FLAT_MAP
decl_stmt|;
comment|/** Overhead for CellChunkMap */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_CHUNK_MAP
decl_stmt|;
comment|/** Overhead for Cell Chunk Map Entry */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_CHUNK_MAP_ENTRY
decl_stmt|;
comment|/** Overhead for CellArrayMap */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_ARRAY_MAP
decl_stmt|;
comment|/** Overhead for Cell Array Entry */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_ARRAY_MAP_ENTRY
decl_stmt|;
comment|/** Overhead for ReentrantReadWriteLock */
specifier|public
specifier|static
specifier|final
name|int
name|REENTRANT_LOCK
decl_stmt|;
comment|/** Overhead for AtomicLong */
specifier|public
specifier|static
specifier|final
name|int
name|ATOMIC_LONG
decl_stmt|;
comment|/** Overhead for AtomicInteger */
specifier|public
specifier|static
specifier|final
name|int
name|ATOMIC_INTEGER
decl_stmt|;
comment|/** Overhead for AtomicBoolean */
specifier|public
specifier|static
specifier|final
name|int
name|ATOMIC_BOOLEAN
decl_stmt|;
comment|/** Overhead for AtomicReference */
specifier|public
specifier|static
specifier|final
name|int
name|ATOMIC_REFERENCE
decl_stmt|;
comment|/** Overhead for CopyOnWriteArraySet */
specifier|public
specifier|static
specifier|final
name|int
name|COPYONWRITE_ARRAYSET
decl_stmt|;
comment|/** Overhead for CopyOnWriteArrayList */
specifier|public
specifier|static
specifier|final
name|int
name|COPYONWRITE_ARRAYLIST
decl_stmt|;
comment|/** Overhead for timerange */
specifier|public
specifier|static
specifier|final
name|int
name|TIMERANGE
decl_stmt|;
comment|/** Overhead for SyncTimeRangeTracker */
specifier|public
specifier|static
specifier|final
name|int
name|SYNC_TIMERANGE_TRACKER
decl_stmt|;
comment|/** Overhead for NonSyncTimeRangeTracker */
specifier|public
specifier|static
specifier|final
name|int
name|NON_SYNC_TIMERANGE_TRACKER
decl_stmt|;
comment|/** Overhead for CellSkipListSet */
specifier|public
specifier|static
specifier|final
name|int
name|CELL_SET
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|STORE_SERVICES
decl_stmt|;
comment|/**    * MemoryLayout abstracts details about the JVM object layout. Default implementation is used in    * case Unsafe is not available.    */
specifier|private
specifier|static
class|class
name|MemoryLayout
block|{
name|int
name|headerSize
parameter_list|()
block|{
return|return
literal|2
operator|*
name|oopSize
argument_list|()
return|;
block|}
name|int
name|arrayHeaderSize
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|align
argument_list|(
literal|3
operator|*
name|oopSize
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Return the size of an "ordinary object pointer". Either 4 or 8, depending on 32/64 bit,      * and CompressedOops      */
name|int
name|oopSize
parameter_list|()
block|{
return|return
name|is32BitJVM
argument_list|()
condition|?
literal|4
else|:
literal|8
return|;
block|}
comment|/**      * Aligns a number to 8.      * @param num number to align to 8      * @return smallest number&gt;= input that is a multiple of 8      */
specifier|public
name|long
name|align
parameter_list|(
name|long
name|num
parameter_list|)
block|{
comment|//The 7 comes from that the alignSize is 8 which is the number of bytes
comment|//stored and sent together
return|return
operator|(
operator|(
name|num
operator|+
literal|7
operator|)
operator|>>
literal|3
operator|)
operator|<<
literal|3
return|;
block|}
name|long
name|sizeOfByteArray
parameter_list|(
name|int
name|len
parameter_list|)
block|{
return|return
name|align
argument_list|(
name|ARRAY
operator|+
name|len
argument_list|)
return|;
block|}
block|}
comment|/**    * UnsafeLayout uses Unsafe to guesstimate the object-layout related parameters like object header    * sizes and oop sizes    * See HBASE-15950.    */
specifier|private
specifier|static
class|class
name|UnsafeLayout
extends|extends
name|MemoryLayout
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
class|class
name|HeaderSize
block|{
specifier|private
name|byte
name|a
decl_stmt|;
block|}
specifier|public
name|UnsafeLayout
parameter_list|()
block|{     }
annotation|@
name|Override
name|int
name|headerSize
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|int
operator|)
name|UnsafeAccess
operator|.
name|theUnsafe
operator|.
name|objectFieldOffset
argument_list|(
name|HeaderSize
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"a"
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
decl||
name|SecurityException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|headerSize
argument_list|()
return|;
block|}
annotation|@
name|Override
name|int
name|arrayHeaderSize
parameter_list|()
block|{
return|return
name|UnsafeAccess
operator|.
name|theUnsafe
operator|.
name|arrayBaseOffset
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
name|int
name|oopSize
parameter_list|()
block|{
comment|// Unsafe.addressSize() returns 8, even with CompressedOops. This is how many bytes each
comment|// element is allocated in an Object[].
return|return
name|UnsafeAccess
operator|.
name|theUnsafe
operator|.
name|ARRAY_OBJECT_INDEX_SCALE
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
name|long
name|sizeOfByteArray
parameter_list|(
name|int
name|len
parameter_list|)
block|{
return|return
name|align
argument_list|(
name|ARRAY
operator|+
name|len
operator|*
name|UnsafeAccess
operator|.
name|theUnsafe
operator|.
name|ARRAY_BYTE_INDEX_SCALE
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|MemoryLayout
name|getMemoryLayout
parameter_list|()
block|{
comment|// Have a safeguard in case Unsafe estimate is wrong. This is static context, there is
comment|// no configuration, so we look at System property.
name|String
name|enabled
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hbase.memorylayout.use.unsafe"
argument_list|)
decl_stmt|;
if|if
condition|(
name|UnsafeAvailChecker
operator|.
name|isAvailable
argument_list|()
operator|&&
operator|(
name|enabled
operator|==
literal|null
operator|||
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|enabled
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using Unsafe to estimate memory layout"
argument_list|)
expr_stmt|;
return|return
operator|new
name|UnsafeLayout
argument_list|()
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Not using Unsafe to estimate memory layout"
argument_list|)
expr_stmt|;
return|return
operator|new
name|MemoryLayout
argument_list|()
return|;
block|}
specifier|private
specifier|static
specifier|final
name|MemoryLayout
name|memoryLayout
init|=
name|getMemoryLayout
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_UNSAFE_LAYOUT
init|=
operator|(
name|memoryLayout
operator|instanceof
name|UnsafeLayout
operator|)
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|boolean
name|useUnsafeLayout
parameter_list|()
block|{
return|return
name|USE_UNSAFE_LAYOUT
return|;
block|}
comment|/**    * Method for reading the arc settings and setting overheads according    * to 32-bit or 64-bit architecture.    */
static|static
block|{
name|REFERENCE
operator|=
name|memoryLayout
operator|.
name|oopSize
argument_list|()
expr_stmt|;
name|OBJECT
operator|=
name|memoryLayout
operator|.
name|headerSize
argument_list|()
expr_stmt|;
name|ARRAY
operator|=
name|memoryLayout
operator|.
name|arrayHeaderSize
argument_list|()
expr_stmt|;
name|ARRAYLIST
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
argument_list|)
operator|+
name|align
argument_list|(
name|ARRAY
argument_list|)
expr_stmt|;
name|LINKEDLIST
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
operator|(
literal|2
operator|*
name|REFERENCE
operator|)
argument_list|)
expr_stmt|;
name|LINKEDLIST_ENTRY
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|2
operator|*
name|REFERENCE
operator|)
argument_list|)
expr_stmt|;
comment|//noinspection PointlessArithmeticExpression
name|BYTE_BUFFER
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
operator|+
operator|(
literal|5
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
operator|(
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
operator|)
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
operator|+
name|align
argument_list|(
name|ARRAY
argument_list|)
expr_stmt|;
name|INTEGER
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|MAP_ENTRY
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
literal|5
operator|*
name|REFERENCE
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
argument_list|)
expr_stmt|;
name|TREEMAP
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
literal|7
operator|*
name|REFERENCE
argument_list|)
expr_stmt|;
comment|// STRING is different size in jdk6 and jdk7. Just use what we estimate as size rather than
comment|// have a conditional on whether jdk7.
name|STRING
operator|=
operator|(
name|int
operator|)
name|estimateBase
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// CONCURRENT_HASHMAP is different size in jdk6 and jdk7; it looks like its different between
comment|// 23.6-b03 and 23.0-b21. Just use what we estimate as size rather than have a conditional on
comment|// whether jdk7.
name|CONCURRENT_HASHMAP
operator|=
operator|(
name|int
operator|)
name|estimateBase
argument_list|(
name|ConcurrentHashMap
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|CONCURRENT_HASHMAP_ENTRY
operator|=
name|align
argument_list|(
name|REFERENCE
operator|+
name|OBJECT
operator|+
operator|(
literal|3
operator|*
name|REFERENCE
operator|)
operator|+
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
argument_list|)
expr_stmt|;
name|CONCURRENT_HASHMAP_SEGMENT
operator|=
name|align
argument_list|(
name|REFERENCE
operator|+
name|OBJECT
operator|+
operator|(
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
name|Bytes
operator|.
name|SIZEOF_FLOAT
operator|+
name|ARRAY
argument_list|)
expr_stmt|;
comment|// The size changes from jdk7 to jdk8, estimate the size rather than use a conditional
name|CONCURRENT_SKIPLISTMAP
operator|=
operator|(
name|int
operator|)
name|estimateBase
argument_list|(
name|ConcurrentSkipListMap
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// CellFlatMap object contains two integers, one boolean and one reference to object, so
comment|// 2*INT + BOOLEAN + REFERENCE
name|CELL_FLAT_MAP
operator|=
name|OBJECT
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
operator|+
name|REFERENCE
expr_stmt|;
comment|// CELL_ARRAY_MAP is the size of an instance of CellArrayMap class, which extends
comment|// CellFlatMap class. CellArrayMap object containing a ref to an Array of Cells
name|CELL_ARRAY_MAP
operator|=
name|align
argument_list|(
name|CELL_FLAT_MAP
operator|+
name|REFERENCE
operator|+
name|ARRAY
argument_list|)
expr_stmt|;
comment|// CELL_CHUNK_MAP is the size of an instance of CellChunkMap class, which extends
comment|// CellFlatMap class. CellChunkMap object containing a ref to an Array of Chunks
name|CELL_CHUNK_MAP
operator|=
name|align
argument_list|(
name|CELL_FLAT_MAP
operator|+
name|REFERENCE
operator|+
name|ARRAY
argument_list|)
expr_stmt|;
name|CONCURRENT_SKIPLISTMAP_ENTRY
operator|=
name|align
argument_list|(
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|3
operator|*
name|REFERENCE
operator|)
argument_list|)
operator|+
comment|/* one node per entry */
name|align
argument_list|(
operator|(
name|OBJECT
operator|+
operator|(
literal|3
operator|*
name|REFERENCE
operator|)
operator|)
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|/* one index per two entries */
comment|// REFERENCE in the CellArrayMap all the rest is counted in KeyValue.heapSize()
name|CELL_ARRAY_MAP_ENTRY
operator|=
name|align
argument_list|(
name|REFERENCE
argument_list|)
expr_stmt|;
comment|// The Cell Representation in the CellChunkMap, the Cell object size shouldn't be counted
comment|// in KeyValue.heapSize()
comment|// each cell-representation requires three integers for chunkID (reference to the ByteBuffer),
comment|// offset and length, and one long for seqID
name|CELL_CHUNK_MAP_ENTRY
operator|=
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
name|REENTRANT_LOCK
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|3
operator|*
name|REFERENCE
operator|)
argument_list|)
expr_stmt|;
name|ATOMIC_LONG
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
name|ATOMIC_INTEGER
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|ATOMIC_BOOLEAN
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
argument_list|)
expr_stmt|;
name|ATOMIC_REFERENCE
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
argument_list|)
expr_stmt|;
name|COPYONWRITE_ARRAYSET
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
argument_list|)
expr_stmt|;
name|COPYONWRITE_ARRAYLIST
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
operator|(
literal|2
operator|*
name|REFERENCE
operator|)
operator|+
name|ARRAY
argument_list|)
expr_stmt|;
name|TIMERANGE
operator|=
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
operator|*
literal|2
operator|+
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
argument_list|)
expr_stmt|;
name|SYNC_TIMERANGE_TRACKER
operator|=
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
literal|2
operator|*
name|REFERENCE
argument_list|)
expr_stmt|;
name|NON_SYNC_TIMERANGE_TRACKER
operator|=
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
expr_stmt|;
name|CELL_SET
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|STORE_SERVICES
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|REFERENCE
operator|+
name|ATOMIC_LONG
argument_list|)
expr_stmt|;
block|}
comment|/**    * The estimate of the size of a class instance depends on whether the JVM    * uses 32 or 64 bit addresses, that is it depends on the size of an object    * reference. It is a linear function of the size of a reference, e.g.    * 24 + 5*r where r is the size of a reference (usually 4 or 8 bytes).    *    * This method returns the coefficients of the linear function, e.g. {24, 5}    * in the above example.    *    * @param cl A class whose instance size is to be estimated    * @param debug debug flag    * @return an array of 3 integers. The first integer is the size of the    * primitives, the second the number of arrays and the third the number of    * references.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|int
index|[]
name|getSizeCoefficients
parameter_list|(
name|Class
name|cl
parameter_list|,
name|boolean
name|debug
parameter_list|)
block|{
name|int
name|primitives
init|=
literal|0
decl_stmt|;
name|int
name|arrays
init|=
literal|0
decl_stmt|;
name|int
name|references
init|=
literal|0
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
literal|null
operator|!=
name|cl
condition|;
name|cl
operator|=
name|cl
operator|.
name|getSuperclass
argument_list|()
control|)
block|{
name|Field
index|[]
name|field
init|=
name|cl
operator|.
name|getDeclaredFields
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|field
condition|)
block|{
for|for
control|(
name|Field
name|aField
range|:
name|field
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|aField
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
continue|continue;
name|Class
name|fieldClass
init|=
name|aField
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldClass
operator|.
name|isArray
argument_list|()
condition|)
block|{
name|arrays
operator|++
expr_stmt|;
name|references
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|fieldClass
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
name|references
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// Is simple primitive
name|String
name|name
init|=
name|fieldClass
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"int"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"I"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_INT
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"long"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"J"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"boolean"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"Z"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_BOOLEAN
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"short"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"S"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_SHORT
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"byte"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"B"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_BYTE
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"char"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"C"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_CHAR
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"F"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_FLOAT
expr_stmt|;
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"double"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"D"
argument_list|)
condition|)
name|primitives
operator|+=
name|Bytes
operator|.
name|SIZEOF_DOUBLE
expr_stmt|;
block|}
if|if
condition|(
name|debug
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|""
operator|+
name|index
operator|+
literal|" "
operator|+
name|aField
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|aField
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|index
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|int
index|[]
block|{
name|primitives
block|,
name|arrays
block|,
name|references
block|}
return|;
block|}
comment|/**    * Estimate the static space taken up by a class instance given the    * coefficients returned by getSizeCoefficients.    *    * @param coeff the coefficients    *    * @param debug debug flag    * @return the size estimate, in bytes    */
specifier|private
specifier|static
name|long
name|estimateBaseFromCoefficients
parameter_list|(
name|int
index|[]
name|coeff
parameter_list|,
name|boolean
name|debug
parameter_list|)
block|{
name|long
name|prealign_size
init|=
name|OBJECT
operator|+
name|coeff
index|[
literal|0
index|]
operator|+
name|coeff
index|[
literal|2
index|]
operator|*
name|REFERENCE
decl_stmt|;
comment|// Round up to a multiple of 8
name|long
name|size
init|=
name|align
argument_list|(
name|prealign_size
argument_list|)
operator|+
name|align
argument_list|(
name|coeff
index|[
literal|1
index|]
operator|*
name|ARRAY
argument_list|)
decl_stmt|;
if|if
condition|(
name|debug
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Primitives="
operator|+
name|coeff
index|[
literal|0
index|]
operator|+
literal|", arrays="
operator|+
name|coeff
index|[
literal|1
index|]
operator|+
literal|", references="
operator|+
name|coeff
index|[
literal|2
index|]
operator|+
literal|", refSize "
operator|+
name|REFERENCE
operator|+
literal|", size="
operator|+
name|size
operator|+
literal|", prealign_size="
operator|+
name|prealign_size
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
comment|/**    * Estimate the static space taken up by the fields of a class. This includes    * the space taken up by by references (the pointer) but not by the referenced    * object. So the estimated size of an array field does not depend on the size    * of the array. Similarly the size of an object (reference) field does not    * depend on the object.    *    * @param cl class    * @param debug debug flag    * @return the size estimate in bytes.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|long
name|estimateBase
parameter_list|(
name|Class
name|cl
parameter_list|,
name|boolean
name|debug
parameter_list|)
block|{
return|return
name|estimateBaseFromCoefficients
argument_list|(
name|getSizeCoefficients
argument_list|(
name|cl
argument_list|,
name|debug
argument_list|)
argument_list|,
name|debug
argument_list|)
return|;
block|}
comment|/**    * Aligns a number to 8.    * @param num number to align to 8    * @return smallest number&gt;= input that is a multiple of 8    */
specifier|public
specifier|static
name|int
name|align
parameter_list|(
name|int
name|num
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|align
argument_list|(
operator|(
name|long
operator|)
name|num
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Aligns a number to 8.    * @param num number to align to 8    * @return smallest number&gt;= input that is a multiple of 8    */
specifier|public
specifier|static
name|long
name|align
parameter_list|(
name|long
name|num
parameter_list|)
block|{
return|return
name|memoryLayout
operator|.
name|align
argument_list|(
name|num
argument_list|)
return|;
block|}
comment|/**    * Determines if we are running in a 32-bit JVM. Some unit tests need to    * know this too.    */
specifier|public
specifier|static
name|boolean
name|is32BitJVM
parameter_list|()
block|{
specifier|final
name|String
name|model
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.arch.data.model"
argument_list|)
decl_stmt|;
return|return
name|model
operator|!=
literal|null
operator|&&
name|model
operator|.
name|equals
argument_list|(
literal|"32"
argument_list|)
return|;
block|}
comment|/**    * Calculate the memory consumption (in byte) of a byte array,    * including the array header and the whole backing byte array.    *    * If the whole byte array is occupied (not shared with other objects), please use this function.    * If not, please use {@link #sizeOfByteArray(int)} instead.    *    * @param b the byte array    * @return the memory consumption (in byte) of the whole byte array    */
specifier|public
specifier|static
name|long
name|sizeOf
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
block|{
return|return
name|memoryLayout
operator|.
name|sizeOfByteArray
argument_list|(
name|b
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Calculate the memory consumption (in byte) of a part of a byte array,    * including the array header and the part of the backing byte array.    *    * This function is used when the byte array backs multiple objects.    * For example, in {@link org.apache.hadoop.hbase.KeyValue},    * multiple KeyValue objects share a same backing byte array ({@link org.apache.hadoop.hbase.KeyValue#bytes}).    * Also see {@link org.apache.hadoop.hbase.KeyValue#heapSize()}.    *    * @param len the length (in byte) used partially in the backing byte array    * @return the memory consumption (in byte) of the part of the byte array    */
specifier|public
specifier|static
name|long
name|sizeOfByteArray
parameter_list|(
name|int
name|len
parameter_list|)
block|{
return|return
name|memoryLayout
operator|.
name|sizeOfByteArray
argument_list|(
name|len
argument_list|)
return|;
block|}
block|}
end_class

end_unit

