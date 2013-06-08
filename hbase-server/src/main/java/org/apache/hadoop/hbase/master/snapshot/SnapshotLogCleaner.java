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
name|master
operator|.
name|snapshot
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
name|Collection
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
name|FileStatus
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
name|master
operator|.
name|cleaner
operator|.
name|BaseLogCleanerDelegate
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
name|snapshot
operator|.
name|SnapshotReferenceUtil
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
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * Implementation of a log cleaner that checks if a log is still used by  * snapshots of HBase tables.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|SnapshotLogCleaner
extends|extends
name|BaseLogCleanerDelegate
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
name|SnapshotLogCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Conf key for the frequency to attempt to refresh the cache of hfiles currently used in    * snapshots (ms)    */
specifier|static
specifier|final
name|String
name|HLOG_CACHE_REFRESH_PERIOD_CONF_KEY
init|=
literal|"hbase.master.hlogcleaner.plugins.snapshot.period"
decl_stmt|;
comment|/** Refresh cache, by default, every 5 minutes */
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_HLOG_CACHE_REFRESH_PERIOD
init|=
literal|300000
decl_stmt|;
specifier|private
name|SnapshotFileCache
name|cache
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|isFileDeletable
parameter_list|(
name|FileStatus
name|fStat
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
literal|null
operator|==
name|cache
condition|)
return|return
literal|false
return|;
return|return
operator|!
name|cache
operator|.
name|contains
argument_list|(
name|fStat
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
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
literal|"Exception while checking if:"
operator|+
name|fStat
operator|.
name|getPath
argument_list|()
operator|+
literal|" was valid, keeping it just in case."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
comment|/**    * This method should only be called<b>once</b>, as it starts a thread to keep the cache    * up-to-date.    *<p>    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|long
name|cacheRefreshPeriod
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|HLOG_CACHE_REFRESH_PERIOD_CONF_KEY
argument_list|,
name|DEFAULT_HLOG_CACHE_REFRESH_PERIOD
argument_list|)
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|cache
operator|=
operator|new
name|SnapshotFileCache
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|cacheRefreshPeriod
argument_list|,
name|cacheRefreshPeriod
argument_list|,
literal|"snapshot-log-cleaner-cache-refresher"
argument_list|,
operator|new
name|SnapshotFileCache
operator|.
name|SnapshotFileInspector
argument_list|()
block|{
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|filesUnderSnapshot
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|SnapshotReferenceUtil
operator|.
name|getHLogNames
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
return|;
block|}
block|}
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
name|error
argument_list|(
literal|"Failed to create snapshot log cleaner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
name|this
operator|.
name|cache
operator|.
name|stop
argument_list|(
name|why
argument_list|)
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
name|this
operator|.
name|cache
operator|.
name|isStopped
argument_list|()
return|;
block|}
block|}
end_class

end_unit

