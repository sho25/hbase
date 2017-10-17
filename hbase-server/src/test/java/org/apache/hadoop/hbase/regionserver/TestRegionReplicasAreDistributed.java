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
name|HashMap
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
name|ServerName
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
name|Table
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|RegionSplitter
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
name|TestRegionReplicasAreDistributed
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
name|TestRegionReplicasAreDistributed
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serverVsOnlineRegions
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serverVsOnlineRegions2
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serverVsOnlineRegions3
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
name|serverVsOnlineRegions4
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|">hbase.master.wait.on.regionservers.mintostart"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|3000
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TestRegionReplicasAreDistributed
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create table then get the single region for our new table.
name|createTableDirectlyFromHTD
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createTableDirectlyFromHTD
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// create a table with 3 replication
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|f
block|}
argument_list|,
name|getSplits
argument_list|(
literal|20
argument_list|)
argument_list|,
operator|new
name|Configuration
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|getSplits
parameter_list|(
name|int
name|numRegions
parameter_list|)
block|{
name|RegionSplitter
operator|.
name|UniformSplit
name|split
init|=
operator|new
name|RegionSplitter
operator|.
name|UniformSplit
argument_list|()
decl_stmt|;
name|split
operator|.
name|setFirstRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|split
operator|.
name|setLastRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|split
operator|.
name|split
argument_list|(
name|numRegions
argument_list|)
return|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionServer
operator|.
name|TEST_SKIP_REPORTING_TRANSITION
operator|=
literal|false
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegionServer
name|getRS
parameter_list|()
block|{
return|return
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|private
name|HRegionServer
name|getSecondaryRS
parameter_list|()
block|{
return|return
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
return|;
block|}
specifier|private
name|HRegionServer
name|getTertiaryRS
parameter_list|()
block|{
return|return
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|2
argument_list|)
return|;
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
name|testRegionReplicasCreatedAreDistributed
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|checkAndAssertRegionDistribution
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// now diesbale and enable the table again. It should be truly distributed
name|HTU
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabled the table "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"enabling the table "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getAdmin
argument_list|()
operator|.
name|enableTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabled the table "
operator|+
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|res
init|=
name|checkAndAssertRegionDistribution
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Region retainment not done "
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HTU
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|checkAndAssertRegionDistribution
parameter_list|(
name|boolean
name|checkfourth
parameter_list|)
throws|throws
name|Exception
block|{
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|onlineRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|getRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|getRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
name|onlineRegions
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|serverVsOnlineRegions
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|serverVsOnlineRegions
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverVsOnlineRegions
operator|.
name|put
argument_list|(
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|onlineRegions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|existingRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|this
operator|.
name|serverVsOnlineRegions
operator|.
name|get
argument_list|(
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count is "
operator|+
name|existingRegions
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|onlineRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|existingRegion
range|:
name|existingRegions
control|)
block|{
if|if
condition|(
operator|!
name|onlineRegions
operator|.
name|contains
argument_list|(
name|existingRegion
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|onlineRegions2
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|getSecondaryRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|getSecondaryRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
name|onlineRegions2
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|serverVsOnlineRegions2
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|serverVsOnlineRegions2
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverVsOnlineRegions2
operator|.
name|put
argument_list|(
name|getSecondaryRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|onlineRegions2
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|existingRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|this
operator|.
name|serverVsOnlineRegions2
operator|.
name|get
argument_list|(
name|getSecondaryRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count is "
operator|+
name|existingRegions
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|onlineRegions2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|existingRegion
range|:
name|existingRegions
control|)
block|{
if|if
condition|(
operator|!
name|onlineRegions2
operator|.
name|contains
argument_list|(
name|existingRegion
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|onlineRegions3
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|getTertiaryRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|getTertiaryRS
argument_list|()
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
name|onlineRegions3
operator|.
name|add
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|serverVsOnlineRegions3
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|serverVsOnlineRegions3
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverVsOnlineRegions3
operator|.
name|put
argument_list|(
name|getTertiaryRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|onlineRegions3
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Collection
argument_list|<
name|RegionInfo
argument_list|>
name|existingRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|this
operator|.
name|serverVsOnlineRegions3
operator|.
name|get
argument_list|(
name|getTertiaryRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count is "
operator|+
name|existingRegions
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|onlineRegions3
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|existingRegion
range|:
name|existingRegions
control|)
block|{
if|if
condition|(
operator|!
name|onlineRegions3
operator|.
name|contains
argument_list|(
name|existingRegion
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
comment|// META and namespace to be added
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

