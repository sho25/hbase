begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HServerInfo
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
name|client
operator|.
name|Result
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
name|Scan
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
name|ArrayList
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
name|Set
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

begin_comment
comment|/**  * Abstract base class for operations that need to examine all HRegionInfo  * objects in a table. (For a table, operate on each of its rows  * in .META.).  */
end_comment

begin_class
specifier|abstract
class|class
name|TableOperation
implements|implements
name|HConstants
block|{
specifier|private
specifier|final
name|Set
argument_list|<
name|MetaRegion
argument_list|>
name|metaRegions
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
comment|// Do regions in order.
specifier|protected
specifier|final
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|unservedRegions
init|=
operator|new
name|TreeSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|HMaster
name|master
decl_stmt|;
specifier|protected
name|TableOperation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|master
operator|.
name|isMasterRunning
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|()
throw|;
block|}
comment|// add the delimiters.
comment|// TODO maybe check if this is necessary?
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
comment|// Don't wait for META table to come on line if we're enabling it
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
comment|// We can not access any meta region if they have not already been
comment|// assigned and scanned.
if|if
condition|(
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|metaScannerThread
operator|.
name|waitForMetaRegionsOrClose
argument_list|()
condition|)
block|{
comment|// We're shutting down. Forget it.
throw|throw
operator|new
name|MasterNotRunningException
argument_list|()
throw|;
block|}
block|}
name|this
operator|.
name|metaRegions
operator|=
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|getMetaRegionsForTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|ProcessTableOperation
extends|extends
name|RetryableMetaOperation
argument_list|<
name|Boolean
argument_list|>
block|{
name|ProcessTableOperation
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HMaster
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|m
argument_list|,
name|master
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|tableExists
init|=
literal|false
decl_stmt|;
comment|// Open a scanner on the meta region
name|byte
index|[]
name|tableNameMetaStart
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|",,"
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|tableNameMetaStart
argument_list|)
operator|.
name|addFamily
argument_list|(
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|long
name|scannerId
init|=
name|this
operator|.
name|server
operator|.
name|openScanner
argument_list|(
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|int
name|rows
init|=
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.meta.scanner.caching"
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
name|rows
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|emptyRows
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|values
init|=
name|this
operator|.
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
name|isEmpty
argument_list|()
condition|)
block|{
break|break;
block|}
name|HRegionInfo
name|info
init|=
name|this
operator|.
name|master
operator|.
name|getHRegionInfo
argument_list|(
name|values
operator|.
name|getRow
argument_list|()
argument_list|,
name|values
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|emptyRows
operator|.
name|add
argument_list|(
name|values
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CATALOG_FAMILY
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|REGIONINFO_QUALIFIER
argument_list|)
operator|+
literal|" not found on "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|values
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|serverAddress
init|=
name|BaseScanner
operator|.
name|getServerAddress
argument_list|(
name|values
argument_list|)
decl_stmt|;
name|String
name|serverName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serverAddress
operator|!=
literal|null
operator|&&
name|serverAddress
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|long
name|startCode
init|=
name|BaseScanner
operator|.
name|getStartCode
argument_list|(
name|values
argument_list|)
decl_stmt|;
name|serverName
operator|=
name|HServerInfo
operator|.
name|getServerName
argument_list|(
name|serverAddress
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Bytes
operator|.
name|compareTo
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
operator|>
literal|0
condition|)
block|{
break|break;
comment|// Beyond any more entries for this table
block|}
name|tableExists
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isBeingServed
argument_list|(
name|serverName
argument_list|)
operator|||
operator|!
name|isEnabled
argument_list|(
name|info
argument_list|)
condition|)
block|{
name|unservedRegions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
name|processScanItem
argument_list|(
name|serverName
argument_list|,
name|info
argument_list|)
expr_stmt|;
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
name|this
operator|.
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
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"closing scanner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
comment|// Get rid of any rows that have a null HRegionInfo
if|if
condition|(
name|emptyRows
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found "
operator|+
name|emptyRows
operator|.
name|size
argument_list|()
operator|+
literal|" rows with empty HRegionInfo while scanning meta region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|master
operator|.
name|deleteEmptyMetaRows
argument_list|(
name|server
argument_list|,
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|emptyRows
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|tableExists
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
throw|;
block|}
name|postProcessMeta
argument_list|(
name|m
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|unservedRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
block|}
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Prevent meta scanner from running
synchronized|synchronized
init|(
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|metaScannerThread
operator|.
name|scannerLock
init|)
block|{
for|for
control|(
name|MetaRegion
name|m
range|:
name|metaRegions
control|)
block|{
operator|new
name|ProcessTableOperation
argument_list|(
name|m
argument_list|,
name|master
argument_list|)
operator|.
name|doWithRetries
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|boolean
name|isBeingServed
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|HServerInfo
name|s
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getServerInfo
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|result
operator|=
name|s
operator|!=
literal|null
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|protected
name|boolean
name|isEnabled
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
return|return
operator|!
name|info
operator|.
name|isOffline
argument_list|()
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|processScanItem
parameter_list|(
name|String
name|serverName
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|postProcessMeta
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HRegionInterface
name|server
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

