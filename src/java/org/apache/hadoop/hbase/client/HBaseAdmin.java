begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|HBaseConfiguration
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
name|HColumnDescriptor
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
name|HConstants
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
name|HRegionLocation
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
name|HTableDescriptor
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
name|MasterNotRunningException
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
name|RemoteExceptionHandler
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
name|TableExistsException
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
name|TableNotFoundException
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
name|io
operator|.
name|Cell
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
name|io
operator|.
name|RowResult
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
name|ipc
operator|.
name|HMasterInterface
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
name|ipc
operator|.
name|HRegionInterface
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
name|Writables
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
name|io
operator|.
name|Text
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

begin_comment
comment|/**  * Provides administrative functions for HBase  */
end_comment

begin_class
specifier|public
class|class
name|HBaseAdmin
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|long
name|pause
decl_stmt|;
specifier|private
specifier|final
name|int
name|numRetries
decl_stmt|;
specifier|private
specifier|volatile
name|HMasterInterface
name|master
decl_stmt|;
comment|/**    * Constructor    *     * @param conf Configuration object    * @throws MasterNotRunningException    */
specifier|public
name|HBaseAdmin
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
name|this
operator|.
name|connection
operator|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|pause
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|30
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|numRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|connection
operator|.
name|getMaster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return proxy connection to master server for this instance    * @throws MasterNotRunningException    */
specifier|public
name|HMasterInterface
name|getMaster
parameter_list|()
throws|throws
name|MasterNotRunningException
block|{
return|return
name|this
operator|.
name|connection
operator|.
name|getMaster
argument_list|()
return|;
block|}
comment|/** @return - true if the master server is running */
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
operator|.
name|isMasterRunning
argument_list|()
return|;
block|}
comment|/**    * @param tableName Table to check.    * @return True if table exists already.    * @throws MasterNotRunningException    */
specifier|public
name|boolean
name|tableExists
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
return|return
name|tableExists
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param tableName Table to check.    * @return True if table exists already.    * @throws MasterNotRunningException    */
specifier|public
name|boolean
name|tableExists
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
return|return
name|tableExists
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param tableName Table to check.    * @return True if table exists already.    * @throws MasterNotRunningException    */
specifier|public
name|boolean
name|tableExists
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
return|return
name|connection
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
return|;
block|}
comment|/**    * List all the userspace tables.  In other words, scan the META table.    *    * If we wanted this to be really fast, we could implement a special    * catalog table that just contains table names and their descriptors.    * Right now, it only exists as part of the META table's region info.    *    * @return - returns an array of HTableDescriptors     * @throws IOException    */
specifier|public
name|HTableDescriptor
index|[]
name|listTables
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|connection
operator|.
name|listTables
argument_list|()
return|;
block|}
comment|/**    * Creates a new table    *     * @param desc table descriptor for table    *     * @throws IllegalArgumentException if the table name is reserved    * @throws MasterNotRunningException if master is not running    * @throws TableExistsException if table already exists (If concurrent    * threads, the table may have been created between test-for-existence    * and attempt-at-creation).    * @throws IOException    */
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|createTableAsync
argument_list|(
name|desc
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
name|tries
operator|<
name|numRetries
condition|;
name|tries
operator|++
control|)
block|{
try|try
block|{
comment|// Wait for new table to come on-line
name|connection
operator|.
name|locateRegion
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|e
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|==
name|numRetries
operator|-
literal|1
condition|)
block|{
comment|// Ran out of tries
throw|throw
name|e
throw|;
block|}
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
comment|/**    * Creates a new table but does not block and wait for it to come online.    *     * @param desc table descriptor for table    *     * @throws IllegalArgumentException Bad table name.    * @throws MasterNotRunningException if master is not running    * @throws TableExistsException if table already exists (If concurrent    * threads, the table may have been created between test-for-existence    * and attempt-at-creation).    * @throws IOException    */
specifier|public
name|void
name|createTableAsync
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Deletes a table    *     * @param tableName name of table to delete    * @throws IOException    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteTable
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes a table    *     * @param tableName name of table to delete    * @throws IOException    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes a table    *     * @param tableName name of table to delete    * @throws IOException    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HRegionLocation
name|firstMetaServer
init|=
name|getFirstMetaServerForTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Wait until first region is deleted
name|HRegionInterface
name|server
init|=
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|firstMetaServer
operator|.
name|getServerAddress
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
name|tries
operator|<
name|numRetries
condition|;
name|tries
operator|++
control|)
block|{
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
try|try
block|{
name|scannerId
operator|=
name|server
operator|.
name|openScanner
argument_list|(
name|firstMetaServer
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO_ARRAY
argument_list|,
name|tableName
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|RowResult
name|values
init|=
name|server
operator|.
name|next
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
operator|||
name|values
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|e
range|:
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
condition|)
block|{
name|info
operator|=
operator|(
name|HRegionInfo
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|info
argument_list|)
expr_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|==
name|numRetries
operator|-
literal|1
condition|)
block|{
comment|// no more tries left
if|if
condition|(
name|ex
operator|instanceof
name|RemoteException
condition|)
block|{
name|ex
operator|=
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
operator|(
name|RemoteException
operator|)
name|ex
argument_list|)
expr_stmt|;
block|}
throw|throw
name|ex
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|scannerId
operator|!=
operator|-
literal|1L
condition|)
block|{
try|try
block|{
name|server
operator|.
name|close
argument_list|(
name|scannerId
argument_list|)
expr_stmt|;
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
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Brings a table on-line (enables it)    *     * @param tableName name of the table    * @throws IOException    */
specifier|public
name|void
name|enableTable
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|enableTable
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Brings a table on-line (enables it)    *     * @param tableName name of the table    * @throws IOException    */
specifier|public
name|void
name|enableTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|enableTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Brings a table on-line (enables it)    *     * @param tableName name of the table    * @throws IOException    */
specifier|public
name|void
name|enableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
try|try
block|{
name|this
operator|.
name|master
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Wait until all regions are enabled
while|while
condition|(
operator|!
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
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
literal|"Sleep. Waiting for all regions to be enabled from "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
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
literal|"Wake. Waiting for all regions to be enabled from "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabled table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disables a table (takes it off-line) If it is being served, the master    * will tell the servers to stop serving it.    *     * @param tableName name of table    * @throws IOException    */
specifier|public
name|void
name|disableTable
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|disableTable
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disables a table (takes it off-line) If it is being served, the master    * will tell the servers to stop serving it.    *     * @param tableName name of table    * @throws IOException    */
specifier|public
name|void
name|disableTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|disableTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disables a table (takes it off-line) If it is being served, the master    * will tell the servers to stop serving it.    *     * @param tableName name of table    * @throws IOException    */
specifier|public
name|void
name|disableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
try|try
block|{
name|this
operator|.
name|master
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Wait until all regions are disabled
while|while
condition|(
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
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
literal|"Sleep. Waiting for all regions to be disabled from "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
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
literal|"Wake. Waiting for all regions to be disabled from "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabled "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if table is on-line    * @throws IOException    */
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|isTableEnabled
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if table is on-line    * @throws IOException    */
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|isTableEnabled
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if table is on-line    * @throws IOException    */
specifier|public
name|boolean
name|isTableEnabled
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Add a column to an existing table    *     * @param tableName name of the table to add column to    * @param column column descriptor of column to be added    * @throws IOException    */
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|addColumn
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a column to an existing table    *     * @param tableName name of the table to add column to    * @param column column descriptor of column to be added    * @throws IOException    */
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a column to an existing table    *     * @param tableName name of the table to add column to    * @param column column descriptor of column to be added    * @throws IOException    */
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|addColumn
argument_list|(
name|tableName
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Delete a column from a table    *     * @param tableName name of table    * @param columnName name of column to be deleted    * @throws IOException    */
specifier|public
name|void
name|deleteColumn
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|,
specifier|final
name|Text
name|columnName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteColumn
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|columnName
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete a column from a table    *     * @param tableName name of table    * @param columnName name of column to be deleted    * @throws IOException    */
specifier|public
name|void
name|deleteColumn
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|String
name|columnName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete a column from a table    *     * @param tableName name of table    * @param columnName name of column to be deleted    * @throws IOException    */
specifier|public
name|void
name|deleteColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|columnName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|deleteColumn
argument_list|(
name|tableName
argument_list|,
name|columnName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Modify an existing column family on a table    *     * @param tableName name of table    * @param columnName name of column to be modified    * @param descriptor new column descriptor to use    * @throws IOException    */
specifier|public
name|void
name|modifyColumn
parameter_list|(
specifier|final
name|Text
name|tableName
parameter_list|,
specifier|final
name|Text
name|columnName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|modifyColumn
argument_list|(
name|tableName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|columnName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Modify an existing column family on a table    *     * @param tableName name of table    * @param columnName name of column to be modified    * @param descriptor new column descriptor to use    * @throws IOException    */
specifier|public
name|void
name|modifyColumn
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|String
name|columnName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|modifyColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnName
argument_list|)
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Modify an existing column family on a table    *     * @param tableName name of table    * @param columnName name of column to be modified    * @param descriptor new column descriptor to use    * @throws IOException    */
specifier|public
name|void
name|modifyColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|columnName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|modifyColumn
argument_list|(
name|tableName
argument_list|,
name|columnName
argument_list|,
name|descriptor
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Modify a table's HTableDescriptor    *     * @param tableName name of table    * @param desc the updated descriptor    * @throws IOException    */
specifier|public
name|void
name|modifyTableMeta
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
name|HTableDescriptor
operator|.
name|isLegalTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|master
operator|.
name|modifyTableMeta
argument_list|(
name|tableName
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**     * Shuts down the HBase instance     * @throws IOException    */
specifier|public
specifier|synchronized
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|master
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|(
literal|"master has been shut down"
argument_list|)
throw|;
block|}
try|try
block|{
name|this
operator|.
name|master
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|e
parameter_list|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|this
operator|.
name|master
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|HRegionLocation
name|getFirstMetaServerForTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|locateRegion
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|HConstants
operator|.
name|NINES
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check to see if HBase is running. Throw an exception if not.    *    * @param conf    * @throws MasterNotRunningException    */
specifier|public
specifier|static
name|void
name|checkHBaseAvailable
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|MasterNotRunningException
block|{
name|HBaseConfiguration
name|copyOfConf
init|=
operator|new
name|HBaseConfiguration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|copyOfConf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
operator|new
name|HBaseAdmin
argument_list|(
name|copyOfConf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

