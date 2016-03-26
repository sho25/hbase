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
operator|.
name|wal
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
name|assertFalse
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
name|fs
operator|.
name|Path
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
name|wal
operator|.
name|WAL
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

begin_comment
comment|/**  * Tests that verifies that the log is forced to be rolled every "hbase.regionserver.logroll.period"  */
end_comment

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
name|TestLogRollPeriod
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
name|AbstractTestLogRolling
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|long
name|LOG_ROLL_PERIOD
init|=
literal|4000
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
comment|// disable the ui
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionsever.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.regionserver.logroll.period"
argument_list|,
name|LOG_ROLL_PERIOD
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests that the LogRoller perform the roll even if there are no edits    */
annotation|@
name|Test
specifier|public
name|void
name|testNoEdits
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestLogRollPeriodNoEdits"
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
literal|"cf"
argument_list|)
expr_stmt|;
try|try
block|{
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegionServer
name|server
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|checkMinLogRolls
argument_list|(
name|log
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Tests that the LogRoller perform the roll with some data in the log    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWithEdits
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
literal|"TestLogRollPeriodWithEdits"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|family
init|=
literal|"cf"
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
try|try
block|{
name|HRegionServer
name|server
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|WAL
name|log
init|=
name|server
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Thread
name|writerThread
init|=
operator|new
name|Thread
argument_list|(
literal|"writer"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|long
name|row
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|interrupted
argument_list|()
condition|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"row%d"
argument_list|,
name|row
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|row
operator|++
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|LOG_ROLL_PERIOD
operator|/
literal|16
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
try|try
block|{
name|writerThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|checkMinLogRolls
argument_list|(
name|log
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writerThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|writerThread
operator|.
name|join
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkMinLogRolls
parameter_list|(
specifier|final
name|WAL
name|log
parameter_list|,
specifier|final
name|int
name|minRolls
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|log
operator|.
name|registerWALActionsListener
argument_list|(
operator|new
name|WALActionsListener
operator|.
name|Base
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldFile
parameter_list|,
name|Path
name|newFile
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"postLogRoll: oldFile="
operator|+
name|oldFile
operator|+
literal|" newFile="
operator|+
name|newFile
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|newFile
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Sleep until we should get at least min-LogRoll events
name|long
name|wtime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
operator|(
name|minRolls
operator|+
literal|1
operator|)
operator|*
name|LOG_ROLL_PERIOD
argument_list|)
expr_stmt|;
comment|// Do some extra sleep in case the machine is slow,
comment|// and the log-roll is not triggered exactly on LOG_ROLL_PERIOD.
specifier|final
name|int
name|NUM_RETRIES
init|=
literal|1
operator|+
literal|8
operator|*
operator|(
name|minRolls
operator|-
name|paths
operator|.
name|size
argument_list|()
operator|)
decl_stmt|;
for|for
control|(
name|int
name|retry
init|=
literal|0
init|;
name|paths
operator|.
name|size
argument_list|()
operator|<
name|minRolls
operator|&&
name|retry
operator|<
name|NUM_RETRIES
condition|;
operator|++
name|retry
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|LOG_ROLL_PERIOD
operator|/
literal|4
argument_list|)
expr_stmt|;
block|}
name|wtime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|wtime
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"got %d rolls after %dms (%dms each) - expected at least %d rolls"
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|,
name|wtime
argument_list|,
name|wtime
operator|/
name|paths
operator|.
name|size
argument_list|()
argument_list|,
name|minRolls
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|paths
operator|.
name|size
argument_list|()
operator|<
name|minRolls
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

