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
operator|.
name|compactions
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
name|compactions
operator|.
name|TestCompactor
operator|.
name|createDummyRequest
import|;
end_import

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
name|compactions
operator|.
name|TestCompactor
operator|.
name|createDummyStoreFile
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
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyBoolean
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyLong
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|FileSystem
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
name|HBaseConfiguration
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
name|HRegionInfo
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
name|regionserver
operator|.
name|ScanInfo
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
name|ScanType
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
name|Store
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
name|StoreFile
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
name|StoreFileScanner
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
name|StoreUtils
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
name|TestCompactor
operator|.
name|Scanner
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
name|TestCompactor
operator|.
name|StoreFileWritersCapture
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
name|throttle
operator|.
name|NoLimitThroughputController
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
name|Parameter
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
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
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
name|TestDateTieredCompactor
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|NAME_OF_THINGS
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|NAME_OF_THINGS
argument_list|,
name|NAME_OF_THINGS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KeyValue
name|KV_A
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
literal|100L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KeyValue
name|KV_B
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
argument_list|,
literal|200L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KeyValue
name|KV_C
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|,
literal|300L
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|KeyValue
name|KV_D
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
argument_list|,
literal|400L
argument_list|)
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: usePrivateReaders={0}"
argument_list|)
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|true
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|false
block|}
argument_list|)
return|;
block|}
annotation|@
name|Parameter
specifier|public
name|boolean
name|usePrivateReaders
decl_stmt|;
specifier|private
name|DateTieredCompactor
name|createCompactor
parameter_list|(
name|StoreFileWritersCapture
name|writers
parameter_list|,
specifier|final
name|KeyValue
index|[]
name|input
parameter_list|,
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storefiles
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.compaction.private.readers"
argument_list|,
name|usePrivateReaders
argument_list|)
expr_stmt|;
specifier|final
name|Scanner
name|scanner
init|=
operator|new
name|Scanner
argument_list|(
name|input
argument_list|)
decl_stmt|;
comment|// Create store mock that is satisfactory for compactor.
name|HColumnDescriptor
name|col
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|NAME_OF_THINGS
argument_list|)
decl_stmt|;
name|ScanInfo
name|si
init|=
operator|new
name|ScanInfo
argument_list|(
name|conf
argument_list|,
name|col
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|0
argument_list|,
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
specifier|final
name|Store
name|store
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getStorefiles
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storefiles
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getScanInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|si
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|areWritesEnabled
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getFileSystem
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|anyLong
argument_list|()
argument_list|,
name|any
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|class
argument_list|)
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
name|writers
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getComparator
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|CellComparator
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|long
name|maxSequenceId
init|=
name|StoreUtils
operator|.
name|getMaxSequenceIdInList
argument_list|(
name|storefiles
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getMaxSequenceId
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|maxSequenceId
argument_list|)
expr_stmt|;
return|return
operator|new
name|DateTieredCompactor
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|InternalScanner
name|createScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|byte
index|[]
name|dropDeletesFromRow
parameter_list|,
name|byte
index|[]
name|dropDeletesToRow
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|scanner
return|;
block|}
annotation|@
name|Override
specifier|protected
name|InternalScanner
name|createScanner
parameter_list|(
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|smallestReadPoint
parameter_list|,
name|long
name|earliestPutTs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|scanner
return|;
block|}
block|}
return|;
block|}
specifier|private
name|void
name|verify
parameter_list|(
name|KeyValue
index|[]
name|input
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|boundaries
parameter_list|,
name|KeyValue
index|[]
index|[]
name|output
parameter_list|,
name|boolean
name|allFiles
parameter_list|)
throws|throws
name|Exception
block|{
name|StoreFileWritersCapture
name|writers
init|=
operator|new
name|StoreFileWritersCapture
argument_list|()
decl_stmt|;
name|StoreFile
name|sf1
init|=
name|createDummyStoreFile
argument_list|(
literal|1L
argument_list|)
decl_stmt|;
name|StoreFile
name|sf2
init|=
name|createDummyStoreFile
argument_list|(
literal|2L
argument_list|)
decl_stmt|;
name|DateTieredCompactor
name|dtc
init|=
name|createCompactor
argument_list|(
name|writers
argument_list|,
name|input
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|sf1
argument_list|,
name|sf2
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|dtc
operator|.
name|compact
argument_list|(
operator|new
name|CompactionRequest
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|sf1
argument_list|)
argument_list|)
argument_list|,
name|boundaries
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|boundaries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|NoLimitThroughputController
operator|.
name|INSTANCE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|writers
operator|.
name|verifyKvs
argument_list|(
name|output
argument_list|,
name|allFiles
argument_list|,
name|boundaries
argument_list|)
expr_stmt|;
if|if
condition|(
name|allFiles
condition|)
block|{
name|assertEquals
argument_list|(
name|output
operator|.
name|length
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|a
parameter_list|(
name|T
modifier|...
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|verify
argument_list|(
name|a
argument_list|(
name|KV_A
argument_list|,
name|KV_B
argument_list|,
name|KV_C
argument_list|,
name|KV_D
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|100L
argument_list|,
literal|200L
argument_list|,
literal|300L
argument_list|,
literal|400L
argument_list|,
literal|500L
argument_list|)
argument_list|,
name|a
argument_list|(
name|a
argument_list|(
name|KV_A
argument_list|)
argument_list|,
name|a
argument_list|(
name|KV_B
argument_list|)
argument_list|,
name|a
argument_list|(
name|KV_C
argument_list|)
argument_list|,
name|a
argument_list|(
name|KV_D
argument_list|)
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|a
argument_list|(
name|KV_A
argument_list|,
name|KV_B
argument_list|,
name|KV_C
argument_list|,
name|KV_D
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
literal|200L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|a
argument_list|(
name|a
argument_list|(
name|KV_A
argument_list|)
argument_list|,
name|a
argument_list|(
name|KV_B
argument_list|,
name|KV_C
argument_list|,
name|KV_D
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|a
argument_list|(
name|KV_A
argument_list|,
name|KV_B
argument_list|,
name|KV_C
argument_list|,
name|KV_D
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
operator|new
name|KeyValue
index|[]
index|[]
block|{
name|a
argument_list|(
name|KV_A
argument_list|,
name|KV_B
argument_list|,
name|KV_C
argument_list|,
name|KV_D
argument_list|)
block|}
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyOutputFile
parameter_list|()
throws|throws
name|Exception
block|{
name|StoreFileWritersCapture
name|writers
init|=
operator|new
name|StoreFileWritersCapture
argument_list|()
decl_stmt|;
name|CompactionRequest
name|request
init|=
name|createDummyRequest
argument_list|()
decl_stmt|;
name|DateTieredCompactor
name|dtc
init|=
name|createCompactor
argument_list|(
name|writers
argument_list|,
operator|new
name|KeyValue
index|[
literal|0
index|]
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|request
operator|.
name|getFiles
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|dtc
operator|.
name|compact
argument_list|(
name|request
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|NoLimitThroughputController
operator|.
name|INSTANCE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|StoreFileWritersCapture
operator|.
name|Writer
argument_list|>
name|dummyWriters
init|=
name|writers
operator|.
name|getWriters
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|dummyWriters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|StoreFileWritersCapture
operator|.
name|Writer
name|dummyWriter
init|=
name|dummyWriters
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|dummyWriter
operator|.
name|kvs
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dummyWriter
operator|.
name|hasMetadata
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

