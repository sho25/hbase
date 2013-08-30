begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|Cell
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
name|KeyValueUtil
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
name|exceptions
operator|.
name|DeserializationException
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
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|BytesBytesPair
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
name|Bytes
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
name|Pair
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Filters data based on fuzzy row key. Performs fast-forwards during scanning.  * It takes pairs (row key, fuzzy info) to match row keys. Where fuzzy info is  * a byte array with 0 or 1 as its values:  *<ul>  *<li>  *     0 - means that this byte in provided row key is fixed, i.e. row key's byte at same position  *         must match  *</li>  *<li>  *     1 - means that this byte in provided row key is NOT fixed, i.e. row key's byte at this  *         position can be different from the one in provided row key  *</li>  *</ul>  *  *  * Example:  * Let's assume row key format is userId_actionId_year_month. Length of userId is fixed  * and is 4, length of actionId is 2 and year and month are 4 and 2 bytes long respectively.  *  * Let's assume that we need to fetch all users that performed certain action (encoded as "99")  * in Jan of any year. Then the pair (row key, fuzzy info) would be the following:  * row key = "????_99_????_01" (one can use any value instead of "?")  * fuzzy info = "\x01\x01\x01\x01\x00\x00\x00\x00\x01\x01\x01\x01\x00\x00\x00"  *  * I.e. fuzzy info tells the matching mask is "????_99_????_01", where at ? can be any value.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|FuzzyRowFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|fuzzyKeysData
decl_stmt|;
specifier|private
name|boolean
name|done
init|=
literal|false
decl_stmt|;
specifier|public
name|FuzzyRowFilter
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|fuzzyKeysData
parameter_list|)
block|{
name|this
operator|.
name|fuzzyKeysData
operator|=
name|fuzzyKeysData
expr_stmt|;
block|}
comment|// TODO: possible improvement: save which fuzzy row key to use when providing a hint
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|kv
parameter_list|)
block|{
comment|// TODO add getRow() equivalent to Cell or change satisfies to take b[],o,l style args.
name|KeyValue
name|v
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|byte
index|[]
name|rowKey
init|=
name|v
operator|.
name|getRow
argument_list|()
decl_stmt|;
comment|// assigning "worst" result first and looking for better options
name|SatisfiesCode
name|bestOption
init|=
name|SatisfiesCode
operator|.
name|NO_NEXT
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fuzzyData
range|:
name|fuzzyKeysData
control|)
block|{
name|SatisfiesCode
name|satisfiesCode
init|=
name|satisfies
argument_list|(
name|rowKey
argument_list|,
name|fuzzyData
operator|.
name|getFirst
argument_list|()
argument_list|,
name|fuzzyData
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|satisfiesCode
operator|==
name|SatisfiesCode
operator|.
name|YES
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
if|if
condition|(
name|satisfiesCode
operator|==
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
condition|)
block|{
name|bestOption
operator|=
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
expr_stmt|;
block|}
block|}
if|if
condition|(
name|bestOption
operator|==
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
return|;
block|}
comment|// the only unhandled SatisfiesCode is NO_NEXT, i.e. we are done
name|done
operator|=
literal|true
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|currentKV
parameter_list|)
block|{
comment|// TODO make matching Column a cell method or CellUtil method.
name|KeyValue
name|v
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|currentKV
argument_list|)
decl_stmt|;
name|byte
index|[]
name|rowKey
init|=
name|v
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|byte
index|[]
name|nextRowKey
init|=
literal|null
decl_stmt|;
comment|// Searching for the "smallest" row key that satisfies at least one fuzzy row key
for|for
control|(
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fuzzyData
range|:
name|fuzzyKeysData
control|)
block|{
name|byte
index|[]
name|nextRowKeyCandidate
init|=
name|getNextForFuzzyRule
argument_list|(
name|rowKey
argument_list|,
name|fuzzyData
operator|.
name|getFirst
argument_list|()
argument_list|,
name|fuzzyData
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextRowKeyCandidate
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|nextRowKey
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|nextRowKeyCandidate
argument_list|,
name|nextRowKey
argument_list|)
operator|<
literal|0
condition|)
block|{
name|nextRowKey
operator|=
name|nextRowKeyCandidate
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nextRowKey
operator|==
literal|null
condition|)
block|{
comment|// SHOULD NEVER happen
comment|// TODO: is there a better way than throw exception? (stop the scanner?)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"No next row key that satisfies fuzzy exists when"
operator|+
literal|" getNextKeyHint() is invoked."
operator|+
literal|" Filter: "
operator|+
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|" currentKV: "
operator|+
name|currentKV
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|nextRowKey
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|done
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|FuzzyRowFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|FuzzyRowFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fuzzyData
range|:
name|fuzzyKeysData
control|)
block|{
name|BytesBytesPair
operator|.
name|Builder
name|bbpBuilder
init|=
name|BytesBytesPair
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|bbpBuilder
operator|.
name|setFirst
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|fuzzyData
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|bbpBuilder
operator|.
name|setSecond
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|fuzzyData
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addFuzzyKeysData
argument_list|(
name|bbpBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link FuzzyRowFilter} instance    * @return An instance of {@link FuzzyRowFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|FuzzyRowFilter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|FilterProtos
operator|.
name|FuzzyRowFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|FuzzyRowFilter
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|int
name|count
init|=
name|proto
operator|.
name|getFuzzyKeysDataCount
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|fuzzyKeysData
init|=
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|(
name|count
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
operator|++
name|i
control|)
block|{
name|BytesBytesPair
name|current
init|=
name|proto
operator|.
name|getFuzzyKeysData
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|current
operator|.
name|getFirst
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|byte
index|[]
name|keyMeta
init|=
name|current
operator|.
name|getSecond
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|fuzzyKeysData
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|(
name|keyBytes
argument_list|,
name|keyMeta
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FuzzyRowFilter
argument_list|(
name|fuzzyKeysData
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"FuzzyRowFilter"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{fuzzyKeysData="
argument_list|)
expr_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|fuzzyData
range|:
name|fuzzyKeysData
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'{'
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|fuzzyData
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|fuzzyData
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}, "
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// Utility methods
specifier|static
enum|enum
name|SatisfiesCode
block|{
comment|// row satisfies fuzzy rule
name|YES
block|,
comment|// row doesn't satisfy fuzzy rule, but there's possible greater row that does
name|NEXT_EXISTS
block|,
comment|// row doesn't satisfy fuzzy rule and there's no greater row that does
name|NO_NEXT
block|}
specifier|static
name|SatisfiesCode
name|satisfies
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|fuzzyKeyBytes
parameter_list|,
name|byte
index|[]
name|fuzzyKeyMeta
parameter_list|)
block|{
return|return
name|satisfies
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|fuzzyKeyBytes
argument_list|,
name|fuzzyKeyMeta
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|SatisfiesCode
name|satisfies
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|byte
index|[]
name|fuzzyKeyBytes
parameter_list|,
name|byte
index|[]
name|fuzzyKeyMeta
parameter_list|)
block|{
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
comment|// do nothing, let scan to proceed
return|return
name|SatisfiesCode
operator|.
name|YES
return|;
block|}
name|boolean
name|nextRowKeyCandidateExists
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fuzzyKeyMeta
operator|.
name|length
operator|&&
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// First, checking if this position is fixed and not equals the given one
name|boolean
name|byteAtPositionFixed
init|=
name|fuzzyKeyMeta
index|[
name|i
index|]
operator|==
literal|0
decl_stmt|;
name|boolean
name|fixedByteIncorrect
init|=
name|byteAtPositionFixed
operator|&&
name|fuzzyKeyBytes
index|[
name|i
index|]
operator|!=
name|row
index|[
name|i
operator|+
name|offset
index|]
decl_stmt|;
if|if
condition|(
name|fixedByteIncorrect
condition|)
block|{
comment|// in this case there's another row that satisfies fuzzy rule and bigger than this row
if|if
condition|(
name|nextRowKeyCandidateExists
condition|)
block|{
return|return
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
return|;
block|}
comment|// If this row byte is less than fixed then there's a byte array bigger than
comment|// this row and which satisfies the fuzzy rule. Otherwise there's no such byte array:
comment|// this row is simply bigger than any byte array that satisfies the fuzzy rule
name|boolean
name|rowByteLessThanFixed
init|=
operator|(
name|row
index|[
name|i
operator|+
name|offset
index|]
operator|&
literal|0xFF
operator|)
operator|<
operator|(
name|fuzzyKeyBytes
index|[
name|i
index|]
operator|&
literal|0xFF
operator|)
decl_stmt|;
return|return
name|rowByteLessThanFixed
condition|?
name|SatisfiesCode
operator|.
name|NEXT_EXISTS
else|:
name|SatisfiesCode
operator|.
name|NO_NEXT
return|;
block|}
comment|// Second, checking if this position is not fixed and byte value is not the biggest. In this
comment|// case there's a byte array bigger than this row and which satisfies the fuzzy rule. To get
comment|// bigger byte array that satisfies the rule we need to just increase this byte
comment|// (see the code of getNextForFuzzyRule below) by one.
comment|// Note: if non-fixed byte is already at biggest value, this doesn't allow us to say there's
comment|//       bigger one that satisfies the rule as it can't be increased.
if|if
condition|(
name|fuzzyKeyMeta
index|[
name|i
index|]
operator|==
literal|1
operator|&&
operator|!
name|isMax
argument_list|(
name|fuzzyKeyBytes
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|nextRowKeyCandidateExists
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|SatisfiesCode
operator|.
name|YES
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isMax
parameter_list|(
name|byte
name|fuzzyKeyByte
parameter_list|)
block|{
return|return
operator|(
name|fuzzyKeyByte
operator|&
literal|0xFF
operator|)
operator|==
literal|255
return|;
block|}
specifier|static
name|byte
index|[]
name|getNextForFuzzyRule
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|fuzzyKeyBytes
parameter_list|,
name|byte
index|[]
name|fuzzyKeyMeta
parameter_list|)
block|{
return|return
name|getNextForFuzzyRule
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|row
operator|.
name|length
argument_list|,
name|fuzzyKeyBytes
argument_list|,
name|fuzzyKeyMeta
argument_list|)
return|;
block|}
comment|/**    * @return greater byte array than given (row) which satisfies the fuzzy rule if it exists,    *         null otherwise    */
specifier|private
specifier|static
name|byte
index|[]
name|getNextForFuzzyRule
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|byte
index|[]
name|fuzzyKeyBytes
parameter_list|,
name|byte
index|[]
name|fuzzyKeyMeta
parameter_list|)
block|{
comment|// To find out the next "smallest" byte array that satisfies fuzzy rule and "greater" than
comment|// the given one we do the following:
comment|// 1. setting values on all "fixed" positions to the values from fuzzyKeyBytes
comment|// 2. if during the first step given row did not increase, then we increase the value at
comment|//    the first "non-fixed" position (where it is not maximum already)
comment|// It is easier to perform this by using fuzzyKeyBytes copy and setting "non-fixed" position
comment|// values than otherwise.
name|byte
index|[]
name|result
init|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|fuzzyKeyBytes
argument_list|,
name|length
operator|>
name|fuzzyKeyBytes
operator|.
name|length
condition|?
name|length
else|:
name|fuzzyKeyBytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|toInc
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|increased
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|result
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>=
name|fuzzyKeyMeta
operator|.
name|length
operator|||
name|fuzzyKeyMeta
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|row
index|[
name|offset
operator|+
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|isMax
argument_list|(
name|row
index|[
name|i
index|]
argument_list|)
condition|)
block|{
comment|// this is "non-fixed" position and is not at max value, hence we can increase it
name|toInc
operator|=
name|i
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|i
operator|<
name|fuzzyKeyMeta
operator|.
name|length
operator|&&
name|fuzzyKeyMeta
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|(
name|row
index|[
name|i
operator|+
name|offset
index|]
operator|&
literal|0xFF
operator|)
operator|<
operator|(
name|fuzzyKeyBytes
index|[
name|i
index|]
operator|&
literal|0xFF
operator|)
condition|)
block|{
comment|// if setting value for any fixed position increased the original array,
comment|// we are OK
name|increased
operator|=
literal|true
expr_stmt|;
break|break;
block|}
if|if
condition|(
operator|(
name|row
index|[
name|i
operator|+
name|offset
index|]
operator|&
literal|0xFF
operator|)
operator|>
operator|(
name|fuzzyKeyBytes
index|[
name|i
index|]
operator|&
literal|0xFF
operator|)
condition|)
block|{
comment|// if setting value for any fixed position makes array "smaller", then just stop:
comment|// in case we found some non-fixed position to increase we will do it, otherwise
comment|// there's no "next" row key that satisfies fuzzy rule and "greater" than given row
break|break;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|increased
condition|)
block|{
if|if
condition|(
name|toInc
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
index|[
name|toInc
index|]
operator|++
expr_stmt|;
comment|// Setting all "non-fixed" positions to zeroes to the right of the one we increased so
comment|// that found "next" row key is the smallest possible
for|for
control|(
name|int
name|i
init|=
name|toInc
operator|+
literal|1
init|;
name|i
operator|<
name|result
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>=
name|fuzzyKeyMeta
operator|.
name|length
operator|||
name|fuzzyKeyMeta
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|result
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|FuzzyRowFilter
operator|)
condition|)
return|return
literal|false
return|;
name|FuzzyRowFilter
name|other
init|=
operator|(
name|FuzzyRowFilter
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|fuzzyKeysData
operator|.
name|size
argument_list|()
operator|!=
name|other
operator|.
name|fuzzyKeysData
operator|.
name|size
argument_list|()
condition|)
return|return
literal|false
return|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fuzzyKeysData
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|thisData
init|=
name|this
operator|.
name|fuzzyKeysData
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|otherData
init|=
name|other
operator|.
name|fuzzyKeysData
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|Bytes
operator|.
name|equals
argument_list|(
name|thisData
operator|.
name|getFirst
argument_list|()
argument_list|,
name|otherData
operator|.
name|getFirst
argument_list|()
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|thisData
operator|.
name|getSecond
argument_list|()
argument_list|,
name|otherData
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

