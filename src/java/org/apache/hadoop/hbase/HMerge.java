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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|hbase
operator|.
name|client
operator|.
name|HConnection
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
name|HConnectionManager
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
name|HTable
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
name|BatchUpdate
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
name|regionserver
operator|.
name|HLog
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
name|regionserver
operator|.
name|HRegion
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

begin_comment
comment|/**   * A non-instantiable class that has a static method capable of compacting  * a table by merging adjacent regions.  */
end_comment

begin_class
class|class
name|HMerge
implements|implements
name|HConstants
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HMerge
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|/*    * Not instantiable    */
specifier|private
name|HMerge
parameter_list|()
block|{}
comment|/**    * Scans the table and merges two adjacent regions if they are small. This    * only happens when a lot of rows are deleted.    *     * When merging the META region, the HBase instance must be offline.    * When merging a normal table, the HBase instance must be online, but the    * table must be disabled.     *     * @param conf        - configuration object for HBase    * @param fs          - FileSystem where regions reside    * @param tableName   - Table to be compacted    * @throws IOException    */
specifier|public
specifier|static
name|void
name|merge
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|masterIsRunning
init|=
name|connection
operator|.
name|isMasterRunning
argument_list|()
decl_stmt|;
name|HConnectionManager
operator|.
name|deleteConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
name|masterIsRunning
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can not compact META table if instance is on-line"
argument_list|)
throw|;
block|}
operator|new
name|OfflineMerger
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|)
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|masterIsRunning
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"HBase instance must be running to merge a normal table"
argument_list|)
throw|;
block|}
operator|new
name|OnlineMerger
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableName
argument_list|)
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|Merger
block|{
specifier|protected
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|tabledir
decl_stmt|;
specifier|protected
specifier|final
name|HLog
name|hlog
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxFilesize
decl_stmt|;
specifier|protected
name|Merger
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|maxFilesize
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
name|DEFAULT_MAX_FILE_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|tabledir
operator|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HBASE_DIR
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|tabledir
argument_list|,
literal|"merge_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|this
operator|.
name|hlog
operator|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|logdir
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
for|for
control|(
name|HRegionInfo
index|[]
name|regionsToMerge
init|=
name|next
argument_list|()
init|;
name|regionsToMerge
operator|!=
literal|null
condition|;
name|regionsToMerge
operator|=
name|next
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|merge
argument_list|(
name|regionsToMerge
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
block|}
finally|finally
block|{
try|try
block|{
name|hlog
operator|.
name|closeAndDelete
argument_list|()
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
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|boolean
name|merge
parameter_list|(
specifier|final
name|HRegionInfo
index|[]
name|info
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|info
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"only one region - nothing to merge"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|HRegion
name|currentRegion
init|=
literal|null
decl_stmt|;
name|long
name|currentSize
init|=
literal|0
decl_stmt|;
name|HRegion
name|nextRegion
init|=
literal|null
decl_stmt|;
name|long
name|nextSize
init|=
literal|0
decl_stmt|;
name|Text
name|midKey
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|info
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|currentRegion
operator|==
literal|null
condition|)
block|{
name|currentRegion
operator|=
operator|new
name|HRegion
argument_list|(
name|tabledir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
index|[
name|i
index|]
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|currentSize
operator|=
name|currentRegion
operator|.
name|largestHStore
argument_list|(
name|midKey
argument_list|)
operator|.
name|getAggregate
argument_list|()
expr_stmt|;
block|}
name|nextRegion
operator|=
operator|new
name|HRegion
argument_list|(
name|tabledir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|info
index|[
name|i
operator|+
literal|1
index|]
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|nextSize
operator|=
name|nextRegion
operator|.
name|largestHStore
argument_list|(
name|midKey
argument_list|)
operator|.
name|getAggregate
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|currentSize
operator|+
name|nextSize
operator|)
operator|<=
operator|(
name|maxFilesize
operator|/
literal|2
operator|)
condition|)
block|{
comment|// We merge two adjacent regions if their total size is less than
comment|// one half of the desired maximum size
name|LOG
operator|.
name|info
argument_list|(
literal|"merging regions "
operator|+
name|currentRegion
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" and "
operator|+
name|nextRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
name|mergedRegion
init|=
name|HRegion
operator|.
name|mergeAdjacent
argument_list|(
name|currentRegion
argument_list|,
name|nextRegion
argument_list|)
decl_stmt|;
name|updateMeta
argument_list|(
name|currentRegion
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|nextRegion
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|mergedRegion
argument_list|)
expr_stmt|;
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"not merging regions "
operator|+
name|currentRegion
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" and "
operator|+
name|nextRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|currentRegion
operator|.
name|close
argument_list|()
expr_stmt|;
name|currentRegion
operator|=
name|nextRegion
expr_stmt|;
name|currentSize
operator|=
name|nextSize
expr_stmt|;
block|}
if|if
condition|(
name|currentRegion
operator|!=
literal|null
condition|)
block|{
name|currentRegion
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|protected
specifier|abstract
name|HRegionInfo
index|[]
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|updateMeta
parameter_list|(
name|Text
name|oldRegion1
parameter_list|,
name|Text
name|oldRegion2
parameter_list|,
name|HRegion
name|newRegion
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/** Instantiated to compact a normal user table */
specifier|private
specifier|static
class|class
name|OnlineMerger
extends|extends
name|Merger
block|{
specifier|private
specifier|final
name|Text
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|HTable
name|table
decl_stmt|;
specifier|private
specifier|final
name|HScannerInterface
name|metaScanner
decl_stmt|;
specifier|private
name|HRegionInfo
name|latestRegion
decl_stmt|;
name|OnlineMerger
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaScanner
operator|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|COL_REGIONINFO_ARRAY
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|latestRegion
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|HRegionInfo
name|nextRegion
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
name|getMetaRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|results
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|results
operator|.
name|get
argument_list|(
name|COL_REGIONINFO
argument_list|)
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
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"meta region entry missing "
operator|+
name|COL_REGIONINFO
argument_list|)
throw|;
block|}
name|HRegionInfo
name|region
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|checkOfflined
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return
name|region
return|;
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
literal|"meta scanner error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|metaScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|ex
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"error closing scanner"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
specifier|protected
name|void
name|checkOfflined
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|TableNotDisabledException
block|{
if|if
condition|(
operator|!
name|hri
operator|.
name|isOffline
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|TableNotDisabledException
argument_list|(
literal|"region "
operator|+
name|hri
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" is not disabled"
argument_list|)
throw|;
block|}
block|}
comment|/*      * Check current row has a HRegionInfo.  Skip to next row if HRI is empty.      * @return A Map of the row content else null if we are off the end.      * @throws IOException      */
specifier|private
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|getMetaRow
parameter_list|()
throws|throws
name|IOException
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|value
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|foundResult
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|metaScanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Row:<"
operator|+
name|key
operator|.
name|getRow
argument_list|()
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|value
operator|.
name|get
argument_list|(
name|COL_REGIONINFO
argument_list|)
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
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|foundResult
operator|=
literal|true
expr_stmt|;
break|break;
block|}
return|return
name|foundResult
condition|?
name|value
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HRegionInfo
index|[]
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|latestRegion
operator|==
literal|null
condition|)
block|{
name|latestRegion
operator|=
name|nextRegion
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|latestRegion
operator|!=
literal|null
condition|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|latestRegion
argument_list|)
expr_stmt|;
block|}
name|latestRegion
operator|=
name|nextRegion
argument_list|()
expr_stmt|;
if|if
condition|(
name|latestRegion
operator|!=
literal|null
condition|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|latestRegion
argument_list|)
expr_stmt|;
block|}
return|return
name|regions
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|updateMeta
parameter_list|(
name|Text
name|oldRegion1
parameter_list|,
name|Text
name|oldRegion2
parameter_list|,
name|HRegion
name|newRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
index|[]
name|regionsToDelete
init|=
block|{
name|oldRegion1
block|,
name|oldRegion2
block|}
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|regionsToDelete
operator|.
name|length
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|regionsToDelete
index|[
name|r
index|]
operator|.
name|equals
argument_list|(
name|latestRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|latestRegion
operator|=
literal|null
expr_stmt|;
block|}
name|table
operator|.
name|deleteAll
argument_list|(
name|regionsToDelete
index|[
name|r
index|]
argument_list|)
expr_stmt|;
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
literal|"updated columns in row: "
operator|+
name|regionsToDelete
index|[
name|r
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|newRegion
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|BatchUpdate
name|update
init|=
operator|new
name|BatchUpdate
argument_list|(
name|newRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|update
operator|.
name|put
argument_list|(
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|newRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|update
argument_list|)
expr_stmt|;
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
literal|"updated columns in row: "
operator|+
name|newRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** Instantiated to compact the meta region */
specifier|private
specifier|static
class|class
name|OfflineMerger
extends|extends
name|Merger
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|metaRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|root
decl_stmt|;
name|OfflineMerger
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|Path
name|rootTableDir
init|=
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HBASE_DIR
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|ROOT_TABLE_NAME
argument_list|)
decl_stmt|;
comment|// Scan root region to find all the meta regions
name|root
operator|=
operator|new
name|HRegion
argument_list|(
name|rootTableDir
argument_list|,
name|hlog
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|HRegionInfo
operator|.
name|rootRegionInfo
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HScannerInterface
name|rootScanner
init|=
name|root
operator|.
name|getScanner
argument_list|(
name|COL_REGIONINFO_ARRAY
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|rootScanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|b
range|:
name|results
operator|.
name|values
argument_list|()
control|)
block|{
name|HRegionInfo
name|info
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|!=
literal|null
condition|)
block|{
name|metaRegions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|rootScanner
operator|.
name|close
argument_list|()
expr_stmt|;
try|try
block|{
name|root
operator|.
name|close
argument_list|()
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
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|HRegionInfo
index|[]
name|next
parameter_list|()
block|{
name|HRegionInfo
index|[]
name|results
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|metaRegions
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|results
operator|=
name|metaRegions
operator|.
name|toArray
argument_list|(
operator|new
name|HRegionInfo
index|[
name|metaRegions
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|metaRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|updateMeta
parameter_list|(
name|Text
name|oldRegion1
parameter_list|,
name|Text
name|oldRegion2
parameter_list|,
name|HRegion
name|newRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
index|[]
name|regionsToDelete
init|=
block|{
name|oldRegion1
block|,
name|oldRegion2
block|}
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|regionsToDelete
operator|.
name|length
condition|;
name|r
operator|++
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|regionsToDelete
index|[
name|r
index|]
argument_list|)
decl_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_REGIONINFO
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_SERVER
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_STARTCODE
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_SPLITA
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|COL_SPLITB
argument_list|)
expr_stmt|;
name|root
operator|.
name|batchUpdate
argument_list|(
name|b
argument_list|)
expr_stmt|;
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
literal|"updated columns in row: "
operator|+
name|regionsToDelete
index|[
name|r
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|HRegionInfo
name|newInfo
init|=
name|newRegion
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|newInfo
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|newRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|newInfo
argument_list|)
argument_list|)
expr_stmt|;
name|root
operator|.
name|batchUpdate
argument_list|(
name|b
argument_list|)
expr_stmt|;
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
literal|"updated columns in row: "
operator|+
name|newRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

