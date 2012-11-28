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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|ChaosMonkey
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
name|Before
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
comment|/**  * A system test which does large data ingestion and verify using {@link LoadTestTool},  * while killing the region servers and the master(s) randomly. You can configure how long  * should the load test run by using "hbase.IntegrationTestDataIngestWithChaosMonkey.runtime"  * configuration parameter.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestDataIngestWithChaosMonkey
extends|extends
name|IngestIntegrationTestBase
block|{
specifier|private
specifier|static
specifier|final
name|int
name|NUM_SLAVES_BASE
init|=
literal|4
decl_stmt|;
comment|//number of slaves for the smallest cluster
comment|// run for 5 min by default
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_RUN_TIME
init|=
literal|5
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|private
name|ChaosMonkey
name|monkey
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|(
name|NUM_SLAVES_BASE
argument_list|)
expr_stmt|;
name|monkey
operator|=
operator|new
name|ChaosMonkey
argument_list|(
name|util
argument_list|,
name|ChaosMonkey
operator|.
name|EVERY_MINUTE_RANDOM_ACTION_POLICY
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|monkey
operator|!=
literal|null
condition|)
block|{
name|monkey
operator|.
name|stop
argument_list|(
literal|"test has finished, that's why"
argument_list|)
expr_stmt|;
name|monkey
operator|.
name|waitForStop
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDataIngest
parameter_list|()
throws|throws
name|Exception
block|{
name|runIngestTest
argument_list|(
name|DEFAULT_RUN_TIME
argument_list|,
literal|2500
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

