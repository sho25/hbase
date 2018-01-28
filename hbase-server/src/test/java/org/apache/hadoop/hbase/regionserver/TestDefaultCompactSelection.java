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
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionRequestImpl
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
name|RatioBasedCompactionPolicy
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
name|EnvironmentEdgeManager
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
name|TimeOffsetEnvironmentEdge
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDefaultCompactSelection
extends|extends
name|TestCompactionPolicy
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
name|TestDefaultCompactSelection
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|config
parameter_list|()
block|{
name|super
operator|.
name|config
argument_list|()
expr_stmt|;
comment|// DON'T change this config since all test cases assume HStore.BLOCKING_STOREFILES_KEY is 10.
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionRatio
parameter_list|()
throws|throws
name|IOException
block|{
name|TimeOffsetEnvironmentEdge
name|edge
init|=
operator|new
name|TimeOffsetEnvironmentEdge
argument_list|()
decl_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
comment|/**      * NOTE: these tests are specific to describe the implementation of the      * current compaction algorithm.  Developed to ensure that refactoring      * doesn't implicitly alter this.      */
name|long
name|tooBig
init|=
name|maxSize
operator|+
literal|1
decl_stmt|;
comment|// default case. preserve user ratio on size
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// less than compact threshold = don't compact
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// greater than compact size = skip those
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
expr_stmt|;
comment|// big size + threshold
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|,
literal|700
argument_list|,
literal|700
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// small files = don't care about ratio
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|7
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// don't exceed max file compact threshold
comment|// note:  file selection starts with largest to smallest.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|50
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|50
argument_list|)
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|251
argument_list|,
literal|253
argument_list|,
literal|251
argument_list|,
name|maxSize
operator|-
literal|1
argument_list|)
argument_list|,
literal|251
argument_list|,
literal|253
argument_list|,
literal|251
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|maxSize
operator|-
literal|1
argument_list|,
name|maxSize
operator|-
literal|1
argument_list|,
name|maxSize
operator|-
literal|1
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// Always try and compact something to get below blocking storefile count
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hstore.compaction.min.size"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|512
argument_list|,
literal|256
argument_list|,
literal|128
argument_list|,
literal|64
argument_list|,
literal|32
argument_list|,
literal|16
argument_list|,
literal|8
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|4
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hstore.compaction.min.size"
argument_list|,
name|minSize
argument_list|)
expr_stmt|;
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|/* MAJOR COMPACTION */
comment|// if a major compaction has been forced, then compact everything
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// also choose files< threshold on major compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// even if one of those files is too big
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// don't exceed max file compact threshold, even with major compaction
name|store
operator|.
name|forceMajor
operator|=
literal|true
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|store
operator|.
name|forceMajor
operator|=
literal|false
expr_stmt|;
comment|// if we exceed maxCompactSize, downgrade to minor
comment|// if not, it creates a 'snowball effect' when files>> maxCompactSize:
comment|// the last file in compaction is the aggregate of all previous compactions
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
comment|// The modTime of the mocked store file is currentTimeMillis, so we need to increase the
comment|// timestamp a bit to make sure that now - lowestModTime is greater than major compaction
comment|// period(1ms).
comment|// trigger an aged major compaction
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
init|=
name|sfCreate
argument_list|(
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|edge
operator|.
name|increment
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|candidates
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// major sure exceeding maxCompactSize also downgrades aged minors
name|candidates
operator|=
name|sfCreate
argument_list|(
literal|100
argument_list|,
literal|50
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|edge
operator|.
name|increment
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|candidates
argument_list|,
literal|23
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|1000
operator|*
literal|60
operator|*
literal|60
operator|*
literal|24
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hregion.majorcompaction.jitter"
argument_list|,
literal|0.20F
argument_list|)
expr_stmt|;
block|}
comment|/* REFERENCES == file is from a region that was split */
comment|// treat storefiles that have references like a major compaction
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|100
argument_list|,
literal|50
argument_list|,
literal|25
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// reference files shouldn't obey max threshold
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
argument_list|,
name|tooBig
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
comment|// reference files should obey max file compact to avoid OOM
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|true
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|7
argument_list|,
literal|6
argument_list|,
literal|5
argument_list|,
literal|4
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// empty case
name|compactEquals
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
comment|/* empty */
argument_list|)
expr_stmt|;
comment|// empty case (because all files are too big)
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
name|tooBig
argument_list|,
name|tooBig
argument_list|)
comment|/* empty */
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOffPeakCompactionRatio
parameter_list|()
throws|throws
name|IOException
block|{
comment|/*      * NOTE: these tests are specific to describe the implementation of the      * current compaction algorithm.  Developed to ensure that refactoring      * doesn't implicitly alter this.      */
comment|// set an off-peak compaction threshold
name|this
operator|.
name|conf
operator|.
name|setFloat
argument_list|(
literal|"hbase.hstore.compaction.ratio.offpeak"
argument_list|,
literal|5.0F
argument_list|)
expr_stmt|;
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|.
name|setConf
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
comment|// Test with and without the flag.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|999
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|999
argument_list|,
literal|50
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStuckStoreCompaction
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Select the smallest compaction if the store is stuck.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|)
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|)
expr_stmt|;
comment|// If not stuck, standard policy applies.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|)
argument_list|,
literal|99
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|)
expr_stmt|;
comment|// Add sufficiently small files to compaction, though
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|15
argument_list|)
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|30
argument_list|,
literal|15
argument_list|)
expr_stmt|;
comment|// Prefer earlier compaction to latter if the benefit is not significant
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|30
argument_list|,
literal|26
argument_list|,
literal|26
argument_list|,
literal|29
argument_list|,
literal|25
argument_list|,
literal|25
argument_list|)
argument_list|,
literal|30
argument_list|,
literal|26
argument_list|,
literal|26
argument_list|)
expr_stmt|;
comment|// Prefer later compaction if the benefit is significant.
name|compactEquals
argument_list|(
name|sfCreate
argument_list|(
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|99
argument_list|,
literal|27
argument_list|,
literal|27
argument_list|,
literal|27
argument_list|,
literal|20
argument_list|,
literal|20
argument_list|,
literal|20
argument_list|)
argument_list|,
literal|20
argument_list|,
literal|20
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactionEmptyHFile
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Set TTL
name|ScanInfo
name|oldScanInfo
init|=
name|store
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|ScanInfo
name|newScanInfo
init|=
name|oldScanInfo
operator|.
name|customize
argument_list|(
name|oldScanInfo
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
literal|600
argument_list|)
decl_stmt|;
name|store
operator|.
name|setScanInfo
argument_list|(
name|newScanInfo
argument_list|)
expr_stmt|;
comment|// Do not compact empty store file
name|List
argument_list|<
name|HStoreFile
argument_list|>
name|candidates
init|=
name|sfCreate
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|HStoreFile
name|file
range|:
name|candidates
control|)
block|{
if|if
condition|(
name|file
operator|instanceof
name|MockHStoreFile
condition|)
block|{
name|MockHStoreFile
name|mockFile
init|=
operator|(
name|MockHStoreFile
operator|)
name|file
decl_stmt|;
name|mockFile
operator|.
name|setTimeRangeTracker
argument_list|(
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|TimeRangeTracker
operator|.
name|Type
operator|.
name|SYNC
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mockFile
operator|.
name|setEntries
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Test Default compactions
name|CompactionRequestImpl
name|result
init|=
operator|(
operator|(
name|RatioBasedCompactionPolicy
operator|)
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|)
operator|.
name|selectCompaction
argument_list|(
name|candidates
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|result
operator|.
name|getFiles
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|setScanInfo
argument_list|(
name|oldScanInfo
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

