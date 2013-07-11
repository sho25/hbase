begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|InterruptedIOException
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
name|conf
operator|.
name|Configuration
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
name|InterProcessLock
operator|.
name|MetadataHandler
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
name|InterProcessReadWriteLock
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
name|exceptions
operator|.
name|LockTimeoutException
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|lock
operator|.
name|ZKInterProcessReadWriteLock
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
name|ByteString
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
comment|/**  * A manager for distributed table level locks.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|TableLockManager
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
name|TableLockManager
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Configuration key for enabling table-level locks for schema changes */
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_LOCK_ENABLE
init|=
literal|"hbase.table.lock.enable"
decl_stmt|;
comment|/** by default we should enable table-level locks for schema changes */
specifier|private
specifier|static
specifier|final
name|boolean
name|DEFAULT_TABLE_LOCK_ENABLE
init|=
literal|true
decl_stmt|;
comment|/** Configuration key for time out for trying to acquire table locks */
specifier|protected
specifier|static
specifier|final
name|String
name|TABLE_WRITE_LOCK_TIMEOUT_MS
init|=
literal|"hbase.table.write.lock.timeout.ms"
decl_stmt|;
comment|/** Configuration key for time out for trying to acquire table locks */
specifier|protected
specifier|static
specifier|final
name|String
name|TABLE_READ_LOCK_TIMEOUT_MS
init|=
literal|"hbase.table.read.lock.timeout.ms"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAULT_TABLE_WRITE_LOCK_TIMEOUT_MS
init|=
literal|600
operator|*
literal|1000
decl_stmt|;
comment|//10 min default
specifier|protected
specifier|static
specifier|final
name|long
name|DEFAULT_TABLE_READ_LOCK_TIMEOUT_MS
init|=
literal|600
operator|*
literal|1000
decl_stmt|;
comment|//10 min default
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_LOCK_EXPIRE_TIMEOUT
init|=
literal|"hbase.table.lock.expire.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_TABLE_LOCK_EXPIRE_TIMEOUT_MS
init|=
literal|600
operator|*
literal|1000
decl_stmt|;
comment|//10 min default
comment|/**    * A distributed lock for a table.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|TableLock
block|{
comment|/**      * Acquire the lock, with the configured lock timeout.      * @throws LockTimeoutException If unable to acquire a lock within a specified      * time period (if any)      * @throws IOException If unrecoverable error occurs      */
name|void
name|acquire
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Release the lock already held.      * @throws IOException If there is an unrecoverable error releasing the lock      */
name|void
name|release
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Returns a TableLock for locking the table for exclusive access    * @param tableName Table to lock    * @param purpose Human readable reason for locking the table    * @return A new TableLock object for acquiring a write lock    */
specifier|public
specifier|abstract
name|TableLock
name|writeLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
function_decl|;
comment|/**    * Returns a TableLock for locking the table for shared access among read-lock holders    * @param tableName Table to lock    * @param purpose Human readable reason for locking the table    * @return A new TableLock object for acquiring a read lock    */
specifier|public
specifier|abstract
name|TableLock
name|readLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
function_decl|;
comment|/**    * Visits all table locks(read and write), and lock attempts with the given callback    * MetadataHandler.    * @param handler the metadata handler to call    * @throws IOException If there is an unrecoverable error    */
specifier|public
specifier|abstract
name|void
name|visitAllLocks
parameter_list|(
name|MetadataHandler
name|handler
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Force releases all table locks(read and write) that have been held longer than    * "hbase.table.lock.expire.ms". Assumption is that the clock skew between zookeeper    * and this servers is negligible.    * The behavior of the lock holders still thinking that they have the lock is undefined.    * @throws IOException If there is an unrecoverable error    */
specifier|public
specifier|abstract
name|void
name|reapAllExpiredLocks
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Force releases table write locks and lock attempts even if this thread does    * not own the lock. The behavior of the lock holders still thinking that they    * have the lock is undefined. This should be used carefully and only when    * we can ensure that all write-lock holders have died. For example if only    * the master can hold write locks, then we can reap it's locks when the backup    * master starts.    * @throws IOException If there is an unrecoverable error    */
specifier|public
specifier|abstract
name|void
name|reapWriteLocks
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a table has been deleted, and after the table lock is  released.    * TableLockManager should do cleanup for the table state.    * @param tableName name of the table    * @throws IOException If there is an unrecoverable error releasing the lock    */
specifier|public
specifier|abstract
name|void
name|tableDeleted
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates and returns a TableLockManager according to the configuration    */
specifier|public
specifier|static
name|TableLockManager
name|createTableLockManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ZooKeeperWatcher
name|zkWatcher
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
comment|// Initialize table level lock manager for schema changes, if enabled.
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|TABLE_LOCK_ENABLE
argument_list|,
name|DEFAULT_TABLE_LOCK_ENABLE
argument_list|)
condition|)
block|{
name|long
name|writeLockTimeoutMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|TABLE_WRITE_LOCK_TIMEOUT_MS
argument_list|,
name|DEFAULT_TABLE_WRITE_LOCK_TIMEOUT_MS
argument_list|)
decl_stmt|;
name|long
name|readLockTimeoutMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|TABLE_READ_LOCK_TIMEOUT_MS
argument_list|,
name|DEFAULT_TABLE_READ_LOCK_TIMEOUT_MS
argument_list|)
decl_stmt|;
name|long
name|lockExpireTimeoutMs
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|TABLE_LOCK_EXPIRE_TIMEOUT
argument_list|,
name|DEFAULT_TABLE_LOCK_EXPIRE_TIMEOUT_MS
argument_list|)
decl_stmt|;
return|return
operator|new
name|ZKTableLockManager
argument_list|(
name|zkWatcher
argument_list|,
name|serverName
argument_list|,
name|writeLockTimeoutMs
argument_list|,
name|readLockTimeoutMs
argument_list|,
name|lockExpireTimeoutMs
argument_list|)
return|;
block|}
return|return
operator|new
name|NullTableLockManager
argument_list|()
return|;
block|}
comment|/**    * A null implementation    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|NullTableLockManager
extends|extends
name|TableLockManager
block|{
specifier|static
class|class
name|NullTableLock
implements|implements
name|TableLock
block|{
annotation|@
name|Override
specifier|public
name|void
name|acquire
parameter_list|()
throws|throws
name|IOException
block|{       }
annotation|@
name|Override
specifier|public
name|void
name|release
parameter_list|()
throws|throws
name|IOException
block|{       }
block|}
annotation|@
name|Override
specifier|public
name|TableLock
name|writeLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
block|{
return|return
operator|new
name|NullTableLock
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableLock
name|readLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
block|{
return|return
operator|new
name|NullTableLock
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reapAllExpiredLocks
parameter_list|()
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|reapWriteLocks
parameter_list|()
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|tableDeleted
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|visitAllLocks
parameter_list|(
name|MetadataHandler
name|handler
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
comment|/** Public for hbck */
specifier|public
specifier|static
name|ZooKeeperProtos
operator|.
name|TableLock
name|fromBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|<
name|pblen
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|ZooKeeperProtos
operator|.
name|TableLock
name|data
init|=
name|ZooKeeperProtos
operator|.
name|TableLock
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|bytes
argument_list|,
name|pblen
argument_list|,
name|bytes
operator|.
name|length
operator|-
name|pblen
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|data
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception in deserialization"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * ZooKeeper based TableLockManager    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|private
specifier|static
class|class
name|ZKTableLockManager
extends|extends
name|TableLockManager
block|{
specifier|private
specifier|static
specifier|final
name|MetadataHandler
name|METADATA_HANDLER
init|=
operator|new
name|MetadataHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleMetadata
parameter_list|(
name|byte
index|[]
name|ownerMetadata
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
block|{
return|return;
block|}
name|ZooKeeperProtos
operator|.
name|TableLock
name|data
init|=
name|fromBytes
argument_list|(
name|ownerMetadata
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table is locked by "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"[tableName=%s, lockOwner=%s, threadId=%s, "
operator|+
literal|"purpose=%s, isShared=%s, createTime=%s]"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|data
operator|.
name|getTableName
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
operator|.
name|getLockOwner
argument_list|()
argument_list|)
argument_list|,
name|data
operator|.
name|getThreadId
argument_list|()
argument_list|,
name|data
operator|.
name|getPurpose
argument_list|()
argument_list|,
name|data
operator|.
name|getIsShared
argument_list|()
argument_list|,
name|data
operator|.
name|getCreateTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
class|class
name|TableLockImpl
implements|implements
name|TableLock
block|{
name|long
name|lockTimeoutMs
decl_stmt|;
name|byte
index|[]
name|tableName
decl_stmt|;
name|String
name|tableNameStr
decl_stmt|;
name|InterProcessLock
name|lock
decl_stmt|;
name|boolean
name|isShared
decl_stmt|;
name|ZooKeeperWatcher
name|zkWatcher
decl_stmt|;
name|ServerName
name|serverName
decl_stmt|;
name|String
name|purpose
decl_stmt|;
specifier|public
name|TableLockImpl
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|ZooKeeperWatcher
name|zkWatcher
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|long
name|lockTimeoutMs
parameter_list|,
name|boolean
name|isShared
parameter_list|,
name|String
name|purpose
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|tableNameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|zkWatcher
operator|=
name|zkWatcher
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|lockTimeoutMs
operator|=
name|lockTimeoutMs
expr_stmt|;
name|this
operator|.
name|isShared
operator|=
name|isShared
expr_stmt|;
name|this
operator|.
name|purpose
operator|=
name|purpose
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|acquire
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Attempt to acquire table "
operator|+
operator|(
name|isShared
condition|?
literal|"read"
else|:
literal|"write"
operator|)
operator|+
literal|" lock on: "
operator|+
name|tableNameStr
operator|+
literal|" for:"
operator|+
name|purpose
argument_list|)
expr_stmt|;
block|}
name|lock
operator|=
name|createTableLock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|lockTimeoutMs
operator|==
operator|-
literal|1
condition|)
block|{
comment|// Wait indefinitely
name|lock
operator|.
name|acquire
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|lock
operator|.
name|tryAcquire
argument_list|(
name|lockTimeoutMs
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|LockTimeoutException
argument_list|(
literal|"Timed out acquiring "
operator|+
operator|(
name|isShared
condition|?
literal|"read"
else|:
literal|"write"
operator|)
operator|+
literal|"lock for table:"
operator|+
name|tableNameStr
operator|+
literal|"for:"
operator|+
name|purpose
operator|+
literal|" after "
operator|+
name|lockTimeoutMs
operator|+
literal|" ms."
argument_list|)
throw|;
block|}
block|}
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
literal|"Interrupted acquiring a lock for "
operator|+
name|tableNameStr
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
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
literal|"Interrupted acquiring a lock"
argument_list|)
throw|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|"Acquired table "
operator|+
operator|(
name|isShared
condition|?
literal|"read"
else|:
literal|"write"
operator|)
operator|+
literal|" lock on "
operator|+
name|tableNameStr
operator|+
literal|" for "
operator|+
name|purpose
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|release
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Attempt to release table "
operator|+
operator|(
name|isShared
condition|?
literal|"read"
else|:
literal|"write"
operator|)
operator|+
literal|" lock on "
operator|+
name|tableNameStr
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lock
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table "
operator|+
name|tableNameStr
operator|+
literal|" is not locked!"
argument_list|)
throw|;
block|}
try|try
block|{
name|lock
operator|.
name|release
argument_list|()
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
literal|"Interrupted while releasing a lock for "
operator|+
name|tableNameStr
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
name|InterruptedIOException
argument_list|()
throw|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Released table lock on "
operator|+
name|tableNameStr
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|InterProcessLock
name|createTableLock
parameter_list|()
block|{
name|String
name|tableLockZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|tableNameStr
argument_list|)
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|TableLock
name|data
init|=
name|ZooKeeperProtos
operator|.
name|TableLock
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|tableName
argument_list|)
argument_list|)
operator|.
name|setLockOwner
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
operator|.
name|setThreadId
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|setPurpose
argument_list|(
name|purpose
argument_list|)
operator|.
name|setIsShared
argument_list|(
name|isShared
argument_list|)
operator|.
name|setCreateTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|byte
index|[]
name|lockMetadata
init|=
name|toBytes
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|InterProcessReadWriteLock
name|lock
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher
argument_list|,
name|tableLockZNode
argument_list|,
name|METADATA_HANDLER
argument_list|)
decl_stmt|;
return|return
name|isShared
condition|?
name|lock
operator|.
name|readLock
argument_list|(
name|lockMetadata
argument_list|)
else|:
name|lock
operator|.
name|writeLock
argument_list|(
name|lockMetadata
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
name|ZooKeeperProtos
operator|.
name|TableLock
name|data
parameter_list|)
block|{
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|data
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zkWatcher
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeLockTimeoutMs
decl_stmt|;
specifier|private
specifier|final
name|long
name|readLockTimeoutMs
decl_stmt|;
specifier|private
specifier|final
name|long
name|lockExpireTimeoutMs
decl_stmt|;
comment|/**      * Initialize a new manager for table-level locks.      * @param zkWatcher      * @param serverName Address of the server responsible for acquiring and      * releasing the table-level locks      * @param writeLockTimeoutMs Timeout (in milliseconds) for acquiring a write lock for a      * given table, or -1 for no timeout      * @param readLockTimeoutMs Timeout (in milliseconds) for acquiring a read lock for a      * given table, or -1 for no timeout      */
specifier|public
name|ZKTableLockManager
parameter_list|(
name|ZooKeeperWatcher
name|zkWatcher
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|long
name|writeLockTimeoutMs
parameter_list|,
name|long
name|readLockTimeoutMs
parameter_list|,
name|long
name|lockExpireTimeoutMs
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
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|writeLockTimeoutMs
operator|=
name|writeLockTimeoutMs
expr_stmt|;
name|this
operator|.
name|readLockTimeoutMs
operator|=
name|readLockTimeoutMs
expr_stmt|;
name|this
operator|.
name|lockExpireTimeoutMs
operator|=
name|lockExpireTimeoutMs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableLock
name|writeLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
block|{
return|return
operator|new
name|TableLockImpl
argument_list|(
name|tableName
argument_list|,
name|zkWatcher
argument_list|,
name|serverName
argument_list|,
name|writeLockTimeoutMs
argument_list|,
literal|false
argument_list|,
name|purpose
argument_list|)
return|;
block|}
specifier|public
name|TableLock
name|readLock
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|purpose
parameter_list|)
block|{
return|return
operator|new
name|TableLockImpl
argument_list|(
name|tableName
argument_list|,
name|zkWatcher
argument_list|,
name|serverName
argument_list|,
name|readLockTimeoutMs
argument_list|,
literal|true
argument_list|,
name|purpose
argument_list|)
return|;
block|}
specifier|public
name|void
name|visitAllLocks
parameter_list|(
name|MetadataHandler
name|handler
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|tableName
range|:
name|getTableNames
argument_list|()
control|)
block|{
name|String
name|tableLockZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|ZKInterProcessReadWriteLock
name|lock
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher
argument_list|,
name|tableLockZNode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|lock
operator|.
name|readLock
argument_list|(
literal|null
argument_list|)
operator|.
name|visitLocks
argument_list|(
name|handler
argument_list|)
expr_stmt|;
name|lock
operator|.
name|writeLock
argument_list|(
literal|null
argument_list|)
operator|.
name|visitLocks
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getTableNames
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
decl_stmt|;
try|try
block|{
name|tableNames
operator|=
name|ZKUtil
operator|.
name|listChildrenNoWatch
argument_list|(
name|zkWatcher
argument_list|,
name|zkWatcher
operator|.
name|tableLockZNode
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
return|return
name|tableNames
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reapWriteLocks
parameter_list|()
throws|throws
name|IOException
block|{
comment|//get the table names
try|try
block|{
for|for
control|(
name|String
name|tableName
range|:
name|getTableNames
argument_list|()
control|)
block|{
name|String
name|tableLockZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|ZKInterProcessReadWriteLock
name|lock
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher
argument_list|,
name|tableLockZNode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|lock
operator|.
name|writeLock
argument_list|(
literal|null
argument_list|)
operator|.
name|reapAllLocks
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught exception while reaping table write locks"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|reapAllExpiredLocks
parameter_list|()
throws|throws
name|IOException
block|{
comment|//get the table names
try|try
block|{
for|for
control|(
name|String
name|tableName
range|:
name|getTableNames
argument_list|()
control|)
block|{
name|String
name|tableLockZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|ZKInterProcessReadWriteLock
name|lock
init|=
operator|new
name|ZKInterProcessReadWriteLock
argument_list|(
name|zkWatcher
argument_list|,
name|tableLockZNode
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|lock
operator|.
name|readLock
argument_list|(
literal|null
argument_list|)
operator|.
name|reapExpiredLocks
argument_list|(
name|lockExpireTimeoutMs
argument_list|)
expr_stmt|;
name|lock
operator|.
name|writeLock
argument_list|(
literal|null
argument_list|)
operator|.
name|reapExpiredLocks
argument_list|(
name|lockExpireTimeoutMs
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
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
annotation|@
name|Override
specifier|public
name|void
name|tableDeleted
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|//table write lock from DeleteHandler is already released, just delete the parent znode
name|String
name|tableNameStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|tableLockZNode
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkWatcher
operator|.
name|tableLockZNode
argument_list|,
name|tableNameStr
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|zkWatcher
argument_list|,
name|tableLockZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
if|if
condition|(
name|ex
operator|.
name|code
argument_list|()
operator|==
name|KeeperException
operator|.
name|Code
operator|.
name|NOTEMPTY
condition|)
block|{
comment|//we might get this in rare occasions where a CREATE table or some other table operation
comment|//is waiting to acquire the lock. In this case, parent znode won't be deleted.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not delete the znode for table locks because NOTEMPTY: "
operator|+
name|tableLockZNode
argument_list|)
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

