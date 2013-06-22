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
name|master
operator|.
name|handler
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|classification
operator|.
name|InterfaceAudience
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
name|Server
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
name|executor
operator|.
name|EventType
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
name|AssignmentManager
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
name|DeadServer
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
name|MasterServices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Shutdown handler for the server hosting<code>.META.</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaServerShutdownHandler
extends|extends
name|ServerShutdownHandler
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
name|MetaServerShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|MetaServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|services
argument_list|,
name|deadServers
argument_list|,
name|serverName
argument_list|,
name|EventType
operator|.
name|M_META_SERVER_SHUTDOWN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|gotException
init|=
literal|true
decl_stmt|;
try|try
block|{
name|AssignmentManager
name|am
init|=
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting META logs for "
operator|+
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|distributedLogReplay
condition|)
block|{
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|prepareMetaLogReplay
argument_list|(
name|serverName
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed log splitting for "
operator|+
name|serverName
operator|+
literal|", will retry"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
comment|// Assign meta if we were carrying it.
comment|// Check again: region may be assigned to other where because of RIT
comment|// timeout
if|if
condition|(
name|am
operator|.
name|isCarryingMeta
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server "
operator|+
name|serverName
operator|+
literal|" was carrying META. Trying to assign."
argument_list|)
expr_stmt|;
name|am
operator|.
name|regionOffline
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
name|verifyAndAssignMetaWithRetries
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|this
operator|.
name|services
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|isMetaLocationAvailable
argument_list|()
condition|)
block|{
comment|// the meta location as per master is null. This could happen in case when meta assignment
comment|// in previous run failed, while meta znode has been updated to null. We should try to
comment|// assign the meta again.
name|verifyAndAssignMetaWithRetries
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"META has been assigned to otherwhere, skip assigning."
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
operator|&&
name|this
operator|.
name|distributedLogReplay
condition|)
block|{
if|if
condition|(
operator|!
name|am
operator|.
name|waitOnRegionToClearRegionsInTransition
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|regionAssignmentWaitTimeout
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Region "
operator|+
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" didn't complete assignment in time"
argument_list|)
throw|;
block|}
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
if|if
condition|(
name|ex
operator|instanceof
name|IOException
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed log splitting for "
operator|+
name|serverName
operator|+
literal|", will retry"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
name|gotException
operator|=
literal|false
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gotException
condition|)
block|{
comment|// If we had an exception, this.deadServers.finish will be skipped in super.process()
name|this
operator|.
name|deadServers
operator|.
name|finish
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|isCarryingMeta
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Before assign the META region, ensure it haven't    *  been assigned by other place    *<p>    * Under some scenarios, the META region can be opened twice, so it seemed online    * in two regionserver at the same time.    * If the META region has been assigned, so the operation can be canceled.    * @throws InterruptedException    * @throws IOException    * @throws KeeperException    */
specifier|private
name|void
name|verifyAndAssignMeta
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
throws|,
name|KeeperException
block|{
name|long
name|timeout
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|timeout
argument_list|)
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|assignMeta
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|serverName
operator|.
name|equals
argument_list|(
name|server
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|getMetaLocation
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|".META. is onlined on the dead server "
operator|+
name|serverName
argument_list|)
throw|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skip assigning .META., because it is online on the "
operator|+
name|server
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|getMetaLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Failed many times, shutdown processing    * @throws IOException    */
specifier|private
name|void
name|verifyAndAssignMetaWithRetries
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|iTimes
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.catalog.verification.retries"
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|long
name|waitTime
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|int
name|iFlag
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|verifyAndAssignMeta
argument_list|()
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"In server shutdown processing, assigning meta"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Aborting"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|iFlag
operator|>=
name|iTimes
condition|)
block|{
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"verifyAndAssignMeta failed after"
operator|+
name|iTimes
operator|+
literal|" times retries, aborting"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Aborting"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted when is the thread sleep"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Interrupted"
argument_list|,
name|e1
argument_list|)
throw|;
block|}
name|iFlag
operator|++
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
return|;
block|}
block|}
end_class

end_unit

