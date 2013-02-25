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
name|assertNotNull
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|hbase
operator|.
name|HBaseTestCase
operator|.
name|FlushCache
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
name|HBaseTestCase
operator|.
name|HTableIncommon
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
name|HBaseTestCase
operator|.
name|Incommon
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
name|exceptions
operator|.
name|MasterNotRunningException
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
name|exceptions
operator|.
name|ZooKeeperConnectionException
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
name|After
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
name|Before
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

begin_comment
comment|/**  * Port of old TestScanMultipleVersions, TestTimestamp and TestGetRowVersions  * from old testing framework to {@link HBaseTestingUtility}.  */
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
name|TestMultiVersions
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
name|TestMultiVersions
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
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_SLAVES
init|=
literal|3
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
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NUM_SLAVES
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
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|ZooKeeperConnectionException
block|{
name|this
operator|.
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**   * Tests user specifiable time stamps putting, getting and scanning.  Also    * tests same in presence of deletes.  Test cores are written so can be    * run against an HRegion and against an HTable: i.e. both local and remote.    *     *<p>Port of old TestTimestamp test to here so can better utilize the spun    * up cluster running more than a single test per spin up.  Keep old tests'    * crazyness.    */
annotation|@
name|Test
specifier|public
name|void
name|testTimestamps
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testTimestamps"
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TimestampTestBase
operator|.
name|FAMILY_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: Remove these deprecated classes or pull them in here if this is
comment|// only test using them.
name|Incommon
name|incommon
init|=
operator|new
name|HTableIncommon
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|TimestampTestBase
operator|.
name|doTestDelete
argument_list|(
name|incommon
argument_list|,
operator|new
name|FlushCache
argument_list|()
block|{
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Perhaps drop and readd the table between tests so the former does
comment|// not pollute this latter?  Or put into separate tests.
name|TimestampTestBase
operator|.
name|doTestTimestampScanning
argument_list|(
name|incommon
argument_list|,
operator|new
name|FlushCache
argument_list|()
block|{
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verifies versions across a cluster restart.    * Port of old TestGetRowVersions test to here so can better utilize the spun    * up cluster running more than a single test per spin up.  Keep old tests'    * crazyness.    */
annotation|@
name|Test
specifier|public
name|void
name|testGetRowVersions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testGetRowVersions"
decl_stmt|;
specifier|final
name|byte
index|[]
name|contents
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
decl_stmt|;
specifier|final
name|long
name|timestamp1
init|=
literal|100L
decl_stmt|;
specifier|final
name|long
name|timestamp2
init|=
literal|200L
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|contents
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
name|timestamp1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|contents
argument_list|,
name|contents
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Shut down and restart the HBase cluster
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"HBase cluster shut down -- restarting"
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
name|NUM_SLAVES
argument_list|)
expr_stmt|;
comment|// Make a new connection.  Use new Configuration instance because old one
comment|// is tied to an HConnection that has since gone stale.
name|table
operator|=
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Overwrite previous value
name|put
operator|=
operator|new
name|Put
argument_list|(
name|row
argument_list|,
name|timestamp2
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|contents
argument_list|,
name|contents
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Now verify that getRow(row, column, latest) works
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
comment|// Should get one version by default
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|r
operator|.
name|getValue
argument_list|(
name|contents
argument_list|,
name|contents
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|value
operator|.
name|length
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now check getRow with multiple versions
name|get
operator|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|get
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|value
operator|=
name|r
operator|.
name|getValue
argument_list|(
name|contents
argument_list|,
name|contents
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|value
operator|.
name|length
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|map
init|=
name|r
operator|.
name|getMap
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|familyMap
init|=
name|map
operator|.
name|get
argument_list|(
name|contents
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|versionMap
init|=
name|familyMap
operator|.
name|get
argument_list|(
name|contents
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|versionMap
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value1
argument_list|,
name|versionMap
operator|.
name|get
argument_list|(
name|timestamp1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|value2
argument_list|,
name|versionMap
operator|.
name|get
argument_list|(
name|timestamp2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Port of old TestScanMultipleVersions test here so can better utilize the    * spun up cluster running more than just a single test.  Keep old tests    * crazyness.    *     *<p>Tests five cases of scans and timestamps.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testScanMultipleVersions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testScanMultipleVersions"
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|rows
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0200"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0800"
argument_list|)
block|}
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|splitRows
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_0500"
argument_list|)
block|}
decl_stmt|;
specifier|final
name|long
index|[]
name|timestamp
init|=
operator|new
name|long
index|[]
block|{
literal|100L
block|,
literal|1000L
block|}
decl_stmt|;
name|this
operator|.
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|splitRows
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// Assert we got the region layout wanted.
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|locations
init|=
name|table
operator|.
name|getRegionLocations
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|locations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|e
range|:
name|locations
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
operator|==
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|splitRows
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|index
operator|==
literal|1
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|splitRows
index|[
literal|0
index|]
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
comment|// Insert data
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|timestamp
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|timestamp
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|timestamp
index|[
name|j
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|timestamp
index|[
name|j
index|]
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
block|}
block|}
comment|// There are 5 cases we have to test. Each is described below.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|timestamp
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|get
operator|.
name|setTimeStamp
argument_list|(
name|timestamp
index|[
name|j
index|]
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|int
name|cellCount
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|KeyValue
name|kv
range|:
name|result
operator|.
name|list
argument_list|()
control|)
block|{
name|cellCount
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|cellCount
operator|==
literal|1
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Case 1: scan with LATEST_TIMESTAMP. Should get two rows
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|ResultScanner
name|s
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|Result
name|rr
init|=
literal|null
init|;
operator|(
name|rr
operator|=
name|s
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|rr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be 2"
argument_list|,
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Case 2: Scan with a timestamp greater than most recent timestamp
comment|// (in this case> 1000 and< LATEST_TIMESTAMP. Should get 2 rows.
name|count
operator|=
literal|0
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setTimeRange
argument_list|(
literal|1000L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|s
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|s
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be 2"
argument_list|,
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Case 3: scan with timestamp equal to most recent timestamp
comment|// (in this case == 1000. Should get 2 rows.
name|count
operator|=
literal|0
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setTimeStamp
argument_list|(
literal|1000L
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|s
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|s
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be 2"
argument_list|,
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Case 4: scan with timestamp greater than first timestamp but less than
comment|// second timestamp (100< timestamp< 1000). Should get 2 rows.
name|count
operator|=
literal|0
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setTimeRange
argument_list|(
literal|100L
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|s
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|s
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be 2"
argument_list|,
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Case 5: scan with timestamp equal to first timestamp (100)
comment|// Should get 2 rows.
name|count
operator|=
literal|0
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setTimeStamp
argument_list|(
literal|100L
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|s
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
name|s
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Number of rows should be 2"
argument_list|,
literal|2
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

