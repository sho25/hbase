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
name|io
operator|.
name|hfile
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
name|assertFalse
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
name|assertTrue
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
name|ArrayList
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
name|hbase
operator|.
name|ByteBufferKeyValue
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
name|PrivateCellUtil
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
name|ColumnFamilyDescriptorBuilder
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
name|Durability
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|Scan
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
name|TableDescriptorBuilder
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
name|HRegion
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
name|HStore
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
name|InternalScanner
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
name|EnvironmentEdgeManagerTestHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestScannerFromBucketCache
block|{
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
name|TestScannerFromBucketCache
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestScannerFromBucketCache
operator|.
name|class
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
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|test_util
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|2
decl_stmt|;
name|byte
index|[]
name|val
init|=
operator|new
name|byte
index|[
literal|512
operator|*
literal|1024
index|]
decl_stmt|;
comment|// Test names
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|void
name|setUp
parameter_list|(
name|boolean
name|useBucketCache
parameter_list|)
throws|throws
name|IOException
block|{
name|test_util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|conf
operator|=
name|test_util
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
if|if
condition|(
name|useBucketCache
condition|)
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.bucketcache.size"
argument_list|,
literal|400
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|BUCKET_CACHE_IOENGINE_KEY
argument_list|,
literal|"offheap"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.bucketcache.writer.threads"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hfile.block.cache.size"
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.regionserver.global.memstore.size"
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
block|}
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|EnvironmentEdgeManagerTestHelper
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning test directory: "
operator|+
name|test_util
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
expr_stmt|;
name|test_util
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
operator|.
name|getMethodName
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicScanWithLRUCache
parameter_list|()
throws|throws
name|IOException
block|{
name|setUp
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"lrucache"
argument_list|)
decl_stmt|;
name|long
name|ts1
init|=
literal|1
decl_stmt|;
comment|// System.currentTimeMillis();
name|long
name|ts2
init|=
name|ts1
operator|+
literal|1
decl_stmt|;
name|long
name|ts3
init|=
name|ts1
operator|+
literal|2
decl_stmt|;
comment|// Setting up region
name|String
name|method
init|=
name|this
operator|.
name|getName
argument_list|()
decl_stmt|;
name|this
operator|.
name|region
operator|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|method
argument_list|,
name|conf
argument_list|,
name|test_util
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|expected
init|=
name|insertData
argument_list|(
name|row1
argument_list|,
name|qf1
argument_list|,
name|qf2
argument_list|,
name|fam1
argument_list|,
name|ts1
argument_list|,
name|ts2
argument_list|,
name|ts3
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|actual
init|=
name|performScan
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|)
decl_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// do the scan again and verify. This time it should be from the lru cache
name|actual
operator|=
name|performScan
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|this
operator|.
name|region
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicScanWithOffheapBucketCache
parameter_list|()
throws|throws
name|IOException
block|{
name|setUp
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1offheap"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"famoffheap"
argument_list|)
decl_stmt|;
name|long
name|ts1
init|=
literal|1
decl_stmt|;
comment|// System.currentTimeMillis();
name|long
name|ts2
init|=
name|ts1
operator|+
literal|1
decl_stmt|;
name|long
name|ts3
init|=
name|ts1
operator|+
literal|2
decl_stmt|;
comment|// Setting up region
name|String
name|method
init|=
name|this
operator|.
name|getName
argument_list|()
decl_stmt|;
name|this
operator|.
name|region
operator|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|method
argument_list|,
name|conf
argument_list|,
name|test_util
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|expected
init|=
name|insertData
argument_list|(
name|row1
argument_list|,
name|qf1
argument_list|,
name|qf2
argument_list|,
name|fam1
argument_list|,
name|ts1
argument_list|,
name|ts2
argument_list|,
name|ts3
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|actual
init|=
name|performScan
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|)
decl_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Wait for the bucket cache threads to move the data to offheap
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
comment|// do the scan again and verify. This time it should be from the bucket cache in offheap mode
name|actual
operator|=
name|performScan
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{     }
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|this
operator|.
name|region
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicScanWithOffheapBucketCacheWithMBB
parameter_list|()
throws|throws
name|IOException
block|{
name|setUp
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1offheap"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qf2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier2"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"famoffheap"
argument_list|)
decl_stmt|;
name|long
name|ts1
init|=
literal|1
decl_stmt|;
comment|// System.currentTimeMillis();
name|long
name|ts2
init|=
name|ts1
operator|+
literal|1
decl_stmt|;
name|long
name|ts3
init|=
name|ts1
operator|+
literal|2
decl_stmt|;
comment|// Setting up region
name|String
name|method
init|=
name|this
operator|.
name|getName
argument_list|()
decl_stmt|;
name|this
operator|.
name|region
operator|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|method
argument_list|,
name|conf
argument_list|,
name|test_util
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|expected
init|=
name|insertData
argument_list|(
name|row1
argument_list|,
name|qf1
argument_list|,
name|qf2
argument_list|,
name|fam1
argument_list|,
name|ts1
argument_list|,
name|ts2
argument_list|,
name|ts3
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|actual
init|=
name|performScan
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|)
decl_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|PrivateCellUtil
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Wait for the bucket cache threads to move the data to offheap
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
comment|// do the scan again and verify. This time it should be from the bucket cache in offheap mode
comment|// but one of the cell will be copied due to the asSubByteBuff call
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|row1
argument_list|)
operator|.
name|addFamily
argument_list|(
name|fam1
argument_list|)
operator|.
name|readVersions
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|actual
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|boolean
name|hasNext
init|=
name|scanner
operator|.
name|next
argument_list|(
name|actual
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|hasNext
argument_list|)
expr_stmt|;
comment|// Verify result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|5
condition|)
block|{
comment|// the last cell fetched will be of type shareable but not offheap because
comment|// the MBB is copied to form a single cell
name|assertTrue
argument_list|(
name|actual
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|ByteBufferKeyValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{     }
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|this
operator|.
name|region
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|insertData
parameter_list|(
name|byte
index|[]
name|row1
parameter_list|,
name|byte
index|[]
name|qf1
parameter_list|,
name|byte
index|[]
name|qf2
parameter_list|,
name|byte
index|[]
name|fam1
parameter_list|,
name|long
name|ts1
parameter_list|,
name|long
name|ts2
parameter_list|,
name|long
name|ts3
parameter_list|,
name|boolean
name|withVal
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Putting data in Region
name|Put
name|put
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv13
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv12
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv11
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv23
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv22
init|=
literal|null
decl_stmt|;
name|KeyValue
name|kv21
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|withVal
condition|)
block|{
name|kv13
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|kv12
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|kv11
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|kv23
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts3
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|kv22
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|kv21
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kv13
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|kv12
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|kv11
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf1
argument_list|,
name|ts1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|kv23
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts3
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|kv22
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|kv21
operator|=
operator|new
name|KeyValue
argument_list|(
name|row1
argument_list|,
name|fam1
argument_list|,
name|qf2
argument_list|,
name|ts1
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
name|put
operator|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv13
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv12
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv11
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv23
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv22
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv21
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HStore
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|fam1
argument_list|)
decl_stmt|;
while|while
condition|(
name|store
operator|.
name|getStorefilesCount
argument_list|()
operator|<=
literal|0
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{       }
block|}
comment|// Expected
name|List
argument_list|<
name|Cell
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|kv13
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|kv12
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|kv23
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
name|kv22
argument_list|)
expr_stmt|;
return|return
name|expected
return|;
block|}
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|performScan
parameter_list|(
name|byte
index|[]
name|row1
parameter_list|,
name|byte
index|[]
name|fam1
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|row1
argument_list|)
operator|.
name|addFamily
argument_list|(
name|fam1
argument_list|)
operator|.
name|readVersions
argument_list|(
name|MAX_VERSIONS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|boolean
name|hasNext
init|=
name|scanner
operator|.
name|next
argument_list|(
name|actual
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|hasNext
argument_list|)
expr_stmt|;
return|return
name|actual
return|;
block|}
specifier|private
specifier|static
name|HRegion
name|initHRegion
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HBaseTestingUtility
name|test_util
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|initHRegion
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|callingMethod
argument_list|,
name|conf
argument_list|,
name|test_util
argument_list|,
literal|false
argument_list|,
name|families
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|HRegion
name|initHRegion
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|stopKey
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HBaseTestingUtility
name|testUtil
parameter_list|,
name|boolean
name|isReadOnly
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionInfo
name|regionInfo
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|startKey
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|stopKey
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setReadOnly
argument_list|(
name|isReadOnly
argument_list|)
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|family
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|regionInfo
argument_list|,
name|testUtil
operator|.
name|getDataTestDir
argument_list|(
name|callingMethod
argument_list|)
argument_list|,
name|conf
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|BlockCacheFactory
operator|.
name|createBlockCache
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

