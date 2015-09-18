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
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|timestamp
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
name|Collection
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
name|nio
operator|.
name|SingleByteBuff
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|timestamp
operator|.
name|TimestampDecoder
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
operator|.
name|LongEncoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
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
name|TestTimestampEncoder
block|{
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
operator|new
name|TestTimestampData
operator|.
name|InMemory
argument_list|()
operator|.
name|getAllAsObjectArray
argument_list|()
return|;
block|}
specifier|private
name|TestTimestampData
name|timestamps
decl_stmt|;
specifier|private
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|private
name|LongEncoder
name|encoder
decl_stmt|;
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|private
name|TimestampDecoder
name|decoder
decl_stmt|;
specifier|public
name|TestTimestampEncoder
parameter_list|(
name|TestTimestampData
name|testTimestamps
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|timestamps
operator|=
name|testTimestamps
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|=
operator|new
name|PrefixTreeBlockMeta
argument_list|()
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|.
name|setNumMetaBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|.
name|setNumRowBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockMeta
operator|.
name|setNumQualifierBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|encoder
operator|=
operator|new
name|LongEncoder
argument_list|()
expr_stmt|;
for|for
control|(
name|Long
name|ts
range|:
name|testTimestamps
operator|.
name|getInputs
argument_list|()
control|)
block|{
name|encoder
operator|.
name|add
argument_list|(
name|ts
argument_list|)
expr_stmt|;
block|}
name|encoder
operator|.
name|compile
argument_list|()
expr_stmt|;
name|blockMeta
operator|.
name|setTimestampFields
argument_list|(
name|encoder
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|encoder
operator|.
name|getByteArray
argument_list|()
expr_stmt|;
name|decoder
operator|=
operator|new
name|TimestampDecoder
argument_list|()
expr_stmt|;
name|decoder
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
operator|new
name|SingleByteBuff
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressorMinimum
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|timestamps
operator|.
name|getMinimum
argument_list|()
argument_list|,
name|encoder
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressorRoundTrip
parameter_list|()
block|{
name|long
index|[]
name|outputs
init|=
name|encoder
operator|.
name|getSortedUniqueTimestamps
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
name|timestamps
operator|.
name|getOutputs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|long
name|input
init|=
name|timestamps
operator|.
name|getOutputs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|output
init|=
name|outputs
index|[
name|i
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|input
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReaderMinimum
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|timestamps
operator|.
name|getMinimum
argument_list|()
argument_list|,
name|decoder
operator|.
name|getLong
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReaderRoundTrip
parameter_list|()
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
name|timestamps
operator|.
name|getOutputs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|long
name|input
init|=
name|timestamps
operator|.
name|getOutputs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|output
init|=
name|decoder
operator|.
name|getLong
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|input
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

