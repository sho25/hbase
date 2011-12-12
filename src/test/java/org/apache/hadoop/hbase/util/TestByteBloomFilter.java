begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

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
name|TestByteBloomFilter
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testBasicBloom
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteBloomFilter
name|bf1
init|=
operator|new
name|ByteBloomFilter
argument_list|(
literal|1000
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
name|Hash
operator|.
name|MURMUR_HASH
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ByteBloomFilter
name|bf2
init|=
operator|new
name|ByteBloomFilter
argument_list|(
literal|1000
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
name|Hash
operator|.
name|MURMUR_HASH
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|bf1
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
name|bf2
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
comment|// test 1: verify no fundamental false negatives or positives
name|byte
index|[]
name|key1
init|=
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|}
decl_stmt|;
name|byte
index|[]
name|key2
init|=
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|7
block|}
decl_stmt|;
name|bf1
operator|.
name|add
argument_list|(
name|key1
argument_list|)
expr_stmt|;
name|bf2
operator|.
name|add
argument_list|(
name|key2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|key1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|key2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf2
operator|.
name|contains
argument_list|(
name|key1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf2
operator|.
name|contains
argument_list|(
name|key2
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bkey
init|=
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
decl_stmt|;
name|byte
index|[]
name|bval
init|=
literal|"this is a much larger byte array"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|bf1
operator|.
name|add
argument_list|(
name|bkey
argument_list|)
expr_stmt|;
name|bf1
operator|.
name|add
argument_list|(
name|bval
argument_list|,
literal|1
argument_list|,
name|bval
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|bkey
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|,
literal|1
argument_list|,
name|bval
operator|.
name|length
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|)
argument_list|)
expr_stmt|;
comment|// test 2: serialization& deserialization.
comment|// (convert bloom to byte array& read byte array back in as input)
name|ByteArrayOutputStream
name|bOut
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|bf1
operator|.
name|writeBloom
argument_list|(
operator|new
name|DataOutputStream
argument_list|(
name|bOut
argument_list|)
argument_list|)
expr_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bOut
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBloomFilter
name|newBf1
init|=
operator|new
name|ByteBloomFilter
argument_list|(
literal|1000
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
name|Hash
operator|.
name|MURMUR_HASH
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|key1
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|key2
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|bkey
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|,
literal|1
argument_list|,
name|bval
operator|.
name|length
operator|-
literal|1
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|newBf1
operator|.
name|contains
argument_list|(
name|bval
argument_list|,
name|bb
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Serialized as "
operator|+
name|bOut
operator|.
name|size
argument_list|()
operator|+
literal|" bytes"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bOut
operator|.
name|size
argument_list|()
operator|-
name|bf1
operator|.
name|byteSize
operator|<
literal|10
argument_list|)
expr_stmt|;
comment|//... allow small padding
block|}
specifier|public
name|void
name|testBloomFold
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test: foldFactor< log(max/actual)
name|ByteBloomFilter
name|b
init|=
operator|new
name|ByteBloomFilter
argument_list|(
literal|1003
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
name|Hash
operator|.
name|MURMUR_HASH
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|b
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
name|long
name|origSize
init|=
name|b
operator|.
name|getByteSize
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1204
argument_list|,
name|origSize
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|12
condition|;
operator|++
name|i
control|)
block|{
name|b
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|compactBloom
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|origSize
operator|>>
literal|2
argument_list|,
name|b
operator|.
name|getByteSize
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|falsePositives
init|=
literal|0
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
literal|25
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|b
operator|.
name|contains
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|i
operator|>=
literal|12
condition|)
name|falsePositives
operator|++
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|i
operator|<
literal|12
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|falsePositives
operator|<=
literal|1
argument_list|)
expr_stmt|;
comment|// test: foldFactor> log(max/actual)
block|}
specifier|public
name|void
name|testBloomPerf
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add
name|float
name|err
init|=
operator|(
name|float
operator|)
literal|0.01
decl_stmt|;
name|ByteBloomFilter
name|b
init|=
operator|new
name|ByteBloomFilter
argument_list|(
literal|10
operator|*
literal|1000
operator|*
literal|1000
argument_list|,
operator|(
name|float
operator|)
name|err
argument_list|,
name|Hash
operator|.
name|MURMUR_HASH
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|b
operator|.
name|allocBloom
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|origSize
init|=
name|b
operator|.
name|getByteSize
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
literal|1
operator|*
literal|1000
operator|*
literal|1000
condition|;
operator|++
name|i
control|)
block|{
name|b
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Total Add time = "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
comment|// fold
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|b
operator|.
name|compactBloom
argument_list|()
expr_stmt|;
name|endTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Total Fold time = "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|origSize
operator|>=
name|b
operator|.
name|getByteSize
argument_list|()
operator|<<
literal|3
argument_list|)
expr_stmt|;
comment|// test
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|int
name|falsePositives
init|=
literal|0
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
literal|2
operator|*
literal|1000
operator|*
literal|1000
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|b
operator|.
name|contains
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|i
operator|>=
literal|1
operator|*
literal|1000
operator|*
literal|1000
condition|)
name|falsePositives
operator|++
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|i
operator|<
literal|1
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
name|endTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Total Contains time = "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"False Positive = "
operator|+
name|falsePositives
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|falsePositives
operator|<=
operator|(
literal|1
operator|*
literal|1000
operator|*
literal|1000
operator|)
operator|*
name|err
argument_list|)
expr_stmt|;
comment|// test: foldFactor> log(max/actual)
block|}
specifier|public
name|void
name|testSizing
parameter_list|()
block|{
name|int
name|bitSize
init|=
literal|8
operator|*
literal|128
operator|*
literal|1024
decl_stmt|;
comment|// 128 KB
name|double
name|errorRate
init|=
literal|0.025
decl_stmt|;
comment|// target false positive rate
comment|// How many keys can we store in a Bloom filter of this size maintaining
comment|// the given false positive rate, not taking into account that the n
name|long
name|maxKeys
init|=
name|ByteBloomFilter
operator|.
name|idealMaxKeys
argument_list|(
name|bitSize
argument_list|,
name|errorRate
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|136570
argument_list|,
name|maxKeys
argument_list|)
expr_stmt|;
comment|// A reverse operation: how many bits would we need to store this many keys
comment|// and keep the same low false positive rate?
name|long
name|bitSize2
init|=
name|ByteBloomFilter
operator|.
name|computeBitSize
argument_list|(
name|maxKeys
argument_list|,
name|errorRate
argument_list|)
decl_stmt|;
comment|// The bit size comes out a little different due to rounding.
name|assertTrue
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|bitSize2
operator|-
name|bitSize
argument_list|)
operator|*
literal|1.0
operator|/
name|bitSize
operator|<
literal|1e-5
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFoldableByteSize
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|128
argument_list|,
name|ByteBloomFilter
operator|.
name|computeFoldableByteSize
argument_list|(
literal|1000
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|640
argument_list|,
name|ByteBloomFilter
operator|.
name|computeFoldableByteSize
argument_list|(
literal|5001
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

