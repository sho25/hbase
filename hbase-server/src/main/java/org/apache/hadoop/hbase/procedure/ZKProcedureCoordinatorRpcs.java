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
name|classification
operator|.
name|InterfaceStability
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * ZooKeeper based {@link ProcedureCoordinatorRpcs} for a {@link ProcedureCoordinator}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ZKProcedureCoordinatorRpcs
implements|implements
name|ProcedureCoordinatorRpcs
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKProcedureCoordinatorRpcs
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ZKProcedureUtil
name|zkProc
init|=
literal|null
decl_stmt|;
specifier|protected
name|ProcedureCoordinator
name|coordinator
init|=
literal|null
decl_stmt|;
comment|// if started this should be non-null
name|ZooKeeperWatcher
name|watcher
decl_stmt|;
name|String
name|procedureType
decl_stmt|;
name|String
name|coordName
decl_stmt|;
comment|/**    * @param watcher zookeeper watcher. Owned by<tt>this</tt> and closed via {@link #close()}    * @param procedureClass procedure type name is a category for when there are multiple kinds of    *    procedures.-- this becomes a znode so be aware of the naming restrictions    * @param coordName name of the node running the coordinator    * @throws KeeperException if an unexpected zk error occurs    */
specifier|public
name|ZKProcedureCoordinatorRpcs
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|procedureClass
parameter_list|,
name|String
name|coordName
parameter_list|)
throws|throws
name|KeeperException
block|{
name|this
operator|.
name|watcher
operator|=
name|watcher
expr_stmt|;
name|this
operator|.
name|procedureType
operator|=
name|procedureClass
expr_stmt|;
name|this
operator|.
name|coordName
operator|=
name|coordName
expr_stmt|;
block|}
comment|/**    * The "acquire" phase.  The coordinator creates a new procType/acquired/ znode dir. If znodes    * appear, first acquire to relevant listener or sets watch waiting for notification of    * the acquire node    *    * @param proc the Procedure    * @param info data to be stored in the acquire node    * @param nodeNames children of the acquire phase    * @throws IOException if any failure occurs.    */
annotation|@
name|Override
specifier|final
specifier|public
name|void
name|sendGlobalBarrierAcquire
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|byte
index|[]
name|info
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|nodeNames
parameter_list|)
throws|throws
name|IOException
throws|,
name|IllegalArgumentException
block|{
name|String
name|procName
init|=
name|proc
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// start watching for the abort node
name|String
name|abortNode
init|=
name|zkProc
operator|.
name|getAbortZNode
argument_list|(
name|procName
argument_list|)
decl_stmt|;
try|try
block|{
comment|// check to see if the abort node already exists
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|abortNode
argument_list|)
condition|)
block|{
name|abort
argument_list|(
name|abortNode
argument_list|)
expr_stmt|;
block|}
comment|// If we get an abort node watch triggered here, we'll go complete creating the acquired
comment|// znode but then handle the acquire znode and bail out
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to watch abort"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed while watching abort node:"
operator|+
name|abortNode
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// create the acquire barrier
name|String
name|acquire
init|=
name|zkProc
operator|.
name|getAcquiredBarrierNode
argument_list|(
name|procName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating acquire znode:"
operator|+
name|acquire
argument_list|)
expr_stmt|;
try|try
block|{
comment|// notify all the procedure listeners to look for the acquire node
name|byte
index|[]
name|data
init|=
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|acquire
argument_list|,
name|data
argument_list|)
expr_stmt|;
comment|// loop through all the children of the acquire phase and watch for them
for|for
control|(
name|String
name|node
range|:
name|nodeNames
control|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|acquire
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Watching for acquire node:"
operator|+
name|znode
argument_list|)
expr_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|znode
argument_list|)
condition|)
block|{
name|coordinator
operator|.
name|memberAcquiredBarrier
argument_list|(
name|procName
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed while creating acquire node:"
operator|+
name|acquire
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendGlobalBarrierReached
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|nodeNames
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|procName
init|=
name|proc
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|reachedNode
init|=
name|zkProc
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
literal|"Creating reached barrier zk node:"
operator|+
name|reachedNode
argument_list|)
expr_stmt|;
try|try
block|{
comment|// create the reached znode and watch for the reached znodes
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|reachedNode
argument_list|)
expr_stmt|;
comment|// loop through all the children of the acquire phase and watch for them
for|for
control|(
name|String
name|node
range|:
name|nodeNames
control|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|reachedNode
argument_list|,
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|ZKUtil
operator|.
name|watchAndCheckExists
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|znode
argument_list|)
condition|)
block|{
name|coordinator
operator|.
name|memberFinishedBarrier
argument_list|(
name|procName
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed while creating reached node:"
operator|+
name|reachedNode
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Delete znodes that are no longer in use.    */
annotation|@
name|Override
specifier|final
specifier|public
name|void
name|resetMembers
parameter_list|(
name|Procedure
name|proc
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|procName
init|=
name|proc
operator|.
name|getName
argument_list|()
decl_stmt|;
name|boolean
name|stillGettingNotifications
init|=
literal|false
decl_stmt|;
do|do
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to clean out zk node for op:"
operator|+
name|procName
argument_list|)
expr_stmt|;
name|zkProc
operator|.
name|clearZNodes
argument_list|(
name|procName
argument_list|)
expr_stmt|;
name|stillGettingNotifications
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NotEmptyException
name|e
parameter_list|)
block|{
comment|// recursive delete isn't transactional (yet) so we need to deal with cases where we get
comment|// children trickling in
name|stillGettingNotifications
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to complete reset procedure "
operator|+
name|procName
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
do|while
condition|(
name|stillGettingNotifications
condition|)
do|;
block|}
comment|/**    * Start monitoring znodes in ZK - subclass hook to start monitoring znodes they are about.    * @return true if succeed, false if encountered initialization errors.    */
specifier|final
specifier|public
name|boolean
name|start
parameter_list|(
specifier|final
name|ProcedureCoordinator
name|coordinator
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|coordinator
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"ZKProcedureCoordinator already started and already has listener installed"
argument_list|)
throw|;
block|}
name|this
operator|.
name|coordinator
operator|=
name|coordinator
expr_stmt|;
try|try
block|{
name|this
operator|.
name|zkProc
operator|=
operator|new
name|ZKProcedureUtil
argument_list|(
name|watcher
argument_list|,
name|procedureType
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
return|return;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Node created: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|logZKTree
argument_list|(
name|this
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
if|if
condition|(
name|isAcquiredPathNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// node wasn't present when we created the watch so zk event triggers acquire
name|coordinator
operator|.
name|memberAcquiredBarrier
argument_list|(
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|,
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isReachedPathNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// node was absent when we created the watch so zk event triggers the finished barrier.
comment|// TODO Nothing enforces that acquire and reached znodes from showing up in wrong order.
name|coordinator
operator|.
name|memberFinishedBarrier
argument_list|(
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|ZKUtil
operator|.
name|getParent
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|,
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isAbortPathNode
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|abort
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
block|}
expr_stmt|;
name|zkProc
operator|.
name|clearChildZNodes
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to start the ZK-based Procedure Coordinator rpcs."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting the controller for procedure member:"
operator|+
name|coordName
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * This is the abort message being sent by the coordinator to member    *    * TODO this code isn't actually used but can be used to issue a cancellation from the    * coordinator.    */
annotation|@
name|Override
specifier|final
specifier|public
name|void
name|sendAbortToMembers
parameter_list|(
name|Procedure
name|proc
parameter_list|,
name|ForeignException
name|ee
parameter_list|)
block|{
name|String
name|procName
init|=
name|proc
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Aborting procedure '"
operator|+
name|procName
operator|+
literal|"' in zk"
argument_list|)
expr_stmt|;
name|String
name|procAbortNode
init|=
name|zkProc
operator|.
name|getAbortZNode
argument_list|(
name|procName
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating abort znode:"
operator|+
name|procAbortNode
argument_list|)
expr_stmt|;
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
name|coordName
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
comment|// first create the znode for the procedure
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|procAbortNode
argument_list|,
name|errorInfo
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished creating abort node:"
operator|+
name|procAbortNode
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
name|zkProc
operator|.
name|logZKTree
argument_list|(
name|zkProc
operator|.
name|baseZNode
argument_list|)
expr_stmt|;
name|coordinator
operator|.
name|rpcConnectionFailure
argument_list|(
literal|"Failed to post zk node:"
operator|+
name|procAbortNode
operator|+
literal|" to abort procedure '"
operator|+
name|procName
operator|+
literal|"'"
argument_list|,
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Receive a notification and propagate it to the local coordinator    * @param abortNode full znode path to the failed procedure information    */
specifier|protected
name|void
name|abort
parameter_list|(
name|String
name|abortNode
parameter_list|)
block|{
name|String
name|procName
init|=
name|ZKUtil
operator|.
name|getNodeName
argument_list|(
name|abortNode
argument_list|)
decl_stmt|;
name|ForeignException
name|ee
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
name|zkProc
operator|.
name|getWatcher
argument_list|()
argument_list|,
name|abortNode
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got an error notification for op:"
operator|+
name|abortNode
operator|+
literal|" but we can't read the information. Killing the procedure."
argument_list|)
expr_stmt|;
comment|// we got a remote exception, but we can't describe it
name|ee
operator|=
operator|new
name|ForeignException
argument_list|(
name|coordName
argument_list|,
literal|"Data in abort node is illegally formatted.  ignoring content."
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
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got an error notification for op:"
operator|+
name|abortNode
operator|+
literal|" but we can't read the information. Killing the procedure."
argument_list|)
expr_stmt|;
comment|// we got a remote exception, but we can't describe it
name|ee
operator|=
operator|new
name|ForeignException
argument_list|(
name|coordName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|coordinator
operator|.
name|rpcConnectionFailure
argument_list|(
literal|"Failed to get data for abort node:"
operator|+
name|abortNode
operator|+
name|zkProc
operator|.
name|getAbortZnode
argument_list|()
argument_list|,
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|coordinator
operator|.
name|abortProcedure
argument_list|(
name|procName
argument_list|,
name|ee
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|final
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|zkProc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Used in testing    */
specifier|final
name|ZKProcedureUtil
name|getZkProcedureUtil
parameter_list|()
block|{
return|return
name|zkProc
return|;
block|}
block|}
end_class

end_unit

