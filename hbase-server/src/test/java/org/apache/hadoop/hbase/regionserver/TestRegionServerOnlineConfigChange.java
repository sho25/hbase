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
name|RegionLocator
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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionConfiguration
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
name|ClassRule
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Verify that the Online config Changes on the HRegionServer side are actually  * happening. We should add tests for important configurations which will be  * changed online.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionServerOnlineConfigChange
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
name|TestRegionServerOnlineConfigChange
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRegionServerOnlineConfigChange
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|hbaseTestingUtility
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Table
name|t1
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|rs1
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|r1name
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Region
name|r1
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|table1Str
init|=
literal|"table1"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|columnFamily1Str
init|=
literal|"columnFamily1"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|TableName
name|TABLE1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|table1Str
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|COLUMN_FAMILY1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily1Str
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|hbaseTestingUtility
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|hbaseTestingUtility
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|t1
operator|=
name|hbaseTestingUtility
operator|.
name|createTable
argument_list|(
name|TABLE1
argument_list|,
name|COLUMN_FAMILY1
argument_list|)
expr_stmt|;
try|try
init|(
name|RegionLocator
name|locator
init|=
name|hbaseTestingUtility
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|TABLE1
argument_list|)
init|)
block|{
name|HRegionInfo
name|firstHRI
init|=
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|r1name
operator|=
name|firstHRI
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|rs1
operator|=
name|hbaseTestingUtility
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|hbaseTestingUtility
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerWith
argument_list|(
name|r1name
argument_list|)
argument_list|)
expr_stmt|;
name|r1
operator|=
name|rs1
operator|.
name|getRegion
argument_list|(
name|r1name
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
name|Exception
block|{
name|hbaseTestingUtility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check if the number of compaction threads changes online    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testNumCompactionThreadsOnlineChange
parameter_list|()
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|rs1
operator|.
name|compactSplitThread
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|int
name|newNumSmallThreads
init|=
name|rs1
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionThreadNum
argument_list|()
operator|+
literal|1
decl_stmt|;
name|int
name|newNumLargeThreads
init|=
name|rs1
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionThreadNum
argument_list|()
operator|+
literal|1
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.thread.compaction.small"
argument_list|,
name|newNumSmallThreads
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.thread.compaction.large"
argument_list|,
name|newNumLargeThreads
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newNumSmallThreads
argument_list|,
name|rs1
operator|.
name|compactSplitThread
operator|.
name|getSmallCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newNumLargeThreads
argument_list|,
name|rs1
operator|.
name|compactSplitThread
operator|.
name|getLargeCompactionThreadNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the configurations in the CompactionConfiguration class change    * properly.    *    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testCompactionConfigurationOnlineChange
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|strPrefix
init|=
literal|"hbase.hstore.compaction."
decl_stmt|;
name|Store
name|s
init|=
name|r1
operator|.
name|getStore
argument_list|(
name|COLUMN_FAMILY1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|s
operator|instanceof
name|HStore
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't test the compaction configuration of HStore class. "
operator|+
literal|"Got a different implementation other than HStore"
argument_list|)
expr_stmt|;
return|return;
block|}
name|HStore
name|hstore
init|=
operator|(
name|HStore
operator|)
name|s
decl_stmt|;
comment|// Set the new compaction ratio to a different value.
name|double
name|newCompactionRatio
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getCompactionRatio
argument_list|()
operator|+
literal|0.1
decl_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|strPrefix
operator|+
literal|"ratio"
argument_list|,
operator|(
name|float
operator|)
name|newCompactionRatio
argument_list|)
expr_stmt|;
comment|// Notify all the observers, which includes the Store object.
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Check if the compaction ratio got updated in the Compaction Configuration
name|assertEquals
argument_list|(
name|newCompactionRatio
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getCompactionRatio
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
comment|// Check if the off peak compaction ratio gets updated.
name|double
name|newOffPeakCompactionRatio
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getCompactionRatioOffPeak
argument_list|()
operator|+
literal|0.1
decl_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|strPrefix
operator|+
literal|"ratio.offpeak"
argument_list|,
operator|(
name|float
operator|)
name|newOffPeakCompactionRatio
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newOffPeakCompactionRatio
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getCompactionRatioOffPeak
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
comment|// Check if the throttle point gets updated.
name|long
name|newThrottlePoint
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getThrottlePoint
argument_list|()
operator|+
literal|10
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.regionserver.thread.compaction.throttle"
argument_list|,
name|newThrottlePoint
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newThrottlePoint
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getThrottlePoint
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if the minFilesToCompact gets updated.
name|int
name|newMinFilesToCompact
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMinFilesToCompact
argument_list|()
operator|+
literal|1
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|strPrefix
operator|+
literal|"min"
argument_list|,
name|newMinFilesToCompact
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMinFilesToCompact
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMinFilesToCompact
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if the maxFilesToCompact gets updated.
name|int
name|newMaxFilesToCompact
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMaxFilesToCompact
argument_list|()
operator|+
literal|1
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|strPrefix
operator|+
literal|"max"
argument_list|,
name|newMaxFilesToCompact
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMaxFilesToCompact
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMaxFilesToCompact
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check OffPeak hours is updated in an online fashion.
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_OFFPEAK_START_HOUR
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_OFFPEAK_END_HOUR
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hstore
operator|.
name|getOffPeakHours
argument_list|()
operator|.
name|isOffPeakHour
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check if the minCompactSize gets updated.
name|long
name|newMinCompactSize
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMinCompactSize
argument_list|()
operator|+
literal|1
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|strPrefix
operator|+
literal|"min.size"
argument_list|,
name|newMinCompactSize
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMinCompactSize
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMinCompactSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if the maxCompactSize gets updated.
name|long
name|newMaxCompactSize
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMaxCompactSize
argument_list|()
operator|-
literal|1
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|strPrefix
operator|+
literal|"max.size"
argument_list|,
name|newMaxCompactSize
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMaxCompactSize
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMaxCompactSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if the offPeakMaxCompactSize gets updated.
name|long
name|newOffpeakMaxCompactSize
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getOffPeakMaxCompactSize
argument_list|()
operator|-
literal|1
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MAX_SIZE_OFFPEAK_KEY
argument_list|,
name|newOffpeakMaxCompactSize
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newOffpeakMaxCompactSize
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getOffPeakMaxCompactSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if majorCompactionPeriod gets updated.
name|long
name|newMajorCompactionPeriod
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMajorCompactionPeriod
argument_list|()
operator|+
literal|10
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
name|newMajorCompactionPeriod
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMajorCompactionPeriod
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMajorCompactionPeriod
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if majorCompactionJitter gets updated.
name|float
name|newMajorCompactionJitter
init|=
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMajorCompactionJitter
argument_list|()
operator|+
literal|0.02F
decl_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
name|newMajorCompactionJitter
argument_list|)
expr_stmt|;
name|rs1
operator|.
name|getConfigurationManager
argument_list|()
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newMajorCompactionJitter
argument_list|,
name|hstore
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|getMajorCompactionJitter
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

