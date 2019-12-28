begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|ByteBufferUtils
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
name|primitives
operator|.
name|Longs
import|;
end_import

begin_comment
comment|/**  * A comparator for case where {@link ByteBufferKeyValue} is prevalent type (BBKV  * is base-type in hbase2). Takes a general comparator as fallback in case types are NOT the  * expected ByteBufferKeyValue.  *  *<p>This is a tricked-out Comparator at heart of hbase read and write. It is in  * the HOT path so we try all sorts of ugly stuff so we can go faster. See below  * in this javadoc comment for the list.  *  *<p>Apply this comparator narrowly so it is fed exclusively ByteBufferKeyValues  * as much as is possible so JIT can settle (e.g. make one per ConcurrentSkipListMap  * in HStore).  *  *<p>Exploits specially added methods in BBKV to save on deserializations of shorts,  * longs, etc: i.e. calculating the family length requires row length; pass it in  * rather than recalculate it, and so on.  *  *<p>This comparator does static dispatch to private final methods so hotspot is comfortable  * deciding inline.  *  *<p>Measurement has it that we almost have it so all inlines from memstore  * ConcurrentSkipListMap on down to the (unsafe) intrinisics that do byte compare  * and deserialize shorts and ints; needs a bit more work.  *  *<p>Does not take a Type to compare: i.e. it is not a Comparator&lt;Cell> or  * CellComparator&lt;Cell> or Comparator&lt;ByteBufferKeyValue> because that adds  * another method to the hierarchy -- from compare(Object, Object)  * to dynamic compare(Cell, Cell) to static private compare -- and inlining doesn't happen if  * hierarchy is too deep (it is the case here).  *  *<p>Be careful making changes. Compare perf before and after and look at what  * hotspot ends up generating before committing change (jitwatch is helpful here).  * Changing this one class doubled write throughput (HBASE-20483).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BBKVComparator
implements|implements
name|Comparator
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BBKVComparator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Comparator
name|fallback
decl_stmt|;
specifier|public
name|BBKVComparator
parameter_list|(
name|Comparator
name|fallback
parameter_list|)
block|{
name|this
operator|.
name|fallback
operator|=
name|fallback
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|l
parameter_list|,
name|Object
name|r
parameter_list|)
block|{
if|if
condition|(
operator|(
name|l
operator|instanceof
name|ByteBufferKeyValue
operator|)
operator|&&
operator|(
name|r
operator|instanceof
name|ByteBufferKeyValue
operator|)
condition|)
block|{
return|return
name|compare
argument_list|(
operator|(
name|ByteBufferKeyValue
operator|)
name|l
argument_list|,
operator|(
name|ByteBufferKeyValue
operator|)
name|r
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|// Skip calling compare(Object, Object) and go direct to compare(Cell, Cell)
return|return
name|this
operator|.
name|fallback
operator|.
name|compare
argument_list|(
operator|(
name|Cell
operator|)
name|l
argument_list|,
operator|(
name|Cell
operator|)
name|r
argument_list|)
return|;
block|}
comment|// TODO: Come back here. We get a few percentage points extra of throughput if this is a
comment|// private method.
specifier|static
name|int
name|compare
parameter_list|(
name|ByteBufferKeyValue
name|left
parameter_list|,
name|ByteBufferKeyValue
name|right
parameter_list|,
name|boolean
name|ignoreSequenceid
parameter_list|)
block|{
comment|// NOTE: Same method is in CellComparatorImpl, also private, not shared, intentionally. Not
comment|// sharing gets us a few percent more throughput in compares. If changes here or there, make
comment|// sure done in both places.
comment|// Compare Rows. Cache row length.
name|int
name|leftRowLength
init|=
name|left
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|rightRowLength
init|=
name|right
operator|.
name|getRowLength
argument_list|()
decl_stmt|;
name|int
name|diff
init|=
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|left
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|leftRowLength
argument_list|,
name|right
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
name|right
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|rightRowLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// If the column is not specified, the "minimum" key type appears as latest in the sorted
comment|// order, regardless of the timestamp. This is used for specifying the last key/value in a
comment|// given row, because there is no "lexicographically last column" (it would be infinitely long).
comment|// The "maximum" key type does not need this behavior. Copied from KeyValue. This is bad in that
comment|// we can't do memcmp w/ special rules like this.
comment|// TODO: Is there a test for this behavior?
name|int
name|leftFamilyLengthPosition
init|=
name|left
operator|.
name|getFamilyLengthPosition
argument_list|(
name|leftRowLength
argument_list|)
decl_stmt|;
name|int
name|leftFamilyLength
init|=
name|left
operator|.
name|getFamilyLength
argument_list|(
name|leftFamilyLengthPosition
argument_list|)
decl_stmt|;
name|int
name|leftKeyLength
init|=
name|left
operator|.
name|getKeyLength
argument_list|()
decl_stmt|;
name|int
name|leftQualifierLength
init|=
name|left
operator|.
name|getQualifierLength
argument_list|(
name|leftKeyLength
argument_list|,
name|leftRowLength
argument_list|,
name|leftFamilyLength
argument_list|)
decl_stmt|;
comment|// No need of left row length below here.
name|byte
name|leftType
init|=
name|left
operator|.
name|getTypeByte
argument_list|(
name|leftKeyLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|leftFamilyLength
operator|+
name|leftQualifierLength
operator|==
literal|0
operator|&&
name|leftType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
comment|// left is "bigger", i.e. it appears later in the sorted order
return|return
literal|1
return|;
block|}
name|int
name|rightFamilyLengthPosition
init|=
name|right
operator|.
name|getFamilyLengthPosition
argument_list|(
name|rightRowLength
argument_list|)
decl_stmt|;
name|int
name|rightFamilyLength
init|=
name|right
operator|.
name|getFamilyLength
argument_list|(
name|rightFamilyLengthPosition
argument_list|)
decl_stmt|;
name|int
name|rightKeyLength
init|=
name|right
operator|.
name|getKeyLength
argument_list|()
decl_stmt|;
name|int
name|rightQualifierLength
init|=
name|right
operator|.
name|getQualifierLength
argument_list|(
name|rightKeyLength
argument_list|,
name|rightRowLength
argument_list|,
name|rightFamilyLength
argument_list|)
decl_stmt|;
comment|// No need of right row length below here.
name|byte
name|rightType
init|=
name|right
operator|.
name|getTypeByte
argument_list|(
name|rightKeyLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|rightFamilyLength
operator|+
name|rightQualifierLength
operator|==
literal|0
operator|&&
name|rightType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// Compare families.
name|int
name|leftFamilyPosition
init|=
name|left
operator|.
name|getFamilyPosition
argument_list|(
name|leftFamilyLengthPosition
argument_list|)
decl_stmt|;
name|int
name|rightFamilyPosition
init|=
name|right
operator|.
name|getFamilyPosition
argument_list|(
name|rightFamilyLengthPosition
argument_list|)
decl_stmt|;
name|diff
operator|=
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|leftFamilyPosition
argument_list|,
name|leftFamilyLength
argument_list|,
name|right
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
name|rightFamilyPosition
argument_list|,
name|rightFamilyLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Compare qualifiers
name|diff
operator|=
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierPosition
argument_list|(
name|leftFamilyPosition
argument_list|,
name|leftFamilyLength
argument_list|)
argument_list|,
name|leftQualifierLength
argument_list|,
name|right
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierPosition
argument_list|(
name|rightFamilyPosition
argument_list|,
name|rightFamilyLength
argument_list|)
argument_list|,
name|rightQualifierLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Timestamps.
comment|// Swap order we pass into compare so we get DESCENDING order.
name|diff
operator|=
name|Long
operator|.
name|compare
argument_list|(
name|right
operator|.
name|getTimestamp
argument_list|(
name|rightKeyLength
argument_list|)
argument_list|,
name|left
operator|.
name|getTimestamp
argument_list|(
name|leftKeyLength
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Compare types. Let the delete types sort ahead of puts; i.e. types
comment|// of higher numbers sort before those of lesser numbers. Maximum (255)
comment|// appears ahead of everything, and minimum (0) appears after
comment|// everything.
name|diff
operator|=
operator|(
literal|0xff
operator|&
name|rightType
operator|)
operator|-
operator|(
literal|0xff
operator|&
name|leftType
operator|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Negate following comparisons so later edits show up first mvccVersion: later sorts first
return|return
name|ignoreSequenceid
condition|?
name|diff
else|:
name|Longs
operator|.
name|compare
argument_list|(
name|right
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|left
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

