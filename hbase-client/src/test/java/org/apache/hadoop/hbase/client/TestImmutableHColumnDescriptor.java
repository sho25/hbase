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
name|client
package|;
end_package

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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|HColumnDescriptor
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
name|KeepDeletedCells
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
name|MemoryCompactionPolicy
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|BloomType
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
name|ClientTests
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
name|util
operator|.
name|BuilderStyleTest
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
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestImmutableHColumnDescriptor
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Consumer
argument_list|<
name|ImmutableHColumnDescriptor
argument_list|>
argument_list|>
name|TEST_FUNCTION
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|hcd
lambda|->
name|hcd
operator|.
name|setValue
argument_list|(
literal|"a"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setConfiguration
argument_list|(
literal|"aaa"
argument_list|,
literal|"ccc"
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|remove
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|removeConfiguration
argument_list|(
literal|"xxx"
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|10
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCacheBloomsOnWrite
argument_list|(
literal|false
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCacheDataInL1
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCacheDataOnWrite
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCacheIndexesOnWrite
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCompactionCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|LZO
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCompressTags
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|LZO
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
literal|10
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setDataBlockEncoding
argument_list|(
name|DataBlockEncoding
operator|.
name|NONE
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setEncryptionKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xxx"
argument_list|)
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setEncryptionType
argument_list|(
literal|"xxx"
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setEvictBlocksOnClose
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setInMemory
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setInMemoryCompaction
argument_list|(
name|MemoryCompactionPolicy
operator|.
name|NONE
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setKeepDeletedCells
argument_list|(
name|KeepDeletedCells
operator|.
name|FALSE
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|1000
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setMinVersions
argument_list|(
literal|10
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setMobCompactPartitionPolicy
argument_list|(
name|MobCompactPartitionPolicy
operator|.
name|DAILY
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setMobThreshold
argument_list|(
literal|10
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setPrefetchBlocksOnOpen
argument_list|(
literal|true
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setScope
argument_list|(
literal|0
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setStoragePolicy
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setTimeToLive
argument_list|(
literal|100
argument_list|)
argument_list|,
name|hcd
lambda|->
name|hcd
operator|.
name|setVersions
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testImmutable
parameter_list|()
block|{
name|ImmutableHColumnDescriptor
name|hcd
init|=
operator|new
name|ImmutableHColumnDescriptor
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
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
operator|!=
name|TEST_FUNCTION
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
name|TEST_FUNCTION
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|accept
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"ImmutableHTableDescriptor can't be modified!!! The index of method is "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{       }
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClassMethodsAreBuilderStyle
parameter_list|()
block|{
name|BuilderStyleTest
operator|.
name|assertClassesAreBuilderStyle
argument_list|(
name|ImmutableHColumnDescriptor
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
