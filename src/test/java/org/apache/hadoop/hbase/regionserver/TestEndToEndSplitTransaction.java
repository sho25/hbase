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
name|client
operator|.
name|Get
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
name|HConnection
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
name|HConnectionManager
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
name|HTable
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
name|PairOfSameType
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
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_class
specifier|public
class|class
name|TestEndToEndSplitTransaction
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
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
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|5
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
name|afterAllTests
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterOpsWhileSplitting
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestSplit"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
name|HRegionServer
name|server
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|byte
index|[]
name|firstRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|splitRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"lll"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lastRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
decl_stmt|;
name|HConnection
name|con
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// this will also cache the region
name|byte
index|[]
name|regionName
init|=
name|con
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|splitRow
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|server
operator|.
name|getRegion
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|SplitTransaction
name|split
init|=
operator|new
name|SplitTransaction
argument_list|(
name|region
argument_list|,
name|splitRow
argument_list|)
decl_stmt|;
name|split
operator|.
name|prepare
argument_list|()
expr_stmt|;
comment|// 1. phase I
name|PairOfSameType
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|split
operator|.
name|createDaughters
argument_list|(
name|server
argument_list|,
name|server
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|firstRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|lastRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// passing null as services prevents final step
comment|// 2, most of phase II
name|split
operator|.
name|openDaughters
argument_list|(
name|server
argument_list|,
literal|null
argument_list|,
name|regions
operator|.
name|getFirst
argument_list|()
argument_list|,
name|regions
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|firstRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|lastRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// 3. finish phase II
comment|// note that this replicates some code from SplitTransaction
comment|// 2nd daughter first
name|server
operator|.
name|postOpenDeployTasks
argument_list|(
name|regions
operator|.
name|getSecond
argument_list|()
argument_list|,
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// THIS is the crucial point:
comment|// the 2nd daughter was added, so querying before the split key should fail.
name|assertFalse
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|firstRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// past splitkey is ok.
name|assertTrue
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|lastRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// first daughter second
name|server
operator|.
name|postOpenDeployTasks
argument_list|(
name|regions
operator|.
name|getFirst
argument_list|()
argument_list|,
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|firstRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|lastRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
comment|// 4. phase III
name|split
operator|.
name|transitionZKNode
argument_list|(
name|server
argument_list|,
name|regions
operator|.
name|getFirst
argument_list|()
argument_list|,
name|regions
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|firstRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|test
argument_list|(
name|con
argument_list|,
name|tableName
argument_list|,
name|lastRow
argument_list|,
name|server
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * attempt to locate the region and perform a get and scan    * @return True if successful, False otherwise.    */
specifier|private
name|boolean
name|test
parameter_list|(
name|HConnection
name|con
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|HRegionServer
name|server
parameter_list|)
block|{
comment|// not using HTable to avoid timeouts and retries
try|try
block|{
name|byte
index|[]
name|regionName
init|=
name|con
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
comment|// get and scan should now succeed without exception
name|server
operator|.
name|get
argument_list|(
name|regionName
argument_list|,
operator|new
name|Get
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|server
operator|.
name|openScanner
argument_list|(
name|regionName
argument_list|,
operator|new
name|Scan
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|x
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

