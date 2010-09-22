begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Properties
import|;
end_import

begin_comment
comment|/**  * Class for determining the "size" of a class, an attempt to calculate the  * actual bytes that an object of this class will occupy in memory  *  * The core of this class is taken from the Derby project  */
end_comment

begin_class
specifier|public
class|class
name|ClassSize
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
name|ClassSize
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|nrOfRefsPerObj
init|=
literal|2
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
specifier|private
specifier|static
specifier|final
name|String
name|THIRTY_TWO
init|=
literal|"32"
decl_stmt|;
comment|/**    * Method for reading the arc settings and setting overheads according    * to 32-bit or 64-bit architecture.    */
static|static
block|{
comment|// Figure out whether this is a 32 or 64 bit machine.
name|Properties
name|sysProps
init|=
name|System
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|String
name|arcModel
init|=
name|sysProps
operator|.
name|getProperty
argument_list|(
literal|"sun.arch.data.model"
argument_list|)
decl_stmt|;
comment|//Default value is set to 8, covering the case when arcModel is unknown
if|if
condition|(
name|arcModel
operator|.
name|equals
argument_list|(
name|THIRTY_TWO
argument_list|)
condition|)
block|{
name|REFERENCE
operator|=
literal|4
expr_stmt|;
block|}
else|else
block|{
name|REFERENCE
operator|=
literal|8
expr_stmt|;
block|}
name|OBJECT
operator|=
literal|2
operator|*
name|REFERENCE
expr_stmt|;
name|ARRAY
operator|=
literal|3
operator|*
name|REFERENCE
expr_stmt|;
name|ARRAYLIST
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|align
argument_list|(
name|REFERENCE
argument_list|)
operator|+
name|align
argument_list|(
name|ARRAY
argument_list|)
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
comment|//noinspection PointlessArithmeticExpression
name|BYTE_BUFFER
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|align
argument_list|(
name|REFERENCE
argument_list|)
operator|+
name|align
argument_list|(
name|ARRAY
argument_list|)
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
name|align
argument_list|(
literal|7
operator|*
name|REFERENCE
argument_list|)
argument_list|)
expr_stmt|;
name|STRING
operator|=
name|align
argument_list|(
name|OBJECT
operator|+
name|ARRAY
operator|+
name|REFERENCE
operator|+
literal|3
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
name|CONCURRENT_HASHMAP
operator|=
name|align
argument_list|(
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
operator|+
name|ARRAY
operator|+
operator|(
literal|6
operator|*
name|REFERENCE
operator|)
operator|+
name|OBJECT
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
name|CONCURRENT_SKIPLISTMAP
operator|=
name|align
argument_list|(
name|Bytes
operator|.
name|SIZEOF_INT
operator|+
name|OBJECT
operator|+
operator|(
literal|8
operator|*
name|REFERENCE
operator|)
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
comment|//The number of references that a new object takes
name|int
name|references
init|=
name|nrOfRefsPerObj
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
operator|!
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
block|{
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
comment|// Write out region name as string and its encoded name.
name|LOG
operator|.
name|debug
argument_list|(
name|aField
operator|.
name|getName
argument_list|()
operator|+
literal|"\n\t"
operator|+
name|aField
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|size
init|=
name|coeff
index|[
literal|0
index|]
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
operator|+
name|coeff
index|[
literal|2
index|]
operator|*
name|REFERENCE
decl_stmt|;
comment|// Round up to a multiple of 8
name|size
operator|=
name|align
argument_list|(
name|size
argument_list|)
expr_stmt|;
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
comment|// Write out region name as string and its encoded name.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Primitives "
operator|+
name|coeff
index|[
literal|0
index|]
operator|+
literal|", arrays "
operator|+
name|coeff
index|[
literal|1
index|]
operator|+
literal|", references(includes "
operator|+
name|nrOfRefsPerObj
operator|+
literal|" for object overhead) "
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
literal|", size "
operator|+
name|size
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
comment|/**    * Aligns a number to 8.    * @param num number to align to 8    * @return smallest number>= input that is a multiple of 8    */
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
comment|/**    * Aligns a number to 8.    * @param num number to align to 8    * @return smallest number>= input that is a multiple of 8    */
specifier|public
specifier|static
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
block|}
end_class

end_unit

