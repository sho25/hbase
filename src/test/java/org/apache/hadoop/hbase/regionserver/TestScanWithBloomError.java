begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Comparator
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
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|KeyValueTestUtil
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
name|io
operator|.
name|hfile
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
name|HFile
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
name|HFilePrettyPrinter
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
name|Before
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Test a multi-column scanner when there is a Bloom filter false-positive.  * This is needed for the multi-column Bloom filter optimization.  */
end_comment

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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestScanWithBloomError
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestScanWithBloomError
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"ScanWithBloomError"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"myCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW
init|=
literal|"theRow"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QUALIFIER_PREFIX
init|=
literal|"qual"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|NavigableSet
argument_list|<
name|Integer
argument_list|>
name|allColIds
init|=
operator|new
name|TreeSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
name|StoreFile
operator|.
name|BloomType
name|bloomType
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
specifier|final
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|configurations
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFile
operator|.
name|BloomType
name|bloomType
range|:
name|StoreFile
operator|.
name|BloomType
operator|.
name|values
argument_list|()
control|)
block|{
name|configurations
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|bloomType
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|configurations
return|;
block|}
specifier|public
name|TestScanWithBloomError
parameter_list|(
name|StoreFile
operator|.
name|BloomType
name|bloomType
parameter_list|)
block|{
name|this
operator|.
name|bloomType
operator|=
name|bloomType
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testThreeStoreFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|region
operator|=
name|TEST_UTIL
operator|.
name|createTestRegion
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|bloomType
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|TestMultiColumnScanner
operator|.
name|MAX_VERSIONS
argument_list|)
argument_list|)
expr_stmt|;
name|createStoreFile
argument_list|(
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|6
block|}
argument_list|)
expr_stmt|;
name|createStoreFile
argument_list|(
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|7
block|}
argument_list|)
expr_stmt|;
name|createStoreFile
argument_list|(
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|9
block|}
argument_list|)
expr_stmt|;
name|scanColSet
argument_list|(
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|4
block|,
literal|6
block|,
literal|7
block|}
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|6
block|,
literal|7
block|}
argument_list|)
expr_stmt|;
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|scanColSet
parameter_list|(
name|int
index|[]
name|colSet
parameter_list|,
name|int
index|[]
name|expectedResultCols
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanning column set: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|colSet
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|ROW_BYTES
argument_list|,
name|ROW_BYTES
argument_list|)
decl_stmt|;
name|addColumnSetToScan
argument_list|(
name|scan
argument_list|,
name|colSet
argument_list|)
expr_stmt|;
name|RegionScannerImpl
name|scanner
init|=
operator|(
name|RegionScannerImpl
operator|)
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|KeyValueHeap
name|storeHeap
init|=
name|scanner
operator|.
name|getStoreHeapForTesting
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|storeHeap
operator|.
name|getHeap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|StoreScanner
name|storeScanner
init|=
operator|(
name|StoreScanner
operator|)
name|storeHeap
operator|.
name|getCurrentForTesting
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"rawtypes"
block|}
argument_list|)
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
init|=
call|(
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
call|)
argument_list|(
name|List
argument_list|)
name|storeScanner
operator|.
name|getAllScannersForTesting
argument_list|()
decl_stmt|;
comment|// Sort scanners by their HFile's modification time.
name|Collections
operator|.
name|sort
argument_list|(
name|scanners
argument_list|,
operator|new
name|Comparator
argument_list|<
name|StoreFileScanner
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|StoreFileScanner
name|s1
parameter_list|,
name|StoreFileScanner
name|s2
parameter_list|)
block|{
name|Path
name|p1
init|=
name|s1
operator|.
name|getReaderForTesting
argument_list|()
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|Path
name|p2
init|=
name|s2
operator|.
name|getReaderForTesting
argument_list|()
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|long
name|t1
decl_stmt|,
name|t2
decl_stmt|;
try|try
block|{
name|t1
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p1
argument_list|)
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
name|t2
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p2
argument_list|)
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
return|return
name|t1
operator|<
name|t2
condition|?
operator|-
literal|1
else|:
name|t1
operator|==
name|t2
condition|?
literal|1
else|:
literal|0
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|StoreFile
operator|.
name|Reader
name|lastStoreFileReader
init|=
literal|null
decl_stmt|;
for|for
control|(
name|StoreFileScanner
name|sfScanner
range|:
name|scanners
control|)
name|lastStoreFileReader
operator|=
name|sfScanner
operator|.
name|getReaderForTesting
argument_list|()
expr_stmt|;
operator|new
name|HFilePrettyPrinter
argument_list|()
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-m"
block|,
literal|"-p"
block|,
literal|"-f"
block|,
name|lastStoreFileReader
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
comment|// Disable Bloom filter for the last store file. The disabled Bloom filter
comment|// will always return "true".
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabling Bloom filter for: "
operator|+
name|lastStoreFileReader
operator|.
name|getHFileReader
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|lastStoreFileReader
operator|.
name|disableBloomFilterForTesting
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|allResults
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
block|{
comment|// Limit the scope of results.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
operator|||
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|allResults
operator|.
name|addAll
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|actualIds
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|allResults
control|)
block|{
name|String
name|qual
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|qual
operator|.
name|startsWith
argument_list|(
name|QUALIFIER_PREFIX
argument_list|)
argument_list|)
expr_stmt|;
name|actualIds
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|qual
operator|.
name|substring
argument_list|(
name|QUALIFIER_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|expectedIds
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|expectedId
range|:
name|expectedResultCols
control|)
name|expectedIds
operator|.
name|add
argument_list|(
name|expectedId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Column ids returned: "
operator|+
name|actualIds
operator|+
literal|", expected: "
operator|+
name|expectedIds
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedIds
operator|.
name|toString
argument_list|()
argument_list|,
name|actualIds
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addColumnSetToScan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|int
index|[]
name|colIds
parameter_list|)
block|{
for|for
control|(
name|int
name|colId
range|:
name|colIds
control|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|qualFromId
argument_list|(
name|colId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|qualFromId
parameter_list|(
name|int
name|colId
parameter_list|)
block|{
return|return
name|QUALIFIER_PREFIX
operator|+
name|colId
return|;
block|}
specifier|private
name|void
name|createStoreFile
parameter_list|(
name|int
index|[]
name|colIds
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW_BYTES
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|colId
range|:
name|colIds
control|)
block|{
name|long
name|ts
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|String
name|qual
init|=
name|qualFromId
argument_list|(
name|colId
argument_list|)
decl_stmt|;
name|allColIds
operator|.
name|add
argument_list|(
name|colId
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|ROW
argument_list|,
name|FAMILY
argument_list|,
name|qual
argument_list|,
name|ts
argument_list|,
name|TestMultiColumnScanner
operator|.
name|createValue
argument_list|(
name|ROW
argument_list|,
name|qual
argument_list|,
name|ts
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
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

