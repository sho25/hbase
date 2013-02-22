begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
operator|.
name|lock
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_comment
comment|/**  * ZooKeeper based read lock: does not exclude other read locks, but excludes  * and is excluded by write locks.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKInterProcessReadLock
extends|extends
name|ZKInterProcessLockBase
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
name|ZKInterProcessReadLock
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ZKInterProcessReadLock
parameter_list|(
name|ZooKeeperWatcher
name|zooKeeperWatcher
parameter_list|,
name|String
name|znode
parameter_list|,
name|byte
index|[]
name|metadata
parameter_list|,
name|MetadataHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|zooKeeperWatcher
argument_list|,
name|znode
argument_list|,
name|metadata
argument_list|,
name|handler
argument_list|,
name|READ_LOCK_CHILD_NODE_PREFIX
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|protected
name|String
name|getLockPath
parameter_list|(
name|String
name|createdZNode
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|children
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TreeSet
argument_list|<
name|String
argument_list|>
name|writeChildren
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|ZNodeComparator
operator|.
name|COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
if|if
condition|(
name|isChildWriteLock
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|writeChildren
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|writeChildren
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SortedSet
argument_list|<
name|String
argument_list|>
name|lowerChildren
init|=
name|writeChildren
operator|.
name|headSet
argument_list|(
name|createdZNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowerChildren
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|pathToWatch
init|=
name|lowerChildren
operator|.
name|last
argument_list|()
decl_stmt|;
name|String
name|nodeHoldingLock
init|=
name|lowerChildren
operator|.
name|first
argument_list|()
decl_stmt|;
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parentLockNode
argument_list|,
name|nodeHoldingLock
argument_list|)
decl_stmt|;
try|try
block|{
name|handleLockMetadata
argument_list|(
name|znode
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
name|warn
argument_list|(
literal|"Error processing lock metadata in "
operator|+
name|nodeHoldingLock
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|pathToWatch
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reapAllLocks
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Lock reaping is not supported for ZK based read locks"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

