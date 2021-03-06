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
name|regionserver
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|master
operator|.
name|cleaner
operator|.
name|TimeToLiveHFileCleaner
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * A chore for refreshing the store files for secondary regions hosted in the region server.  *  * This chore should run periodically with a shorter interval than HFile TTL  * ("hbase.master.hfilecleaner.ttl", default 5 minutes).  * It ensures that if we cannot refresh files longer than that amount, the region  * will stop serving read requests because the referenced files might have been deleted (by the  * primary region).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StorefileRefresherChore
extends|extends
name|ScheduledChore
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
name|StorefileRefresherChore
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * The period (in milliseconds) for refreshing the store files for the secondary regions.    */
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
init|=
literal|"hbase.regionserver.storefile.refresh.period"
decl_stmt|;
specifier|static
specifier|final
name|int
name|DEFAULT_REGIONSERVER_STOREFILE_REFRESH_PERIOD
init|=
literal|0
decl_stmt|;
comment|//disabled by default
comment|/**    * Whether all storefiles should be refreshed, as opposed to just hbase:meta's    * Meta region doesn't have WAL replication for replicas enabled yet    */
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_META_STOREFILE_REFRESH_PERIOD
init|=
literal|"hbase.regionserver.meta.storefile.refresh.period"
decl_stmt|;
specifier|private
name|HRegionServer
name|regionServer
decl_stmt|;
specifier|private
name|long
name|hfileTtl
decl_stmt|;
specifier|private
name|int
name|period
decl_stmt|;
specifier|private
name|boolean
name|onlyMetaRefresh
init|=
literal|true
decl_stmt|;
comment|//ts of last time regions store files are refreshed
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastRefreshTimes
decl_stmt|;
comment|// encodedName -> long
specifier|public
name|StorefileRefresherChore
parameter_list|(
name|int
name|period
parameter_list|,
name|boolean
name|onlyMetaRefresh
parameter_list|,
name|HRegionServer
name|regionServer
parameter_list|,
name|Stoppable
name|stoppable
parameter_list|)
block|{
name|super
argument_list|(
literal|"StorefileRefresherChore"
argument_list|,
name|stoppable
argument_list|,
name|period
argument_list|)
expr_stmt|;
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|regionServer
expr_stmt|;
name|this
operator|.
name|hfileTtl
operator|=
name|this
operator|.
name|regionServer
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|,
name|TimeToLiveHFileCleaner
operator|.
name|DEFAULT_TTL
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlyMetaRefresh
operator|=
name|onlyMetaRefresh
expr_stmt|;
if|if
condition|(
name|period
operator|>
name|hfileTtl
operator|/
literal|2
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
operator|+
literal|" should be set smaller than half of "
operator|+
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|)
throw|;
block|}
name|lastRefreshTimes
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
for|for
control|(
name|Region
name|r
range|:
name|regionServer
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|r
operator|.
name|isReadOnly
argument_list|()
condition|)
block|{
comment|// skip checking for this region if it can accept writes
continue|continue;
block|}
comment|// don't refresh unless enabled for all files, or it the meta region
comment|// meta region don't have WAL replication for replicas enabled yet
if|if
condition|(
name|onlyMetaRefresh
operator|&&
operator|!
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
continue|continue;
name|String
name|encodedName
init|=
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|long
name|time
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|lastRefreshTimes
operator|.
name|containsKey
argument_list|(
name|encodedName
argument_list|)
condition|)
block|{
name|lastRefreshTimes
operator|.
name|put
argument_list|(
name|encodedName
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|Store
name|store
range|:
name|r
operator|.
name|getStores
argument_list|()
control|)
block|{
comment|// TODO: some stores might see new data from flush, while others do not which
comment|// MIGHT break atomic edits across column families. We can fix this with setting
comment|// mvcc read numbers that we know every store has seen
name|store
operator|.
name|refreshStoreFiles
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while trying to refresh store files for region:"
operator|+
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|+
literal|", exception:"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
comment|// Store files have a TTL in the archive directory. If we fail to refresh for that long, we stop serving reads
if|if
condition|(
name|isRegionStale
argument_list|(
name|encodedName
argument_list|,
name|time
argument_list|)
condition|)
block|{
operator|(
operator|(
name|HRegion
operator|)
name|r
operator|)
operator|.
name|setReadsEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// stop serving reads
block|}
continue|continue;
block|}
name|lastRefreshTimes
operator|.
name|put
argument_list|(
name|encodedName
argument_list|,
name|time
argument_list|)
expr_stmt|;
operator|(
operator|(
name|HRegion
operator|)
name|r
operator|)
operator|.
name|setReadsEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// restart serving reads
block|}
comment|// remove closed regions
name|Iterator
argument_list|<
name|String
argument_list|>
name|lastRefreshTimesIter
init|=
name|lastRefreshTimes
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|lastRefreshTimesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|encodedName
init|=
name|lastRefreshTimesIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionServer
operator|.
name|getRegion
argument_list|(
name|encodedName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|lastRefreshTimesIter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|boolean
name|isRegionStale
parameter_list|(
name|String
name|encodedName
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|long
name|lastRefreshTime
init|=
name|lastRefreshTimes
operator|.
name|get
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
return|return
name|time
operator|-
name|lastRefreshTime
operator|>
name|hfileTtl
operator|-
name|period
return|;
block|}
block|}
end_class

end_unit

