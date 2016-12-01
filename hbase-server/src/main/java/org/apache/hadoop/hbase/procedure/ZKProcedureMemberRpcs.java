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
name|procedure
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
name|Arrays
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
name|errorhandling
operator|.
name|ForeignException
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
name|ProtobufUtil
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
comment|/**  * ZooKeeper based controller for a procedure member.  *<p>  * There can only be one {@link ZKProcedureMemberRpcs} per procedure type per member,  * since each procedure type is bound to a single set of znodes. You can have multiple  * {@link ZKProcedureMemberRpcs} on the same server, each serving a different member  * name, but each individual rpcs is still bound to a single member name (and since they are  * used to determine global progress, its important to not get this wrong).  *<p>  * To make this slightly more confusing, you can run multiple, concurrent procedures at the same  * time (as long as they have different types), from the same controller, but the same node name  * must be used for each procedure (though there is no conflict between the two procedure as long  * as they have distinct names).  *<p>  * There is no real error recovery with this mechanism currently -- if any the coordinator fails,  * its re-initialization will delete the znodes and require all in progress subprocedures to start  * anew.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKProcedureMemberRpcs
implements|implements
name|ProcedureMemberRpcs
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
name|ZKProcedureMemberRpcs
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ZKProcedureUtil
name|zkController
decl_stmt|;
specifier|protected
name|ProcedureMember
name|member
decl_stmt|;
specifier|private
name|String
name|memberName
decl_stmt|;
comment|/**    * Must call {@link #start(String, ProcedureMember)} before this can be used.    * @param watcher {@link ZooKeeperWatcher} to be owned by<tt>this</tt>. Closed via    *          {@link #close()}.    * @param procType name of the znode describing the procedure type    * @throws KeeperException if we can't reach zookeeper    */
specifier|public
name|ZKProcedureMemberRpcs
parameter_list|(
specifier|final
name|ZooKeeperWatcher
name|watcher
parameter_list|,
specifier|final
name|String
name|procType
parameter_list|)
throws|throws
name|KeeperException
block|{
name|this
operator|.
name|zkController
operator|=
operator|new
name|ZKProcedureUtil
argument_list|(
name|watcher
argument_list|,
name|procType
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|nodeCreated
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isInProcedurePath
argument_list|(
name|path
argument_list|)
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Received created event:"
operator|+
name|path
argument_list|)
expr_stmt|;
comment|// if it is a simple start/end/abort then we just rewatch the node
if|if
condition|(
name|isAcquiredNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|waitForNewProcedures
argument_list|()
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|isAbortNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|watchForAbortedProcedures
argument_list|()
expr_stmt|;
return|return;
block|}
name|String
name|parent
init|=
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|path
argument_list|)
decl_stmt|;
comment|// if its the end barrier, the procedure can be completed
if|if
condition|(
name|isReachedNode
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|receivedReachedGlobalBarrier
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|isAbortNode
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|abort
argument_list|(
name|path
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|isAcquiredNode
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|startNewSubprocedure
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring created notification for node:"
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeChildrenChanged
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|this
operator|.
name|acquiredZnode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received procedure start children changed event: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|waitForNewProcedures
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|path
operator|.
name|equals
argument_list|(
name|this
operator|.
name|abortZnode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received procedure abort children changed event: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|watchForAbortedProcedures
argument_list|()
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
block|}
specifier|public
name|ZKProcedureUtil
name|getZkController
parameter_list|()
block|{
return|return
name|zkController
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMemberName
parameter_list|()
block|{
return|return
name|memberName
return|;
block|}
comment|/**    * Pass along the procedure global barrier notification to any listeners    * @param path full znode path that cause the notification    */
specifier|private
name|void
name|receivedReachedGlobalBarrier
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Recieved reached global barrier:"
operator|+
name|path
argument_list|)
expr_stmt|;
name|String
name|procName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|this
operator|.
name|member
operator|.
name|receivedReachedGlobalBarrier
argument_list|(
name|procName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|watchForAbortedProcedures
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking for aborted procedures on node: '"
operator|+
name|zkController
operator|.
name|getAbortZnode
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// this is the list of the currently aborted procedues
for|for
control|(
name|String
name|node
range|:
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|zkController
operator|.
name|getAbortZnode
argument_list|()
argument_list|)
control|)
block|{
name|String
name|abortNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkController
operator|.
name|getAbortZnode
argument_list|()
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|abort
argument_list|(
name|abortNode
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to list children for abort node:"
operator|+
name|zkController
operator|.
name|getAbortZnode
argument_list|()
argument_list|,
name|e
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|waitForNewProcedures
parameter_list|()
block|{
comment|// watch for new procedues that we need to start subprocedures for
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking for new procedures under znode:'"
operator|+
name|zkController
operator|.
name|getAcquiredBarrier
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|runningProcedures
init|=
literal|null
decl_stmt|;
try|try
block|{
name|runningProcedures
operator|=
name|ZKUtil
operator|.
name|listChildrenAndWatchForNewChildren
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|zkController
operator|.
name|getAcquiredBarrier
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|runningProcedures
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No running procedures."
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"General failure when watching for new procedures"
argument_list|,
name|e
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|runningProcedures
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No running procedures."
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|String
name|procName
range|:
name|runningProcedures
control|)
block|{
comment|// then read in the procedure information
name|String
name|path
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkController
operator|.
name|getAcquiredBarrier
argument_list|()
argument_list|,
name|procName
argument_list|)
decl_stmt|;
name|startNewSubprocedure
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Kick off a new sub-procedure on the listener with the data stored in the passed znode.    *<p>    * Will attempt to create the same procedure multiple times if an procedure znode with the same    * name is created. It is left up the coordinator to ensure this doesn't occur.    * @param path full path to the znode for the procedure to start    */
specifier|private
specifier|synchronized
name|void
name|startNewSubprocedure
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found procedure znode: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|String
name|opName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
decl_stmt|;
comment|// start watching for an abort notification for the procedure
name|String
name|abortZNode
init|=
name|zkController
operator|.
name|getAbortZNode
argument_list|(
name|opName
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|abortZNode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Not starting:"
operator|+
name|opName
operator|+
literal|" because we already have an abort notification."
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to get the abort znode ("
operator|+
name|abortZNode
operator|+
literal|") for procedure :"
operator|+
name|opName
argument_list|,
name|e
argument_list|,
name|opName
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// get the data for the procedure
name|Subprocedure
name|subproc
init|=
literal|null
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Data in for starting procedure "
operator|+
name|opName
operator|+
literal|" is illegally formatted (no pb magic). "
operator|+
literal|"Killing the procedure: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"start proc data length is "
operator|+
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|data
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|data
argument_list|,
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found data for znode:"
operator|+
name|path
argument_list|)
expr_stmt|;
name|subproc
operator|=
name|member
operator|.
name|createSubprocedure
argument_list|(
name|opName
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|member
operator|.
name|submitSubprocedure
argument_list|(
name|subproc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Illegal argument exception"
argument_list|,
name|iae
argument_list|)
expr_stmt|;
name|sendMemberAborted
argument_list|(
name|subproc
argument_list|,
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
name|iae
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Illegal state exception "
argument_list|,
name|ise
argument_list|)
expr_stmt|;
name|sendMemberAborted
argument_list|(
name|subproc
argument_list|,
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
name|ise
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to get data for new procedure:"
operator|+
name|opName
argument_list|,
name|e
argument_list|,
name|opName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to get data for new procedure:"
operator|+
name|opName
argument_list|,
name|e
argument_list|,
name|opName
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
block|}
block|}
comment|/**    * This attempts to create an acquired state znode for the procedure (snapshot name).    *    * It then looks for the reached znode to trigger in-barrier execution.  If not present we    * have a watcher, if present then trigger the in-barrier action.    */
annotation|@
name|Override
specifier|public
name|void
name|sendMemberAcquired
parameter_list|(
name|Subprocedure
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|procName
init|=
name|sub
operator|.
name|getName
argument_list|()
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Member: '"
operator|+
name|memberName
operator|+
literal|"' joining acquired barrier for procedure ("
operator|+
name|procName
operator|+
literal|") in zk"
argument_list|)
expr_stmt|;
name|String
name|acquiredZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|ZKProcedureUtil
operator|.
name|getAcquireBarrierNode
argument_list|(
name|zkController
argument_list|,
name|procName
argument_list|)
argument_list|,
name|memberName
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|acquiredZNode
argument_list|)
expr_stmt|;
comment|// watch for the complete node for this snapshot
name|String
name|reachedBarrier
init|=
name|zkController
operator|.
name|getReachedBarrierNode
argument_list|(
name|procName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Watch for global barrier reached:"
operator|+
name|reachedBarrier
argument_list|)
expr_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|reachedBarrier
argument_list|)
condition|)
block|{
name|receivedReachedGlobalBarrier
argument_list|(
name|reachedBarrier
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to acquire barrier for procedure: "
operator|+
name|procName
operator|+
literal|" and member: "
operator|+
name|memberName
argument_list|,
name|e
argument_list|,
name|procName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This acts as the ack for a completed procedure    */
annotation|@
name|Override
specifier|public
name|void
name|sendMemberCompleted
parameter_list|(
name|Subprocedure
name|sub
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|procName
init|=
name|sub
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking procedure  '"
operator|+
name|procName
operator|+
literal|"' completed for member '"
operator|+
name|memberName
operator|+
literal|"' in zk"
argument_list|)
expr_stmt|;
name|String
name|joinPath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkController
operator|.
name|getReachedBarrierNode
argument_list|(
name|procName
argument_list|)
argument_list|,
name|memberName
argument_list|)
decl_stmt|;
comment|// ProtobufUtil.prependPBMagic does not take care of null
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|data
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
block|}
try|try
block|{
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|joinPath
argument_list|,
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to post zk node:"
operator|+
name|joinPath
operator|+
literal|" to join procedure barrier."
argument_list|,
name|e
argument_list|,
name|procName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This should be called by the member and should write a serialized root cause exception as    * to the abort znode.    */
annotation|@
name|Override
specifier|public
name|void
name|sendMemberAborted
parameter_list|(
name|Subprocedure
name|sub
parameter_list|,
name|ForeignException
name|ee
parameter_list|)
block|{
if|if
condition|(
name|sub
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed due to null subprocedure"
argument_list|,
name|ee
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|procName
init|=
name|sub
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Aborting procedure ("
operator|+
name|procName
operator|+
literal|") in zk"
argument_list|)
expr_stmt|;
name|String
name|procAbortZNode
init|=
name|zkController
operator|.
name|getAbortZNode
argument_list|(
name|procName
argument_list|)
decl_stmt|;
try|try
block|{
name|String
name|source
init|=
operator|(
name|ee
operator|.
name|getSource
argument_list|()
operator|==
literal|null
operator|)
condition|?
name|memberName
else|:
name|ee
operator|.
name|getSource
argument_list|()
decl_stmt|;
name|byte
index|[]
name|errorInfo
init|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|ForeignException
operator|.
name|serialize
argument_list|(
name|source
argument_list|,
name|ee
argument_list|)
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|procAbortZNode
argument_list|,
name|errorInfo
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished creating abort znode:"
operator|+
name|procAbortZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// possible that we get this error for the procedure if we already reset the zk state, but in
comment|// that case we should still get an error for that procedure anyways
name|zkController
operator|.
name|logZKTree
argument_list|(
name|zkController
operator|.
name|getBaseZnode
argument_list|()
argument_list|)
expr_stmt|;
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to post zk node:"
operator|+
name|procAbortZNode
operator|+
literal|" to abort procedure"
argument_list|,
name|e
argument_list|,
name|procName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Pass along the found abort notification to the listener    * @param abortZNode full znode path to the failed procedure information    */
specifier|protected
name|void
name|abort
parameter_list|(
name|String
name|abortZNode
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Aborting procedure member for znode "
operator|+
name|abortZNode
argument_list|)
expr_stmt|;
name|String
name|opName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|abortZNode
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkController
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|abortZNode
argument_list|)
decl_stmt|;
comment|// figure out the data we need to pass
name|ForeignException
name|ee
decl_stmt|;
try|try
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// ignore
return|return;
block|}
elseif|else
if|if
condition|(
operator|!
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"Illegally formatted data in abort node for proc "
operator|+
name|opName
operator|+
literal|".  Killing the procedure."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
comment|// we got a remote exception, but we can't describe it so just return exn from here
name|ee
operator|=
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|data
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|data
argument_list|,
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|ee
operator|=
name|ForeignException
operator|.
name|deserialize
argument_list|(
name|data
argument_list|)
expr_stmt|;
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
name|warn
argument_list|(
literal|"Got an error notification for op:"
operator|+
name|opName
operator|+
literal|" but we can't read the information. Killing the procedure."
argument_list|)
expr_stmt|;
comment|// we got a remote exception, but we can't describe it so just return exn from here
name|ee
operator|=
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|member
operator|.
name|receiveAbortProcedure
argument_list|(
name|opName
argument_list|,
name|ee
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|member
operator|.
name|controllerConnectionFailure
argument_list|(
literal|"Failed to get data for abort znode:"
operator|+
name|abortZNode
operator|+
name|zkController
operator|.
name|getAbortZnode
argument_list|()
argument_list|,
name|e
argument_list|,
name|opName
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
literal|"abort already in progress"
argument_list|,
name|e
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
block|}
block|}
specifier|public
name|void
name|start
parameter_list|(
specifier|final
name|String
name|memberName
parameter_list|,
specifier|final
name|ProcedureMember
name|listener
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting procedure member '"
operator|+
name|memberName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|this
operator|.
name|member
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|memberName
operator|=
name|memberName
expr_stmt|;
name|watchForAbortedProcedures
argument_list|()
expr_stmt|;
name|waitForNewProcedures
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|zkController
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

