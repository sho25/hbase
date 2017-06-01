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
name|snapshot
package|;
end_package

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
name|testclassification
operator|.
name|VerySlowRegionServerTests
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
name|Ignore
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
comment|/**  * Test Export Snapshot Tool  */
end_comment

begin_class
annotation|@
name|Ignore
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowRegionServerTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMobExportSnapshot
extends|extends
name|TestExportSnapshot
block|{
specifier|public
specifier|static
name|void
name|setUpBaseConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|TestExportSnapshot
operator|.
name|setUpBaseConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MobConstants
operator|.
name|MOB_FILE_CACHE_SIZE_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
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
name|setUpBaseConf
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|MobSnapshotTestingUtils
operator|.
name|createPreSplitMobTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|2
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RegionPredicate
name|getBypassRegionPredicate
parameter_list|()
block|{
return|return
operator|new
name|RegionPredicate
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
name|MobUtils
operator|.
name|isMobRegionInfo
argument_list|(
name|regionInfo
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

