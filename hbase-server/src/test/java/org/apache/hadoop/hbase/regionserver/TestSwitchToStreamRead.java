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
name|Collection
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|Result
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
name|Scan
operator|.
name|ReadType
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|FilterBase
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
operator|.
name|RegionScannerImpl
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
name|ScannerContext
operator|.
name|LimitScope
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
name|Ignore
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
name|TestSwitchToStreamRead
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
name|TestSwitchToStreamRead
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"stream"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|VALUE_PREFIX
decl_stmt|;
specifier|private
specifier|static
name|HRegion
name|REGION
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|StoreScanner
operator|.
name|STORESCANNER_PREAD_MAX_BYTES
argument_list|,
literal|2048
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|256
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
literal|255
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
name|char
operator|)
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|'A'
argument_list|,
literal|'z'
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|VALUE_PREFIX
operator|=
name|sb
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|REGION
operator|=
name|UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setBlocksize
argument_list|(
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
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
literal|900
condition|;
name|i
operator|++
control|)
block|{
name|REGION
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|REGION
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|900
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|REGION
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|VALUE_PREFIX
operator|+
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|REGION
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|StoreFileReader
argument_list|>
name|getStreamReaders
parameter_list|()
block|{
name|List
argument_list|<
name|HStore
argument_list|>
name|stores
init|=
name|REGION
operator|.
name|getStores
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HStore
name|firstStore
init|=
name|stores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|firstStore
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|storeFiles
init|=
name|firstStore
operator|.
name|getStorefiles
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|storeFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HStoreFile
name|firstSToreFile
init|=
name|storeFiles
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|firstSToreFile
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|firstSToreFile
operator|.
name|streamReaders
argument_list|)
return|;
block|}
comment|/**    * Test Case for HBASE-21551    */
annotation|@
name|Test
specifier|public
name|void
name|testStreamReadersCleanup
parameter_list|()
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|StoreFileReader
argument_list|>
name|streamReaders
init|=
name|getStreamReaders
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|getStreamReaders
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|RegionScannerImpl
name|scanner
init|=
name|REGION
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|setReadType
argument_list|(
name|ReadType
operator|.
name|STREAM
argument_list|)
argument_list|)
init|)
block|{
name|StoreScanner
name|storeScanner
init|=
call|(
name|StoreScanner
call|)
argument_list|(
name|scanner
argument_list|)
operator|.
name|getStoreHeapForTesting
argument_list|()
operator|.
name|getCurrentForTesting
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|sfScanners
init|=
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|kvs
lambda|->
name|kvs
operator|instanceof
name|StoreFileScanner
argument_list|)
operator|.
name|map
argument_list|(
name|kvs
lambda|->
operator|(
name|StoreFileScanner
operator|)
name|kvs
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sfScanners
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|StoreFileScanner
name|sfScanner
init|=
name|sfScanners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|sfScanner
operator|.
name|getReader
argument_list|()
operator|.
name|shared
argument_list|)
expr_stmt|;
comment|// There should be a stream reader
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getStreamReaders
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|getStreamReaders
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// The streamsReader should be clear after region close even if there're some opened stream
comment|// scanner.
name|RegionScannerImpl
name|scanner
init|=
name|REGION
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|setReadType
argument_list|(
name|ReadType
operator|.
name|STREAM
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getStreamReaders
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|REGION
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|streamReaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|RegionScannerImpl
name|scanner
init|=
name|REGION
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|StoreScanner
name|storeScanner
init|=
call|(
name|StoreScanner
call|)
argument_list|(
name|scanner
argument_list|)
operator|.
name|getStoreHeapForTesting
argument_list|()
operator|.
name|getCurrentForTesting
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValueScanner
name|kvs
range|:
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
control|)
block|{
if|if
condition|(
name|kvs
operator|instanceof
name|StoreFileScanner
condition|)
block|{
name|StoreFileScanner
name|sfScanner
init|=
operator|(
name|StoreFileScanner
operator|)
name|kvs
decl_stmt|;
comment|// starting from pread so we use shared reader here.
name|assertTrue
argument_list|(
name|sfScanner
operator|.
name|getReader
argument_list|()
operator|.
name|shared
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
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
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|Result
operator|.
name|create
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|VALUE_PREFIX
operator|+
name|i
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|KeyValueScanner
name|kvs
range|:
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
control|)
block|{
if|if
condition|(
name|kvs
operator|instanceof
name|StoreFileScanner
condition|)
block|{
name|StoreFileScanner
name|sfScanner
init|=
operator|(
name|StoreFileScanner
operator|)
name|kvs
decl_stmt|;
comment|// we should have convert to use stream read now.
name|assertFalse
argument_list|(
name|sfScanner
operator|.
name|getReader
argument_list|()
operator|.
name|shared
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|500
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|i
operator|!=
literal|999
argument_list|,
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|Result
operator|.
name|create
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|VALUE_PREFIX
operator|+
name|i
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
block|}
comment|// make sure all scanners are closed.
for|for
control|(
name|HStoreFile
name|sf
range|:
name|REGION
operator|.
name|getStore
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|getStorefiles
argument_list|()
control|)
block|{
name|assertFalse
argument_list|(
name|sf
operator|.
name|isReferencedInReads
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MatchLastRowKeyFilter
extends|extends
name|FilterBase
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Bytes
operator|.
name|toInt
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|)
operator|!=
literal|999
return|;
block|}
block|}
specifier|private
name|void
name|testFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|RegionScannerImpl
name|scanner
init|=
name|REGION
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
argument_list|)
init|)
block|{
name|StoreScanner
name|storeScanner
init|=
call|(
name|StoreScanner
call|)
argument_list|(
name|scanner
argument_list|)
operator|.
name|getStoreHeapForTesting
argument_list|()
operator|.
name|getCurrentForTesting
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValueScanner
name|kvs
range|:
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
control|)
block|{
if|if
condition|(
name|kvs
operator|instanceof
name|StoreFileScanner
condition|)
block|{
name|StoreFileScanner
name|sfScanner
init|=
operator|(
name|StoreFileScanner
operator|)
name|kvs
decl_stmt|;
comment|// starting from pread so we use shared reader here.
name|assertTrue
argument_list|(
name|sfScanner
operator|.
name|getReader
argument_list|()
operator|.
name|shared
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// should return before finishing the scan as we want to switch from pread to stream
name|assertTrue
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|,
name|ScannerContext
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTimeLimit
argument_list|(
name|LimitScope
operator|.
name|BETWEEN_CELLS
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cells
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|shipped
argument_list|()
expr_stmt|;
for|for
control|(
name|KeyValueScanner
name|kvs
range|:
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
control|)
block|{
if|if
condition|(
name|kvs
operator|instanceof
name|StoreFileScanner
condition|)
block|{
name|StoreFileScanner
name|sfScanner
init|=
operator|(
name|StoreFileScanner
operator|)
name|kvs
decl_stmt|;
comment|// we should have convert to use stream read now.
name|assertFalse
argument_list|(
name|sfScanner
operator|.
name|getReader
argument_list|()
operator|.
name|shared
argument_list|)
expr_stmt|;
block|}
block|}
name|assertFalse
argument_list|(
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|,
name|ScannerContext
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTimeLimit
argument_list|(
name|LimitScope
operator|.
name|BETWEEN_CELLS
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|Result
operator|.
name|create
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|VALUE_PREFIX
operator|+
literal|999
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
comment|// make sure all scanners are closed.
for|for
control|(
name|HStoreFile
name|sf
range|:
name|REGION
operator|.
name|getStore
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|getStorefiles
argument_list|()
control|)
block|{
name|assertFalse
argument_list|(
name|sf
operator|.
name|isReferencedInReads
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// We use a different logic to implement filterRowKey, where we will keep calling kvHeap.next
comment|// until the row key is changed. And there we can only use NoLimitScannerContext so we can not
comment|// make the upper layer return immediately. Simply do not use NoLimitScannerContext will lead to
comment|// an infinite loop. Need to dig more, the code are way too complicated...
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testFilterRowKey
parameter_list|()
throws|throws
name|IOException
block|{
name|testFilter
argument_list|(
operator|new
name|MatchLastRowKeyFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MatchLastRowCellNextColFilter
extends|extends
name|FilterBase
block|{
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
name|Cell
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Bytes
operator|.
name|toInt
argument_list|(
name|c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|c
operator|.
name|getRowOffset
argument_list|()
argument_list|)
operator|==
literal|999
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
else|else
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterCellNextCol
parameter_list|()
throws|throws
name|IOException
block|{
name|testFilter
argument_list|(
operator|new
name|MatchLastRowCellNextColFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MatchLastRowCellNextRowFilter
extends|extends
name|FilterBase
block|{
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
name|Cell
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Bytes
operator|.
name|toInt
argument_list|(
name|c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|c
operator|.
name|getRowOffset
argument_list|()
argument_list|)
operator|==
literal|999
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
else|else
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterCellNextRow
parameter_list|()
throws|throws
name|IOException
block|{
name|testFilter
argument_list|(
operator|new
name|MatchLastRowCellNextRowFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MatchLastRowFilterRowFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|boolean
name|exclude
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|filterRowCells
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
block|{
name|Cell
name|c
init|=
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|exclude
operator|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|c
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|c
operator|.
name|getRowOffset
argument_list|()
argument_list|)
operator|!=
literal|999
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|exclude
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|exclude
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterRow
parameter_list|()
throws|throws
name|IOException
block|{
name|testFilter
argument_list|(
operator|new
name|MatchLastRowFilterRowFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

