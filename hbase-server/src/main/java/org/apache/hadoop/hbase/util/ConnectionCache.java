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
name|util
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
name|Map
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
name|ConcurrentHashMap
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
name|locks
operator|.
name|Lock
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
name|ChoreService
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
name|ScheduledChore
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
name|Stoppable
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|RegionLocator
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
name|security
operator|.
name|UserProvider
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * A utility to store user specific HConnections in memory.  * There is a chore to clean up connections idle for too long.  * This class is used by REST server and Thrift server to  * support authentication and impersonation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ConnectionCache
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ConnectionCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ConnectionInfo
argument_list|>
name|connections
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|KeyLocker
argument_list|<
name|String
argument_list|>
name|locker
init|=
operator|new
name|KeyLocker
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|realUserName
decl_stmt|;
specifier|private
specifier|final
name|UserGroupInformation
name|realUser
decl_stmt|;
specifier|private
specifier|final
name|UserProvider
name|userProvider
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ChoreService
name|choreService
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|effectiveUserNames
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|String
name|initialValue
parameter_list|()
block|{
return|return
name|realUserName
return|;
block|}
block|}
decl_stmt|;
specifier|public
name|ConnectionCache
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|UserProvider
name|userProvider
parameter_list|,
specifier|final
name|int
name|cleanInterval
parameter_list|,
specifier|final
name|int
name|maxIdleTime
parameter_list|)
throws|throws
name|IOException
block|{
name|Stoppable
name|stoppable
init|=
operator|new
name|Stoppable
argument_list|()
block|{
specifier|private
specifier|volatile
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|isStopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|isStopped
return|;
block|}
block|}
decl_stmt|;
name|this
operator|.
name|choreService
operator|=
operator|new
name|ChoreService
argument_list|(
literal|"ConnectionCache"
argument_list|)
expr_stmt|;
name|ScheduledChore
name|cleaner
init|=
operator|new
name|ScheduledChore
argument_list|(
literal|"ConnectionCleaner"
argument_list|,
name|stoppable
argument_list|,
name|cleanInterval
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ConnectionInfo
argument_list|>
name|entry
range|:
name|connections
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ConnectionInfo
name|connInfo
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|connInfo
operator|.
name|timedOut
argument_list|(
name|maxIdleTime
argument_list|)
condition|)
block|{
if|if
condition|(
name|connInfo
operator|.
name|admin
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connInfo
operator|.
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got exception in closing idle admin"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|connInfo
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got exception in closing idle connection"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
decl_stmt|;
comment|// Start the daemon cleaner chore
name|choreService
operator|.
name|scheduleChore
argument_list|(
name|cleaner
argument_list|)
expr_stmt|;
name|this
operator|.
name|realUser
operator|=
name|userProvider
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|this
operator|.
name|realUserName
operator|=
name|realUser
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
name|this
operator|.
name|userProvider
operator|=
name|userProvider
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Set the current thread local effective user    */
specifier|public
name|void
name|setEffectiveUser
parameter_list|(
name|String
name|user
parameter_list|)
block|{
name|effectiveUserNames
operator|.
name|set
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the current thread local effective user    */
specifier|public
name|String
name|getEffectiveUser
parameter_list|()
block|{
return|return
name|effectiveUserNames
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Called when cache is no longer needed so that it can perform cleanup operations    */
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|choreService
operator|!=
literal|null
condition|)
name|choreService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Caller doesn't close the admin afterwards.    * We need to manage it and close it properly.    */
specifier|public
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
name|ConnectionInfo
name|connInfo
init|=
name|getCurrentConnection
argument_list|()
decl_stmt|;
if|if
condition|(
name|connInfo
operator|.
name|admin
operator|==
literal|null
condition|)
block|{
name|Lock
name|lock
init|=
name|locker
operator|.
name|acquireLock
argument_list|(
name|getEffectiveUser
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|connInfo
operator|.
name|admin
operator|==
literal|null
condition|)
block|{
name|connInfo
operator|.
name|admin
operator|=
name|connInfo
operator|.
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|connInfo
operator|.
name|admin
return|;
block|}
comment|/**    * Caller closes the table afterwards.    */
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|ConnectionInfo
name|connInfo
init|=
name|getCurrentConnection
argument_list|()
decl_stmt|;
return|return
name|connInfo
operator|.
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Retrieve a regionLocator for the table. The user should close the RegionLocator.    */
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getCurrentConnection
argument_list|()
operator|.
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the cached connection for the current user.    * If none or timed out, create a new one.    */
name|ConnectionInfo
name|getCurrentConnection
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|userName
init|=
name|getEffectiveUser
argument_list|()
decl_stmt|;
name|ConnectionInfo
name|connInfo
init|=
name|connections
operator|.
name|get
argument_list|(
name|userName
argument_list|)
decl_stmt|;
if|if
condition|(
name|connInfo
operator|==
literal|null
operator|||
operator|!
name|connInfo
operator|.
name|updateAccessTime
argument_list|()
condition|)
block|{
name|Lock
name|lock
init|=
name|locker
operator|.
name|acquireLock
argument_list|(
name|userName
argument_list|)
decl_stmt|;
try|try
block|{
name|connInfo
operator|=
name|connections
operator|.
name|get
argument_list|(
name|userName
argument_list|)
expr_stmt|;
if|if
condition|(
name|connInfo
operator|==
literal|null
condition|)
block|{
name|UserGroupInformation
name|ugi
init|=
name|realUser
decl_stmt|;
if|if
condition|(
operator|!
name|userName
operator|.
name|equals
argument_list|(
name|realUserName
argument_list|)
condition|)
block|{
name|ugi
operator|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|userName
argument_list|,
name|realUser
argument_list|)
expr_stmt|;
block|}
name|User
name|user
init|=
name|userProvider
operator|.
name|create
argument_list|(
name|ugi
argument_list|)
decl_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|,
name|user
argument_list|)
decl_stmt|;
name|connInfo
operator|=
operator|new
name|ConnectionInfo
argument_list|(
name|conn
argument_list|,
name|userName
argument_list|)
expr_stmt|;
name|connections
operator|.
name|put
argument_list|(
name|userName
argument_list|,
name|connInfo
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|connInfo
return|;
block|}
comment|/**    * Updates the access time for the current connection. Used to keep Connections alive for    * long-lived scanners.    * @return whether we successfully updated the last access time    */
specifier|public
name|boolean
name|updateConnectionAccessTime
parameter_list|()
block|{
name|String
name|userName
init|=
name|getEffectiveUser
argument_list|()
decl_stmt|;
name|ConnectionInfo
name|connInfo
init|=
name|connections
operator|.
name|get
argument_list|(
name|userName
argument_list|)
decl_stmt|;
if|if
condition|(
name|connInfo
operator|!=
literal|null
condition|)
block|{
return|return
name|connInfo
operator|.
name|updateAccessTime
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @return Cluster ID for the HBase cluster or null if there is an err making the connection.    */
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
try|try
block|{
name|ConnectionInfo
name|connInfo
init|=
name|getCurrentConnection
argument_list|()
decl_stmt|;
return|return
name|connInfo
operator|.
name|connection
operator|.
name|getClusterId
argument_list|()
return|;
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
literal|"Error getting connection: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
class|class
name|ConnectionInfo
block|{
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|final
name|String
name|userName
decl_stmt|;
specifier|volatile
name|Admin
name|admin
decl_stmt|;
specifier|private
name|long
name|lastAccessTime
decl_stmt|;
specifier|private
name|boolean
name|closed
decl_stmt|;
name|ConnectionInfo
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|String
name|user
parameter_list|)
block|{
name|lastAccessTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|connection
operator|=
name|conn
expr_stmt|;
name|closed
operator|=
literal|false
expr_stmt|;
name|userName
operator|=
name|user
expr_stmt|;
block|}
specifier|synchronized
name|boolean
name|updateAccessTime
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|connection
operator|.
name|isAborted
argument_list|()
operator|||
name|connection
operator|.
name|isClosed
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unexpected: cached Connection is aborted/closed, removed from cache"
argument_list|)
expr_stmt|;
name|connections
operator|.
name|remove
argument_list|(
name|userName
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|lastAccessTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|synchronized
name|boolean
name|timedOut
parameter_list|(
name|int
name|maxIdleTime
parameter_list|)
block|{
name|long
name|timeoutTime
init|=
name|lastAccessTime
operator|+
name|maxIdleTime
decl_stmt|;
if|if
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|>
name|timeoutTime
condition|)
block|{
name|connections
operator|.
name|remove
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

