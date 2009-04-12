begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|KeyValue
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
name|regionserver
operator|.
name|InternalScanner
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
name|Store
import|;
end_import

begin_comment
comment|/**  * Contains utility methods for manipulating HBase meta tables.  * Be sure to call {@link #shutdown()} when done with this class so it closes  * resources opened during meta processing (ROOT, META, etc.).  Be careful  * how you use this class.  If used during migrations, be careful when using  * this class to check whether migration is needed.  */
end_comment

begin_class
specifier|public
class|class
name|MetaUtils
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
name|MetaUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|rootdir
decl_stmt|;
specifier|private
name|HLog
name|log
decl_stmt|;
specifier|private
name|HRegion
name|rootRegion
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|HRegion
argument_list|>
name|metaRegions
init|=
name|Collections
operator|.
name|synchronizedSortedMap
argument_list|(
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|HRegion
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
argument_list|)
decl_stmt|;
comment|/** Default constructor     * @throws IOException */
specifier|public
name|MetaUtils
parameter_list|()
throws|throws
name|IOException
block|{
name|this
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** @param conf HBaseConfiguration     * @throws IOException */
specifier|public
name|MetaUtils
parameter_list|(
name|HBaseConfiguration
name|conf
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootRegion
operator|=
literal|null
expr_stmt|;
name|initialize
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verifies that DFS is available and that HBase is off-line.    * @throws IOException    */
specifier|private
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
comment|// Get root directory of HBase installation
name|this
operator|.
name|rootdir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/** @return the HLog     * @throws IOException */
specifier|public
specifier|synchronized
name|HLog
name|getLog
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|log
operator|==
literal|null
condition|)
block|{
name|Path
name|logdir
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|fs
operator|.
name|getHomeDirectory
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|+
literal|"_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|log
operator|=
operator|new
name|HLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|logdir
argument_list|,
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|log
return|;
block|}
comment|/**    * @return HRegion for root region    * @throws IOException    */
specifier|public
name|HRegion
name|getRootRegion
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|rootRegion
operator|==
literal|null
condition|)
block|{
name|openRootRegion
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|rootRegion
return|;
block|}
comment|/**    * Open or return cached opened meta region    *     * @param metaInfo HRegionInfo for meta region    * @return meta HRegion    * @throws IOException    */
specifier|public
name|HRegion
name|getMetaRegion
parameter_list|(
name|HRegionInfo
name|metaInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|meta
init|=
name|metaRegions
operator|.
name|get
argument_list|(
name|metaInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|meta
operator|==
literal|null
condition|)
block|{
name|meta
operator|=
name|openMetaRegion
argument_list|(
name|metaInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaRegions
operator|.
name|put
argument_list|(
name|metaInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|meta
argument_list|)
expr_stmt|;
block|}
return|return
name|meta
return|;
block|}
comment|/**    * Closes catalog regions if open. Also closes and deletes the HLog. You    * must call this method if you want to persist changes made during a    * MetaUtils edit session.    */
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|rootRegion
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|rootRegion
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
literal|"closing root region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|rootRegion
operator|=
literal|null
expr_stmt|;
block|}
block|}
try|try
block|{
for|for
control|(
name|HRegion
name|r
range|:
name|metaRegions
operator|.
name|values
argument_list|()
control|)
block|{
name|r
operator|.
name|close
argument_list|()
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
name|error
argument_list|(
literal|"closing meta region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|metaRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|this
operator|.
name|log
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|this
operator|.
name|log
operator|.
name|closeAndDelete
argument_list|()
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
name|error
argument_list|(
literal|"closing HLog"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|log
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Used by scanRootRegion and scanMetaRegion to call back the caller so it    * can process the data for a row.    */
specifier|public
interface|interface
name|ScannerListener
block|{
comment|/**      * Callback so client of scanner can process row contents      *       * @param info HRegionInfo for row      * @return false to terminate the scan      * @throws IOException      */
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Scans the root region. For every meta region found, calls the listener with    * the HRegionInfo of the meta region.    *     * @param listener method to be called for each meta region found    * @throws IOException    */
specifier|public
name|void
name|scanRootRegion
parameter_list|(
name|ScannerListener
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Open root region so we can scan it
if|if
condition|(
name|this
operator|.
name|rootRegion
operator|==
literal|null
condition|)
block|{
name|openRootRegion
argument_list|()
expr_stmt|;
block|}
name|InternalScanner
name|rootScanner
init|=
name|rootRegion
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
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
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|rootScanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{
name|HRegionInfo
name|info
init|=
literal|null
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|info
operator|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"region info is null for row "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|" in table "
operator|+
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
if|if
condition|(
operator|!
name|listener
operator|.
name|processRow
argument_list|(
name|info
argument_list|)
condition|)
block|{
break|break;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rootScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Scans a meta region. For every region found, calls the listener with    * the HRegionInfo of the region.    * TODO: Use Visitor rather than Listener pattern.  Allow multiple Visitors.    * Use this everywhere we scan meta regions: e.g. in metascanners, in close    * handling, etc.  Have it pass in the whole row, not just HRegionInfo.    *     * @param metaRegionInfo HRegionInfo for meta region    * @param listener method to be called for each meta region found    * @throws IOException    */
specifier|public
name|void
name|scanMetaRegion
parameter_list|(
name|HRegionInfo
name|metaRegionInfo
parameter_list|,
name|ScannerListener
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Open meta region so we can scan it
name|HRegion
name|metaRegion
init|=
name|openMetaRegion
argument_list|(
name|metaRegionInfo
argument_list|)
decl_stmt|;
name|scanMetaRegion
argument_list|(
name|metaRegion
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scan the passed in metaregion<code>m</code> invoking the passed    *<code>listener</code> per row found.    * @param m    * @param listener    * @throws IOException    */
specifier|public
name|void
name|scanMetaRegion
parameter_list|(
specifier|final
name|HRegion
name|m
parameter_list|,
specifier|final
name|ScannerListener
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalScanner
name|metaScanner
init|=
name|m
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
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
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|metaScanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{
name|HRegionInfo
name|info
init|=
literal|null
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
if|if
condition|(
name|KeyValue
operator|.
name|META_COMPARATOR
operator|.
name|compareColumns
argument_list|(
name|kv
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
operator|.
name|length
argument_list|)
operator|==
literal|0
condition|)
block|{
name|info
operator|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"region info is null for row "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|" in table "
operator|+
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|listener
operator|.
name|processRow
argument_list|(
name|info
argument_list|)
condition|)
block|{
break|break;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|metaScanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|synchronized
name|HRegion
name|openRootRegion
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|rootRegion
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|rootRegion
return|;
block|}
name|this
operator|.
name|rootRegion
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
argument_list|,
name|this
operator|.
name|rootdir
argument_list|,
name|getLog
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootRegion
operator|.
name|compactStores
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|rootRegion
return|;
block|}
specifier|private
name|HRegion
name|openMetaRegion
parameter_list|(
name|HRegionInfo
name|metaInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|meta
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|metaInfo
argument_list|,
name|this
operator|.
name|rootdir
argument_list|,
name|getLog
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|meta
operator|.
name|compactStores
argument_list|()
expr_stmt|;
return|return
name|meta
return|;
block|}
comment|/**    * Set a single region on/offline.    * This is a tool to repair tables that have offlined tables in their midst.    * Can happen on occasion.  Use at your own risk.  Call from a bit of java    * or jython script.  This method is 'expensive' in that it creates a    * {@link HTable} instance per invocation to go against<code>.META.</code>    * @param c A configuration that has its<code>hbase.master</code>    * properly set.    * @param row Row in the catalog .META. table whose HRegionInfo's offline    * status we want to change.    * @param onlineOffline Pass<code>true</code> to OFFLINE the region.    * @throws IOException    */
specifier|public
specifier|static
name|void
name|changeOnlineStatus
parameter_list|(
specifier|final
name|HBaseConfiguration
name|c
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|boolean
name|onlineOffline
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Cell
name|cell
init|=
name|t
operator|.
name|get
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"no information for row "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|)
throw|;
block|}
comment|// Throws exception if null.
name|HRegionInfo
name|info
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|info
operator|.
name|setOffline
argument_list|(
name|onlineOffline
argument_list|)
expr_stmt|;
name|b
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|)
expr_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|)
expr_stmt|;
name|t
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**    * Offline version of the online TableOperation,    * org.apache.hadoop.hbase.master.AddColumn.    * @param tableName    * @param hcd Add this column to<code>tableName</code>    * @throws IOException     */
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|metas
init|=
name|getMETARows
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|metas
control|)
block|{
specifier|final
name|HRegion
name|m
init|=
name|getMetaRegion
argument_list|(
name|hri
argument_list|)
decl_stmt|;
name|scanMetaRegion
argument_list|(
name|m
argument_list|,
operator|new
name|ScannerListener
argument_list|()
block|{
specifier|private
name|boolean
name|inTable
init|=
literal|true
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"synthetic-access"
argument_list|)
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Testing "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" against "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
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
name|this
operator|.
name|inTable
operator|=
literal|false
expr_stmt|;
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|updateMETARegionInfo
argument_list|(
name|m
argument_list|,
name|info
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// If we got here and we have not yet encountered the table yet,
comment|// inTable will be false.  Otherwise, we've passed out the table.
comment|// Stop the scanner.
return|return
name|this
operator|.
name|inTable
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Offline version of the online TableOperation,    * org.apache.hadoop.hbase.master.DeleteColumn.    * @param tableName    * @param columnFamily Name of column name to remove.    * @throws IOException    */
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
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|metas
init|=
name|getMETARows
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|metas
control|)
block|{
specifier|final
name|HRegion
name|m
init|=
name|getMetaRegion
argument_list|(
name|hri
argument_list|)
decl_stmt|;
name|scanMetaRegion
argument_list|(
name|m
argument_list|,
operator|new
name|ScannerListener
argument_list|()
block|{
specifier|private
name|boolean
name|inTable
init|=
literal|true
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"synthetic-access"
argument_list|)
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
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
name|this
operator|.
name|inTable
operator|=
literal|false
expr_stmt|;
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|removeFamily
argument_list|(
name|columnFamily
argument_list|)
expr_stmt|;
name|updateMETARegionInfo
argument_list|(
name|m
argument_list|,
name|info
argument_list|)
expr_stmt|;
name|Path
name|tabledir
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
name|Store
operator|.
name|getStoreHomedir
argument_list|(
name|tabledir
argument_list|,
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|columnFamily
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed delete of "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|// If we got here and we have not yet encountered the table yet,
comment|// inTable will be false.  Otherwise, we've passed out the table.
comment|// Stop the scanner.
return|return
name|this
operator|.
name|inTable
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Update COL_REGIONINFO in meta region r with HRegionInfo hri    *     * @param r    * @param hri    * @throws IOException    */
specifier|public
name|void
name|updateMETARegionInfo
parameter_list|(
name|HRegion
name|r
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|HRegionInfo
name|h
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|r
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Old "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|+
literal|" for "
operator|+
name|hri
operator|.
name|toString
argument_list|()
operator|+
literal|" in "
operator|+
name|r
operator|.
name|toString
argument_list|()
operator|+
literal|" is: "
operator|+
name|h
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|b
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|batchUpdate
argument_list|(
name|b
argument_list|,
literal|null
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
name|HRegionInfo
name|h
init|=
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|r
operator|.
name|get
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"New "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|+
literal|" for "
operator|+
name|hri
operator|.
name|toString
argument_list|()
operator|+
literal|" in "
operator|+
name|r
operator|.
name|toString
argument_list|()
operator|+
literal|" is: "
operator|+
name|h
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return List of {@link HRegionInfo} rows found in the ROOT or META    * catalog table.    * @param tableName Name of table to go looking for.    * @throws IOException    * @see #getMetaRegion(HRegionInfo)    */
specifier|public
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getMETARows
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// If passed table name is META, then  return the root region.
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|openRootRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Return all meta regions that contain the passed tablename.
name|scanRootRegion
argument_list|(
operator|new
name|ScannerListener
argument_list|()
block|{
specifier|private
specifier|final
name|Log
name|SL_LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
name|SL_LOG
operator|.
name|debug
argument_list|(
literal|"Testing "
operator|+
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
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|info
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
block|}
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param n Table name.    * @return True if a catalog table, -ROOT- or .META.    */
specifier|public
specifier|static
name|boolean
name|isMetaTableName
parameter_list|(
specifier|final
name|byte
index|[]
name|n
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|n
argument_list|,
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|n
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
return|;
block|}
block|}
end_class

end_unit

