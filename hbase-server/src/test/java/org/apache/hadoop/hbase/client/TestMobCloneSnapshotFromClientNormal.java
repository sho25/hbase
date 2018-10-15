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
name|master
operator|.
name|cleaner
operator|.
name|TimeToLiveHFileCleaner
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
name|snapshot
operator|.
name|MobSnapshotTestingUtils
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
name|snapshot
operator|.
name|SnapshotTestingUtils
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
name|ClientTests
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
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMobCloneSnapshotFromClientNormal
extends|extends
name|CloneSnapshotFromClientNormalTestBase
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
name|TestMobCloneSnapshotFromClientNormal
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|void
name|setupConfiguration
parameter_list|()
block|{
name|CloneSnapshotFromClientTestBase
operator|.
name|setupConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|setupConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|createTable
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|MobSnapshotTestingUtils
operator|.
name|createMobTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
name|SnapshotTestingUtils
operator|.
name|getSplitKeys
argument_list|()
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|numRowsToLoad
parameter_list|()
block|{
return|return
literal|20
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|countRows
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|MobSnapshotTestingUtils
operator|.
name|countMobRows
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|verifyRowCount
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|util
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|long
name|expectedRows
parameter_list|)
throws|throws
name|IOException
block|{
name|MobSnapshotTestingUtils
operator|.
name|verifyMobRowCount
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|expectedRows
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

