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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|Action
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MultiRequest
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|RegionAction
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Tests logging of large batch commands via Multi. Tests are fast, but uses a mini-cluster (to test  * via "Multi" commands) so classified as MediumTests  */
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
name|TestMultiLogThreshold
block|{
specifier|private
specifier|static
name|RSRpcServices
name|SERVICES
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|CONF
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|RSRpcServices
operator|.
name|LogDelegate
name|LD
decl_stmt|;
specifier|private
specifier|static
name|HRegionServer
name|RS
decl_stmt|;
specifier|private
specifier|static
name|int
name|THRESHOLD
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tableName"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
expr_stmt|;
name|CONF
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|THRESHOLD
operator|=
name|CONF
operator|.
name|getInt
argument_list|(
name|RSRpcServices
operator|.
name|BATCH_ROWS_THRESHOLD_NAME
argument_list|,
name|RSRpcServices
operator|.
name|BATCH_ROWS_THRESHOLD_DEFAULT
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|TEST_FAM
argument_list|)
expr_stmt|;
name|RS
operator|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setupTest
parameter_list|()
throws|throws
name|Exception
block|{
name|LD
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RSRpcServices
operator|.
name|LogDelegate
operator|.
name|class
argument_list|)
expr_stmt|;
name|SERVICES
operator|=
operator|new
name|RSRpcServices
argument_list|(
name|RS
argument_list|,
name|LD
argument_list|)
expr_stmt|;
block|}
specifier|private
enum|enum
name|ActionType
block|{
name|REGION_ACTIONS
block|,
name|ACTIONS
block|;   }
comment|/**    * Sends a multi request with a certain amount of rows, will populate Multi command with either    * "rows" number of RegionActions with one Action each or one RegionAction with "rows" number of    * Actions    */
specifier|private
name|void
name|sendMultiRequest
parameter_list|(
name|int
name|rows
parameter_list|,
name|ActionType
name|actionType
parameter_list|)
throws|throws
name|ServiceException
block|{
name|RpcController
name|rpcc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcController
operator|.
name|class
argument_list|)
decl_stmt|;
name|MultiRequest
operator|.
name|Builder
name|builder
init|=
name|MultiRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|int
name|numRAs
init|=
literal|1
decl_stmt|;
name|int
name|numAs
init|=
literal|1
decl_stmt|;
switch|switch
condition|(
name|actionType
condition|)
block|{
case|case
name|REGION_ACTIONS
case|:
name|numRAs
operator|=
name|rows
expr_stmt|;
break|break;
case|case
name|ACTIONS
case|:
name|numAs
operator|=
name|rows
expr_stmt|;
break|break;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRAs
condition|;
name|i
operator|++
control|)
block|{
name|RegionAction
operator|.
name|Builder
name|rab
init|=
name|RegionAction
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|rab
operator|.
name|setRegion
argument_list|(
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
operator|new
name|String
argument_list|(
literal|"someStuff"
operator|+
name|i
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numAs
condition|;
name|j
operator|++
control|)
block|{
name|Action
operator|.
name|Builder
name|ab
init|=
name|Action
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|rab
operator|.
name|addAction
argument_list|(
name|ab
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addRegionAction
argument_list|(
name|rab
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|SERVICES
operator|.
name|multi
argument_list|(
name|rpcc
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
comment|// swallow expected exception due to mocked RpcController
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiLogThresholdRegionActions
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|sendMultiRequest
argument_list|(
name|THRESHOLD
operator|+
literal|1
argument_list|,
name|ActionType
operator|.
name|REGION_ACTIONS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|LD
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|logBatchWarning
argument_list|(
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiNoLogThresholdRegionActions
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|sendMultiRequest
argument_list|(
name|THRESHOLD
argument_list|,
name|ActionType
operator|.
name|REGION_ACTIONS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|LD
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|logBatchWarning
argument_list|(
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiLogThresholdActions
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|sendMultiRequest
argument_list|(
name|THRESHOLD
operator|+
literal|1
argument_list|,
name|ActionType
operator|.
name|ACTIONS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|LD
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|logBatchWarning
argument_list|(
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiNoLogThresholdAction
parameter_list|()
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|sendMultiRequest
argument_list|(
name|THRESHOLD
argument_list|,
name|ActionType
operator|.
name|ACTIONS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|LD
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|logBatchWarning
argument_list|(
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
