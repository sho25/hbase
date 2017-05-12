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
name|ThreadLocalRandom
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
name|HTableDescriptor
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
name|BeforeClass
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
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setBlocksize
argument_list|(
literal|1024
argument_list|)
argument_list|)
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
name|RegionScanner
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
operator|(
name|RegionScannerImpl
operator|)
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
block|}
block|}
comment|// make sure all scanners are closed.
for|for
control|(
name|StoreFile
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
block|}
end_class

end_unit
