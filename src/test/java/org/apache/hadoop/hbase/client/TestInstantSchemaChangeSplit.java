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
name|client
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
name|List
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
name|LargeTests
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
name|HRegionServer
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
name|zookeeper
operator|.
name|MasterSchemaChangeTracker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestInstantSchemaChangeSplit
extends|extends
name|InstantSchemaChangeTestBase
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * The objective of the following test is to validate that schema exclusions happen properly.    * When a RS server dies or crashes(?) mid-flight during a schema refresh, we would exclude    * all online regions in that RS, as well as the RS itself from schema change process.    *    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeExclusions
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeExclusions() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeExclusions"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|99
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|HRegionServer
name|hrs
init|=
name|findRSWithOnlineRegionFor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|//miniHBaseCluster.getRegionServer(0).abort("killed for test");
name|admin
operator|.
name|modifyColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|hrs
operator|.
name|abort
argument_list|(
literal|"Aborting for tests"
argument_list|)
expr_stmt|;
name|hrs
operator|.
name|getSchemaChangeTracker
argument_list|()
operator|.
name|setSleepTimeMillis
argument_list|(
literal|20000
argument_list|)
expr_stmt|;
comment|//admin.modifyColumn(Bytes.toBytes(tableName), hcd);
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for Schema Change process to complete"
argument_list|)
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|,
literal|15000
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Sleep for some time so that our region is reassigned to some other RS
comment|// by master.
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
name|miniHBaseCluster
operator|.
name|getRegions
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testInstantSchemaChangeExclusions"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|!
name|onlineRegions
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegion
name|onlineRegion
range|:
name|onlineRegions
control|)
block|{
name|HTableDescriptor
name|htd
init|=
name|onlineRegion
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|HColumnDescriptor
name|tableHcd
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tableHcd
operator|.
name|isBlockCacheEnabled
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableHcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
literal|99
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeExclusions() "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * This test validates that when a schema change request fails on the    * RS side, we appropriately register the failure in the Master Schema change    * tracker's node as well as capture the error cause.    *    * Currently an alter request fails if RS fails with an IO exception say due to    * missing or incorrect codec. With instant schema change the same failure happens    * and we register the failure with associated cause and also update the    * monitor status appropriately.    *    * The region(s) will be orphaned in both the cases.    *    */
annotation|@
name|Test
specifier|public
name|void
name|testInstantSchemaChangeWhileRSOpenRegionFailure
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testInstantSchemaChangeWhileRSOpenRegionFailure() "
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testInstantSchemaChangeWhileRSOpenRegionFailure"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// create now 100 regions
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|ht
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// wait for all the regions to be assigned
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|onlineRegions
init|=
name|miniHBaseCluster
operator|.
name|getRegions
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testInstantSchemaChangeWhileRSOpenRegionFailure"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|onlineRegions
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// we will not have any online regions
name|LOG
operator|.
name|info
argument_list|(
literal|"Size of online regions = "
operator|+
name|onlineRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|99
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|SNAPPY
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|msct
operator|.
name|doesSchemaChangeNodeExists
argument_list|(
name|tableName
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
comment|// get the current alter status and validate that its failure with appropriate error msg.
name|MasterSchemaChangeTracker
operator|.
name|MasterAlterStatus
name|mas
init|=
name|msct
operator|.
name|getMasterAlterStatus
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|mas
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mas
operator|.
name|getCurrentAlterStatus
argument_list|()
argument_list|,
name|MasterSchemaChangeTracker
operator|.
name|MasterAlterStatus
operator|.
name|AlterState
operator|.
name|FAILURE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|mas
operator|.
name|getErrorCause
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testInstantSchemaChangeWhileRSOpenRegionFailure() "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentInstantSchemaChangeAndSplit
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|KeeperException
block|{
specifier|final
name|String
name|tableName
init|=
literal|"testConcurrentInstantSchemaChangeAndSplit"
decl_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start testConcurrentInstantSchemaChangeAndSplit()"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|newFamily
init|=
literal|"newFamily"
decl_stmt|;
name|HTable
name|ht
init|=
name|createTableAndValidate
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|MasterSchemaChangeTracker
name|msct
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getSchemaChangeTracker
argument_list|()
decl_stmt|;
comment|// create now 10 regions
name|TEST_UTIL
operator|.
name|createMultiRegions
argument_list|(
name|conf
argument_list|,
name|ht
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|int
name|rowCount
init|=
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|ht
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
comment|//assertRowCount(t, rowCount);
name|Runnable
name|splitter
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// run the splits now.
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting table now "
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Runnable
name|schemaChanger
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|newFamily
argument_list|)
decl_stmt|;
try|try
block|{
name|admin
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|ioe
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|schemaChanger
operator|.
name|run
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|splitter
operator|.
name|run
argument_list|()
expr_stmt|;
name|waitForSchemaChangeProcess
argument_list|(
name|tableName
argument_list|,
literal|40000
argument_list|)
expr_stmt|;
name|Put
name|put1
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put1
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******** Put into new column family "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|put
argument_list|(
name|put1
argument_list|)
expr_stmt|;
name|ht
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******** Get from new column family "
argument_list|)
expr_stmt|;
name|Get
name|get1
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get1
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|ht
operator|.
name|get
argument_list|(
name|get1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tvalue
init|=
name|r
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamily
argument_list|)
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|" Value put = "
operator|+
name|value
operator|+
literal|" value from table = "
operator|+
name|tvalue
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
name|tvalue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|result
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"End testConcurrentInstantSchemaChangeAndSplit() "
argument_list|)
expr_stmt|;
name|ht
operator|.
name|close
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

