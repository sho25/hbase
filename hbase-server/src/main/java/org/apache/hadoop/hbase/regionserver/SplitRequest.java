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
name|security
operator|.
name|PrivilegedAction
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
name|security
operator|.
name|User
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Handles processing region splits. Put in a queue, owned by HRegionServer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SplitRequest
implements|implements
name|Runnable
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
name|SplitRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|parent
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|midKey
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
specifier|private
specifier|final
name|User
name|user
decl_stmt|;
name|SplitRequest
parameter_list|(
name|Region
name|region
parameter_list|,
name|byte
index|[]
name|midKey
parameter_list|,
name|HRegionServer
name|hrs
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hrs
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
operator|(
name|HRegion
operator|)
name|region
expr_stmt|;
name|this
operator|.
name|midKey
operator|=
name|midKey
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|hrs
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"regionName="
operator|+
name|parent
operator|+
literal|", midKey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|midKey
argument_list|)
return|;
block|}
specifier|private
name|void
name|doSplitting
parameter_list|()
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|server
operator|.
name|metricsRegionServer
operator|.
name|incrSplitRequest
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
try|try
block|{
name|long
name|procId
decl_stmt|;
if|if
condition|(
name|user
operator|!=
literal|null
operator|&&
name|user
operator|.
name|getUGI
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|procId
operator|=
name|user
operator|.
name|getUGI
argument_list|()
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|run
parameter_list|()
block|{
try|try
block|{
return|return
name|server
operator|.
name|requestRegionSplit
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|midKey
argument_list|)
return|;
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
literal|"Failed to complete region split "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|long
operator|)
operator|-
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|procId
operator|=
name|server
operator|.
name|requestRegionSplit
argument_list|(
name|parent
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|midKey
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|procId
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// wait for the split to complete or get interrupted.  If the split completes successfully,
comment|// the procedure will return true; if the split fails, the procedure would throw exception.
comment|//
try|try
block|{
while|while
condition|(
operator|!
operator|(
name|success
operator|=
name|server
operator|.
name|isProcedureFinished
argument_list|(
name|procId
argument_list|)
operator|)
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Split region "
operator|+
name|parent
operator|+
literal|" is still in progress.  Not waiting..."
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
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
literal|"Split region "
operator|+
name|parent
operator|+
literal|" failed."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Fail to split region "
operator|+
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|this
operator|.
name|parent
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|parent
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postCompleteSplit
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|io
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Split failed "
operator|+
name|this
argument_list|,
name|io
operator|instanceof
name|RemoteException
condition|?
operator|(
operator|(
name|RemoteException
operator|)
name|io
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
else|:
name|io
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Update regionserver metrics with the split transaction total running time
name|server
operator|.
name|metricsRegionServer
operator|.
name|updateSplitTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|parent
operator|.
name|shouldForceSplit
argument_list|()
condition|)
block|{
name|parent
operator|.
name|clearSplit
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|success
condition|)
block|{
name|server
operator|.
name|metricsRegionServer
operator|.
name|incrSplitSuccess
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopping
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping split because server is stopping="
operator|+
name|this
operator|.
name|server
operator|.
name|isStopping
argument_list|()
operator|+
literal|" or stopped="
operator|+
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|doSplitting
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

