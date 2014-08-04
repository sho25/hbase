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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|client
operator|.
name|Admin
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
name|util
operator|.
name|RegionSplitter
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
operator|.
name|SplitAlgorithm
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * An integration test to detect regressions in HBASE-7220. Create  * a table with many regions and verify it completes within a  * reasonable amount of time.  * @see<a href="https://issues.apache.org/jira/browse/HBASE-7220">HBASE-7220</a>  */
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
name|IntegrationTestManyRegions
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|IntegrationTestManyRegions
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IntegrationTestManyRegions
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|COLUMN_NAME
init|=
literal|"f"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|REGION_COUNT_KEY
init|=
name|String
operator|.
name|format
argument_list|(
literal|"hbase.%s.regions"
argument_list|,
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|REGIONSERVER_COUNT_KEY
init|=
name|String
operator|.
name|format
argument_list|(
literal|"hbase.%s.regionServers"
argument_list|,
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|TIMEOUT_MINUTES_KEY
init|=
name|String
operator|.
name|format
argument_list|(
literal|"hbase.%s.timeoutMinutes"
argument_list|,
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_REGION_COUNT
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_REGIONSERVER_COUNT
init|=
literal|1
decl_stmt|;
comment|// running this test on my laptop consistently takes about 2.5
comment|// minutes. A timeout of 4 minutes should be reasonably safe.
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_TIMEOUT_MINUTES
init|=
literal|4
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|IntegrationTestingUtility
name|util
init|=
operator|new
name|IntegrationTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|REGION_COUNT
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|REGION_COUNT_KEY
argument_list|,
name|DEFAULT_REGION_COUNT
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|REGION_SERVER_COUNT
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|REGIONSERVER_COUNT_KEY
argument_list|,
name|DEFAULT_REGIONSERVER_COUNT
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|TIMEOUT_MINUTES
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|TIMEOUT_MINUTES_KEY
argument_list|,
name|DEFAULT_TIMEOUT_MINUTES
argument_list|)
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
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Initializing cluster with %d region servers."
argument_list|,
name|REGION_SERVER_COUNT
argument_list|)
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|REGION_SERVER_COUNT
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster initialized"
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Deleting existing table %s."
argument_list|,
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Existing table %s deleted."
argument_list|,
name|TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster ready"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning up after test."
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring cluster."
argument_list|)
expr_stmt|;
name|util
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster restored."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateTableWithRegions
parameter_list|()
throws|throws
name|Exception
block|{
name|CountDownLatch
name|doneSignal
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Worker
name|worker
init|=
operator|new
name|Worker
argument_list|(
name|doneSignal
argument_list|,
name|util
operator|.
name|getHBaseAdmin
argument_list|()
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|worker
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Launching worker thread to create the table."
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|boolean
name|workerComplete
init|=
literal|false
decl_stmt|;
name|workerComplete
operator|=
name|doneSignal
operator|.
name|await
argument_list|(
name|TIMEOUT_MINUTES
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|workerComplete
condition|)
block|{
name|t
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Timeout limit expired."
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Table creation failed."
argument_list|,
name|worker
operator|.
name|isSuccess
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|Worker
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|CountDownLatch
name|doneSignal
decl_stmt|;
specifier|private
specifier|final
name|Admin
name|admin
decl_stmt|;
specifier|private
name|boolean
name|success
init|=
literal|false
decl_stmt|;
specifier|public
name|Worker
parameter_list|(
specifier|final
name|CountDownLatch
name|doneSignal
parameter_list|,
specifier|final
name|Admin
name|admin
parameter_list|)
block|{
name|this
operator|.
name|doneSignal
operator|=
name|doneSignal
expr_stmt|;
name|this
operator|.
name|admin
operator|=
name|admin
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSuccess
parameter_list|()
block|{
return|return
name|this
operator|.
name|success
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|startTime
decl_stmt|,
name|endTime
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|SplitAlgorithm
name|algo
init|=
operator|new
name|RegionSplitter
operator|.
name|HexStringSplit
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|splits
init|=
name|algo
operator|.
name|split
argument_list|(
name|REGION_COUNT
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Creating table %s with %d splits."
argument_list|,
name|TABLE_NAME
argument_list|,
name|REGION_COUNT
argument_list|)
argument_list|)
expr_stmt|;
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|,
name|splits
argument_list|)
expr_stmt|;
name|endTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Pre-split table created successfully in %dms."
argument_list|,
operator|(
name|endTime
operator|-
name|startTime
operator|)
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to create table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|doneSignal
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

