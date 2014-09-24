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
name|Collections
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
name|hbase
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
name|wal
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
name|wal
operator|.
name|HLogFactory
import|;
end_import

begin_comment
comment|/**  * Contains utility methods for manipulating HBase meta tables.  * Be sure to call {@link #shutdown()} when done with this class so it closes  * resources opened during meta processing (ROOT, META, etc.).  Be careful  * how you use this class.  If used during migrations, be careful when using  * this class to check whether migration is needed.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|HLog
name|log
decl_stmt|;
specifier|private
name|HRegion
name|metaRegion
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
comment|/** Default constructor    * @throws IOException e    */
specifier|public
name|MetaUtils
parameter_list|()
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param conf Configuration    * @throws IOException e    */
specifier|public
name|MetaUtils
parameter_list|(
name|Configuration
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
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaRegion
operator|=
literal|null
expr_stmt|;
name|initialize
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verifies that DFS is available and that HBase is off-line.    * @throws IOException e    */
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
block|}
comment|/**    * @return the HLog    * @throws IOException e    */
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
name|String
name|logName
init|=
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
decl_stmt|;
name|this
operator|.
name|log
operator|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|fs
operator|.
name|getHomeDirectory
argument_list|()
argument_list|,
name|logName
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|log
return|;
block|}
comment|/**    * @return HRegion for meta region    * @throws IOException e    */
specifier|public
name|HRegion
name|getMetaRegion
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|metaRegion
operator|==
literal|null
condition|)
block|{
name|openMetaRegion
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|metaRegion
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
name|metaRegion
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|metaRegion
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
literal|"closing meta region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|metaRegion
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
name|LOG
operator|.
name|info
argument_list|(
literal|"CLOSING hbase:meta "
operator|+
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
specifier|private
specifier|synchronized
name|HRegion
name|openMetaRegion
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|metaRegion
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|metaRegion
return|;
block|}
name|this
operator|.
name|metaRegion
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
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
name|metaRegion
operator|.
name|compactStores
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|metaRegion
return|;
block|}
block|}
end_class

end_unit

