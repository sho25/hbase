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
name|Closeable
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
name|ZooKeeperListener
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
comment|/**  * This is a shared ZooKeeper-based znode management utils for distributed procedure.  All znode  * operations should go through the provided methods in coordinators and members.  *  * Layout of nodes in ZK is  * /hbase/[op name]/acquired/  *                    [op instance] - op data/  *                        /[nodes that have acquired]  *                 /reached/  *                    [op instance]/  *                        /[nodes that have completed]  *                 /abort/  *                    [op instance] - failure data  *  * NOTE: while acquired and completed are znode dirs, abort is actually just a znode.  *  * Assumption here that procedure names are unique  */
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
specifier|abstract
class|class
name|ZKProcedureUtil
extends|extends
name|ZooKeeperListener
implements|implements
name|Closeable
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
name|ZKProcedureUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ACQUIRED_BARRIER_ZNODE_DEFAULT
init|=
literal|"acquired"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REACHED_BARRIER_ZNODE_DEFAULT
init|=
literal|"reached"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ABORT_ZNODE_DEFAULT
init|=
literal|"abort"
decl_stmt|;
specifier|public
specifier|final
name|String
name|baseZNode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|acquiredZnode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|reachedZnode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|abortZnode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|memberName
decl_stmt|;
comment|/**    * Top-level watcher/controller for procedures across the cluster.    *<p>    * On instantiation, this ensures the procedure znodes exist.  This however requires the passed in    *  watcher has been started.    * @param watcher watcher for the cluster ZK. Owned by<tt>this</tt> and closed via    *          {@link #close()}    * @param procDescription name of the znode describing the procedure to run    * @param memberName name of the member from which we are interacting with running procedures    * @throws KeeperException when the procedure znodes cannot be created    */
specifier|public
name|ZKProcedureUtil
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|String
name|procDescription
parameter_list|,
name|String
name|memberName
parameter_list|)
throws|throws
name|KeeperException
block|{
name|super
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|memberName
operator|=
name|memberName
expr_stmt|;
comment|// make sure we are listening for events
name|watcher
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// setup paths for the zknodes used in procedures
name|this
operator|.
name|baseZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|baseZNode
argument_list|,
name|procDescription
argument_list|)
expr_stmt|;
name|acquiredZnode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|ACQUIRED_BARRIER_ZNODE_DEFAULT
argument_list|)
expr_stmt|;
name|reachedZnode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|REACHED_BARRIER_ZNODE_DEFAULT
argument_list|)
expr_stmt|;
name|abortZnode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|baseZNode
argument_list|,
name|ABORT_ZNODE_DEFAULT
argument_list|)
expr_stmt|;
comment|// first make sure all the ZK nodes exist
comment|// make sure all the parents exist (sometimes not the case in tests)
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|watcher
argument_list|,
name|acquiredZnode
argument_list|)
expr_stmt|;
comment|// regular create because all the parents exist
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|watcher
argument_list|,
name|reachedZnode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|watcher
argument_list|,
name|abortZnode
argument_list|)
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
if|if
condition|(
name|watcher
operator|!=
literal|null
condition|)
block|{
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getAcquiredBarrierNode
parameter_list|(
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKProcedureUtil
operator|.
name|getAcquireBarrierNode
argument_list|(
name|this
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
specifier|public
name|String
name|getReachedBarrierNode
parameter_list|(
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKProcedureUtil
operator|.
name|getReachedBarrierNode
argument_list|(
name|this
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAbortZNode
parameter_list|(
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKProcedureUtil
operator|.
name|getAbortNode
argument_list|(
name|this
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAbortZnode
parameter_list|()
block|{
return|return
name|abortZnode
return|;
block|}
specifier|public
name|String
name|getBaseZnode
parameter_list|()
block|{
return|return
name|baseZNode
return|;
block|}
specifier|public
name|String
name|getAcquiredBarrier
parameter_list|()
block|{
return|return
name|acquiredZnode
return|;
block|}
specifier|public
name|String
name|getMemberName
parameter_list|()
block|{
return|return
name|memberName
return|;
block|}
comment|/**    * Get the full znode path for the node used by the coordinator to trigger a global barrier    * acquire on each subprocedure.    * @param controller controller running the procedure    * @param opInstanceName name of the running procedure instance (not the procedure description).    * @return full znode path to the prepare barrier/start node    */
specifier|public
specifier|static
name|String
name|getAcquireBarrierNode
parameter_list|(
name|ZKProcedureUtil
name|controller
parameter_list|,
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|controller
operator|.
name|acquiredZnode
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
comment|/**    * Get the full znode path for the node used by the coordinator to trigger a global barrier    * execution and release on each subprocedure.    * @param controller controller running the procedure    * @param opInstanceName name of the running procedure instance (not the procedure description).    * @return full znode path to the commit barrier    */
specifier|public
specifier|static
name|String
name|getReachedBarrierNode
parameter_list|(
name|ZKProcedureUtil
name|controller
parameter_list|,
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|controller
operator|.
name|reachedZnode
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
comment|/**    * Get the full znode path for the node used by the coordinator or member to trigger an abort    * of the global barrier acquisition or execution in subprocedures.    * @param controller controller running the procedure    * @param opInstanceName name of the running procedure instance (not the procedure description).    * @return full znode path to the abort znode    */
specifier|public
specifier|static
name|String
name|getAbortNode
parameter_list|(
name|ZKProcedureUtil
name|controller
parameter_list|,
name|String
name|opInstanceName
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|controller
operator|.
name|abortZnode
argument_list|,
name|opInstanceName
argument_list|)
return|;
block|}
specifier|public
name|ZooKeeperWatcher
name|getWatcher
parameter_list|()
block|{
return|return
name|watcher
return|;
block|}
comment|/**    * Is this a procedure related znode path?    *    * TODO: this is not strict, can return true if had name just starts with same prefix but is    * different zdir.    *    * @return true if starts with baseZnode    */
name|boolean
name|isInProcedurePath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|startsWith
argument_list|(
name|baseZNode
argument_list|)
return|;
block|}
comment|/**    * Is this the exact procedure barrier acquired znode    */
name|boolean
name|isAcquiredNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|equals
argument_list|(
name|acquiredZnode
argument_list|)
return|;
block|}
comment|/**    * Is this in the procedure barrier acquired znode path    */
name|boolean
name|isAcquiredPathNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|startsWith
argument_list|(
name|this
operator|.
name|acquiredZnode
argument_list|)
operator|&&
operator|!
name|path
operator|.
name|equals
argument_list|(
name|acquiredZnode
argument_list|)
return|;
block|}
comment|/**    * Is this the exact procedure barrier reached znode    */
name|boolean
name|isReachedNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|equals
argument_list|(
name|reachedZnode
argument_list|)
return|;
block|}
comment|/**    * Is this in the procedure barrier reached znode path    */
name|boolean
name|isReachedPathNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|startsWith
argument_list|(
name|this
operator|.
name|reachedZnode
argument_list|)
operator|&&
operator|!
name|path
operator|.
name|equals
argument_list|(
name|reachedZnode
argument_list|)
return|;
block|}
comment|/**    * Is this in the procedure barrier abort znode path    */
name|boolean
name|isAbortNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|equals
argument_list|(
name|abortZnode
argument_list|)
return|;
block|}
comment|/**    * Is this in the procedure barrier abort znode path    */
specifier|public
name|boolean
name|isAbortPathNode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|startsWith
argument_list|(
name|this
operator|.
name|abortZnode
argument_list|)
operator|&&
operator|!
name|path
operator|.
name|equals
argument_list|(
name|abortZnode
argument_list|)
return|;
block|}
comment|// --------------------------------------------------------------------------
comment|// internal debugging methods
comment|// --------------------------------------------------------------------------
comment|/**    * Recursively print the current state of ZK (non-transactional)    * @param root name of the root directory in zk to print    * @throws KeeperException    */
specifier|public
name|void
name|logZKTree
parameter_list|(
name|String
name|root
parameter_list|)
block|{
if|if
condition|(
operator|!
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
return|return;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current zk system:"
argument_list|)
expr_stmt|;
name|String
name|prefix
init|=
literal|"|-"
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|prefix
operator|+
name|root
argument_list|)
expr_stmt|;
try|try
block|{
name|logZKTree
argument_list|(
name|root
argument_list|,
name|prefix
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Helper method to print the current state of the ZK tree.    * @see #logZKTree(String)    * @throws KeeperException if an unexpected exception occurs    */
specifier|protected
name|void
name|logZKTree
parameter_list|(
name|String
name|root
parameter_list|,
name|String
name|prefix
parameter_list|)
throws|throws
name|KeeperException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|watcher
argument_list|,
name|root
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|prefix
operator|+
name|child
argument_list|)
expr_stmt|;
name|String
name|node
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|root
operator|.
name|equals
argument_list|(
literal|"/"
argument_list|)
condition|?
literal|""
else|:
name|root
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|logZKTree
argument_list|(
name|node
argument_list|,
name|prefix
operator|+
literal|"---"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|clearChildZNodes
parameter_list|()
throws|throws
name|KeeperException
block|{
comment|// TODO This is potentially racy since not atomic. update when we support zk that has multi
comment|// If the coordinator was shutdown mid-procedure, then we are going to lose
comment|// an procedure that was previously started by cleaning out all the previous state. Its much
comment|// harder to figure out how to keep an procedure going and the subject of HBASE-5487.
name|ZKUtil
operator|.
name|deleteChildrenRecursively
argument_list|(
name|watcher
argument_list|,
name|acquiredZnode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteChildrenRecursively
argument_list|(
name|watcher
argument_list|,
name|reachedZnode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteChildrenRecursively
argument_list|(
name|watcher
argument_list|,
name|abortZnode
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clearZNodes
parameter_list|(
name|String
name|procedureName
parameter_list|)
throws|throws
name|KeeperException
block|{
comment|// TODO This is potentially racy since not atomic. update when we support zk that has multi
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|watcher
argument_list|,
name|getAcquiredBarrierNode
argument_list|(
name|procedureName
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|watcher
argument_list|,
name|getReachedBarrierNode
argument_list|(
name|procedureName
argument_list|)
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|watcher
argument_list|,
name|getAbortZNode
argument_list|(
name|procedureName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

