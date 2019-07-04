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
name|regionserver
package|;
end_package

begin_import
import|import static
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
name|DefaultStoreEngine
operator|.
name|DEFAULT_COMPACTOR_CLASS_KEY
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|Path
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
name|HBaseClassTestRule
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
operator|.
name|KeyOnlyKeyValue
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
name|TableName
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
name|client
operator|.
name|Table
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
name|ByteBuffAllocator
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
name|HFileWriterImpl
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
name|compactions
operator|.
name|CompactionRequestImpl
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
name|compactions
operator|.
name|DefaultCompactor
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
name|MediumTests
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
name|RegionServerTests
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
name|junit
operator|.
name|AfterClass
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCompactorMemLeak
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Configuration
name|CONF
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|IS_LAST_CELL_ON_HEAP
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
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
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestCompactorMemLeak
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|IS_LAST_CELL_ON_HEAP
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Must use the ByteBuffAllocator here
name|CONF
operator|.
name|setBoolean
argument_list|(
name|ByteBuffAllocator
operator|.
name|ALLOCATOR_POOL_ENABLED_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Must use OFF-HEAP BucketCache here.
name|CONF
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|CONF
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
comment|// 32MB for BucketCache.
name|CONF
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_SIZE_KEY
argument_list|,
literal|32
argument_list|)
expr_stmt|;
comment|// Use the MyCompactor
name|CONF
operator|.
name|set
argument_list|(
name|DEFAULT_COMPACTOR_CLASS_KEY
argument_list|,
name|MyCompactor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|IS_LAST_CELL_ON_HEAP
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|assertMajorCompactionOK
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|region
operator|.
name|getStores
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMemLeak
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// Put and Flush #1
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Put and Flush #2
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Major compact
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|majorCompact
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|6000
argument_list|)
expr_stmt|;
name|assertMajorCompactionOK
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// The last cell before Compactor#commitWriter must be an heap one.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|IS_LAST_CELL_ON_HEAP
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MyCompactor
extends|extends
name|DefaultCompactor
block|{
specifier|public
name|MyCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HStore
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Path
argument_list|>
name|commitWriter
parameter_list|(
name|StoreFileWriter
name|writer
parameter_list|,
name|FileDetails
name|fd
parameter_list|,
name|CompactionRequestImpl
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileWriterImpl
name|writerImpl
init|=
operator|(
name|HFileWriterImpl
operator|)
name|writer
operator|.
name|writer
decl_stmt|;
name|Cell
name|cell
init|=
name|writerImpl
operator|.
name|getLastCell
argument_list|()
decl_stmt|;
comment|// The cell should be backend with an KeyOnlyKeyValue.
name|IS_LAST_CELL_ON_HEAP
operator|.
name|set
argument_list|(
name|cell
operator|instanceof
name|KeyOnlyKeyValue
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|commitWriter
argument_list|(
name|writer
argument_list|,
name|fd
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

