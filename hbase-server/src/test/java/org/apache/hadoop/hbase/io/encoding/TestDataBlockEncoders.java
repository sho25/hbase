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
name|io
operator|.
name|encoding
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

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
name|Collection
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
name|HBaseTestingUtility
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
name|HConstants
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
name|KeyValue
operator|.
name|Type
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
name|LargeTests
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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|HFileContext
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
name|HFileContextBuilder
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
name|test
operator|.
name|RedundantKVGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Test all of the data block encoding algorithms for correctness. Most of the  * class generate data which will test different branches in code.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDataBlockEncoders
block|{
specifier|private
specifier|static
name|int
name|NUMBER_OF_KV
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|static
name|int
name|NUM_RANDOM_SEEKS
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|static
name|int
name|ENCODED_DATA_OFFSET
init|=
name|HConstants
operator|.
name|HFILEBLOCK_HEADER_SIZE
operator|+
name|DataBlockEncoding
operator|.
name|ID_SIZE
decl_stmt|;
specifier|private
name|RedundantKVGenerator
name|generator
init|=
operator|new
name|RedundantKVGenerator
argument_list|()
decl_stmt|;
specifier|private
name|Random
name|randomizer
init|=
operator|new
name|Random
argument_list|(
literal|42l
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|includesMemstoreTS
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|includesTags
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
name|HBaseTestingUtility
operator|.
name|MEMSTORETS_TAGS_PARAMETRIZED
return|;
block|}
specifier|public
name|TestDataBlockEncoders
parameter_list|(
name|boolean
name|includesMemstoreTS
parameter_list|,
name|boolean
name|includesTag
parameter_list|)
block|{
name|this
operator|.
name|includesMemstoreTS
operator|=
name|includesMemstoreTS
expr_stmt|;
name|this
operator|.
name|includesTags
operator|=
name|includesTag
expr_stmt|;
block|}
specifier|private
name|HFileBlockEncodingContext
name|getEncodingContext
parameter_list|(
name|Compression
operator|.
name|Algorithm
name|algo
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|)
block|{
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTags
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|algo
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|encoder
operator|!=
literal|null
condition|)
block|{
return|return
name|encoder
operator|.
name|newDataBlockEncodingContext
argument_list|(
name|encoding
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|,
name|meta
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|HFileBlockDefaultEncodingContext
argument_list|(
name|encoding
argument_list|,
name|HConstants
operator|.
name|HFILEBLOCK_DUMMY_HEADER
argument_list|,
name|meta
argument_list|)
return|;
block|}
block|}
specifier|private
name|byte
index|[]
name|encodeBytes
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|,
name|ByteBuffer
name|dataset
parameter_list|)
throws|throws
name|IOException
block|{
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|HFileBlockEncodingContext
name|encodingCtx
init|=
name|getEncodingContext
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|,
name|encoding
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|encodeKeyValues
argument_list|(
name|dataset
argument_list|,
name|encodingCtx
argument_list|)
expr_stmt|;
name|byte
index|[]
name|encodedBytesWithHeader
init|=
name|encodingCtx
operator|.
name|getUncompressedBytesWithHeader
argument_list|()
decl_stmt|;
name|byte
index|[]
name|encodedData
init|=
operator|new
name|byte
index|[
name|encodedBytesWithHeader
operator|.
name|length
operator|-
name|ENCODED_DATA_OFFSET
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|encodedBytesWithHeader
argument_list|,
name|ENCODED_DATA_OFFSET
argument_list|,
name|encodedData
argument_list|,
literal|0
argument_list|,
name|encodedData
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|encodedData
return|;
block|}
specifier|private
name|void
name|testAlgorithm
parameter_list|(
name|ByteBuffer
name|dataset
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
parameter_list|)
throws|throws
name|IOException
block|{
comment|// encode
name|byte
index|[]
name|encodedBytes
init|=
name|encodeBytes
argument_list|(
name|encoding
argument_list|,
name|dataset
argument_list|)
decl_stmt|;
comment|// decode
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|encodedBytes
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|ByteBuffer
name|actualDataset
decl_stmt|;
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTags
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|actualDataset
operator|=
name|encoder
operator|.
name|decodeKeyValues
argument_list|(
name|dis
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
expr_stmt|;
name|dataset
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|actualDataset
operator|.
name|rewind
argument_list|()
expr_stmt|;
comment|// this is because in case of prefix tree the decoded stream will not have
comment|// the
comment|// mvcc in it.
comment|// if (encoding != DataBlockEncoding.PREFIX_TREE) {
name|assertEquals
argument_list|(
literal|"Encoding -> decoding gives different results for "
operator|+
name|encoder
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|dataset
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|actualDataset
argument_list|)
argument_list|)
expr_stmt|;
comment|// }
block|}
comment|/**    * Test data block encoding of empty KeyValue.    *     * @throws IOException    *           On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testEmptyKeyValues
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|family
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|includesTags
condition|)
block|{
name|kvList
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
literal|0l
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|kvList
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
literal|0l
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|metaValue1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metaValue1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|metaValue2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metaValue2"
argument_list|)
decl_stmt|;
name|kvList
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
literal|0l
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
name|metaValue1
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|kvList
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
literal|0l
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
name|metaValue2
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|testEncodersOnDataset
argument_list|(
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|kvList
argument_list|,
name|includesMemstoreTS
argument_list|)
argument_list|,
name|kvList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test KeyValues with negative timestamp.    *     * @throws IOException    *           On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testNegativeTimestamps
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|family
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|includesTags
condition|)
block|{
name|byte
index|[]
name|metaValue1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metaValue1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|metaValue2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metaValue2"
argument_list|)
decl_stmt|;
name|kvList
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
literal|0l
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
name|metaValue1
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|kvList
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
literal|0l
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
name|metaValue2
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kvList
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
operator|-
literal|1l
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|kvList
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
operator|-
literal|2l
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|testEncodersOnDataset
argument_list|(
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|kvList
argument_list|,
name|includesMemstoreTS
argument_list|)
argument_list|,
name|kvList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test whether compression -> decompression gives the consistent results on    * pseudorandom sample.    * @throws IOException On test failure.    */
annotation|@
name|Test
specifier|public
name|void
name|testExecutionOnSample
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
name|NUMBER_OF_KV
argument_list|,
name|includesTags
argument_list|)
decl_stmt|;
name|testEncodersOnDataset
argument_list|(
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|kvList
argument_list|,
name|includesMemstoreTS
argument_list|)
argument_list|,
name|kvList
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test seeking while file is encoded.    */
annotation|@
name|Test
specifier|public
name|void
name|testSeekingOnSample
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|sampleKv
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
name|NUMBER_OF_KV
argument_list|,
name|includesTags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|originalBuffer
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|sampleKv
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
comment|// create all seekers
name|List
argument_list|<
name|DataBlockEncoder
operator|.
name|EncodedSeeker
argument_list|>
name|encodedSeekers
init|=
operator|new
name|ArrayList
argument_list|<
name|DataBlockEncoder
operator|.
name|EncodedSeeker
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|encoding
operator|.
name|getEncoder
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|ByteBuffer
name|encodedBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|encodeBytes
argument_list|(
name|encoding
argument_list|,
name|originalBuffer
argument_list|)
argument_list|)
decl_stmt|;
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTags
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DataBlockEncoder
operator|.
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|setCurrentBuffer
argument_list|(
name|encodedBuffer
argument_list|)
expr_stmt|;
name|encodedSeekers
operator|.
name|add
argument_list|(
name|seeker
argument_list|)
expr_stmt|;
block|}
comment|// test it!
comment|// try a few random seeks
for|for
control|(
name|boolean
name|seekBefore
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_RANDOM_SEEKS
condition|;
operator|++
name|i
control|)
block|{
name|int
name|keyValueId
decl_stmt|;
if|if
condition|(
operator|!
name|seekBefore
condition|)
block|{
name|keyValueId
operator|=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|sampleKv
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyValueId
operator|=
name|randomizer
operator|.
name|nextInt
argument_list|(
name|sampleKv
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|+
literal|1
expr_stmt|;
block|}
name|KeyValue
name|keyValue
init|=
name|sampleKv
operator|.
name|get
argument_list|(
name|keyValueId
argument_list|)
decl_stmt|;
name|checkSeekingConsistency
argument_list|(
name|encodedSeekers
argument_list|,
name|seekBefore
argument_list|,
name|keyValue
argument_list|)
expr_stmt|;
block|}
block|}
comment|// check edge cases
name|checkSeekingConsistency
argument_list|(
name|encodedSeekers
argument_list|,
literal|false
argument_list|,
name|sampleKv
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|boolean
name|seekBefore
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|checkSeekingConsistency
argument_list|(
name|encodedSeekers
argument_list|,
name|seekBefore
argument_list|,
name|sampleKv
operator|.
name|get
argument_list|(
name|sampleKv
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|KeyValue
name|midKv
init|=
name|sampleKv
operator|.
name|get
argument_list|(
name|sampleKv
operator|.
name|size
argument_list|()
operator|/
literal|2
argument_list|)
decl_stmt|;
name|KeyValue
name|lastMidKv
init|=
name|midKv
operator|.
name|createLastOnRowCol
argument_list|()
decl_stmt|;
name|checkSeekingConsistency
argument_list|(
name|encodedSeekers
argument_list|,
name|seekBefore
argument_list|,
name|lastMidKv
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNextOnSample
parameter_list|()
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|sampleKv
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
name|NUMBER_OF_KV
argument_list|,
name|includesTags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|originalBuffer
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|sampleKv
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|encoding
operator|.
name|getEncoder
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|ByteBuffer
name|encodedBuffer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|encodedBuffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|encodeBytes
argument_list|(
name|encoding
argument_list|,
name|originalBuffer
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Bug while encoding using '%s'"
argument_list|,
name|encoder
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withHBaseCheckSum
argument_list|(
literal|false
argument_list|)
operator|.
name|withIncludesMvcc
argument_list|(
name|includesMemstoreTS
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
name|includesTags
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DataBlockEncoder
operator|.
name|EncodedSeeker
name|seeker
init|=
name|encoder
operator|.
name|createSeeker
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
argument_list|,
name|encoder
operator|.
name|newDataBlockDecodingContext
argument_list|(
name|meta
argument_list|)
argument_list|)
decl_stmt|;
name|seeker
operator|.
name|setCurrentBuffer
argument_list|(
name|encodedBuffer
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
do|do
block|{
name|KeyValue
name|expectedKeyValue
init|=
name|sampleKv
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ByteBuffer
name|keyValue
init|=
name|seeker
operator|.
name|getKeyValueBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keyValue
operator|.
name|array
argument_list|()
argument_list|,
name|keyValue
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|keyValue
operator|.
name|limit
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getOffset
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getLength
argument_list|()
argument_list|)
condition|)
block|{
name|int
name|commonPrefix
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|left
init|=
name|keyValue
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|right
init|=
name|expectedKeyValue
operator|.
name|getBuffer
argument_list|()
decl_stmt|;
name|int
name|leftOff
init|=
name|keyValue
operator|.
name|arrayOffset
argument_list|()
decl_stmt|;
name|int
name|rightOff
init|=
name|expectedKeyValue
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|Math
operator|.
name|min
argument_list|(
name|keyValue
operator|.
name|limit
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|commonPrefix
operator|<
name|length
operator|&&
name|left
index|[
name|commonPrefix
operator|+
name|leftOff
index|]
operator|==
name|right
index|[
name|commonPrefix
operator|+
name|rightOff
index|]
condition|)
block|{
name|commonPrefix
operator|++
expr_stmt|;
block|}
name|fail
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"next() produces wrong results "
operator|+
literal|"encoder: %s i: %d commonPrefix: %d"
operator|+
literal|"\n expected %s\n actual      %s"
argument_list|,
name|encoder
operator|.
name|toString
argument_list|()
argument_list|,
name|i
argument_list|,
name|commonPrefix
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|expectedKeyValue
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getOffset
argument_list|()
argument_list|,
name|expectedKeyValue
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|keyValue
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|seeker
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
block|}
comment|/**    * Test whether the decompression of first key is implemented correctly.    */
annotation|@
name|Test
specifier|public
name|void
name|testFirstKeyInBlockOnSample
parameter_list|()
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|sampleKv
init|=
name|generator
operator|.
name|generateTestKeyValues
argument_list|(
name|NUMBER_OF_KV
argument_list|,
name|includesTags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|originalBuffer
init|=
name|RedundantKVGenerator
operator|.
name|convertKvToByteBuffer
argument_list|(
name|sampleKv
argument_list|,
name|includesMemstoreTS
argument_list|)
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|encoding
operator|.
name|getEncoder
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|DataBlockEncoder
name|encoder
init|=
name|encoding
operator|.
name|getEncoder
argument_list|()
decl_stmt|;
name|ByteBuffer
name|encodedBuffer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|encodedBuffer
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|encodeBytes
argument_list|(
name|encoding
argument_list|,
name|originalBuffer
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Bug while encoding using '%s'"
argument_list|,
name|encoder
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|ByteBuffer
name|keyBuffer
init|=
name|encoder
operator|.
name|getFirstKeyInBlock
argument_list|(
name|encodedBuffer
argument_list|)
decl_stmt|;
name|KeyValue
name|firstKv
init|=
name|sampleKv
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keyBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|keyBuffer
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|keyBuffer
operator|.
name|limit
argument_list|()
argument_list|,
name|firstKv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|firstKv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|firstKv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
condition|)
block|{
name|int
name|commonPrefix
init|=
literal|0
decl_stmt|;
name|int
name|length
init|=
name|Math
operator|.
name|min
argument_list|(
name|keyBuffer
operator|.
name|limit
argument_list|()
argument_list|,
name|firstKv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|commonPrefix
operator|<
name|length
operator|&&
name|keyBuffer
operator|.
name|array
argument_list|()
index|[
name|keyBuffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|commonPrefix
index|]
operator|==
name|firstKv
operator|.
name|getBuffer
argument_list|()
index|[
name|firstKv
operator|.
name|getKeyOffset
argument_list|()
operator|+
name|commonPrefix
index|]
condition|)
block|{
name|commonPrefix
operator|++
expr_stmt|;
block|}
name|fail
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Bug in '%s' commonPrefix %d"
argument_list|,
name|encoder
operator|.
name|toString
argument_list|()
argument_list|,
name|commonPrefix
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|checkSeekingConsistency
parameter_list|(
name|List
argument_list|<
name|DataBlockEncoder
operator|.
name|EncodedSeeker
argument_list|>
name|encodedSeekers
parameter_list|,
name|boolean
name|seekBefore
parameter_list|,
name|KeyValue
name|keyValue
parameter_list|)
block|{
name|ByteBuffer
name|expectedKeyValue
init|=
literal|null
decl_stmt|;
name|ByteBuffer
name|expectedKey
init|=
literal|null
decl_stmt|;
name|ByteBuffer
name|expectedValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DataBlockEncoder
operator|.
name|EncodedSeeker
name|seeker
range|:
name|encodedSeekers
control|)
block|{
name|seeker
operator|.
name|seekToKeyInBlock
argument_list|(
name|keyValue
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|keyValue
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|keyValue
operator|.
name|getKeyLength
argument_list|()
argument_list|,
name|seekBefore
argument_list|)
expr_stmt|;
name|seeker
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|ByteBuffer
name|actualKeyValue
init|=
name|seeker
operator|.
name|getKeyValueBuffer
argument_list|()
decl_stmt|;
name|ByteBuffer
name|actualKey
init|=
name|seeker
operator|.
name|getKeyDeepCopy
argument_list|()
decl_stmt|;
name|ByteBuffer
name|actualValue
init|=
name|seeker
operator|.
name|getValueShallowCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|expectedKeyValue
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedKeyValue
argument_list|,
name|actualKeyValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|expectedKeyValue
operator|=
name|actualKeyValue
expr_stmt|;
block|}
if|if
condition|(
name|expectedKey
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedKey
argument_list|,
name|actualKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|expectedKey
operator|=
name|actualKey
expr_stmt|;
block|}
if|if
condition|(
name|expectedValue
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedValue
argument_list|,
name|actualValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|expectedValue
operator|=
name|actualValue
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|testEncodersOnDataset
parameter_list|(
name|ByteBuffer
name|onDataset
parameter_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|dataset
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|onDataset
operator|.
name|capacity
argument_list|()
argument_list|)
decl_stmt|;
name|onDataset
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|dataset
operator|.
name|put
argument_list|(
name|onDataset
argument_list|)
expr_stmt|;
name|onDataset
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|dataset
operator|.
name|flip
argument_list|()
expr_stmt|;
for|for
control|(
name|DataBlockEncoding
name|encoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|encoding
operator|.
name|getEncoder
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|testAlgorithm
argument_list|(
name|dataset
argument_list|,
name|encoding
argument_list|,
name|kvList
argument_list|)
expr_stmt|;
comment|// ensure that dataset is unchanged
name|dataset
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Input of two methods is changed"
argument_list|,
name|onDataset
argument_list|,
name|dataset
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

