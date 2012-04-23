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
name|*
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
name|MiniHBaseCluster
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
name|HBaseAdmin
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
name|ResultScanner
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
name|master
operator|.
name|HMaster
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
name|MediumTests
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

begin_comment
comment|/**  * Tests that need to spin up a cluster testing an {@link HRegion}.  Use  * {@link TestHRegion} if you don't need a cluster, if you can test w/ a  * standalone {@link HRegion}.  */
end_comment

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
name|TestHRegionOnCluster
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
name|Test
argument_list|(
name|timeout
operator|=
literal|180000
argument_list|)
specifier|public
name|void
name|testDataCorrectnessReplayingRecoveredEdits
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|NUM_MASTERS
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|NUM_RS
init|=
literal|3
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_MASTERS
argument_list|,
name|NUM_RS
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testDataCorrectnessReplayingRecoveredEdits"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// Create table
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|hbaseAdmin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|hbaseAdmin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hbaseAdmin
operator|.
name|isTableAvailable
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// Put data: r1->v1
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|putDataAndVerify
argument_list|(
name|table
argument_list|,
literal|"r1"
argument_list|,
name|FAMILY
argument_list|,
literal|"v1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Move region to target server
name|HRegionInfo
name|regionInfo
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
literal|"r1"
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|int
name|originServerNum
init|=
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionServer
name|originServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|originServerNum
argument_list|)
decl_stmt|;
name|int
name|targetServerNum
init|=
operator|(
name|originServerNum
operator|+
literal|1
operator|)
operator|%
name|NUM_RS
decl_stmt|;
name|HRegionServer
name|targetServer
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|targetServerNum
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|originServer
operator|.
name|equals
argument_list|(
name|targetServer
argument_list|)
argument_list|)
expr_stmt|;
name|hbaseAdmin
operator|.
name|move
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|targetServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|==
name|originServerNum
condition|)
do|;
comment|// Put data: r2->v2
name|putDataAndVerify
argument_list|(
name|table
argument_list|,
literal|"r2"
argument_list|,
name|FAMILY
argument_list|,
literal|"v2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// Move region to origin server
name|hbaseAdmin
operator|.
name|move
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|originServer
operator|.
name|getServerName
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|==
name|targetServerNum
condition|)
do|;
comment|// Put data: r3->v3
name|putDataAndVerify
argument_list|(
name|table
argument_list|,
literal|"r3"
argument_list|,
name|FAMILY
argument_list|,
literal|"v3"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Kill target server
name|targetServer
operator|.
name|kill
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
name|targetServerNum
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Wait until finish processing of shutdown
while|while
condition|(
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|areDeadServersInProgress
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
comment|// Kill origin server
name|originServer
operator|.
name|kill
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
name|originServerNum
argument_list|)
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// Put data: r4->v4
name|putDataAndVerify
argument_list|(
name|table
argument_list|,
literal|"r4"
argument_list|,
name|FAMILY
argument_list|,
literal|"v4"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|putDataAndVerify
parameter_list|(
name|HTable
name|table
parameter_list|,
name|String
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|String
name|value
parameter_list|,
name|int
name|verifyNum
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"=========Putting data :"
operator|+
name|row
argument_list|)
expr_stmt|;
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
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|ResultScanner
name|resultScanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Result
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|r
init|=
name|resultScanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
break|break;
name|results
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
name|resultScanner
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|!=
name|verifyNum
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|verifyNum
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
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

