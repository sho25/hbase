begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
package|;
end_package

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
name|UnknownScannerException
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|fail
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
name|TestReplicationKillRS
extends|extends
name|TestReplicationBase
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
name|TestReplicationKillRS
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Load up 1 tables over 2 region servers and kill a source during    * the upload. The failover happens internally.    *    * WARNING this test sometimes fails because of HBASE-3515    *    * @throws Exception    */
specifier|public
name|void
name|loadTableAndKillRS
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|)
throws|throws
name|Exception
block|{
comment|// killing the RS with hbase:meta can result into failed puts until we solve
comment|// IO fencing
name|int
name|rsToKill1
init|=
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getServerWithMeta
argument_list|()
operator|==
literal|0
condition|?
literal|1
else|:
literal|0
decl_stmt|;
comment|// Takes about 20 secs to run the full loading, kill around the middle
name|Thread
name|killer
init|=
name|killARegionServer
argument_list|(
name|util
argument_list|,
literal|5000
argument_list|,
name|rsToKill1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start loading table"
argument_list|)
expr_stmt|;
name|int
name|initialCount
init|=
name|utility1
operator|.
name|loadTable
argument_list|(
name|htable1
argument_list|,
name|famName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done loading table"
argument_list|)
expr_stmt|;
name|killer
operator|.
name|join
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done waiting for threads"
argument_list|)
expr_stmt|;
name|Result
index|[]
name|res
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|htable1
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|res
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|initialCount
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster wasn't ready yet, restarting scanner"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Test we actually have all the rows, we may miss some because we
comment|// don't have IO fencing.
if|if
condition|(
name|res
operator|.
name|length
operator|!=
name|initialCount
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"We lost some rows on the master cluster!"
argument_list|)
expr_stmt|;
comment|// We don't really expect the other cluster to have more rows
name|initialCount
operator|=
name|res
operator|.
name|length
expr_stmt|;
block|}
name|int
name|lastCount
init|=
literal|0
decl_stmt|;
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|i
operator|==
name|NB_RETRIES
operator|-
literal|1
condition|)
block|{
name|fail
argument_list|(
literal|"Waited too much time for queueFailover replication. "
operator|+
literal|"Waited "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|+
literal|"ms."
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan2
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|scanner2
init|=
name|htable2
operator|.
name|getScanner
argument_list|(
name|scan2
argument_list|)
decl_stmt|;
name|Result
index|[]
name|res2
init|=
name|scanner2
operator|.
name|next
argument_list|(
name|initialCount
operator|*
literal|2
argument_list|)
decl_stmt|;
name|scanner2
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|res2
operator|.
name|length
operator|<
name|initialCount
condition|)
block|{
if|if
condition|(
name|lastCount
operator|<
name|res2
operator|.
name|length
condition|)
block|{
name|i
operator|--
expr_stmt|;
comment|// Don't increment timeout if we make progress
block|}
else|else
block|{
name|i
operator|++
expr_stmt|;
block|}
name|lastCount
operator|=
name|res2
operator|.
name|length
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Only got "
operator|+
name|lastCount
operator|+
literal|" rows instead of "
operator|+
name|initialCount
operator|+
literal|" current i="
operator|+
name|i
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
operator|*
literal|2
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
specifier|private
specifier|static
name|Thread
name|killARegionServer
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|utility
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|,
specifier|final
name|int
name|rs
parameter_list|)
block|{
name|Thread
name|killer
init|=
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
name|utility
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|rs
argument_list|)
operator|.
name|stop
argument_list|(
literal|"Stopping as part of the test"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't kill a region server"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|killer
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|killer
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|killer
return|;
block|}
block|}
end_class

end_unit

