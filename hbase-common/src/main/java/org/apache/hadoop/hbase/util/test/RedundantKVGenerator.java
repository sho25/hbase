begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|test
package|;
end_package

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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Random
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
name|CellComparator
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
name|Tag
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
name|hadoop
operator|.
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Generate list of key values which are very useful to test data block encoding  * and compression.  */
end_comment

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"RV_ABSOLUTE_VALUE_OF_RANDOM_INT"
argument_list|,
name|justification
operator|=
literal|"Should probably fix"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RedundantKVGenerator
block|{
comment|// row settings
specifier|static
name|byte
index|[]
name|DEFAULT_COMMON_PREFIX
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|static
name|int
name|DEFAULT_NUMBER_OF_ROW_PREFIXES
init|=
literal|10
decl_stmt|;
specifier|static
name|int
name|DEFAULT_AVERAGE_PREFIX_LENGTH
init|=
literal|6
decl_stmt|;
specifier|static
name|int
name|DEFAULT_PREFIX_LENGTH_VARIANCE
init|=
literal|3
decl_stmt|;
specifier|static
name|int
name|DEFAULT_AVERAGE_SUFFIX_LENGTH
init|=
literal|3
decl_stmt|;
specifier|static
name|int
name|DEFAULT_SUFFIX_LENGTH_VARIANCE
init|=
literal|3
decl_stmt|;
specifier|static
name|int
name|DEFAULT_NUMBER_OF_ROW
init|=
literal|500
decl_stmt|;
comment|// qualifier
specifier|static
name|float
name|DEFAULT_CHANCE_FOR_SAME_QUALIFIER
init|=
literal|0.5f
decl_stmt|;
specifier|static
name|float
name|DEFAULT_CHANCE_FOR_SIMILIAR_QUALIFIER
init|=
literal|0.4f
decl_stmt|;
specifier|static
name|int
name|DEFAULT_AVERAGE_QUALIFIER_LENGTH
init|=
literal|9
decl_stmt|;
specifier|static
name|int
name|DEFAULT_QUALIFIER_LENGTH_VARIANCE
init|=
literal|3
decl_stmt|;
specifier|static
name|int
name|DEFAULT_COLUMN_FAMILY_LENGTH
init|=
literal|9
decl_stmt|;
specifier|static
name|int
name|DEFAULT_VALUE_LENGTH
init|=
literal|8
decl_stmt|;
specifier|static
name|float
name|DEFAULT_CHANCE_FOR_ZERO_VALUE
init|=
literal|0.5f
decl_stmt|;
specifier|static
name|int
name|DEFAULT_BASE_TIMESTAMP_DIVIDE
init|=
literal|1000000
decl_stmt|;
specifier|static
name|int
name|DEFAULT_TIMESTAMP_DIFF_SIZE
init|=
literal|100000000
decl_stmt|;
comment|/**    * Default constructor, assumes all parameters from class constants.    */
specifier|public
name|RedundantKVGenerator
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Random
argument_list|(
literal|42L
argument_list|)
argument_list|,
name|DEFAULT_NUMBER_OF_ROW_PREFIXES
argument_list|,
name|DEFAULT_AVERAGE_PREFIX_LENGTH
argument_list|,
name|DEFAULT_PREFIX_LENGTH_VARIANCE
argument_list|,
name|DEFAULT_AVERAGE_SUFFIX_LENGTH
argument_list|,
name|DEFAULT_SUFFIX_LENGTH_VARIANCE
argument_list|,
name|DEFAULT_NUMBER_OF_ROW
argument_list|,
name|DEFAULT_CHANCE_FOR_SAME_QUALIFIER
argument_list|,
name|DEFAULT_CHANCE_FOR_SIMILIAR_QUALIFIER
argument_list|,
name|DEFAULT_AVERAGE_QUALIFIER_LENGTH
argument_list|,
name|DEFAULT_QUALIFIER_LENGTH_VARIANCE
argument_list|,
name|DEFAULT_COLUMN_FAMILY_LENGTH
argument_list|,
name|DEFAULT_VALUE_LENGTH
argument_list|,
name|DEFAULT_CHANCE_FOR_ZERO_VALUE
argument_list|,
name|DEFAULT_BASE_TIMESTAMP_DIVIDE
argument_list|,
name|DEFAULT_TIMESTAMP_DIFF_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Various configuration options for generating key values    * @param randomizer pick things by random    */
specifier|public
name|RedundantKVGenerator
parameter_list|(
name|Random
name|randomizer
parameter_list|,
name|int
name|numberOfRowPrefixes
parameter_list|,
name|int
name|averagePrefixLength
parameter_list|,
name|int
name|prefixLengthVariance
parameter_list|,
name|int
name|averageSuffixLength
parameter_list|,
name|int
name|suffixLengthVariance
parameter_list|,
name|int
name|numberOfRows
parameter_list|,
name|float
name|chanceForSameQualifier
parameter_list|,
name|float
name|chanceForSimiliarQualifier
parameter_list|,
name|int
name|averageQualifierLength
parameter_list|,
name|int
name|qualifierLengthVariance
parameter_list|,
name|int
name|columnFamilyLength
parameter_list|,
name|int
name|valueLength
parameter_list|,
name|float
name|chanceForZeroValue
parameter_list|,
name|int
name|baseTimestampDivide
parameter_list|,
name|int
name|timestampDiffSize
parameter_list|)
block|{
name|this
operator|.
name|randomizer
operator|=
name|randomizer
expr_stmt|;
name|this
operator|.
name|commonPrefix
operator|=
name|DEFAULT_COMMON_PREFIX
expr_stmt|;
name|this
operator|.
name|numberOfRowPrefixes
operator|=
name|numberOfRowPrefixes
expr_stmt|;
name|this
operator|.
name|averagePrefixLength
operator|=
name|averagePrefixLength
expr_stmt|;
name|this
operator|.
name|prefixLengthVariance
operator|=
name|prefixLengthVariance
expr_stmt|;
name|this
operator|.
name|averageSuffixLength
operator|=
name|averageSuffixLength
expr_stmt|;
name|this
operator|.
name|suffixLengthVariance
operator|=
name|suffixLengthVariance
expr_stmt|;
name|this
operator|.
name|numberOfRows
operator|=
name|numberOfRows
expr_stmt|;
name|this
operator|.
name|chanceForSameQualifier
operator|=
name|chanceForSameQualifier
expr_stmt|;
name|this
operator|.
name|chanceForSimilarQualifier
operator|=
name|chanceForSimiliarQualifier
expr_stmt|;
name|this
operator|.
name|averageQualifierLength
operator|=
name|averageQualifierLength
expr_stmt|;
name|this
operator|.
name|qualifierLengthVariance
operator|=
name|qualifierLengthVariance
expr_stmt|;
name|this
operator|.
name|columnFamilyLength
operator|=
name|columnFamilyLength
expr_stmt|;
name|this
operator|.
name|valueLength
operator|=
name|valueLength
expr_stmt|;
name|this
operator|.
name|chanceForZeroValue
operator|=
name|chanceForZeroValue
expr_stmt|;
name|this
operator|.
name|baseTimestampDivide
operator|=
name|baseTimestampDivide
expr_stmt|;
name|this
operator|.
name|timestampDiffSize
operator|=
name|timestampDiffSize
expr_stmt|;
block|}
comment|/** Used to generate dataset */
specifier|private
name|Random
name|randomizer
decl_stmt|;
comment|// row settings
specifier|private
name|byte
index|[]
name|commonPrefix
decl_stmt|;
comment|//global prefix before rowPrefixes
specifier|private
name|int
name|numberOfRowPrefixes
decl_stmt|;
specifier|private
name|int
name|averagePrefixLength
init|=
literal|6
decl_stmt|;
specifier|private
name|int
name|prefixLengthVariance
init|=
literal|3
decl_stmt|;
specifier|private
name|int
name|averageSuffixLength
init|=
literal|3
decl_stmt|;
specifier|private
name|int
name|suffixLengthVariance
init|=
literal|3
decl_stmt|;
specifier|private
name|int
name|numberOfRows
init|=
literal|500
decl_stmt|;
comment|//family
specifier|private
name|byte
index|[]
name|family
decl_stmt|;
comment|// qualifier
specifier|private
name|float
name|chanceForSameQualifier
init|=
literal|0.5f
decl_stmt|;
specifier|private
name|float
name|chanceForSimilarQualifier
init|=
literal|0.4f
decl_stmt|;
specifier|private
name|int
name|averageQualifierLength
init|=
literal|9
decl_stmt|;
specifier|private
name|int
name|qualifierLengthVariance
init|=
literal|3
decl_stmt|;
specifier|private
name|int
name|columnFamilyLength
init|=
literal|9
decl_stmt|;
specifier|private
name|int
name|valueLength
init|=
literal|8
decl_stmt|;
specifier|private
name|float
name|chanceForZeroValue
init|=
literal|0.5f
decl_stmt|;
specifier|private
name|int
name|baseTimestampDivide
init|=
literal|1000000
decl_stmt|;
specifier|private
name|int
name|timestampDiffSize
init|=
literal|100000000
decl_stmt|;
specifier|private
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|generateRows
parameter_list|()
block|{
comment|// generate prefixes
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|prefixes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|prefixes
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numberOfRowPrefixes
condition|;
operator|++
name|i
control|)
block|{
name|int
name|prefixLength
init|=
name|averagePrefixLength
decl_stmt|;
name|prefixLength
operator|+=
name|randomizer
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|prefixLengthVariance
operator|+
literal|1
argument_list|)
operator|-
name|prefixLengthVariance
expr_stmt|;
name|byte
index|[]
name|newPrefix
init|=
operator|new
name|byte
index|[
name|prefixLength
index|]
decl_stmt|;
name|randomizer
operator|.
name|nextBytes
argument_list|(
name|newPrefix
argument_list|)
expr_stmt|;
name|byte
index|[]
name|newPrefixWithCommon
init|=
name|newPrefix
decl_stmt|;
name|prefixes
operator|.
name|add
argument_list|(
name|newPrefixWithCommon
argument_list|)
expr_stmt|;
block|}
comment|// generate rest of the row
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
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
name|numberOfRows
condition|;
operator|++
name|i
control|)
block|{
name|int
name|suffixLength
init|=
name|averageSuffixLength
decl_stmt|;
name|suffixLength
operator|+=
name|randomizer
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|suffixLengthVariance
operator|+
literal|1
argument_list|)
operator|-
name|suffixLengthVariance
expr_stmt|;
name|int
name|randomPrefix
init|=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|prefixes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
operator|new
name|byte
index|[
name|prefixes
operator|.
name|get
argument_list|(
name|randomPrefix
argument_list|)
operator|.
name|length
operator|+
name|suffixLength
index|]
decl_stmt|;
name|byte
index|[]
name|rowWithCommonPrefix
init|=
name|Bytes
operator|.
name|concat
argument_list|(
name|commonPrefix
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|rows
operator|.
name|add
argument_list|(
name|rowWithCommonPrefix
argument_list|)
expr_stmt|;
block|}
return|return
name|rows
return|;
block|}
comment|/**    * Generate test data useful to test encoders.    * @param howMany How many Key values should be generated.    * @return sorted list of key values    */
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|generateTestKeyValues
parameter_list|(
name|int
name|howMany
parameter_list|)
block|{
return|return
name|generateTestKeyValues
argument_list|(
name|howMany
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Generate test data useful to test encoders.    * @param howMany How many Key values should be generated.    * @return sorted list of key values    */
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|generateTestKeyValues
parameter_list|(
name|int
name|howMany
parameter_list|,
name|boolean
name|useTags
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
init|=
name|generateRows
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|rowsToQualifier
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|family
operator|==
literal|null
condition|)
block|{
name|family
operator|=
operator|new
name|byte
index|[
name|columnFamilyLength
index|]
expr_stmt|;
name|randomizer
operator|.
name|nextBytes
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
name|long
name|baseTimestamp
init|=
name|Math
operator|.
name|abs
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|/
name|baseTimestampDivide
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|valueLength
index|]
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
name|howMany
condition|;
operator|++
name|i
control|)
block|{
name|long
name|timestamp
init|=
name|baseTimestamp
decl_stmt|;
if|if
condition|(
name|timestampDiffSize
operator|>
literal|0
condition|)
block|{
name|timestamp
operator|+=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|timestampDiffSize
argument_list|)
expr_stmt|;
block|}
name|Integer
name|rowId
init|=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|rows
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|rows
operator|.
name|get
argument_list|(
name|rowId
argument_list|)
decl_stmt|;
comment|// generate qualifier, sometimes it is same, sometimes similar,
comment|// occasionally completely different
name|byte
index|[]
name|qualifier
decl_stmt|;
name|float
name|qualifierChance
init|=
name|randomizer
operator|.
name|nextFloat
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|rowsToQualifier
operator|.
name|containsKey
argument_list|(
name|rowId
argument_list|)
operator|||
name|qualifierChance
operator|>
name|chanceForSameQualifier
operator|+
name|chanceForSimilarQualifier
condition|)
block|{
name|int
name|qualifierLength
init|=
name|averageQualifierLength
decl_stmt|;
name|qualifierLength
operator|+=
name|randomizer
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|qualifierLengthVariance
operator|+
literal|1
argument_list|)
operator|-
name|qualifierLengthVariance
expr_stmt|;
name|qualifier
operator|=
operator|new
name|byte
index|[
name|qualifierLength
index|]
expr_stmt|;
name|randomizer
operator|.
name|nextBytes
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
comment|// add it to map
if|if
condition|(
operator|!
name|rowsToQualifier
operator|.
name|containsKey
argument_list|(
name|rowId
argument_list|)
condition|)
block|{
name|rowsToQualifier
operator|.
name|put
argument_list|(
name|rowId
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rowsToQualifier
operator|.
name|get
argument_list|(
name|rowId
argument_list|)
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|qualifierChance
operator|>
name|chanceForSameQualifier
condition|)
block|{
comment|// similar qualifier
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|previousQualifiers
init|=
name|rowsToQualifier
operator|.
name|get
argument_list|(
name|rowId
argument_list|)
decl_stmt|;
name|byte
index|[]
name|originalQualifier
init|=
name|previousQualifiers
operator|.
name|get
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|(
name|previousQualifiers
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|qualifier
operator|=
operator|new
name|byte
index|[
name|originalQualifier
operator|.
name|length
index|]
expr_stmt|;
name|int
name|commonPrefix
init|=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|qualifier
operator|.
name|length
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|originalQualifier
argument_list|,
literal|0
argument_list|,
name|qualifier
argument_list|,
literal|0
argument_list|,
name|commonPrefix
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
name|commonPrefix
init|;
name|j
operator|<
name|qualifier
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|qualifier
index|[
name|j
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xff
argument_list|)
expr_stmt|;
block|}
name|rowsToQualifier
operator|.
name|get
argument_list|(
name|rowId
argument_list|)
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// same qualifier
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|previousQualifiers
init|=
name|rowsToQualifier
operator|.
name|get
argument_list|(
name|rowId
argument_list|)
decl_stmt|;
name|qualifier
operator|=
name|previousQualifiers
operator|.
name|get
argument_list|(
name|randomizer
operator|.
name|nextInt
argument_list|(
name|previousQualifiers
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomizer
operator|.
name|nextFloat
argument_list|()
operator|<
name|chanceForZeroValue
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|value
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|value
index|[
name|j
index|]
operator|=
operator|(
name|byte
operator|)
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
name|randomizer
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|useTags
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|value
argument_list|,
operator|new
name|Tag
index|[]
block|{
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"value1"
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|result
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Convert list of KeyValues to byte buffer.    * @param keyValues list of KeyValues to be converted.    * @return buffer with content from key values    */
specifier|public
specifier|static
name|ByteBuffer
name|convertKvToByteBuffer
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keyValues
parameter_list|,
name|boolean
name|includesMemstoreTS
parameter_list|)
block|{
name|int
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|keyValues
control|)
block|{
name|totalSize
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|includesMemstoreTS
condition|)
block|{
name|totalSize
operator|+=
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|kv
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ByteBuffer
name|result
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|totalSize
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|keyValues
control|)
block|{
name|result
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|includesMemstoreTS
condition|)
block|{
name|ByteBufferUtils
operator|.
name|writeVLong
argument_list|(
name|result
argument_list|,
name|kv
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/************************ get/set ***********************************/
specifier|public
name|RedundantKVGenerator
name|setCommonPrefix
parameter_list|(
name|byte
index|[]
name|prefix
parameter_list|)
block|{
name|this
operator|.
name|commonPrefix
operator|=
name|prefix
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setRandomizer
parameter_list|(
name|Random
name|randomizer
parameter_list|)
block|{
name|this
operator|.
name|randomizer
operator|=
name|randomizer
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setNumberOfRowPrefixes
parameter_list|(
name|int
name|numberOfRowPrefixes
parameter_list|)
block|{
name|this
operator|.
name|numberOfRowPrefixes
operator|=
name|numberOfRowPrefixes
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setAveragePrefixLength
parameter_list|(
name|int
name|averagePrefixLength
parameter_list|)
block|{
name|this
operator|.
name|averagePrefixLength
operator|=
name|averagePrefixLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setPrefixLengthVariance
parameter_list|(
name|int
name|prefixLengthVariance
parameter_list|)
block|{
name|this
operator|.
name|prefixLengthVariance
operator|=
name|prefixLengthVariance
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setAverageSuffixLength
parameter_list|(
name|int
name|averageSuffixLength
parameter_list|)
block|{
name|this
operator|.
name|averageSuffixLength
operator|=
name|averageSuffixLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setSuffixLengthVariance
parameter_list|(
name|int
name|suffixLengthVariance
parameter_list|)
block|{
name|this
operator|.
name|suffixLengthVariance
operator|=
name|suffixLengthVariance
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setNumberOfRows
parameter_list|(
name|int
name|numberOfRows
parameter_list|)
block|{
name|this
operator|.
name|numberOfRows
operator|=
name|numberOfRows
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setChanceForSameQualifier
parameter_list|(
name|float
name|chanceForSameQualifier
parameter_list|)
block|{
name|this
operator|.
name|chanceForSameQualifier
operator|=
name|chanceForSameQualifier
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setChanceForSimilarQualifier
parameter_list|(
name|float
name|chanceForSimiliarQualifier
parameter_list|)
block|{
name|this
operator|.
name|chanceForSimilarQualifier
operator|=
name|chanceForSimiliarQualifier
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setAverageQualifierLength
parameter_list|(
name|int
name|averageQualifierLength
parameter_list|)
block|{
name|this
operator|.
name|averageQualifierLength
operator|=
name|averageQualifierLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setQualifierLengthVariance
parameter_list|(
name|int
name|qualifierLengthVariance
parameter_list|)
block|{
name|this
operator|.
name|qualifierLengthVariance
operator|=
name|qualifierLengthVariance
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setColumnFamilyLength
parameter_list|(
name|int
name|columnFamilyLength
parameter_list|)
block|{
name|this
operator|.
name|columnFamilyLength
operator|=
name|columnFamilyLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|columnFamilyLength
operator|=
name|family
operator|.
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setValueLength
parameter_list|(
name|int
name|valueLength
parameter_list|)
block|{
name|this
operator|.
name|valueLength
operator|=
name|valueLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setChanceForZeroValue
parameter_list|(
name|float
name|chanceForZeroValue
parameter_list|)
block|{
name|this
operator|.
name|chanceForZeroValue
operator|=
name|chanceForZeroValue
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setBaseTimestampDivide
parameter_list|(
name|int
name|baseTimestampDivide
parameter_list|)
block|{
name|this
operator|.
name|baseTimestampDivide
operator|=
name|baseTimestampDivide
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RedundantKVGenerator
name|setTimestampDiffSize
parameter_list|(
name|int
name|timestampDiffSize
parameter_list|)
block|{
name|this
operator|.
name|timestampDiffSize
operator|=
name|timestampDiffSize
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

