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
name|Comparator
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|InterProcessLock
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
name|hbase
operator|.
name|zookeeper
operator|.
name|DeletionListener
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
name|CreateMode
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
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|BadVersionException
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
name|data
operator|.
name|Stat
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
comment|/**  * ZooKeeper based HLock implementation. Based on the Shared Locks recipe.  * (see:  *<a href="http://zookeeper.apache.org/doc/trunk/recipes.html">  * ZooKeeper Recipes and Solutions  *</a>)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ZKInterProcessLockBase
implements|implements
name|InterProcessLock
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
name|ZKInterProcessLockBase
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** ZNode prefix used by processes acquiring reader locks */
specifier|protected
specifier|static
specifier|final
name|String
name|READ_LOCK_CHILD_NODE_PREFIX
init|=
literal|"read-"
decl_stmt|;
comment|/** ZNode prefix used by processes acquiring writer locks */
specifier|protected
specifier|static
specifier|final
name|String
name|WRITE_LOCK_CHILD_NODE_PREFIX
init|=
literal|"write-"
decl_stmt|;
specifier|protected
specifier|final
name|ZooKeeperWatcher
name|zkWatcher
decl_stmt|;
specifier|protected
specifier|final
name|String
name|parentLockNode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|fullyQualifiedZNode
decl_stmt|;
specifier|protected
specifier|final
name|String
name|childZNode
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|metadata
decl_stmt|;
specifier|protected
specifier|final
name|MetadataHandler
name|handler
decl_stmt|;
comment|// If we acquire a lock, update this field
specifier|protected
specifier|final
name|AtomicReference
argument_list|<
name|AcquiredLock
argument_list|>
name|acquiredLock
init|=
operator|new
name|AtomicReference
argument_list|<
name|AcquiredLock
argument_list|>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
comment|/**    * Represents information about a lock held by this thread.    */
specifier|protected
specifier|static
class|class
name|AcquiredLock
block|{
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
specifier|private
specifier|final
name|int
name|version
decl_stmt|;
comment|/**      * Store information about a lock.      * @param path The path to a lock's ZNode      * @param version The current version of the lock's ZNode      */
specifier|public
name|AcquiredLock
parameter_list|(
name|String
name|path
parameter_list|,
name|int
name|version
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|public
name|String
name|getPath
parameter_list|()
block|{
return|return
name|path
return|;
block|}
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AcquiredLockInfo{"
operator|+
literal|"path='"
operator|+
name|path
operator|+
literal|'\''
operator|+
literal|", version="
operator|+
name|version
operator|+
literal|'}'
return|;
block|}
block|}
specifier|protected
specifier|static
class|class
name|ZNodeComparator
implements|implements
name|Comparator
argument_list|<
name|String
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|ZNodeComparator
name|COMPARATOR
init|=
operator|new
name|ZNodeComparator
argument_list|()
decl_stmt|;
specifier|private
name|ZNodeComparator
parameter_list|()
block|{     }
comment|/** Parses sequenceId from the znode name. Zookeeper documentation      * states: The sequence number is always fixed length of 10 digits, 0 padded      */
specifier|public
specifier|static
name|long
name|getChildSequenceId
parameter_list|(
name|String
name|childZNode
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|childZNode
argument_list|)
expr_stmt|;
assert|assert
name|childZNode
operator|.
name|length
argument_list|()
operator|>=
literal|10
assert|;
name|String
name|sequenceIdStr
init|=
name|childZNode
operator|.
name|substring
argument_list|(
name|childZNode
operator|.
name|length
argument_list|()
operator|-
literal|10
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|sequenceIdStr
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|String
name|zNode1
parameter_list|,
name|String
name|zNode2
parameter_list|)
block|{
name|long
name|seq1
init|=
name|getChildSequenceId
argument_list|(
name|zNode1
argument_list|)
decl_stmt|;
name|long
name|seq2
init|=
name|getChildSequenceId
argument_list|(
name|zNode2
argument_list|)
decl_stmt|;
if|if
condition|(
name|seq1
operator|==
name|seq2
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|seq1
operator|<
name|seq2
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
block|}
comment|/**    * Called by implementing classes.    * @param zkWatcher    * @param parentLockNode The lock ZNode path    * @param metadata    * @param handler    * @param childNode The prefix for child nodes created under the parent    */
specifier|protected
name|ZKInterProcessLockBase
parameter_list|(
name|ZooKeeperWatcher
name|zkWatcher
parameter_list|,
name|String
name|parentLockNode
parameter_list|,
name|byte
index|[]
name|metadata
parameter_list|,
name|MetadataHandler
name|handler
parameter_list|,
name|String
name|childNode
parameter_list|)
block|{
name|this
operator|.
name|zkWatcher
operator|=
name|zkWatcher
expr_stmt|;
name|this
operator|.
name|parentLockNode
operator|=
name|parentLockNode
expr_stmt|;
name|this
operator|.
name|fullyQualifiedZNode
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parentLockNode
argument_list|,
name|childNode
argument_list|)
expr_stmt|;
name|this
operator|.
name|metadata
operator|=
name|metadata
expr_stmt|;
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|childZNode
operator|=
name|childNode
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|acquire
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|tryAcquire
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|boolean
name|tryAcquire
parameter_list|(
name|long
name|timeoutMs
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|boolean
name|hasTimeout
init|=
name|timeoutMs
operator|!=
operator|-
literal|1
decl_stmt|;
name|long
name|waitUntilMs
init|=
name|hasTimeout
condition|?
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|timeoutMs
else|:
operator|-
literal|1
decl_stmt|;
name|String
name|createdZNode
decl_stmt|;
try|try
block|{
name|createdZNode
operator|=
name|createLockZNode
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create znode: "
operator|+
name|fullyQualifiedZNode
argument_list|,
name|ex
argument_list|)
throw|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
decl_stmt|;
try|try
block|{
name|children
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|parentLockNode
argument_list|)
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
literal|"Unexpected ZooKeeper error when listing children"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected ZooKeeper exception"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|String
name|pathToWatch
decl_stmt|;
if|if
condition|(
operator|(
name|pathToWatch
operator|=
name|getLockPath
argument_list|(
name|createdZNode
argument_list|,
name|children
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|CountDownLatch
name|deletedLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|zkPathToWatch
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parentLockNode
argument_list|,
name|pathToWatch
argument_list|)
decl_stmt|;
name|DeletionListener
name|deletionListener
init|=
operator|new
name|DeletionListener
argument_list|(
name|zkWatcher
argument_list|,
name|zkPathToWatch
argument_list|,
name|deletedLatch
argument_list|)
decl_stmt|;
name|zkWatcher
operator|.
name|registerListener
argument_list|(
name|deletionListener
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|setWatchIfNodeExists
argument_list|(
name|zkWatcher
argument_list|,
name|zkPathToWatch
argument_list|)
condition|)
block|{
comment|// Wait for the watcher to fire
if|if
condition|(
name|hasTimeout
condition|)
block|{
name|long
name|remainingMs
init|=
name|waitUntilMs
operator|-
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingMs
operator|<
literal|0
operator|||
operator|!
name|deletedLatch
operator|.
name|await
argument_list|(
name|remainingMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to acquire the lock in "
operator|+
name|timeoutMs
operator|+
literal|" milliseconds."
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkWatcher
argument_list|,
name|createdZNode
argument_list|)
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
name|warn
argument_list|(
literal|"Unable to remove ZNode "
operator|+
name|createdZNode
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
name|deletedLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|deletionListener
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|Throwable
name|t
init|=
name|deletionListener
operator|.
name|getException
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception in the watcher"
argument_list|,
name|t
argument_list|)
throw|;
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
literal|"Unexpected ZooKeeper exception"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|zkWatcher
operator|.
name|unregisterListener
argument_list|(
name|deletionListener
argument_list|)
expr_stmt|;
block|}
block|}
name|updateAcquiredLock
argument_list|(
name|createdZNode
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Acquired a lock for "
operator|+
name|createdZNode
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|String
name|createLockZNode
parameter_list|()
throws|throws
name|KeeperException
block|{
try|try
block|{
return|return
name|ZKUtil
operator|.
name|createNodeIfNotExistsNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|fullyQualifiedZNode
argument_list|,
name|metadata
argument_list|,
name|CreateMode
operator|.
name|EPHEMERAL_SEQUENTIAL
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
comment|//create parents, retry
name|ZKUtil
operator|.
name|createWithParents
argument_list|(
name|zkWatcher
argument_list|,
name|parentLockNode
argument_list|)
expr_stmt|;
return|return
name|createLockZNode
argument_list|()
return|;
block|}
block|}
comment|/**    * Check if a child znode represents a read lock.    * @param child The child znode we want to check.    * @return whether the child znode represents a read lock    */
specifier|protected
specifier|static
name|boolean
name|isChildReadLock
parameter_list|(
name|String
name|child
parameter_list|)
block|{
name|int
name|idx
init|=
name|child
operator|.
name|lastIndexOf
argument_list|(
name|ZKUtil
operator|.
name|ZNODE_PATH_SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
name|child
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|suffix
operator|.
name|startsWith
argument_list|(
name|WRITE_LOCK_CHILD_NODE_PREFIX
argument_list|)
return|;
block|}
comment|/**    * Check if a child znode represents a write lock.    * @param child The child znode we want to check.    * @return whether the child znode represents a write lock    */
specifier|protected
specifier|static
name|boolean
name|isChildWriteLock
parameter_list|(
name|String
name|child
parameter_list|)
block|{
name|int
name|idx
init|=
name|child
operator|.
name|lastIndexOf
argument_list|(
name|ZKUtil
operator|.
name|ZNODE_PATH_SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
name|child
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|suffix
operator|.
name|startsWith
argument_list|(
name|WRITE_LOCK_CHILD_NODE_PREFIX
argument_list|)
return|;
block|}
comment|/**    * Check if a child znode represents the same type(read or write) of lock    * @param child The child znode we want to check.    * @return whether the child znode represents the same type(read or write) of lock    */
specifier|protected
name|boolean
name|isChildOfSameType
parameter_list|(
name|String
name|child
parameter_list|)
block|{
name|int
name|idx
init|=
name|child
operator|.
name|lastIndexOf
argument_list|(
name|ZKUtil
operator|.
name|ZNODE_PATH_SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
name|child
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|suffix
operator|.
name|startsWith
argument_list|(
name|this
operator|.
name|childZNode
argument_list|)
return|;
block|}
comment|/**    * Update state as to indicate that a lock is held    * @param createdZNode The lock znode    * @throws IOException If an unrecoverable ZooKeeper error occurs    */
specifier|protected
name|void
name|updateAcquiredLock
parameter_list|(
name|String
name|createdZNode
parameter_list|)
throws|throws
name|IOException
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
literal|null
decl_stmt|;
name|Exception
name|ex
init|=
literal|null
decl_stmt|;
try|try
block|{
name|data
operator|=
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|createdZNode
argument_list|,
name|stat
argument_list|)
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
name|warn
argument_list|(
literal|"Cannot getData for znode:"
operator|+
name|createdZNode
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't acquire a lock on a non-existent node "
operator|+
name|createdZNode
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"ZNode "
operator|+
name|createdZNode
operator|+
literal|"no longer exists!"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|AcquiredLock
name|newLock
init|=
operator|new
name|AcquiredLock
argument_list|(
name|createdZNode
argument_list|,
name|stat
operator|.
name|getVersion
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|acquiredLock
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|newLock
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"The lock "
operator|+
name|fullyQualifiedZNode
operator|+
literal|" has already been acquired by another process!"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|fullyQualifiedZNode
operator|+
literal|" is held by another process"
argument_list|)
throw|;
block|}
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|release
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|AcquiredLock
name|lock
init|=
name|acquiredLock
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|lock
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot release lock"
operator|+
literal|", process does not have a lock for "
operator|+
name|fullyQualifiedZNode
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"No lock held for "
operator|+
name|fullyQualifiedZNode
argument_list|)
throw|;
block|}
try|try
block|{
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkWatcher
argument_list|,
name|lock
operator|.
name|getPath
argument_list|()
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|boolean
name|ret
init|=
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkWatcher
argument_list|,
name|lock
operator|.
name|getPath
argument_list|()
argument_list|,
name|lock
operator|.
name|getVersion
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ret
operator|&&
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zkWatcher
argument_list|,
name|lock
operator|.
name|getPath
argument_list|()
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Couldn't delete "
operator|+
name|lock
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|acquiredLock
operator|.
name|compareAndSet
argument_list|(
name|lock
argument_list|,
literal|null
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Current process no longer holds "
operator|+
name|lock
operator|+
literal|" for "
operator|+
name|fullyQualifiedZNode
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Not holding a lock for "
operator|+
name|fullyQualifiedZNode
operator|+
literal|"!"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Released "
operator|+
name|lock
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|BadVersionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|e
argument_list|)
throw|;
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
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Process metadata stored in a ZNode using a callback    *<p>    * @param lockZNode The node holding the metadata    * @return True if metadata was ready and processed, false otherwise.    */
specifier|protected
name|boolean
name|handleLockMetadata
parameter_list|(
name|String
name|lockZNode
parameter_list|)
block|{
return|return
name|handleLockMetadata
argument_list|(
name|lockZNode
argument_list|,
name|handler
argument_list|)
return|;
block|}
comment|/**    * Process metadata stored in a ZNode using a callback object passed to    * this instance.    *<p>    * @param lockZNode The node holding the metadata    * @param handler the metadata handler    * @return True if metadata was ready and processed, false on exception.    */
specifier|protected
name|boolean
name|handleLockMetadata
parameter_list|(
name|String
name|lockZNode
parameter_list|,
name|MetadataHandler
name|handler
parameter_list|)
block|{
if|if
condition|(
name|handler
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
name|byte
index|[]
name|metadata
init|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkWatcher
argument_list|,
name|lockZNode
argument_list|)
decl_stmt|;
name|handler
operator|.
name|handleMetadata
argument_list|(
name|metadata
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error processing lock metadata in "
operator|+
name|lockZNode
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
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
name|reapExpiredLocks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Will delete all lock znodes of this type (either read or write) which are "expired"    * according to timeout. Assumption is that the clock skew between zookeeper and this servers    * is negligible.    * Referred in zk recipe as "Revocable Shared Locks with Freaking Laser Beams".    * (http://zookeeper.apache.org/doc/trunk/recipes.html).    */
specifier|public
name|void
name|reapExpiredLocks
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
decl_stmt|;
try|try
block|{
name|children
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|parentLockNode
argument_list|)
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
literal|"Unexpected ZooKeeper error when listing children"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected ZooKeeper exception"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|children
operator|==
literal|null
condition|)
return|return;
name|KeeperException
name|deferred
init|=
literal|null
decl_stmt|;
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|long
name|expireDate
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timeout
decl_stmt|;
comment|//we are using cTime in zookeeper
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
name|isChildOfSameType
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parentLockNode
argument_list|,
name|child
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|getDataNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|znode
argument_list|,
name|stat
argument_list|)
expr_stmt|;
if|if
condition|(
name|stat
operator|.
name|getCtime
argument_list|()
operator|<
name|expireDate
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Reaping lock for znode:"
operator|+
name|znode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|zkWatcher
argument_list|,
name|znode
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error reaping the znode for write lock :"
operator|+
name|znode
argument_list|)
expr_stmt|;
name|deferred
operator|=
name|ex
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|deferred
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"ZK exception while reaping locks:"
argument_list|,
name|deferred
argument_list|)
throw|;
block|}
block|}
comment|/**    * Visits the locks (both held and attempted) with the given MetadataHandler.    * @throws InterruptedException If there is an unrecoverable error    */
specifier|public
name|void
name|visitLocks
parameter_list|(
name|MetadataHandler
name|handler
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|children
decl_stmt|;
try|try
block|{
name|children
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|parentLockNode
argument_list|)
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
literal|"Unexpected ZooKeeper error when listing children"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected ZooKeeper exception"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|children
operator|!=
literal|null
operator|&&
name|children
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
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
name|isChildOfSameType
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|String
name|znode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|parentLockNode
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|String
name|childWatchesZNode
init|=
name|getLockPath
argument_list|(
name|child
argument_list|,
name|children
argument_list|)
decl_stmt|;
if|if
condition|(
name|childWatchesZNode
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Lock is held by: "
operator|+
name|child
argument_list|)
expr_stmt|;
block|}
name|handleLockMetadata
argument_list|(
name|znode
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Determine based on a list of children under a ZNode, whether or not a    * process which created a specified ZNode has obtained a lock. If a lock is    * not obtained, return the path that we should watch awaiting its deletion.    * Otherwise, return null.    * This method is abstract as the logic for determining whether or not a    * lock is obtained depends on the type of lock being implemented.    * @param myZNode The ZNode created by the process attempting to acquire    *                a lock    * @param children List of all child ZNodes under the lock's parent ZNode    * @return The path to watch, or null if myZNode can represent a correctly    *         acquired lock.    */
specifier|protected
specifier|abstract
name|String
name|getLockPath
parameter_list|(
name|String
name|myZNode
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|children
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

