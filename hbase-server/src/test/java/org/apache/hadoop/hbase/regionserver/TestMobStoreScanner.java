begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|fs
operator|.
name|FileStatus
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
name|CellUtil
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
name|client
operator|.
name|*
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|FSUtils
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
name|HFileArchiveUtil
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMobStoreScanner
block|{
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
specifier|private
specifier|final
specifier|static
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
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
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
specifier|private
specifier|final
specifier|static
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
specifier|protected
specifier|final
name|byte
index|[]
name|qf3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|HColumnDescriptor
name|hcd
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|private
specifier|static
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|long
name|defaultThreshold
init|=
literal|10
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.regionserver.info.port.auto"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.client.keyvalue.maxsize"
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setUp
parameter_list|(
name|long
name|threshold
parameter_list|,
name|String
name|TN
parameter_list|)
throws|throws
name|Exception
block|{
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMobThreshold
argument_list|(
name|threshold
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generate the mob value.    *    * @param size the size of the value    * @return the mob value generated    */
specifier|private
specifier|static
name|byte
index|[]
name|generateMobValue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|byte
index|[]
name|mobVal
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|mobVal
argument_list|)
expr_stmt|;
return|return
name|mobVal
return|;
block|}
comment|/**    * Set the scan attribute    *    * @param reversed if true, scan will be backward order    * @param mobScanRaw if true, scan will get the mob reference    * @return this    */
specifier|public
name|void
name|setScan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|boolean
name|reversed
parameter_list|,
name|boolean
name|mobScanRaw
parameter_list|)
block|{
name|scan
operator|.
name|setReversed
argument_list|(
name|reversed
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
literal|4
argument_list|)
expr_stmt|;
if|if
condition|(
name|mobScanRaw
condition|)
block|{
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobStoreScanner
parameter_list|()
throws|throws
name|Exception
block|{
name|testGetFromFiles
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testGetFromMemStore
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testGetReferences
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testMobThreshold
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testGetFromArchive
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReversedMobStoreScanner
parameter_list|()
throws|throws
name|Exception
block|{
name|testGetFromFiles
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testGetFromMemStore
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testGetReferences
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testMobThreshold
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testGetFromArchive
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testGetMassive
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testGetMassive"
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
comment|// Put some data 5 10, 15, 20  mb ok  (this would be right below protobuf default max size of 64MB.
comment|// 25, 30, 40 fail.  these is above protobuf max size of 64MB
name|byte
index|[]
name|bigValue
init|=
operator|new
name|byte
index|[
literal|25
operator|*
literal|1024
operator|*
literal|1024
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|bigValue
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|bigValue
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|bigValue
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
comment|// should not have blown up.
block|}
specifier|public
name|void
name|testGetFromFiles
parameter_list|(
name|boolean
name|reversed
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testGetFromFiles"
operator|+
name|reversed
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|,
name|reversed
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetFromMemStore
parameter_list|(
name|boolean
name|reversed
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testGetFromMemStore"
operator|+
name|reversed
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
empty_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|,
name|reversed
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetReferences
parameter_list|(
name|boolean
name|reversed
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testGetReferences"
operator|+
name|reversed
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
empty_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|,
name|reversed
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|assertIsMobReference
argument_list|(
name|cell
argument_list|,
name|row1
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testMobThreshold
parameter_list|(
name|boolean
name|reversed
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testMobThreshold"
operator|+
name|reversed
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|byte
index|[]
name|valueLess
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|-
literal|1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valueEqual
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valueGreater
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|valueLess
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|valueEqual
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|valueGreater
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|,
name|reversed
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Cell
name|cellLess
init|=
literal|null
decl_stmt|;
name|Cell
name|cellEqual
init|=
literal|null
decl_stmt|;
name|Cell
name|cellGreater
init|=
literal|null
decl_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|String
name|qf
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|qf
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|qf1
argument_list|)
argument_list|)
condition|)
block|{
name|cellLess
operator|=
name|cell
expr_stmt|;
block|}
if|if
condition|(
name|qf
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|qf2
argument_list|)
argument_list|)
condition|)
block|{
name|cellEqual
operator|=
name|cell
expr_stmt|;
block|}
if|if
condition|(
name|qf
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|qf3
argument_list|)
argument_list|)
condition|)
block|{
name|cellGreater
operator|=
name|cell
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|assertNotMobReference
argument_list|(
name|cellLess
argument_list|,
name|row1
argument_list|,
name|family
argument_list|,
name|valueLess
argument_list|)
expr_stmt|;
name|assertNotMobReference
argument_list|(
name|cellEqual
argument_list|,
name|row1
argument_list|,
name|family
argument_list|,
name|valueEqual
argument_list|)
expr_stmt|;
name|assertIsMobReference
argument_list|(
name|cellGreater
argument_list|,
name|row1
argument_list|,
name|family
argument_list|,
name|valueGreater
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testGetFromArchive
parameter_list|(
name|boolean
name|reversed
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|TN
init|=
literal|"testGetFromArchive"
operator|+
name|reversed
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
decl_stmt|;
name|setUp
argument_list|(
name|defaultThreshold
argument_list|,
name|TN
argument_list|)
expr_stmt|;
name|long
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
operator|(
name|int
operator|)
name|defaultThreshold
operator|+
literal|1
argument_list|)
decl_stmt|;
empty_stmt|;
comment|// Put some data
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf1
argument_list|,
name|ts3
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf2
argument_list|,
name|ts2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put1
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qf3
argument_list|,
name|ts1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn
argument_list|)
expr_stmt|;
comment|// Get the files in the mob path
name|Path
name|mobFamilyPath
decl_stmt|;
name|mobFamilyPath
operator|=
operator|new
name|Path
argument_list|(
name|MobUtils
operator|.
name|getMobRegionPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
comment|// Get the archive path
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|regionInfo
init|=
name|MobUtils
operator|.
name|getMobRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|storeArchiveDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|regionInfo
argument_list|,
name|tableDir
argument_list|,
name|family
argument_list|)
decl_stmt|;
comment|// Move the files from mob path to archive path
name|fs
operator|.
name|mkdirs
argument_list|(
name|storeArchiveDir
argument_list|)
expr_stmt|;
name|int
name|fileCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|fileCount
operator|++
expr_stmt|;
name|Path
name|filePath
init|=
name|file
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|Path
name|src
init|=
operator|new
name|Path
argument_list|(
name|mobFamilyPath
argument_list|,
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|dst
init|=
operator|new
name|Path
argument_list|(
name|storeArchiveDir
argument_list|,
name|filePath
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|rename
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
comment|// Verify the moving success
name|FileStatus
index|[]
name|files1
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|mobFamilyPath
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|files1
operator|.
name|length
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|files2
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|storeArchiveDir
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|fileCount
argument_list|,
name|files2
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Scan from archive
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|,
name|reversed
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|results
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|res
operator|.
name|listCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// Verify the value
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert the value is not store in mob.    */
specifier|private
specifier|static
name|void
name|assertNotMobReference
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert the value is store in mob.    */
specifier|private
specifier|static
name|void
name|assertIsMobReference
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|String
name|TN
parameter_list|)
throws|throws
name|IOException
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|value
argument_list|)
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|referenceValue
init|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|referenceValue
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|referenceValue
operator|.
name|length
operator|-
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|int
name|valLen
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|referenceValue
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|value
operator|.
name|length
argument_list|,
name|valLen
argument_list|)
expr_stmt|;
name|Path
name|mobFamilyPath
decl_stmt|;
name|mobFamilyPath
operator|=
operator|new
name|Path
argument_list|(
name|MobUtils
operator|.
name|getMobRegionPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|TN
argument_list|)
argument_list|)
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|targetPath
init|=
operator|new
name|Path
argument_list|(
name|mobFamilyPath
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|targetPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

